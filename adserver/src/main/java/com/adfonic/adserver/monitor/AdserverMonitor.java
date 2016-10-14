package com.adfonic.adserver.monitor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.annotation.NotThreadSafe;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@NotThreadSafe
public class AdserverMonitor {

    private Map<Long, Set<Long>> creativeMonitoringMap = new ConcurrentHashMap<>();
    private Map<Long, Set<Long>> campaignMonitoringMap = new ConcurrentHashMap<>();
    private Map<String, String> eleminationReasons = new ConcurrentHashMap<String, String>(200);
    private boolean monitor = false;
    private TargetingEventListener targetingEventListener = new AdserverMonitoringTargetEventListener();

    private ConcurrentMap<Long, ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>>> creativeRejectionCounters = new ConcurrentHashMap<>();

    private ConcurrentMap<Long, ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>>> campaignRejectionCounters = new ConcurrentHashMap<>();

    /**
     * Get an existing counter by name, or create it if it doesn't exist yet.
     */
    private AtomicLong getOrCreateCounter(ConcurrentMap<Long, ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>>> rejectionCounters, long creativeCampaignId, Long adspaceId,
            String error) {
        ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> creativeCampaignMap = rejectionCounters.get(creativeCampaignId);
        if (creativeCampaignMap == null) {
            creativeCampaignMap = new ConcurrentHashMap<>();
            ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> gotThereFirst = rejectionCounters.putIfAbsent(creativeCampaignId, creativeCampaignMap);
            if (gotThereFirst != null) {
                creativeCampaignMap = gotThereFirst;
            }
        }
        if (adspaceId == null) {
            adspaceId = 0L;
        }
        ConcurrentMap<String, AtomicLong> adSpaceMap = creativeCampaignMap.get(adspaceId);
        if (adSpaceMap == null) {
            adSpaceMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, AtomicLong> gotThereFirst = creativeCampaignMap.putIfAbsent(adspaceId, adSpaceMap);
            if (gotThereFirst != null) {
                adSpaceMap = gotThereFirst;
            }
        }
        AtomicLong counter = adSpaceMap.get(error);
        if (counter == null) {
            counter = new AtomicLong(0);
            AtomicLong gotThereFirst = adSpaceMap.putIfAbsent(error, counter);
            if (gotThereFirst != null) {
                counter = gotThereFirst;
            }
        }
        return counter;
    }

    //Not using the system wide instance of counter Manager as its not required to use that one

    public ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> getCreativeMonitoringData(long creativeId) {
        return creativeRejectionCounters.get(creativeId);
    }

    public ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> getCampaignMonitoringData(long campaignId) {
        return campaignRejectionCounters.get(campaignId);
    }

    public void setCreativeMonitorning(long creativeId) {
        Set<Long> adspaceIds = creativeMonitoringMap.get(creativeId);
        if (adspaceIds == null) {
            adspaceIds = new HashSet<Long>();
            creativeMonitoringMap.put(creativeId, adspaceIds);
        }
        monitor = true;
    }

    public void setCreativeAdspaceMonitorning(long creativeId, long adspaceId) {
        Set<Long> adspaceIds = creativeMonitoringMap.get(creativeId);
        if (adspaceIds == null) {
            adspaceIds = new HashSet<Long>();
            creativeMonitoringMap.put(creativeId, adspaceIds);
        }
        adspaceIds.add(adspaceId);
        monitor = true;
    }

    public void setCampaignMonitorning(long campaignId) {
        Set<Long> adspaceIds = campaignMonitoringMap.get(campaignId);
        if (adspaceIds == null) {
            adspaceIds = new HashSet<Long>();
            campaignMonitoringMap.put(campaignId, adspaceIds);
        }
        monitor = true;
    }

    public void setCampaignAdspaceMonitorning(long campaignId, long adspaceId) {
        Set<Long> adspaceIds = campaignMonitoringMap.get(campaignId);
        if (adspaceIds == null) {
            adspaceIds = new HashSet<Long>();
            campaignMonitoringMap.put(campaignId, adspaceIds);
        }
        adspaceIds.add(adspaceId);
        monitor = true;
    }

    private void updateMonitoringStatus() {
        if (campaignMonitoringMap.isEmpty() && creativeMonitoringMap.isEmpty()) {
            monitor = false;
        } else {
            monitor = true;
        }
    }

    public void clearAllMonitoring() {
        campaignMonitoringMap.clear();
        creativeMonitoringMap.clear();
        creativeRejectionCounters.clear();
        campaignRejectionCounters.clear();
        monitor = false;
    }

    public void clearCreativeMonitoring(long creativeId) {
        creativeMonitoringMap.remove(creativeId);
        creativeRejectionCounters.remove(creativeId);
        updateMonitoringStatus();
    }

    public void clearCampaignMonitoring(long campaignId) {
        campaignMonitoringMap.remove(campaignId);
        campaignRejectionCounters.remove(campaignId);
        updateMonitoringStatus();
    }

    public void clearAllCreativeMonitoring() {
        creativeMonitoringMap.clear();
        creativeRejectionCounters.clear();
        updateMonitoringStatus();
    }

    public void clearAllCampaignMonitoring() {
        campaignMonitoringMap.clear();
        campaignRejectionCounters.clear();
        updateMonitoringStatus();
    }

    public TargetingEventListener getTargetingEventListener() {
        if (monitor) {
            return targetingEventListener;
        }
        return null;
    }

    public Set<Long> getAllCreativesBeingMonitored() {
        return creativeMonitoringMap.keySet();
    }

    public Set<Long> getAllCampaignsBeingMonitored() {
        return campaignMonitoringMap.keySet();
    }

    public class AdserverMonitoringTargetEventListener implements TargetingEventListener {

        @Override
        public void attributesDerived(AdSpaceDto adSpace, TargetingContext context) {
            // TODO Auto-generated method stub

        }

        @Override
        public void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives) {
            // TODO Auto-generated method stub

        }

        @Override
        public void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason) {
            try {
                AdserverMonitor.this.eleminationReasons.put(reason.name(), detailedReason);
                Set<Long> creativeAdspaces = creativeMonitoringMap.get(creative.getId());
                if (creativeAdspaces != null) {
                    updateCounters(creativeRejectionCounters, creative.getId(), creativeAdspaces, reason, adSpace.getId());
                }
                Set<Long> campaignAdspaces = campaignMonitoringMap.get(creative.getCampaign().getId());
                if (campaignAdspaces != null) {
                    updateCounters(campaignRejectionCounters, creative.getCampaign().getId(), campaignAdspaces, reason, adSpace.getId());
                }
            } catch (Exception ex) {
                //any exception just let it go silently shudnt affect adserver functionality.
            }

        }

        private void updateCounters(ConcurrentMap<Long, ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>>> rejectionCounters, Long creativeCampaignId, Set<Long> adspaceids,
                CreativeEliminatedReason reason, Long adspaceId) {
            AtomicLong counter;
            if (adspaceids.isEmpty()) {
                counter = getOrCreateCounter(rejectionCounters, creativeCampaignId, null, reason.name());
                counter.incrementAndGet();
            } else {
                for (Long oneAdspaceId : adspaceids) {
                    if (oneAdspaceId.equals(adspaceId)) {
                        counter = getOrCreateCounter(rejectionCounters, creativeCampaignId, adspaceId, reason.name());
                        counter.incrementAndGet();
                    }
                }
            }

        }

        @Override
        public void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives) {
            // TODO Auto-generated method stub

        }

        @Override
        public void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative) {
            try {
                AdserverMonitor.this.eleminationReasons.put("Creative Selected", "Creative has been selected");
                Set<Long> creativeAdspaces = creativeMonitoringMap.get(creative.getId());
                if (creativeAdspaces != null) {
                    updateCounters(creativeRejectionCounters, creative.getId(), creativeAdspaces, CreativeEliminatedReason.Selected, adSpace.getId());
                }
                Set<Long> campaignAdspaces = campaignMonitoringMap.get(creative.getCampaign().getId());
                if (campaignAdspaces != null) {
                    updateCounters(campaignRejectionCounters, creative.getCampaign().getId(), campaignAdspaces, CreativeEliminatedReason.Selected, adSpace.getId());
                }
            } catch (Exception ex) {
                //any exception just let it go silently shudnt affect adserver functionality.
            }
        }

        @Override
        public void unfilledRequest(AdSpaceDto adSpace, TargetingContext context) {
            // TODO Auto-generated method stub

        }

        @Override
        public void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit) {
            // TODO Auto-generated method stub

        }

    }
}
