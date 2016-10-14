package com.adfonic.adserver;

import java.util.Collection;

import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public interface TargetingEngine {
    
    /** Normal method used to find an ad for a given request */
    SelectedCreative selectCreative(AdSpaceDto adSpace,
                                    Collection<Long> allowedFormatIds,
                                    TargetingContext context,
                                    boolean diagnosticMode,
                                    boolean strictlyUseFirstDisplayType,
                                    TimeLimit timeLimit,
                                    TargetingEventListener listener);
}
