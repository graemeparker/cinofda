package com.adfonic.tracker;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.test.AbstractAdfonicTest;

public class TestVideoViewAdEventLogic extends AbstractAdfonicTest {
    private AdEventFactory adEventFactory;
    private VideoViewAdEventLogic videoViewAdEventLogic;

    @Before
    public void runBeforeEachTest() {
        adEventFactory = mock(AdEventFactory.class);
        videoViewAdEventLogic = new VideoViewAdEventLogic(adEventFactory);
    }

    @Test
    public void test01_getAdEventsToLog_VIEW_Q1() {
        final Click click = mock(Click.class);
        final int viewMs = 0;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q1); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }
    
    @Test
    public void test02_getAdEventsToLog_VIEW_Q1() {
        final Click click = mock(Click.class);
        final int viewMs = 249;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q1); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }

    @Test
    public void test03_getAdEventsToLog_VIEW_Q2() {
        final Click click = mock(Click.class);
        final int viewMs = 250;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q2); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }
    
    @Test
    public void test04_getAdEventsToLog_VIEW_Q2() {
        final Click click = mock(Click.class);
        final int viewMs = 499;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q2); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }

    @Test
    public void test05_getAdEventsToLog_VIEW_Q3() {
        final Click click = mock(Click.class);
        final int viewMs = 500;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q3); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }
    
    @Test
    public void test06_getAdEventsToLog_VIEW_Q3() {
        final Click click = mock(Click.class);
        final int viewMs = 749;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q3); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }
    
    @Test
    public void test07_getAdEventsToLog_VIEW_Q4() {
        final Click click = mock(Click.class);
        final int viewMs = 750;
        final int clipMs = 1000;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q4); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(1, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
    }
    
    @Test
    public void test08_getAdEventsToLog_VIEW_Q4_and_COMPLETED_VIEW() {
        final Click click = mock(Click.class);
        final int viewMs = 1000;
        final int clipMs = viewMs;
        final long campaignId = randomLong();
        final long publicationId = randomLong();
        final AdEvent viewEvent = mock(AdEvent.class, "viewEvent");
        final AdEvent completedViewEvent = mock(AdEvent.class, "completedViewEvent");
        expect(new Expectations() {{
            oneOf (adEventFactory).newInstance(AdAction.VIEW_Q4); will(returnValue(viewEvent));
            oneOf (viewEvent).populate(click, campaignId, publicationId);
            oneOf (viewEvent).setActionValue(viewMs);
            oneOf (adEventFactory).newInstance(AdAction.COMPLETED_VIEW); will(returnValue(completedViewEvent));
            oneOf (completedViewEvent).populate(click, campaignId, publicationId);
        }});
        List<AdEvent> adEvents = videoViewAdEventLogic.getAdEventsToLog(click, viewMs, clipMs, campaignId, publicationId);
        assertEquals(2, adEvents.size());
        assertEquals(viewEvent, adEvents.get(0));
        assertEquals(completedViewEvent, adEvents.get(1));
    }
}
