package com.adfonic.domain.cache.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.annotation.NotThreadSafe;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;

@NotThreadSafe
public class DSRejectionListenerImpl implements DSRejectionListener {

    private static final transient Logger LOG = Logger.getLogger(DSRejectionListenerImpl.class.getName());

    private Map<Long, Set<Long>> adspaceCreativeMonitors = new HashMap<>();
    private Map<Long, Set<Long>> adspaceCampaignMonitors = new HashMap<>();
    private Map<Long, Set<Long>> publicationCreativeMonitors = new HashMap<>();
    private Map<Long, Set<Long>> publicationCampaignMonitors = new HashMap<>();
    private Map<Long, Set<Long>> publisherCreativeMonitors = new HashMap<>();
    private Map<Long, Set<Long>> publisherCampaignMonitors = new HashMap<>();

    private ConcurrentMap<AdspaceCreativeRejection, String> rejectionReasons = new ConcurrentHashMap<>();

    /*
        @Override
        public void monitor(Long adspaceId, Long publicationId, Long publisherId, Long creativeId, Long campaignId) {
            addEntryToHashMap(publisherId, creativeId, publisherCreativeMonitors, true);
            addEntryToHashMap(publisherId, campaignId, publisherCampaignMonitors, false);
            addEntryToHashMap(publicationId, creativeId, publicationCreativeMonitors, true);
            addEntryToHashMap(publicationId, campaignId, publicationCampaignMonitors, false);
            addEntryToHashMap(adspaceId, creativeId, adspaceCreativeMonitors, true);
            addEntryToHashMap(adspaceId, campaignId, adspaceCampaignMonitors, false);
        }
    */
    private void addEntryToHashMap(Long keyId, Long value, Map<Long, Set<Long>> map, boolean addEvenIfValueIsNull) {
        if (keyId != null) {
            Set<Long> valueSet = map.get(keyId);
            if (valueSet == null) {
                if (addEvenIfValueIsNull) {
                    valueSet = new HashSet<>();
                    map.put(keyId, valueSet);
                }
            }
            if (value != null && valueSet != null) {
                valueSet.add(value);
            }
        }
    }

    @Override
    public void eligible(AdSpaceDto adSpace, CreativeDto creative, int effectivePriority) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("adid: " + adSpace.getId() + ", crid: " + creative.getId() + " ELIGIBLE with priority " + effectivePriority);
        }
    }

    @Override
    public void ineligible(AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, String reason) {
        doLog(adSpace, creative, segment, reason);
        doReject(adSpace, creative, reason);
    }

    @Override
    public void ineligible(AdSpaceDto adSpace, CreativeDto creative, String reason) {
        doLog(adSpace, creative, null, reason);
        doReject(adSpace, creative, reason);
    }

    @Override
    public void reject(CreativeDto creative, String reason) {
        doLog(null, creative, null, reason);
    }

    private void doLog(AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, String reason) {
        if (LOG.isLoggable(Level.FINEST)) {
            StringBuilder builder = new StringBuilder();
            if (adSpace != null) {
                builder.append("adid: ").append(adSpace.getId());
            }
            if (creative != null) {
                builder.append(", crid: ").append(creative.getId()).append(" REJECTED");
            } else {
                builder.append(" REJECTED ANY Creative");
            }
            if (segment != null) {
                builder.append(", sgid: ").append(segment.getId());
            }
            builder.append(", Reason: ").append(reason);
            LOG.finest(builder.toString());
        }
    }

    private void doReject(AdSpaceDto adSpace, CreativeDto creative, String reason) {
        if (adSpace != null && creative != null) {
            addRejectionReason(adSpace.getPublication().getPublisher().getId(), creative.getId(), publisherCreativeMonitors, adSpace, creative, reason);
            addRejectionReason(adSpace.getPublication().getPublisher().getId(), creative.getCampaign().getId(), publisherCampaignMonitors, adSpace, creative, reason);
            addRejectionReason(adSpace.getPublication().getId(), creative.getId(), publicationCreativeMonitors, adSpace, creative, reason);
            addRejectionReason(adSpace.getPublication().getId(), creative.getCampaign().getId(), publicationCampaignMonitors, adSpace, creative, reason);
            addRejectionReason(adSpace.getId(), creative.getId(), adspaceCreativeMonitors, adSpace, creative, reason);
            addRejectionReason(adSpace.getId(), creative.getCampaign().getId(), adspaceCampaignMonitors, adSpace, creative, reason);
            addRejectionReason(-1L, creative.getId(), adspaceCreativeMonitors, adSpace, creative, reason);
            addRejectionReason(-1L, creative.getCampaign().getId(), adspaceCampaignMonitors, adSpace, creative, reason);
        }
    }

    private void addRejectionReason(Long publisherSideId, Long advertisingSideId, Map<Long, Set<Long>> map, AdSpaceDto adSpace, CreativeDto creative, String reason) {
        boolean monitorIt = false;
        Set<Long> valueSet = map.get(publisherSideId);
        if (valueSet != null) {
            if (valueSet.isEmpty()) {
                monitorIt = true;
            } else if (valueSet.contains(advertisingSideId)) {
                monitorIt = true;
            }
        }

        if (monitorIt) {
            AdspaceCreativeRejection adspaceCreativeRejection = new AdspaceCreativeRejection(adSpace.getId(), creative.getId());
            //For a unique key it will/should be called only once
            rejectionReasons.putIfAbsent(adspaceCreativeRejection, reason);
        }
    }

    public static class AdspaceCreativeRejection {
        private Long adspaceId;
        private Long creativeId;

        public AdspaceCreativeRejection(Long adspaceId, Long creativeId) {
            super();
            this.adspaceId = adspaceId;
            this.creativeId = creativeId;
        }

        public Long getAdspaceId() {
            return adspaceId;
        }

        public void setAdspaceId(Long adspaceId) {
            this.adspaceId = adspaceId;
        }

        public Long getCreativeId() {
            return creativeId;
        }

        public void setCreativeId(Long creativeId) {
            this.creativeId = creativeId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((adspaceId == null) ? 0 : adspaceId.hashCode());
            result = prime * result + ((creativeId == null) ? 0 : creativeId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AdspaceCreativeRejection other = (AdspaceCreativeRejection) obj;
            if (adspaceId == null) {
                if (other.adspaceId != null)
                    return false;
            } else if (!adspaceId.equals(other.adspaceId))
                return false;
            if (creativeId == null) {
                if (other.creativeId != null)
                    return false;
            } else if (!creativeId.equals(other.creativeId))
                return false;
            return true;
        }
    }
    /*
        @Override
        public void printAllRejectReasons(Logger logger, Level level) {
            //System.out.println("Printing all rejection reasons : "+ logger.isLoggable(level));
            if (logger.isLoggable(level)) {
                for (Entry<AdspaceCreativeRejection, String> oneEntry : rejectionReasons.entrySet()) {
                    logger.log(level, oneEntry.getKey().getAdspaceId() + " , " + oneEntry.getKey().getCreativeId() + " ineligible for the reason \"" + oneEntry.getValue() + "\"");
                }
            }

        }

        @Override
        public void clearAll() {
            adspaceCreativeMonitors.clear();
            adspaceCampaignMonitors.clear();
            publicationCreativeMonitors.clear();
            publicationCampaignMonitors.clear();
            publisherCreativeMonitors.clear();
            publisherCampaignMonitors.clear();
            rejectionReasons.clear();

            rejectionReasons = new ConcurrentHashMap<>();
        }

        @Override
        public Map<AdspaceCreativeRejection, String> getAllRejectReasons() {
            return new HashMap<>(rejectionReasons);
        }
    */

}
