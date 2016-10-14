package com.adfonic.domain.cache.listener;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;

public interface DSRejectionListener {

    void reject(CreativeDto creative, String reason);

    void eligible(AdSpaceDto adSpace, CreativeDto creative, int effectivePriority);

    void ineligible(AdSpaceDto adSpace, CreativeDto creative, String reason);

    void ineligible(AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, String reason);

}
