package com.adfonic.adserver.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.adserver.Stoppage;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.stoppages.StoppagesService;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.jms.StopAdvertiserMessage;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.jms.UnStopAdvertiserMessage;
import com.adfonic.jms.UnStopCampaignMessage;

public class StoppageManagerImpl implements StoppageManager {

    private static final transient Logger LOG = Logger.getLogger(StoppageManagerImpl.class.getName());

    // These are ConcurrentHashMap so that gets are non-blocking.
    // That yields the best performance considering the normal use
    // case is the targeting engine doing real-time checks, and
    // there's never any iteration of these maps' entry sets.
    private final Map<Long, Stoppage> advertiserStoppages = new ConcurrentHashMap<Long, Stoppage>();
    private final Map<Long, Stoppage> campaignStoppages = new ConcurrentHashMap<Long, Stoppage>();

    // private final InternalWebServicesClient iwsClient;
    private final StoppagesService stoppagesService;

    private final boolean lazyInit;
    private volatile boolean initialized = false;

    public StoppageManagerImpl(boolean lazyInit, StoppagesService stoppagesService) throws java.io.IOException {
        this.lazyInit = lazyInit;
        this.stoppagesService = stoppagesService;

        if (lazyInit) {
            LOG.warning("Lazy init allowed, delaying initialization");
        } else {
            initialize();
        }
    }

    /** @{inheritDoc} */
    @Override
    public Map<Long, Stoppage> getAdvertiserStoppages() {
        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through...
            }
        }

        return advertiserStoppages;
    }

    /** @{inheritDoc} */
    @Override
    public Map<Long, Stoppage> getCampaignStoppages() {
        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through...
            }
        }

        return campaignStoppages;
    }

    private void initialize() throws java.io.IOException {
        // Make sure we haven't already initialized...depends on the value
        // of lazyInit at constructor time
        if (initialized) {
            return;
        }

        synchronized (this) {
            if (!initialized) {
                LOG.info("Initializing stoppages");

                advertiserStoppages.putAll(stoppagesService.getAdvertiserStoppages());
                campaignStoppages.putAll(stoppagesService.getCampaignStoppages());

                initialized = true;
                LOG.info("Started StoppageManager adv: " + advertiserStoppages.size() + " campaigns: " + campaignStoppages.size());
            }
        }
    }

    /** @{inheritDoc} */
    @Override
    public boolean isCreativeStopped(CreativeDto creative) {
        return isCampaignStopped(creative.getCampaign()) || isAdvertiserStopped(creative.getCampaign().getAdvertiser());
    }

    /** @{inheritDoc} */
    @Override
    public boolean isCampaignStopped(CampaignDto campaign) {
        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through...
            }
        }

        Stoppage stoppage = campaignStoppages.get(campaign.getId());
        if (stoppage == null) {
            return false;
        } else if (stoppage.isStillInEffect()) {
            return true;
        } else {
            // Remove the stoppage since it no longer applies.
            // That way we don't take the "is still in effect" hit
            // on subsequent checks.
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Removing expired " + stoppage + " for Campaign id=" + campaign.getId());
            }
            campaignStoppages.remove(campaign.getId());
            return false;
        }
    }

    /** @{inheritDoc} */
    @Override
    public boolean isAdvertiserStopped(AdvertiserDto advertiser) {
        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through...
            }
        }

        Stoppage stoppage = advertiserStoppages.get(advertiser.getId());
        if (stoppage == null) {
            return false;
        } else if (stoppage.isStillInEffect()) {
            return true;
        } else {
            // Remove the stoppage since it no longer applies.
            // That way we don't take the "is still in effect" hit
            // on subsequent checks.
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Removing expired " + stoppage + " for Advertiser id=" + advertiser.getId());
            }
            advertiserStoppages.remove(advertiser.getId());
            return false;
        }
    }

    public void onStopAdvertiser(StopAdvertiserMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through and handle the message...we can at least do that
            }
        }

        // Update the advertiser stoppage map
        advertiserStoppages.put(msg.getAdvertiserId(), new Stoppage(msg.getTimestamp(), msg.getReactivateDate()));
    }

    public void onUnStopAdvertiser(UnStopAdvertiserMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through and handle the message...we can at least do that
            }
        }

        // Update the advertiser stoppage map
        advertiserStoppages.remove(msg.getAdvertiserId());
    }

    public void onStopCampaign(StopCampaignMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through and handle the message...we can at least do that
            }
        }

        // Update the campaign stoppage map
        campaignStoppages.put(msg.getCampaignId(), new Stoppage(msg.getTimestamp(), msg.getReactivateDate()));
    }

    public void onUnStopCampaign(UnStopCampaignMessage msg) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Handling: " + msg);
        }

        // Initialize if we haven't already, i.e. when lazyInit=true
        if (lazyInit && !initialized) {
            try {
                initialize();
            } catch (java.io.IOException e) {
                LOG.log(Level.SEVERE, "Initialization failed", e);
                // Still fall through and handle the message...we can at least do that
            }
        }

        // Update the campaign stoppage map
        campaignStoppages.remove(msg.getCampaignId());
    }
}
