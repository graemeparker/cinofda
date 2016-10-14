package com.adfonic.domainserializer.xaudit;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domainserializer.loader.AdCacheBuildContext;

public interface AuditEligibilityCheck {

    boolean isEligible(CreativeDto creative, AdSpaceDto adSpace, AdCacheBuildContext td);
}
