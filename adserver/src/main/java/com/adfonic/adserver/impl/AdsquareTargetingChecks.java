package com.adfonic.adserver.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.deriver.impl.AdsquareEnrichAudienceDeriver.AdsquareEnrichAudiences;
import com.adfonic.adserver.rtb.impl.AdsquareWorker;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpAttributeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpSelectorDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.byyd.adsquare.v2.AdsqrEnrichQueryRequest;

/**
 * 
 * @author mvanek
 *
 */
@Component
public class AdsquareTargetingChecks {

    private static final Integer EXCLUSIONS_ONLY_ID = -1;

    @Autowired
    private AdsquareWorker adsquareWorker;

    public CreativeEliminatedReason check(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, SegmentDto segment, TargetingEventListener listener) {
        Set<CampaignAudienceDto> campaignAudiences = creative.getCampaign().getAdsquareAudiences();
        if (campaignAudiences == null || campaignAudiences.isEmpty()) {
            return null;
        }
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (!adsquareWorker.isCountryWhitelisted(country)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AdsquareMismatch, "Country " + country
                        + " is not whitelisted for Adsquare Enrich API");
            }
            return CreativeEliminatedReason.AdsquareMismatch;
        }

        AdsquareEnrichAudiences derivedResponse = context.getAttribute(TargetingContext.ADSQUARE_ENRICH_AUDIENCES); // AdsquareAudienceDeriver & Adsquare rest service
        if (derivedResponse == null) { // null is error accessing adsquare service
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AdsquareMismatch, "Failed or disabled Adsquare Enrich API call");
            }
            return CreativeEliminatedReason.AdsquareMismatch;
        }

        Integer adsqrAudienceId = checkAudienceMatches(context, adSpace, creative, listener, campaignAudiences, derivedResponse.getAudiences());

        if (adsqrAudienceId != null && adsqrAudienceId != EXCLUSIONS_ONLY_ID) {
            // Store Enrichment API request with creatives into context so we can build and send Tracking API request if some of targeted creatives wins
            AdsquareEnrichCreatives creatives = context.getAttribute(TargetingContext.ADSQUARE_ENRICH_CREATIVES, AdsquareEnrichCreatives.class);
            if (creatives == null) {
                creatives = new AdsquareEnrichCreatives(derivedResponse.getQueryRequest(), creative.getId(), adsqrAudienceId);
                context.setAttribute(TargetingContext.ADSQUARE_ENRICH_CREATIVES, creatives);
            } else {
                creatives.addCreativeAudience(creative.getId(), adsqrAudienceId);
            }
        }
        return adsqrAudienceId != null ? null : CreativeEliminatedReason.AdsquareMismatch;
    }

    /**
     * return null when not targeted, otherwise return Adsquare audience id
     */
    private static Integer checkAudienceMatches(TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, TargetingEventListener listener,
            Set<CampaignAudienceDto> campaignAudiences, Collection<Integer> adsquareAudienceIds) {
        if (adsquareAudienceIds == null) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AdsquareMismatch, "No Adsquare API audiences vs " + campaignAudiences);
            }
            return null;
        }

        boolean onlyExcludingAudiences = true;
        // First round - eliminate excluded audiences
        for (CampaignAudienceDto campaignAudience : campaignAudiences) {
            if (campaignAudience.isInclude()) {
                onlyExcludingAudiences = false;
            } else {
                boolean isMatching = null != matchAudiences(adsquareAudienceIds, campaignAudience.getDmpAttributes());
                if (isMatching) {
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AdsquareMismatch, "Adsquare API audience: " + adsquareAudienceIds
                                + " excluded by " + campaignAudience);
                    }
                }
                return null; // Excluded -> not targeted
            }
        }

        if (onlyExcludingAudiences) {
            // Not being excluded and 0 including audiences -> targeted
            return EXCLUSIONS_ONLY_ID;
        }

        // Second round - including audiences make union (any matched is enough to be targeted)
        for (CampaignAudienceDto campaignAudience : campaignAudiences) {
            if (campaignAudience.isInclude()) {
                Integer adsrAudienceId = matchAudiences(adsquareAudienceIds, campaignAudience.getDmpAttributes());
                if (adsrAudienceId != null) {
                    return adsrAudienceId;
                }
            }
        }

        // Sad so sad. No matching campaign audience found for bid location
        if (listener != null) {
            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AdsquareMismatch, "Adsquare API audience: " + adsquareAudienceIds + " not match "
                    + campaignAudiences);
        }
        return null;
    }

    private static Integer matchAudiences(Collection<Integer> adsquareAudienceIds, List<DmpAttributeDto> dmpAttributes) {
        Integer matchedAudExtId = null;
        // attributes make intersection - (and) (all must be matched)
        for (DmpAttributeDto dmpAttribute : dmpAttributes) {
            List<DmpSelectorDto> selectors = dmpAttribute.getSelectors();
            boolean attrMatched = false;
            for (DmpSelectorDto selector : selectors) {
                // selectors make union (or) inside single attribute (any matched is enough)
                int selectorExtId = Integer.parseInt(selector.getExternalId());
                if (adsquareAudienceIds.contains(selectorExtId)) {
                    attrMatched = true;
                    if (matchedAudExtId == null) {
                        // Save first matching audience id to be returned at the end if others will get matched
                        matchedAudExtId = selectorExtId;
                    }
                    break;
                }
            }
            if (!attrMatched) {
                return null;
            }
        }
        return matchedAudExtId;
    }

    public static class AdsquareEnrichCreatives implements Serializable {

        private static final long serialVersionUID = 1L;

        private final AdsqrEnrichQueryRequest queryRequest;

        private final Map<Long, Integer> creativesAudiencesIds;

        public AdsquareEnrichCreatives(AdsqrEnrichQueryRequest queryRequest, Long creativeId, Integer adsqrAudienceId) {
            this.creativesAudiencesIds = new HashMap<Long, Integer>();
            this.creativesAudiencesIds.put(creativeId, adsqrAudienceId);
            this.queryRequest = queryRequest;

        }

        public void addCreativeAudience(Long creativeId, Integer adsqrAudienceId) {
            this.creativesAudiencesIds.put(creativeId, adsqrAudienceId);
        }

        public Map<Long, Integer> getCreativesAudiencesIds() {
            return creativesAudiencesIds;
        }

        public AdsqrEnrichQueryRequest getQueryRequest() {
            return queryRequest;
        }

        @Override
        public String toString() {
            return "AdsquareEnrichCreatives {creativesAudiencesIds=" + creativesAudiencesIds + ", queryRequest=" + queryRequest + "}";
        }

    }

}
