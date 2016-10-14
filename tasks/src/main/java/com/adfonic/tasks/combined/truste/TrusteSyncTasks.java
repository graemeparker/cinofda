package com.adfonic.tasks.combined.truste;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.tasks.combined.DeviceIdentifierValidator;
import com.adfonic.tasks.combined.truste.dao.OptOutServiceDao;
import com.adfonic.tasks.combined.truste.dao.TasksDao;
import com.adfonic.tasks.combined.truste.dto.AdditionalIds;
import com.adfonic.tasks.combined.truste.dto.BatchPreference;

/**
 * This is the Spring scheduled job to sync in a user/device OptOuts from
 * Truste's API.
 * @author graemeparker
 *
 */
@Component
public class TrusteSyncTasks {

    private final transient Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

    // Batch size of OptOuts to process, taken from property file
    @Value("${Truste.batchsize}")
    int batchSize;

    // Shared code to validate the deviec ids based on the database regex.
    // No point sending bad data to MUID.
    @Autowired
    private DeviceIdentifierValidator deviceIdentifierValidator;

    // This is where the magic happens. Tasks calls MUID with the devices to 
    // OptOut. Which then tells DMP tasks to update redis.
    @Autowired
    private OptOutServiceDao optOutServiceDao;

    // Hard coded mapping of DEVICE_IDENTIFIER_TYPE.SYSTEM_NAME to internal 
    // database index. DEVICE_IDENTIFIER_TYPE.ID. No idea why we are not using 
    // TRUSTE_ID_TYPE. Every time we add an new device type we need to update 
    // this.  
    @Autowired
    private TrusteIdTypeMapper idTypeMapper;

    // The "batch" calls to Truste's API.
    @Autowired
    private BatchPreferenceService batchPreferenceService;

    // This is the data access object for when the job was last run. 
    // Stored in the tools database.
    @Autowired
    private TasksDao tasksDao;

    // Date time this job was last run. So we known how far back to query the 
    // Truste API.
    DateTime lastRunTime;

    /**
     * Main entry point for job and where the scheduled time is configured. 
     */
    //@Scheduled(fixedDelay = 3600500)
    public void process() {
        LOGGER.info("Starting offline sync with Truste");

        if (lastRunTime == null) {
            try {
                lastRunTime = tasksDao.loadLastRunTime();
            } catch (Exception e) {
                LOGGER.error("failed to load lastRunTime", e);
            }
        }

        if (lastRunTime == null) {
            lastRunTime = new DateTime().withTimeAtStartOfDay().minusDays(500);
            LOGGER.info("TrusteSyncTasks lastRunTime not known, setting to {}", lastRunTime);
        }

        DateTime now = new DateTime();
        try {
            int count = batchPreferenceService.getCount(lastRunTime, now);
            LOGGER.info("batchPreferenceService.getCount: {}", count);

            processBatch(count, lastRunTime, now);
            lastRunTime = now;
            tasksDao.storeLastRunTime(lastRunTime);

            LOGGER.info("Finished offline sync with Truste");
        } catch (TrusteUnreachableException e) {
            LOGGER.error("failed to update batch", e);
        } catch (Exception e) {
            LOGGER.error("error saving lastRunTime", e);
        }
    }

    /**
     * Process the batch of preferences from Truste
     * 
     * @param batchCount
     * @throws TrusteUnreachableException
     */
    void processBatch(int batchCount, DateTime changeAfter, DateTime changeBefore) throws TrusteUnreachableException {
        LOGGER.info("Starting processing batch preferences from Truste changeAfter: {} changeBefore: {} batchCount: {}", changeAfter, changeBefore, batchCount);

        for (int i = 0; i < batchCount; i += batchSize) {
            List<BatchPreference> preferences = batchPreferenceService.getBatch(i, i + batchSize, changeAfter, changeBefore);
            if (preferences.isEmpty()) {
                LOGGER.warn("Truste expected some entries");
            }

            for (BatchPreference batchPreference : preferences) {
                processSingleDevice(batchPreference);
            }

        }
        LOGGER.info("Finished processing batch preferences from Truste, batchCount: {}", batchCount);
    }

    /**
     * Process a single device from the batch
     * @param batchPreference
     */
    private void processSingleDevice(BatchPreference batchPreference) {

        List<AdditionalIds> additionalIds = batchPreference.getAdditionalIds();

        if (additionalIds.isEmpty()) {
            LOGGER.warn("no ids given for tpid: {}", batchPreference.getTpid());
            return;
        }

        LineBuilder builder = new LineBuilder();
        for (AdditionalIds additionalId : additionalIds) {
            String idName = additionalId.getIdName();
            long idType = idTypeMapper.mapAdfonicIdType(idName);
            if (idType <= 0) {
                LOGGER.error("Truste ignoring unknown idType: {}", idName);
                continue;
            }

            String idValue = additionalId.getIdValue();

            // Hack: MUID doesn't support IFA yet. Promoting to HIFA.
            if (idType == 6) {
                idType = 7;
                idValue = hashSHA1(idValue.toUpperCase());

            }

            boolean valid = deviceIdentifierValidator.isDeviceIdValid(idValue, idType);

            if (valid) {
                builder.append(idValue, idType);
            } else {
                LOGGER.error("Device id not valid. idValue={};idType={};", idValue, idType);
            }
        }

        boolean optinFlag = batchPreference.isOptinFlag();
        String ids = builder.toString();
        if (StringUtils.isBlank(ids)) {
            LOGGER.warn("no valid ids to save tpid: {}", batchPreference.getTpid());
            return;
        }

        int saveResult = optOutServiceDao.saveUserPreferences(ids, optinFlag);
        if (saveResult <= 0) {
            LOGGER.error("Truste failed saveOptIn( {} ) result: {}", ids, saveResult);
        } else {
            LOGGER.info("Truste success saveOptIn( {} ) result: {}", ids, saveResult);
        }

    }

    /**
     * Format MUID wants to receive the device OptOut/device.
     * @author graemeparker
     *
     */
    private static class LineBuilder {
        private static final char IdTypeSeparator = '~';
        private static final char IdsSeparator = '|';

        private StringBuilder sb = new StringBuilder();
        int idCount = 0;

        void append(String idValue, long idType) {
            if (idCount++ > 0) {
                sb.append(IdsSeparator);
            }
            sb.append(idValue);
            sb.append(IdTypeSeparator);
            sb.append(idType);
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private String hashSHA1(String rawValue) {
        String sha1Value = DigestUtils.shaHex(rawValue);
        return sha1Value;
    }

}