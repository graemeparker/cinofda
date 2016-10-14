package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.controller.dbg.DebugBidController;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgCreativeEliminationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto.AudienceType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpAttributeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpSelectorDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.byyd.factual.MatchResponse;

public class FactualTargetingChecksTest {

    @Test
    public void testProximityTargeted() {

        // Given - Our audience definition
        String code = "proximity-1234";
        CreativeDto creative = buildWithFactual(code, null, true);

        // Given - Simulated Factual response
        List<MatchResponse> factualMatches = new ArrayList<MatchResponse>();
        factualMatches.add(new MatchResponse("design-1234", code, "deployment-1234", "set-1234", "https://api.factual.com/geopulse/pixel/some-encoded-stuff"));
        TargetingContext context = new SimpleTargetingContext();
        context.setAttribute(TargetingContext.FACTUAL_PROXIMITY_MATCHES, factualMatches);

        AdSpaceDto adspace = buildWithPublisher(123l);

        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, adspace, creative, new SegmentDto(), null);

        // Then - targeted
        Assertions.assertThat(elimination).isNull();

        Map<Long, String> trackers = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
        Assertions.assertThat(trackers).hasSize(1);
        // https://api.factual.com/geopulse/pixel/o_bjFACwACAAAAJDUzM2Y4NWI0LWE5ODAtNGZkOC05YTdkLTI5NjgyNWFmNzY1MwgAAwAAAAQKAARMsoeNZAe2QwoABQAMo9tssimBCgAKD113K4sSYHAA
        String pixelUrl = trackers.get(creative.getId());
        Assertions.assertThat(pixelUrl).startsWith("https://api.factual.com/geopulse/pixel/");
        Assertions.assertThat(pixelUrl).endsWith("?deploymentId=deployment-1234&setId=set-1234");
    }

    @Test
    public void testProximityNoAudience() {
        // Given 
        CreativeDto creative = buildWithFactual("whatever-1234", null, true);
        // Given - no Factual response
        TargetingContext context = new SimpleTargetingContext();

        // Prepare listener...
        DbgBidDto dbgBid = new DbgBidDto(RtbExchange.Smaato);
        TargetingEventListener listener = new DebugBidController.DebugTargetingEventListener(dbgBid, new ArrayList<String>());

        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, new AdSpaceDto(), creative, new SegmentDto(), listener);

        // Then - eliminated in targeting
        Assertions.assertThat(elimination).isEqualTo(CreativeEliminatedReason.FactualMismatch);

        List<DbgCreativeEliminationDto> eliminated = dbgBid.getTargetingInfo().getEliminated();
        Assertions.assertThat(eliminated).hasSize(1);
        Assertions.assertThat(eliminated.get(0).getReason()).isEqualTo(CreativeEliminatedReason.FactualMismatch);
        //Assertions.assertThat(eliminated.get(0).getMessage()).isEqualTo("No Bid Factual matches vs " + creative.getCampaign().getFactualProximityAudiences());

        Assertions.assertThat(context.getAttribute(TargetingContext.FACTUAL_CREATIVES)).isNull();
    }

    @Test
    public void testProximityExclusion() {

        // Given - Our audience definition as excluding
        String code = "proximity-1234";
        CreativeDto creative = buildWithFactual(code, null, false);

        // Given - Simulated Factual response
        List<MatchResponse> factualMatches = new ArrayList<MatchResponse>();
        factualMatches.add(new MatchResponse("design-1234", code, "deployment-1234", "set-1234", "https://api.factual.com/geopulse/pixel/some-encoded-stuff"));
        TargetingContext context = new SimpleTargetingContext();
        context.setAttribute(TargetingContext.FACTUAL_PROXIMITY_MATCHES, factualMatches);

        // Prepare listener...
        DbgBidDto dbgBid = new DbgBidDto(RtbExchange.Smaato);
        TargetingEventListener listener = new DebugBidController.DebugTargetingEventListener(dbgBid, new ArrayList<String>());

        AdSpaceDto adspace = buildWithPublisher(123l);
        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, adspace, creative, new SegmentDto(), listener);

        // Then
        Assertions.assertThat(elimination).isEqualTo(CreativeEliminatedReason.FactualMismatch);

        List<DbgCreativeEliminationDto> eliminated = dbgBid.getTargetingInfo().getEliminated();
        Assertions.assertThat(eliminated).hasSize(1);
        Assertions.assertThat(eliminated.get(0).getReason()).isEqualTo(CreativeEliminatedReason.FactualMismatch);
        Assertions.assertThat(eliminated.get(0).getMessage()).isEqualTo(
                "Bid Factual API matches: " + factualMatches + " excluded by " + creative.getCampaign().getFactualProximityAudiences().iterator().next());

        //XXX really excluded beacon?!?!? 
        Map<Long, String> trackers = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
        Assertions.assertThat(trackers).hasSize(1);
    }

    @Test
    public void testProximityDifferentAudience() {

        // Given - Our audience definition with different code
        CreativeDto creative = buildWithFactual("different-1234", null, true);

        // Given - Simulated Factual response with different code
        List<MatchResponse> factualMatches = new ArrayList<MatchResponse>();
        factualMatches.add(new MatchResponse("design-1234", "proximity-1234", "deployment-1234", "set-1234", "https://api.factual.com/geopulse/pixel/some-encoded-stuff"));
        TargetingContext context = new SimpleTargetingContext();
        context.setAttribute(TargetingContext.FACTUAL_PROXIMITY_MATCHES, factualMatches);

        // Prepare listener...
        DbgBidDto dbgBid = new DbgBidDto(RtbExchange.Smaato);
        TargetingEventListener listener = new DebugBidController.DebugTargetingEventListener(dbgBid, new ArrayList<String>());

        AdSpaceDto adspace = buildWithPublisher(132l);
        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, adspace, creative, new SegmentDto(), listener);

        // Then - eliminated in targeting
        Assertions.assertThat(elimination).isEqualTo(CreativeEliminatedReason.FactualMismatch);

        List<DbgCreativeEliminationDto> eliminated = dbgBid.getTargetingInfo().getEliminated();
        Assertions.assertThat(eliminated).hasSize(1);
        Assertions.assertThat(eliminated.get(0).getReason()).isEqualTo(CreativeEliminatedReason.FactualMismatch);
        Assertions.assertThat(eliminated.get(0).getMessage()).isEqualTo(
                "Bid Factual API matches: " + factualMatches + " vs " + creative.getCampaign().getFactualProximityAudiences());

        Assertions.assertThat(context.getAttribute(TargetingContext.FACTUAL_CREATIVES)).isNull();
    }

    @Test
    public void testAudienceTargeted() {

        // Given - Our audience definition
        String code = "audience-1234";
        long publisherId = RtbExchange.Smaato.getPublisherId();
        CreativeDto creative = buildWithFactual(code, publisherId, true);
        // Given - request from right publisher
        AdSpaceDto adSpaceHit = buildWithPublisher(publisherId);

        // Simulated Factual response
        List<MatchResponse> factualMatches = new ArrayList<MatchResponse>();
        factualMatches.add(new MatchResponse("design-1234", code, "deployment-1234", "set-1234", "https://api.factual.com/geopulse/pixel/some-encoded-stuff"));
        TargetingContext context = new SimpleTargetingContext();
        context.setAttribute(TargetingContext.FACTUAL_AUDIENCE_MATCHES, factualMatches);

        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, adSpaceHit, creative, new SegmentDto(), null);

        // Then - targeted
        Assertions.assertThat(elimination).isNull();

        Map<Long, String> trackers = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
        Assertions.assertThat(trackers).hasSize(1);
        String pixelUrl = trackers.get(creative.getId());
        Assertions.assertThat(pixelUrl).startsWith("https://api.factual.com/geopulse/pixel/");
        Assertions.assertThat(pixelUrl).endsWith("?deploymentId=deployment-1234&setId=set-1234");
    }

    @Test
    public void testAudienceDifferentExchange() {
        // Given - Our audience definition
        String code = "audience-1234";
        CreativeDto creative = buildWithFactual(code, RtbExchange.Smaato.getPublisherId(), true);
        // Given - request from different publisher
        AdSpaceDto adSpaceDiff = buildWithPublisher(RtbExchange.Mopub.getPublisherId());

        // Simulated Factual response
        List<MatchResponse> factualMatches = new ArrayList<MatchResponse>();
        factualMatches.add(new MatchResponse("design-1234", code, "deployment-1234", "set-1234", "https://api.factual.com/geopulse/pixel/some-encoded-stuff"));
        TargetingContext context = new SimpleTargetingContext();
        context.setAttribute(TargetingContext.FACTUAL_AUDIENCE_MATCHES, factualMatches);

        // Prepare listener...
        DbgBidDto dbgBid = new DbgBidDto(RtbExchange.Rubicon);
        TargetingEventListener listener = new DebugBidController.DebugTargetingEventListener(dbgBid, new ArrayList<String>());

        // When
        CreativeEliminatedReason elimination = FactualTargetingChecks.check(context, adSpaceDiff, creative, new SegmentDto(), listener);

        // Then - eliminated
        Assertions.assertThat(elimination).isEqualTo(CreativeEliminatedReason.FactualMismatch);

        List<DbgCreativeEliminationDto> eliminated = dbgBid.getTargetingInfo().getEliminated();
        Assertions.assertThat(eliminated).hasSize(1);
        Assertions.assertThat(eliminated.get(0).getReason()).isEqualTo(CreativeEliminatedReason.FactualMismatch);
        Assertions.assertThat(eliminated.get(0).getMessage()).isEqualTo(
                "Bid Exchange " + RtbExchange.Mopub.getPublisherId() + " not in Factual Audiences: " + creative.getCampaign().getFactualAudienceAudiences());

        Map<Long, String> trackers = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
        Assertions.assertThat(trackers).isNull();
    }

    private AdSpaceDto buildWithPublisher(Long publisherId) {
        PublisherDto publisher = new PublisherDto();
        publisher.setId(publisherId);
        PublicationDto publication = new PublicationDto();
        publication.setPublisher(publisher);
        AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setPublication(publication);
        return adSpace;
    }

    private CreativeDto buildWithFactual(String code, Long publisherId, boolean including) {
        DmpSelectorDto selector = new DmpSelectorDto(1l, code, new BigDecimal(2.5), publisherId);
        List<DmpSelectorDto> selectors = Arrays.asList(selector);
        DmpAttributeDto attribute = new DmpAttributeDto(1l, selectors);
        List<DmpAttributeDto> attributes = new ArrayList<DmpAttributeDto>();
        attributes.add(attribute);

        CampaignAudienceDto audience = new CampaignAudienceDto(1l, 1l, AudienceType.FACTUAL, including, attributes, 1, 1, null);
        CampaignDto campaign = new CampaignDto();
        campaign.setId(1l);
        if (publisherId != null) {
            campaign.addFactualAudienceAudience(audience);
        } else {
            campaign.addFactualProximityAudience(audience);
        }

        CreativeDto creative = new CreativeDto();
        creative.setId(1l);
        creative.setCampaign(campaign);
        return creative;
    }
}
