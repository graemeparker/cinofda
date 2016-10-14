package com.adfonic.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.StopWatch;

import com.adfonic.ddr.DdrService;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.UserAgentUpdatedMessage;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.ConstraintsHelper;

public class PlatformMapper implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformMapper.class);

    private static final String MIDP_PLATFORM_SYSTEM_NAME = "midp";
    private static final String OTHER_PLATFORM_SYSTEM_NAME = "other";

    // The DEVICE_GROUP.NAME
    private static final String DEFAULT_DEVICE_GROUP_SYSTEM_NAME = "MOBILE";

    @Autowired
    private DdrService ddrService;
    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;
    @Autowired
    @Qualifier(ConfUtils.ADM_REPORTING_DS)
    private DataSource admReportingDataSource;
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;
    @Autowired
    private JmsUtils jmsUtils;
    @Value("${PlatformMapper.modelPlatformSqlFile}")
    private File modelPlatformSqlFile;
    @Value("${PlatformMapper.modelDeviceGroupSqlFile}")
    private File modelDeviceGroupSqlFile;
    @Value("${PlatformMapper.threadPool.size}")
    private int threadPoolSize;

    private enum Match {
        YES, NO, CONFLICT
    }

    //If this UserAgentRow received by a thread it wil be considered as the end of thread processing
    private static final UserAgentRow END_OF_DATA_USER_AGENT_ROW = new UserAgentRow(0l, 0l, null);

    private final StopWatch taskTimeCounter = new StopWatch("Platform Mapper");

    @Override
    public void run() {
        try {
            // Update model platform mappings
            mapPlatforms();

            // Generate a .sql file that can be run in other environments to stay in sync
            generateModelPlatformSql();
            generateModelDeviceGroupMappingSql();
        } catch (Exception e) {
            throw new IllegalStateException("PlatformMapper failed to complete successfully", e);
        }
    }

    //This deviceGroup Id will be used for devices which could not be mapped from info in any user agent 
    private Long defaultDeviceGroupId = null;

    private void mapPlatforms() throws java.sql.SQLException {
        Connection toolsConn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            toolsConn = toolsDataSource.getConnection();

            taskTimeCounter.start("Reading Device Groups");
            List<DeviceGroupRow> deviceGroups = new ArrayList<PlatformMapper.DeviceGroupRow>();
            LOG.info("Loading DEVICE_GROUP entries from toolsdb...");
            pst = toolsConn.prepareStatement("SELECT ID, SYSTEM_NAME, CONSTRAINTS FROM DEVICE_GROUP ORDER BY PROCESSING_ORDER ASC");
            rs = pst.executeQuery();
            while (rs.next()) {
                DeviceGroupRow deviceGroupRow = new DeviceGroupRow(rs.getLong(1), rs.getString(2), rs.getString(3));
                deviceGroups.add(deviceGroupRow);
                if (DEFAULT_DEVICE_GROUP_SYSTEM_NAME.equalsIgnoreCase(deviceGroupRow.getSystemName())) {
                    defaultDeviceGroupId = deviceGroupRow.getId();
                }
            }
            DbUtils.closeQuietly(null, pst, rs);
            LOG.info("Loaded {} DeviceGroups", deviceGroups.size());
            taskTimeCounter.stop();

            if (defaultDeviceGroupId == null) {
                throw new IllegalStateException("No Device Group found with SYSTEM_NAME=" + DEFAULT_DEVICE_GROUP_SYSTEM_NAME + " to use as default. Cannot proceed any further!");
            } else {
                LOG.info("Found default DeviceGroup id={} for {}", defaultDeviceGroupId, DEFAULT_DEVICE_GROUP_SYSTEM_NAME);
            }

            taskTimeCounter.start("Loading platform from DB");
            LOG.info("Loading PLATFORM entries from toolsdb...");
            // We'll need to keep track of the "midp" platform, since it's considered
            // the lowest priority match...see below...
            Long midpPlatformId = null;
            // Same deal with the "other" platform, which we'll associate with models
            // that don't otherwise have a platform associated.
            Long otherPlatformId = null;
            Map<Long, String> platformSystemNames = new TreeMap<Long, String>();
            Map<Long, String> platformConstraints = new TreeMap<Long, String>();
            pst = toolsConn.prepareStatement("SELECT ID, SYSTEM_NAME, CONSTRAINTS FROM PLATFORM");
            rs = pst.executeQuery();
            while (rs.next()) {
                platformSystemNames.put(rs.getLong(1), rs.getString(2));
                platformConstraints.put(rs.getLong(1), rs.getString(3));
                if (MIDP_PLATFORM_SYSTEM_NAME.equals(rs.getString(2))) {
                    midpPlatformId = rs.getLong(1);
                } else if (OTHER_PLATFORM_SYSTEM_NAME.equals(rs.getString(2))) {
                    otherPlatformId = rs.getLong(1);
                }
            }
            LOG.info("Loaded {} platforms", platformConstraints.size());
            DbUtils.closeQuietly(null, pst, rs);
            taskTimeCounter.stop();

            if (midpPlatformId == null) {
                throw new IllegalStateException("Failed to find \"" + MIDP_PLATFORM_SYSTEM_NAME + "\" platform");
            } else {
                LOG.info("Found \"{}\" platform id={}", MIDP_PLATFORM_SYSTEM_NAME, midpPlatformId);
            }

            if (otherPlatformId == null) {
                throw new IllegalStateException("Failed to fine \"" + OTHER_PLATFORM_SYSTEM_NAME + "\" platform");
            } else {
                LOG.info("Found \"{}\" platform id={}", OTHER_PLATFORM_SYSTEM_NAME, otherPlatformId);
            }

            taskTimeCounter.start("Loading Models from DB");
            LOG.info("Loading MODEL ids from toolsdb...");
            ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel = new ConcurrentHashMap<Long, Set<Long>>();
            Map<String, Long> modelIdByExternalId = new HashMap<String, Long>();
            pst = toolsConn.prepareStatement("SELECT EXTERNAL_ID, ID FROM MODEL");
            rs = pst.executeQuery();
            while (rs.next()) {
                modelIdByExternalId.put(rs.getString(1), rs.getLong(2));
                //Creating and populating this hashMap before going into multiple threads, so that
                //we will have minimum locking/synchronization there.
                matchingDeviceGroupsByModel.put(rs.getLong(2), new ConcurrentSkipListSet<Long>());
            }
            LOG.info("Loaded {} models", modelIdByExternalId.size());
            DbUtils.closeQuietly(null, pst, rs);
            taskTimeCounter.stop();

            // modelId + platformId -> Match
            Map<Long, Map<Long, Match>> matchesByModelAndPlatform = new HashMap<Long, Map<Long, Match>>();

            taskTimeCounter.start("Creating Threads and starting them");
            int dataQueueCapacity = (threadPoolSize + 1) * 100;
            LOG.info("Creating queue with {} Capacity", dataQueueCapacity);
            LinkedBlockingQueue<UserAgentRow> workerThreadDataQueue = new LinkedBlockingQueue<PlatformMapper.UserAgentRow>(dataQueueCapacity);
            CountDownLatch threadPoolCountDownLatch = null;
            // Set up the thread pool
            UserAgentProcessingThread[] threadPool = null;
            if (threadPoolSize > 1) {
                threadPoolCountDownLatch = new CountDownLatch(threadPoolSize);
                LOG.info("Creating thread pool with {} threads", threadPoolSize);
                threadPool = new UserAgentProcessingThread[threadPoolSize];
                for (int k = 0; k < threadPoolSize; ++k) {
                    threadPool[k] = new UserAgentProcessingThread(modelIdByExternalId, platformConstraints, matchesByModelAndPlatform, matchingDeviceGroupsByModel, deviceGroups,
                            workerThreadDataQueue, threadPoolCountDownLatch);
                    threadPool[k].start();
                }
            }
            taskTimeCounter.stop();

            LOG.info("Getting an approximate MAX(ID) from USER_AGENT in admReporting db...");
            taskTimeCounter.start("Reading MAX(ID) from USER_AGENT");
            pst = toolsConn.prepareStatement("SELECT MAX(ID) FROM USER_AGENT");
            rs = pst.executeQuery();
            if (!rs.next()) {
                throw new IllegalStateException("Could not get MAX(ID) from USER_AGENT");
            }
            long maxUserAgentId = rs.getLong(1);
            DbUtils.closeQuietly(null, pst, rs);
            taskTimeCounter.stop();

            LOG.info("MAX(ID) from USER_AGENT: {}", maxUserAgentId);

            final int USER_AGENT_BATCH_SIZE = 10000;

            // Round up not only to the next highest batch size, but one full batch beyond
            // that.  This covers the case where, let's say, maxUserAgentId = 9999 and batch
            // size is 10000.  Instead of using 10000 as the max for batches, we'll actually
            // use 20000.  The reason is because entries are pouring into USER_AGENT all
            // the time, and we don't want to miss the most recent entries by using an
            // unnecessarily low max id.
            long maxIdForBatches = USER_AGENT_BATCH_SIZE * (1 + ((maxUserAgentId + USER_AGENT_BATCH_SIZE - 1) / USER_AGENT_BATCH_SIZE));
            LOG.info("Effective MAX(ID) for batches: {}", maxIdForBatches);

            //I am removing the join with MODEL as i don't see why it is being done, its making query run slow
            //Model id can be retrieved from USER_AGENT anyways also MODEL_ID is always not null
            //and if this process will set proper MODEL_ID in case its wrong now.
            taskTimeCounter.start("Reading full USER_TABLE and finding match from Platform and DEVICE_GROUP");
            LOG.info("Processing USER_AGENT entries from admReporting db...");
            pst = toolsConn.prepareStatement("SELECT ID, MODEL_ID, UA_HEADER FROM USER_AGENT WHERE ID BETWEEN ? AND ? AND DELETED=false ORDER BY ID"); // we are only processing the user_agents that are not soft deleted
            // Don't bother doing pst.setFetchSize, MySQL Connector/J ignores it.
            // And don't do result set streaming, since that locks the table,
            // which will block replication (not good).  That's why we're reading
            // in batches by ID range.
            int counter = 0;
            for (long minId = 1; minId < maxIdForBatches; minId += USER_AGENT_BATCH_SIZE) {
                long maxId = minId + USER_AGENT_BATCH_SIZE - 1;
                pst.setLong(1, minId);
                pst.setLong(2, maxId);
                rs = pst.executeQuery();
                int batchRowCount = 0;
                while (rs.next()) {
                    ++batchRowCount;
                    long uaId = rs.getLong(1);
                    long modelId = rs.getLong(2);
                    String userAgent = rs.getString(3);
                    UserAgentRow row = new UserAgentRow(uaId, modelId, userAgent);

                    if (threadPool != null) {
                        // Concurrent mode
                        try {
                            workerThreadDataQueue.put(row);
                        } catch (InterruptedException e) {
                            LOG.warn("Interrupted {}", e);
                        }
                    } else {
                        // Single threaded mode
                        processUserAgentRow(row, modelIdByExternalId, platformConstraints, matchesByModelAndPlatform, matchingDeviceGroupsByModel, deviceGroups);
                    }
                }
                LOG.info("Read ID range: {} to {} ({} rows)", minId, maxId, batchRowCount);
                DbUtils.closeQuietly(rs);
            }
            LOG.info("Processed {} USER_AGENT entries", counter);
            DbUtils.closeQuietly(pst);

            if (threadPool != null) {
                LOG.info("Stopping and joining thread pool");
                for (UserAgentProcessingThread thread : threadPool) {
                    try {
                        // Signal to the thread that we're done by passing it END_OF_DATA_USER_AGENT_ROW
                        LOG.info("Sending END_OF_DATA_USER_AGENT_ROW=" + END_OF_DATA_USER_AGENT_ROW);
                        workerThreadDataQueue.put(END_OF_DATA_USER_AGENT_ROW);
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted");
                        return;
                    }
                }
                try {
                    threadPoolCountDownLatch.await();
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted {}", e);
                }
            }
            taskTimeCounter.stop();

            // Now it's time to reconcile MODEL_PLATFORM...

            // Iterate through every model
            taskTimeCounter.start("Updating All Models");
            pst = toolsConn
                    .prepareStatement("SELECT MODEL.ID, VENDOR.NAME, MODEL.NAME, MODEL.DEVICE_GROUP_ID FROM MODEL JOIN VENDOR ON VENDOR.ID=MODEL.VENDOR_ID ORDER BY MODEL.ID");
            rs = pst.executeQuery();
            while (rs.next()) {
                long modelId = rs.getLong(1);
                String modelFullName = rs.getString(2) + " " + rs.getString(3);
                long existingDeviceGroupId = rs.getLong(4);

                updateModelPlatform(modelId, modelFullName, matchesByModelAndPlatform, midpPlatformId, otherPlatformId, platformSystemNames);

                updateModelDeviceGroup(modelId, modelFullName, existingDeviceGroupId, matchingDeviceGroupsByModel, defaultDeviceGroupId);
            }
            DbUtils.closeQuietly(null, pst, rs);
            taskTimeCounter.stop();

            // We shouldn't need to, but if we had to enforce Bugzilla 1701 and make
            // sure all models that don't otherwise exist in MODEL_PLATFORM get set
            // up in there with the "other" platform, since single SQL statement
            // would accomplish that in one fell swoop:
            // INSERT INTO MODEL_PLATFORM (MODEL_ID, PLATFORM_ID) SELECT MODEL.ID, PLATFORM.ID FROM MODEL, PLATFORM WHERE PLATFORM.SYSTEM_NAME='other' AND NOT EXISTS (SELECT 1 FROM MODEL_PLATFORM WHERE MODEL_PLATFORM.MODEL_ID=MODEL.ID);
            // But we shouldn't need to do that, since the matchingPlatformId fallback
            // code above should take care of the association for us.
        } finally {
            DbUtils.closeQuietly(toolsConn);
            LOG.info(taskTimeCounter.prettyPrint());
        }
    }

    private void updateModelDeviceGroup(long modelId, String modelFullName, long existingDeviceGroupId, ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel,
            Long defaultDeviceGroupId) throws SQLException {
        long newDeviceGroupid = getSingleDeviceGroupIdForModel(matchingDeviceGroupsByModel, modelId, defaultDeviceGroupId);
        if (newDeviceGroupid != existingDeviceGroupId) {
            //update the database only if their is change in device category id else leave it
            LOG.info("UPDATING: {} from DEVICE_GROUP_ID => {} to {}", modelFullName, existingDeviceGroupId, newDeviceGroupid);
            Connection toolsConn = toolsDataSource.getConnection();
            PreparedStatement pst = null;
            try {
                pst = toolsConn.prepareStatement("UPDATE MODEL SET DEVICE_GROUP_ID=? WHERE ID=?");
                pst.setLong(1, newDeviceGroupid);
                pst.setLong(2, modelId);
                pst.executeUpdate();
            } finally {
                DbUtils.closeQuietly(toolsConn, pst, null);
            }
        }
    }

    private static long getSingleDeviceGroupIdForModel(ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel, long modelId, Long defaultDeviceGroupId) {
        Set<Long> deviceGroupSetForModel = matchingDeviceGroupsByModel.get(modelId);
        long singleDeviceGroupId = defaultDeviceGroupId;
        if (CollectionUtils.isEmpty(deviceGroupSetForModel)) {
            LOG.debug("Unable to detect device group for model id={}", modelId);
        } else {
            if (deviceGroupSetForModel.size() > 1) {
                // more than one device group is satisfied by this model
                for (long deviceGroupId : deviceGroupSetForModel) {
                    if (deviceGroupId < singleDeviceGroupId) {
                        singleDeviceGroupId = deviceGroupId;
                    }
                }
            } else {
                //Only 1 device category available
                singleDeviceGroupId = deviceGroupSetForModel.iterator().next();
            }
        }
        return singleDeviceGroupId;
    }

    private void updateModelPlatform(long modelId, String modelFullName, Map<Long, Map<Long, Match>> matchesByModelAndPlatform, Long midpPlatformId, Long otherPlatformId,
            Map<Long, String> platformSystemNames) throws SQLException {
        LOG.debug("Reconciling model id={} ({})", modelId, modelFullName);
        // Load the set of platforms previously associated with the model
        Set<Long> dbModelPlatforms = new HashSet<Long>();
        Connection toolsConn = toolsDataSource.getConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = toolsConn.prepareStatement("SELECT PLATFORM_ID FROM MODEL_PLATFORM WHERE MODEL_ID=?");
            pst.setLong(1, modelId);
            rs = pst.executeQuery();
            while (rs.next()) {
                dbModelPlatforms.add(rs.getLong(1));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // As of Bugzilla 1574, we only allow one platform per model, so we can narrow
            // things down for this model to just a single platform, if any matched.
            Long matchingPlatformId = getSingleMatchingPlatformId(matchesByModelAndPlatform, modelId, midpPlatformId);

            // Bugzilla 1701 - if there's no specific platform for this model,
            // always associate the "other" platform with it.
            if (matchingPlatformId == null) {
                matchingPlatformId = otherPlatformId;
            }

            LOG.debug("{} => {}", modelFullName, platformSystemNames.get(matchingPlatformId));

            // Reconcile MODEL_PLATFORM for this model...we need to do deletes first
            // to avoid multiple rows per model (there's a unique constraint now).
            for (Long platformId : dbModelPlatforms) {
                if (!platformId.equals(matchingPlatformId)) {
                    LOG.info("REMOVING: {} => {} ({},{})", modelFullName, platformSystemNames.get(platformId), modelId, platformId);
                    pst = toolsConn.prepareStatement("DELETE FROM MODEL_PLATFORM WHERE MODEL_ID=? AND PLATFORM_ID=?");
                    pst.setLong(1, modelId);
                    pst.setLong(2, platformId);
                    pst.executeUpdate();
                    DbUtils.closeQuietly(pst);
                } else {
                    LOG.debug("LEAVING: {} => {} ({},{})", modelFullName, platformSystemNames.get(platformId), modelId, platformId);
                }
            }

            // Insert the newly found match if it wasn't in the db already
            if (!dbModelPlatforms.contains(matchingPlatformId)) {
                LOG.info("ADDING: {} => {} ({},{})", modelFullName, platformSystemNames.get(matchingPlatformId), modelId, matchingPlatformId);
                pst = toolsConn.prepareStatement("INSERT INTO MODEL_PLATFORM (MODEL_ID, PLATFORM_ID) VALUES (?,?)");
                pst.setLong(1, modelId);
                pst.setLong(2, matchingPlatformId);
                pst.executeUpdate();
                DbUtils.closeQuietly(pst);
            }
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, rs);
        }
    }

    private void processUserAgentRow(UserAgentRow row, Map<String, Long> modelIdByExternalId, Map<Long, String> platformConstraints,
            Map<Long, Map<Long, Match>> matchesByModelAndPlatform, ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel, List<DeviceGroupRow> deviceGroups)
            throws java.sql.SQLException {
        long uaId = row.getUserAgentId();
        long modelId = row.getModelId();
        String userAgent = row.getUserAgent();

        LOG.debug("Processing USER_AGENT row id={}", uaId);

        // Load the DDR properties for the respective device
        Map<String, String> props;
        try {
            props = ddrService.getDdrProperties(userAgent);
        } catch (Exception e) {
            LOG.error("Failed to getDdrProperties for model id={}, User-Agent: {} {}", modelId, userAgent, e);
            return;
        }
        if (MapUtils.isEmpty(props)) {
            LOG.warn("No properties for model id={}, User-Agent: {} {}", modelId, userAgent);
            // Delete the USER_AGENT entirely
            deleteUserAgentAndNotify(uaId, userAgent, modelId);
            return;
        }

        String deviceID = props.get("id");
        if (deviceID == null) {
            LOG.warn("No \"id\" device property Model id={}, User-Agent: {} {}", modelId, userAgent);
            // Delete the USER_AGENT entirely and notify subscribers
            deleteUserAgentAndNotify(uaId, userAgent, modelId);
            return;
        }

        Long validModelId = modelIdByExternalId.get(deviceID);
        if (validModelId == null) {
            // We can't find a Model by that externalID...consider it invalid
            LOG.warn("Device id={} not recognized as a Model.externalID in our system", deviceID);
            deleteUserAgentAndNotify(uaId, userAgent, modelId);
            return;
        } else if (modelId != validModelId) {
            LOG.debug("The modelId associated with the USER_AGENT needs to be updated");
            // The modelId associated with the USER_AGENT needs to be updated
            updateUserAgentAndNotify(uaId, userAgent, modelId, validModelId);
            // Substitute the now-valid modelId and proceed
            modelId = validModelId;
        }
        // Otherwise, everything matches up...proceed

        processUserAgentRowForPlatformMapping(modelId, platformConstraints, matchesByModelAndPlatform, props);

        LOG.debug("Processing USER_AGENT row id={} for DeviceGroup Update", uaId);
        processUserAgentRowForDeviceGroupUpdate(modelId, matchingDeviceGroupsByModel, deviceGroups, props);
    }

    private static void processUserAgentRowForPlatformMapping(long modelId, Map<Long, String> platformConstraints, Map<Long, Map<Long, Match>> matchesByModelAndPlatform,
            Map<String, String> props) {
        // Iterate through all platforms and see which ones match
        for (Map.Entry<Long, String> entry : platformConstraints.entrySet()) {
            long platformId = entry.getKey();

            Map<Long, Match> matchesByPlatform;
            synchronized (matchesByModelAndPlatform) {
                matchesByPlatform = matchesByModelAndPlatform.get(modelId);
                if (matchesByPlatform == null) {
                    matchesByPlatform = Collections.synchronizedMap(new HashMap<Long, Match>());
                    matchesByModelAndPlatform.put(modelId, matchesByPlatform);
                }
            }

            // Check for a previous match status for this model/platform combo
            Match prevMatch = matchesByPlatform.get(platformId);
            if (prevMatch == Match.CONFLICT) {
                // Don't bother checking constraints, there's already a conflict
                LOG.debug("Don't bother checking constraints, there's already a conflict");
                continue;
            }

            // See if the device properties satisfy the platform constraints
            String constraints = entry.getValue();
            boolean matches = ConstraintsHelper.eval(constraints, new ConstraintsHelper.MapPropertySource(props));
            if (prevMatch == null) {
                matchesByPlatform.put(platformId, matches ? Match.YES : Match.NO);
            } else if (matches ^ (prevMatch == Match.YES)) {
                matchesByPlatform.put(platformId, Match.CONFLICT);
            }
        }
    }

    private static void processUserAgentRowForDeviceGroupUpdate(long modelId, ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel, List<DeviceGroupRow> deviceGroups,
            Map<String, String> props) {
        // Iterate through all Device group and see which ones match
        for (DeviceGroupRow deviceGroupRow : deviceGroups) {
            // See if the device properties satisfy the device group constraints
            //LOG.debug("Device Group Constraints="+ deviceGroupRow.getConstraints());
            //LOG.debug("props="+ props);
            boolean matches = ConstraintsHelper.eval(deviceGroupRow.getConstraints(), new ConstraintsHelper.MapPropertySource(props));
            if (matches) {
                matchingDeviceGroupsByModel.get(modelId).add(deviceGroupRow.getId());
                break;
            }
        }
    }

    private void updateUserAgentAndNotify(long uaId, String userAgent, long oldModelId, long newModelId) throws java.sql.SQLException {
        Connection toolsConn = toolsDataSource.getConnection();
        PreparedStatement pst = null;
        try {
            LOG.info("Updating USER_AGENT id={}, changing modelId {} => {} for {}", uaId, oldModelId, newModelId, userAgent);
            pst = toolsConn.prepareStatement("UPDATE USER_AGENT SET MODEL_ID=? WHERE ID=? AND DELETED=false"); //we only update the USER_AGENTs that are not soft deleted
            pst.setLong(1, newModelId);
            pst.setLong(2, uaId);
            pst.executeUpdate();
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, null);
        }

        UserAgentUpdatedMessage msg = new UserAgentUpdatedMessage();
        msg.setChangeType(UserAgentUpdatedMessage.ChangeType.UPDATE);
        msg.setUserAgentId(uaId);
        msg.setUserAgentHeader(userAgent);
        msg.setOldModelId(oldModelId);
        msg.setNewModelId(newModelId);
        jmsUtils.sendObject(centralJmsTemplate, JmsResource.UA_UPDATED_TOPIC, msg);
    }

    private void deleteUserAgentAndNotify(long uaId, String userAgent, long modelId) throws java.sql.SQLException {
        Connection toolsConn = toolsDataSource.getConnection();
        PreparedStatement pst = null;
        try {
            LOG.info("Soft Deleting USER_AGENT id={}, modelId={}, uaHeader={}", uaId, modelId, userAgent);
            pst = toolsConn.prepareStatement("UPDATE USER_AGENT SET DELETED=true WHERE ID=?"); // we do a soft delete
            pst.setLong(1, uaId);
            pst.executeUpdate();
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, null);
        }

        UserAgentUpdatedMessage msg = new UserAgentUpdatedMessage();
        msg.setChangeType(UserAgentUpdatedMessage.ChangeType.DELETE);
        msg.setUserAgentId(uaId);
        msg.setUserAgentHeader(userAgent);
        msg.setOldModelId(modelId);
        jmsUtils.sendObject(centralJmsTemplate, JmsResource.UA_UPDATED_TOPIC, msg);
    }

    /**
     * Get the single "best match" platform for a given model.  This iterates
     * through all matches found and takes the first match...but considers "midp"
     * to be lower priority than all other platforms.
     */
    private static Long getSingleMatchingPlatformId(Map<Long, Map<Long, Match>> matchesByModelAndPlatform, long modelId, long midpPlatformId) {
        Long matchingPlatformId = null;
        Map<Long, Match> matchesByPlatform = matchesByModelAndPlatform.get(modelId);
        if (matchesByPlatform != null) {
            for (Map.Entry<Long, Match> entry : matchesByPlatform.entrySet()) {
                if (entry.getValue() == Match.YES) {
                    if (matchingPlatformId == null) {
                        // This is the first match encountered...assume that's it for now
                        matchingPlatformId = entry.getKey();
                        LOG.debug("Found first match for model id={}, platform id={}", modelId, matchingPlatformId);
                    } else if (matchingPlatformId == midpPlatformId) {
                        // There was already a match, but it was midp, which is the lowest
                        // priority match and can always be replaced with another match.
                        matchingPlatformId = entry.getKey();
                        LOG.debug("Replacing \"{}\" for model id={} with platform id={}", MIDP_PLATFORM_SYSTEM_NAME, modelId, matchingPlatformId);
                    } else {
                        // There was already a match, but it wasn't midp...so that one stays.
                        LOG.debug("Ignoring non-first match for model id={}, platform id={} (previous matching platform id={})", modelId, entry.getKey(), matchingPlatformId);
                    }
                }
            }
        }
        return matchingPlatformId;
    }

    private void generateModelPlatformSql() throws java.io.IOException, java.sql.SQLException {
        LOG.info("Writing MODEL_PLATFORM sql file: " + modelPlatformSqlFile.getCanonicalPath());
        FileOutputStream fos = new FileOutputStream(modelPlatformSqlFile);
        PrintStream out = new PrintStream(fos);
        out.println("truncate MODEL_PLATFORM;");

        Connection toolsConn = toolsDataSource.getConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = toolsConn
                    .prepareStatement("SELECT MODEL.EXTERNAL_ID, PLATFORM.SYSTEM_NAME FROM MODEL_PLATFORM JOIN MODEL ON MODEL.ID=MODEL_PLATFORM.MODEL_ID JOIN PLATFORM ON PLATFORM.ID=MODEL_PLATFORM.PLATFORM_ID");
            rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                if (count++ == 0) {
                    out.print("INSERT INTO MODEL_PLATFORM (MODEL_ID, PLATFORM_ID) VALUES ");
                } else {
                    out.print(",");
                }
                // This makes this sql file "portable" and not ID-dependent.
                // Also, we're using "bulk insert" with a single insert statement
                // instead of having thousands of inserts.
                out.print("((SELECT ID FROM MODEL WHERE EXTERNAL_ID='");
                out.print(rs.getString(1));
                out.print("'),(SELECT ID FROM PLATFORM WHERE SYSTEM_NAME='");
                out.print(rs.getString(2));
                out.print("'))");
            }
            out.println(";");
            out.close();
            fos.close();
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, rs);
        }
    }

    private void generateModelDeviceGroupMappingSql() throws java.io.IOException, java.sql.SQLException {
        LOG.info("Writing MODEL DEVICE_GROUP mapping sql file: {}", modelDeviceGroupSqlFile.getCanonicalPath());
        FileOutputStream fos = new FileOutputStream(modelDeviceGroupSqlFile);
        PrintStream out = new PrintStream(fos);
        out.println("UPDATE MODEL SET DEVICE_GROUP_ID=(SELECT ID FROM DEVICE_GROUP WHERE SYSTEM_NAME='" + DEFAULT_DEVICE_GROUP_SYSTEM_NAME + "');");

        Connection toolsConn = toolsDataSource.getConnection();
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = toolsConn
                    .prepareStatement("SELECT MODEL.EXTERNAL_ID, MODEL.DEVICE_GROUP_ID, DEVICE_GROUP.SYSTEM_NAME FROM MODEL JOIN DEVICE_GROUP ON MODEL.DEVICE_GROUP_ID=DEVICE_GROUP.ID WHERE DEVICE_GROUP.ID!="
                            + defaultDeviceGroupId + " ORDER BY MODEL.DEVICE_GROUP_ID;");
            rs = pst.executeQuery();
            long incumbentDevGroupId = defaultDeviceGroupId;
            final String updQueryTerminator = ");\n";
            String separator = "";
            while (rs.next()) {
                long deviceGroupId = rs.getInt(2);
                if (incumbentDevGroupId == deviceGroupId) {
                    out.print(", " + rs.getString(1));
                } else {
                    out.print(separator + "UPDATE MODEL SET DEVICE_GROUP_ID=(SELECT ID FROM DEVICE_GROUP WHERE SYSTEM_NAME='" + rs.getString(3) + "') WHERE EXTERNAL_ID IN ("
                            + rs.getString(1));
                    incumbentDevGroupId = deviceGroupId;
                }
                separator = updQueryTerminator;
            }
            out.println(separator);
            out.close();
            fos.close();
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, rs);
        }
    }

    private final class UserAgentProcessingThread extends Thread {
        private final Map<String, Long> modelIdByExternalId;
        private final Map<Long, String> platformConstraints;
        private final Map<Long, Map<Long, Match>> matchesByModelAndPlatform;
        private final List<DeviceGroupRow> deviceGroups;
        private final ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel;
        private final LinkedBlockingQueue<UserAgentRow> workerThreadDataQueue;
        private final CountDownLatch threadPoolCountDownLatch;

        private UserAgentProcessingThread(Map<String, Long> modelIdByExternalId, Map<Long, String> platformConstraints, Map<Long, Map<Long, Match>> matchesByModelAndPlatform,
                ConcurrentMap<Long, Set<Long>> matchingDeviceGroupsByModel, List<DeviceGroupRow> deviceGroups, LinkedBlockingQueue<UserAgentRow> workerThreadDataQueue,
                CountDownLatch threadPoolCountDownLatch) {
            super("PlatformMapperThread");
            this.modelIdByExternalId = modelIdByExternalId;
            this.platformConstraints = platformConstraints;
            this.matchesByModelAndPlatform = matchesByModelAndPlatform;
            this.matchingDeviceGroupsByModel = matchingDeviceGroupsByModel;
            this.deviceGroups = deviceGroups;
            this.workerThreadDataQueue = workerThreadDataQueue;
            this.threadPoolCountDownLatch = threadPoolCountDownLatch;
        }

        @Override
        public void run() {
            UserAgentRow row;
            try {
                while ((row = workerThreadDataQueue.take()) != END_OF_DATA_USER_AGENT_ROW) {
                    try {
                        processUserAgentRow(row, modelIdByExternalId, platformConstraints, matchesByModelAndPlatform, matchingDeviceGroupsByModel, deviceGroups);
                    } catch (java.sql.SQLException e) {
                        LOG.error("Failed to process: {} {}", row, e);
                    }
                }
                LOG.debug("************ Thread {}/{} Ending Now", Thread.currentThread().getId(), Thread.currentThread().getName());
                threadPoolCountDownLatch.countDown();
            } catch (InterruptedException e) {
                LOG.warn("Interrupted");
            }
        }
    }

    private static final class UserAgentRow {
        private final long userAgentId;
        private final long modelId;
        private final String userAgent;

        private UserAgentRow(long userAgentId, long modelId, String userAgent) {
            this.userAgentId = userAgentId;
            this.modelId = modelId;
            this.userAgent = userAgent;
        }

        public long getUserAgentId() {
            return userAgentId;
        }

        public long getModelId() {
            return modelId;
        }

        public String getUserAgent() {
            return userAgent;
        }

        @Override
        public String toString() {
            return "UserAgentRow[userAgentId=" + userAgentId + ",modelId=" + modelId + ",userAgent=" + userAgent + "]";
        }
    }

    private static final class DeviceGroupRow {
        private final long id;
        private final String systemName;
        private final String constraints;

        private DeviceGroupRow(long id, String systemName, String constraints) {
            this.id = id;
            this.systemName = systemName;
            this.constraints = constraints;
        }

        public long getId() {
            return id;
        }

        public String getSystemName() {
            return systemName;
        }

        public String getConstraints() {
            return constraints;
        }

        @Override
        public String toString() {
            return "DeviceGroupRow[id=" + id + ", systemName=" + systemName + ", constraints=" + constraints + "]";
        }
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SpringTaskBase.runBean(PlatformMapper.class, "adfonic-tasks-context.xml", "adfonic-toolsdb-context.xml", "adfonic-admreportingdb-context.xml",
                    "platform-mapper-context.xml");
        } catch (Throwable e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
