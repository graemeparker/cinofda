package com.adfonic.tasks.combined.tracker;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.adfonic.tracker.InstallService;
import com.adfonic.tracker.PendingAuthenticatedInstall;
import com.adfonic.tracker.PendingInstall;

/**
 * Performs retries of installs that tracker scheduled for retry.
 */
@Component
public class InstallRetry extends AbstractTrackerRetry {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${InstallRetry.batchSize:1000}")
    private int batchSize;
    @Autowired
    private InstallService installService;

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    //@Scheduled(fixedRate=5000)
    public void processPendingInstalls() {
        LOG.debug("Processing pending installs");
        //Overriding batchSize to 0 and it will make it streaming
        // There was an issue while upgrading mysql to mariadb that connectors were
        //converting this statment to SET OPTION SQL_SELECT_LIMIT=batchSize
        //And mariadb do not except OPTION word
        batchSize = 0;
        List<PendingInstall> batch = installService.getPendingInstallsToRetry(batchSize);
        LOG.debug("Pending installs found in batch: {}", batch.size());
        for (PendingInstall pendingInstall : batch) {
            retryPendingInstall(pendingInstall);
        }
    }

    //@Scheduled(fixedRate=5000)
    public void processPendingAuthenticatedInstalls() {
        LOG.debug("Processing pending authenticated installs");
        List<PendingAuthenticatedInstall> batch = installService.getPendingAuthenticatedInstallsToRetry(batchSize);
        LOG.debug("Pending authenticated installs found in batch: {}", batch.size());
        for (PendingAuthenticatedInstall pendingAuthenticatedInstall : batch) {
            retryPendingAuthenticatedInstall(pendingAuthenticatedInstall);
        }
    }

    void retryPendingInstall(PendingInstall pendingInstall) {
        boolean scheduleAnotherRetry = true;
        try {
            LOG.debug("Retrying install for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", pendingInstall.getApplicationId(),
                    pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());

            Click click = getClickService().getClick(pendingInstall);
            if (click == null) {
                LOG.info("Click not found for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", pendingInstall.getApplicationId(),
                        pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());
            } else {
                // Simply finding the click means this retry was successful,
                // regardless of the outcome of tracking the install.
                scheduleAnotherRetry = false;

                trackInstall(click, pendingInstall.getCreationTime(), pendingInstall.isClaim());
            }
        } catch (Exception e) {
            LOG.error("Install tracking retry failed for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={} {}", pendingInstall.getApplicationId(),
                    pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier(), e);
        } finally {
            if (scheduleAnotherRetry) {
                LOG.debug("Rescheduling install retry for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", pendingInstall.getApplicationId(),
                        pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());
                installService.scheduleRetry(pendingInstall);
            } else {
                // Delete it
                LOG.debug("Deleting scheduled install retry for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", pendingInstall.getApplicationId(),
                        pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());
                installService.deleteScheduledInstallRetry(pendingInstall);
            }
        }
    }

    void retryPendingAuthenticatedInstall(PendingAuthenticatedInstall pendingAuthenticatedInstall) {
        boolean scheduleAnotherRetry = true;
        try {
            LOG.debug("Retrying install for clickExternalID={}", pendingAuthenticatedInstall.getClickExternalID());

            Click click = getClickService().getClick(pendingAuthenticatedInstall);
            if (click == null) {
                LOG.info("Click not found for clickExternalID={}", pendingAuthenticatedInstall.getClickExternalID());
            } else {
                // Simply finding the click means this retry was successful,
                // regardless of the outcome of tracking the install.
                scheduleAnotherRetry = false;

                trackInstall(click, pendingAuthenticatedInstall.getCreationTime(), true);
            }
        } catch (Exception e) {
            LOG.error("Authenticated install tracking retry failed for clickExternalID={} {}", pendingAuthenticatedInstall.getClickExternalID(), e);
        } finally {
            if (scheduleAnotherRetry) {
                LOG.debug("Rescheduling authenticated install retry for clickExternalID={}", pendingAuthenticatedInstall.getClickExternalID());
                installService.scheduleRetry(pendingAuthenticatedInstall);
            } else {
                // Delete it
                LOG.debug("Deleting scheduled authenticated install retry for clickExternalID={}", pendingAuthenticatedInstall.getClickExternalID());
                installService.deleteScheduledAuthenticatedInstallRetry(pendingAuthenticatedInstall);
            }
        }
    }

    void trackInstall(Click click, Date eventTime, boolean claim) {
        if (!claim) {
            return; // outta here...don't do anything else
        }

        if (!installService.trackInstall(click)) {
            LOG.warn("Duplicate install tracking request, click.externalID={}, Creative id={}, AdSpace id={}", click.getExternalID(), click.getCreativeId(), click.getAdSpaceId());
            return;
        }

        Creative creative = getCreative(click);
        if (creative == null) {
            LOG.error("Failed to load Creative id={} for click.externalID={}", click.getCreativeId(), click.getExternalID());
            return;
        }

        // We need deviceIdentifiers loaded in order to save retargeting data
        // SC-134 - device identifiers now get logged with the AdEvent
        getClickService().loadDeviceIdentifiers(click);

        AdSpace adSpace = getAdSpace(click);
        if (adSpace == null) {
            LOG.error("Failed to load AdSpace id={} for click.externalID={}", click.getAdSpaceId(), click.getExternalID());
            return;
        }

        AdEvent event = getAdEventFactory().newInstance(AdAction.INSTALL);
        event.populate(click, creative.getCampaign().getId(), adSpace.getPublication().getId());
        event.setEventTime(eventTime, click.getUserTimeZone()); // important: use the actual time of the original event, not the retry
        try{
            net.byyd.archive.model.v1.AdEvent ae = getJSONAdEvent (event);
            logAdEvent(ae);
        }catch (Exception e){
            LOG.error("Error logging to kafka " + e.getMessage());
        }
    }
}
