package com.adfonic.adserver;

import java.util.List;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public interface TargetingEventListener {

    void attributesDerived(AdSpaceDto adSpace, TargetingContext context);

    void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives);

    void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason);

    void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives);

    void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative);

    void unfilledRequest(AdSpaceDto adSpace, TargetingContext context);

    void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit);
}
