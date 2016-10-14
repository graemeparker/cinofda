package com.adfonic.tracker.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.VideoViewAdEventLogic;
import com.adfonic.tracker.VideoViewService;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.publication.service.PublicationManager;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-jpa-context.xml"})
public class TestVideoViewController extends AbstractAdfonicTest {
    private CreativeManager creativeManager;
    private PublicationManager publicationManager;
    private ClickService clickService;
    private VideoViewService videoViewService;
    private AdEventFactory adEventFactory;
    private VideoViewAdEventLogic videoViewAdEventLogic;
    private TrackerKafka trackerKafka;
    private VideoViewController videoViewController;
    private final int minimumViewMs = 1000;
    private V1DomainModelMapper mapper;

    @Before
    public void runBeforeEachTest() {
        creativeManager = mock(CreativeManager.class);
        publicationManager = mock(PublicationManager.class);
        clickService = mock(ClickService.class);
        videoViewService = mock(VideoViewService.class);
        adEventFactory = mock(AdEventFactory.class);
        videoViewAdEventLogic = new VideoViewAdEventLogic(adEventFactory);
        trackerKafka = mock(TrackerKafka.class);
        mapper = mock(V1DomainModelMapper.class);
        
        videoViewController = new VideoViewController();
        inject(videoViewController, "creativeManager", creativeManager);
        inject(videoViewController, "publicationManager", publicationManager);
        inject(videoViewController, "clickService", clickService);
        inject(videoViewController, "videoViewService", videoViewService);
        inject(videoViewController, "trackerKafka", trackerKafka);
        inject(videoViewController, "minimumViewMs", minimumViewMs);
        inject(videoViewController, "videoViewAdEventLogic", videoViewAdEventLogic);
        inject(videoViewController, "mapper", mapper);
    }
    
    @Test
    public void testTrackVideoView02_clipMs_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = randomInteger();
        final Integer clipMs = null;
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView03_viewMs_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = null;
        final Integer clipMs = randomInteger();
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView05_invalid_clipMs() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = randomInteger();
        final Integer clipMs = 0;
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(0, response.get(VideoViewController.SUCCESS));
        assertEquals(VideoViewController.ERROR_INVALID_VALUE_FOR_CLIPMS, response.get(VideoViewController.ERROR));
    }
    
    @Test
    public void testTrackVideoView06_invalid_viewMs() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = 2000;
        final Integer clipMs = 1999;
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(0, response.get(VideoViewController.SUCCESS));
        assertNotNull(response.get(VideoViewController.ERROR));
    }
    
    @Test
    public void testTrackVideoView07_viewMs_below_threshold() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs - 1;
        final Integer clipMs = viewMs + 1000;
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView08_click_not_found() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = viewMs + 1000;

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(null));
            oneOf (videoViewService).scheduleVideoViewRetry(clickExternalID, viewMs, clipMs);
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView09_duplicate() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = viewMs + 1000;
        final Click click = mock(Click.class);

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(false));
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(0, response.get(VideoViewController.SUCCESS));
        assertEquals(VideoViewController.ERROR_DUPLICATE, response.get(VideoViewController.ERROR));
    }
    
    @Test
    public void testTrackVideoView10_VIEW_Q1() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = 1 + (int)Math.round(viewMs / 0.25);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class,"adevent2");
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
            oneOf (clickService).loadDeviceIdentifiers(click);
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q1); will(returnValue(viewEvent));
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getId(); will(returnValue(campaignId));
            oneOf (adSpace).getPublication(); will(returnValue(publication));
            oneOf (publication).getId(); will(returnValue(publicationId));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            allowing (viewEvent).getAdAction(); will(returnValue(AdAction.VIEW_Q1));
            oneOf (mapper).map(viewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView11_VIEW_Q2() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = 1 + (int)Math.round(viewMs / 0.5);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class,"adevent2");
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
            oneOf (clickService).loadDeviceIdentifiers(click);
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q2); will(returnValue(viewEvent));
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getId(); will(returnValue(campaignId));
            oneOf (adSpace).getPublication(); will(returnValue(publication));
            oneOf (publication).getId(); will(returnValue(publicationId));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            allowing (viewEvent).getAdAction(); will(returnValue(AdAction.VIEW_Q2));
            oneOf (mapper).map(viewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView12_VIEW_Q3() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = 1 + (int)Math.round(viewMs / 0.75);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class,"adevent2");
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
            oneOf (clickService).loadDeviceIdentifiers(click);
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q3); will(returnValue(viewEvent));
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getId(); will(returnValue(campaignId));
            oneOf (adSpace).getPublication(); will(returnValue(publication));
            oneOf (publication).getId(); will(returnValue(publicationId));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            allowing (viewEvent).getAdAction(); will(returnValue(AdAction.VIEW_Q3));
            oneOf (mapper).map(viewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView13_VIEW_Q4() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = (int)Math.floor(viewMs / 0.76);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class,"adevent2");
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
            oneOf (clickService).loadDeviceIdentifiers(click);
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q4); will(returnValue(viewEvent));
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getId(); will(returnValue(campaignId));
            oneOf (adSpace).getPublication(); will(returnValue(publication));
            oneOf (publication).getId(); will(returnValue(publicationId));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            allowing (viewEvent).getAdAction(); will(returnValue(AdAction.VIEW_Q4));
            oneOf (mapper).map(viewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
    
    @Test
    public void testTrackVideoView14_creative_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = (int)Math.floor(viewMs / 0.76);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(null));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(0, response.get(VideoViewController.SUCCESS));
        assertEquals(VideoViewController.ERROR_INTERNAL_ERROR, response.get(VideoViewController.ERROR));
    }
    
    @Test
    public void testTrackVideoView15_adSpace_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = (int)Math.floor(viewMs / 0.76);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(null));
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(0, response.get(VideoViewController.SUCCESS));
        assertEquals(VideoViewController.ERROR_INTERNAL_ERROR, response.get(VideoViewController.ERROR));
    }
    
    @Test
    public void testTrackVideoView17_COMPLETED_VIEW() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Integer viewMs = minimumViewMs;
        final Integer clipMs = viewMs;
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class,"adevent2");
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent completedViewEvent = mock(AdEvent.class, "completedViewEvent");

        expect(new Expectations() {{
            oneOf (clickService).getClickByExternalID(clickExternalID); will(returnValue(click));
            oneOf (videoViewService).trackVideoView(click, viewMs, clipMs); will(returnValue(true));
            allowing (click).getCreativeId(); will(returnValue(creativeId));
            oneOf (creativeManager).getCreativeById(creativeId, VideoViewController.CREATIVE_FETCH_STRATEGY); will(returnValue(creative));
            allowing (click).getAdSpaceId(); will(returnValue(adSpaceId));
            oneOf (publicationManager).getAdSpaceById(adSpaceId, VideoViewController.AD_SPACE_FETCH_STRATEGY); will(returnValue(adSpace));
            oneOf (clickService).loadDeviceIdentifiers(click);
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q4); will(returnValue(viewEvent));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (adSpace).getPublication(); will(returnValue(publication));
            allowing (publication).getId(); will(returnValue(publicationId));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            allowing (viewEvent).getAdAction(); will(returnValue(AdAction.VIEW_Q4));
            oneOf (mapper).map(viewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
            oneOf (adEventFactory).newInstance(AdAction.COMPLETED_VIEW); will(returnValue(completedViewEvent));
            oneOf (completedViewEvent).populate(click, campaignId, publicationId);
            allowing (completedViewEvent).getAdAction(); will(returnValue(AdAction.COMPLETED_VIEW));
            oneOf (mapper).map(completedViewEvent); will(returnValue(ae));
            oneOf (trackerKafka).logAdEvent(ae);
            allowing (ae).getCreativeId();
            allowing (ae).getAdSpaceId();
        }});
        
        Map<String,Object> response = videoViewController.trackVideoView(clickExternalID, viewMs, clipMs);
        assertEquals(1, response.get(VideoViewController.SUCCESS));
    }
}