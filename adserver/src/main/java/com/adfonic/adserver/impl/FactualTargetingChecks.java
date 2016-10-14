package com.adfonic.adserver.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpAttributeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpSelectorDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.byyd.factual.MatchResponse;

/**
 * 
 * @author mvanek
 * 
 * MAD-847 check audiences against Factual API they call Outpost 
 * 
 */
public class FactualTargetingChecks {

    /**
     * Factual UI allows design fine grained audience definition such as attaching Proximity into Audience to make combination of those two.
     * 
     * It makes litle sense for user to use Byyd audience compositions with inclusions/exclusions instead of using Factual design, 
     * but for sake of consistence with the rest of the Byyd audience types we will follow all usual rules here.
     * 
     * It means that possibly both "proximity" and "audience" Factual audiences can be used together in the same campaign...
     */
    public static CreativeEliminatedReason check(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, TargetingEventListener listener) {
        MatchResult proximityResult;
        Set<CampaignAudienceDto> proximityAudiences = creative.getCampaign().getFactualProximityAudiences();
        if (proximityAudiences != null && !proximityAudiences.isEmpty()) {
            List<MatchResponse> proximityResponse = context.getAttribute(TargetingContext.FACTUAL_PROXIMITY_MATCHES); // FactualAudienceDeriver & Factual Outpost API
            proximityResult = checkInner(context, adSpace, creative, listener, proximityAudiences, proximityResponse);
        } else {
            proximityResult = MatchResult.POSITIVE;
        }

        MatchResult audienceResult;
        Set<CampaignAudienceDto> audienceAudiences = creative.getCampaign().getFactualAudienceAudiences();
        if (audienceAudiences != null && !audienceAudiences.isEmpty()) {
            // Do not call API if we are bidding for untargeted exchange - Factual Audience audiences are defined for particular exchange
            Long publisherId = adSpace.getPublication().getPublisher().getId();
            if (false == hasAudienceForExchange(audienceAudiences, publisherId)) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.FactualMismatch, "Bid Exchange " + publisherId + " not in Factual Audiences: "
                            + audienceAudiences);
                }
                return CreativeEliminatedReason.FactualMismatch;
            }
            List<MatchResponse> audienceResponse = context.getAttribute(TargetingContext.FACTUAL_AUDIENCE_MATCHES); // FactualAudienceDeriver & Factual Outpost API
            audienceResult = checkInner(context, adSpace, creative, listener, audienceAudiences, audienceResponse);
        } else {
            audienceResult = MatchResult.POSITIVE;
        }

        MatchResponse proximityMatch = proximityResult.getMatch();

        if (proximityMatch != null) {
            addPixelUrl(creative, proximityMatch, context);
        }
        MatchResponse audienceMatch = audienceResult.getMatch();
        if (audienceMatch != null) {
            addPixelUrl(creative, audienceMatch, context);
        }
        return proximityResult.isPositive() && audienceResult.isPositive() ? null : CreativeEliminatedReason.FactualMismatch;
    }

    /**
     * Store creative with Factual pixel url into context to be added to markup if this creative bid/win/display
     * 
     * Build Factual Pixel: http://developer.factual.com/geopulse-data-pixel/
     */
    private static void addPixelUrl(CreativeDto creative, MatchResponse match, TargetingContext context) {
        Map<Long, String> creatives = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
        if (creatives == null) {
            creatives = new HashMap<Long, String>();
            context.setAttribute(TargetingContext.FACTUAL_CREATIVES, creatives);
        }
        StringBuilder sb = new StringBuilder(match.getDataPixelUrlPrefix());
        sb.append("?deploymentId=").append(match.getDeploymentId());
        String setId = match.getSetId();
        if (setId != null) {
            sb.append("&setId=").append(setId);
        }
        creatives.put(creative.getId(), sb.toString());
    }

    private static boolean hasAudienceForExchange(Set<CampaignAudienceDto> audienceAudiences, Long publisherId) {
        for (CampaignAudienceDto audience : audienceAudiences) {
            List<DmpAttributeDto> dmpAttributes = audience.getDmpAttributes();
            for (DmpAttributeDto atribute : dmpAttributes) {
                List<DmpSelectorDto> selectors = atribute.getSelectors();
                for (DmpSelectorDto selector : selectors) {
                    if (publisherId.equals(selector.getPublisherId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static MatchResult checkInner(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, TargetingEventListener listener,
            Set<CampaignAudienceDto> campaignAudiences, List<MatchResponse> factualMatches) {

        if (factualMatches == null) {
            if (listener != null) {
                // Null is for failures
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.FactualMismatch, "Failed or disabled Factual API call");
            }
            return MatchResult.NEGATIVE;
        } else if (factualMatches.isEmpty()) {
            if (listener != null) {
                // When deviceid/latitude/longitude is NOT in bid request, we do NOT call Factual obviously and empty list is returned
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.FactualMismatch, "Factual API returned no matches (or nothing to query with)");
            }
            return MatchResult.NEGATIVE;
        }

        Long publisherId = adSpace.getPublication().getPublisher().getId();

        boolean onlyExcludingAudiences = true;
        // First round - excluding audiences
        for (CampaignAudienceDto campaignAudience : campaignAudiences) {
            if (campaignAudience.isInclude()) {
                onlyExcludingAudiences = false;
            } else {
                MatchResponse matched = findMatchBySelectors(factualMatches, campaignAudience.getDmpAttributes(), publisherId);
                if (matched != null) {
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.FactualMismatch, "Bid Factual API matches: " + factualMatches
                                + " excluded by " + campaignAudience);
                    }
                }
                return new MatchResult(false, matched); // TODO this is questionable, will we report exclusions to factual too ???
            }
        }

        if (onlyExcludingAudiences) {
            // Not excluded AND NO including audiences => targeted
            return MatchResult.NEGATIVE;
        }

        // Second round - including audiences create union (any match is enough to be targeted)
        for (CampaignAudienceDto campaignAudience : campaignAudiences) {
            if (campaignAudience.isInclude()) {
                MatchResponse matched = findMatchBySelectors(factualMatches, campaignAudience.getDmpAttributes(), publisherId);
                if (matched != null) {
                    return new MatchResult(true, matched);
                }
            }
        }

        // Sad so sad. No matching campaign audience found for bid location
        if (listener != null) {
            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.FactualMismatch, "Bid Factual API matches: " + factualMatches + " vs "
                    + campaignAudiences);
        }
        return MatchResult.NEGATIVE;
    }

    private static MatchResponse findMatchBySelectors(List<MatchResponse> factualMatches, List<DmpAttributeDto> dmpAttributes, Long bidPublisherId) {
        for (DmpAttributeDto attribute : dmpAttributes) {
            for (DmpSelectorDto selector : attribute.getSelectors()) {
                // There can be one selector for every exchange so make sure to check it against bid publisher
                // skip this check for proximity audiences that have no publisher
                Long audiencePublisherId = selector.getPublisherId();
                if (audiencePublisherId == null || bidPublisherId.equals(selector.getPublisherId())) {
                    String selectorExtId = selector.getExternalId();
                    for (MatchResponse matchResponse : factualMatches) {
                        // Well only targeting code should be really checked (alphanumeric with uderscores), but I expect that many user will do same mistake as I did and use design id (8-12-4-4 UUID)
                        if (selectorExtId.equals(matchResponse.getTargetingCode()) || selectorExtId.equals(matchResponse.getDesignId())) {
                            return matchResponse; // Hurray on first match. TODO verify if it's right
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class MatchResult {

        public static MatchResult POSITIVE = new MatchResult(true, null);
        public static MatchResult NEGATIVE = new MatchResult(false, null);

        private final boolean positive;

        private final MatchResponse match;

        public MatchResult(boolean targeted, MatchResponse match) {
            this.positive = targeted;
            this.match = match;
        }

        public boolean isPositive() {
            return positive;
        }

        public MatchResponse getMatch() {
            return match;
        }

        @Override
        public String toString() {
            return "MatchResult {positive=" + positive + ", match=" + match + "}";
        }

    }

}
