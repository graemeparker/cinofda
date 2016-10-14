package com.adfonic.adserver;

import java.util.List;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/** This is a base class that provides empty functionality for all targeting
    event listener methods.  Simply extend this class and override the method
    or methods you need.
*/
public class TargetingEventAdapter implements TargetingEventListener {

    @Override
    public void attributesDerived(AdSpaceDto adSpace, TargetingContext context) {
    }

    @Override
    public void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives) {
    }

    @Override
    public void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative) {
    }

    @Override
    public void unfilledRequest(AdSpaceDto adSpace, TargetingContext context) {
    }

    @Override
    public void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit) {
    }

    @Override
    public void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives) {
    }

    @Override
    public void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason) {
    }
}
