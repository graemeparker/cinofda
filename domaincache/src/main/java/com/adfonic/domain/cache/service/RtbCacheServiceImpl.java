package com.adfonic.domain.cache.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public class RtbCacheServiceImpl implements RtbCacheService {

    private static final long serialVersionUID = 1L;
    private boolean rtbEnabled;

    public RtbCacheServiceImpl() {

    }

    public RtbCacheServiceImpl(RtbCacheServiceImpl copy) {
        this.rtbAdSpaces.putAll(copy.rtbAdSpaces);
    }

    final private Map<Long, Map<String, AdSpaceDto>> rtbAdSpaces = new HashMap<Long, Map<String, AdSpaceDto>>();

    @Override
    public void addRtbPublicationAdSpace(AdSpaceDto adspace) {
        if (!StringUtils.isBlank(adspace.getPublication().getRtbId())) {
            Map<String, AdSpaceDto> publisherRtbAdSpaces = rtbAdSpaces.get(adspace.getPublication().getPublisher().getId());
            if (publisherRtbAdSpaces == null) {
                publisherRtbAdSpaces = new HashMap<String, AdSpaceDto>();
                rtbAdSpaces.put(adspace.getPublication().getPublisher().getId(), publisherRtbAdSpaces);
            }
            publisherRtbAdSpaces.put(adspace.getPublication().getRtbId(), adspace);
        }

    }

    @Override
    public Map<String, AdSpaceDto> getPublisherRtbAdSpacesMap(Long publisherId) {
        return rtbAdSpaces.get(publisherId);
    }

    @Override
    public void afterDeserialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeSerialization() {
        // TODO Auto-generated method stub

    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "RTB Enabled = " + this.isRtbEnabled());
            logger.log(level, "Total RTB AdSpaces by Publiser = " + this.rtbAdSpaces.size());
            if (this.rtbAdSpaces.size() > 0) {
                for (Entry<Long, Map<String, AdSpaceDto>> oneEntry : this.rtbAdSpaces.entrySet()) {
                    logger.log(level, "    Total RTB AdSpaces for Publiser " + oneEntry.getKey() + " = " + oneEntry.getValue().size());
                }
            }
        }
    }

    @Override
    public AdSpaceDto getAdSpaceByPublicationRtbId(Long publisherId, String publicationRtbId) {
        Map<String, AdSpaceDto> publisherRtbAdSpaces = rtbAdSpaces.get(publisherId);
        if (publisherRtbAdSpaces != null) {
            return publisherRtbAdSpaces.get(publicationRtbId);
        }
        return null;
    }

    @Override
    public boolean isRtbEnabled() {
        return rtbEnabled;
    }

    public void setRtbEnabled(boolean rtbEnabled) {
        this.rtbEnabled = rtbEnabled;
    }
}
