package com.adfonic.adserver.rtb.impl;

import javax.jms.Queue;

import org.junit.Before;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.RtbBidManager;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.jms.JmsUtils;

public class TestRtbLogicImpl extends BaseAdserverTest{
    
//    private DomainCache domainCache;
//    private AdserverDomainCache adserverDomainCache;
//    private TargetingContext targetingContext;
    private JmsTemplate centralJmsTemplate;
    private TrackingIdentifierLogic trackingIdentifierLogic;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private TargetingEngine targetingEngine;
    private DisplayTypeUtils displayTypeUtils;
    private RtbBidManager rtbBidManager;
    private VhostManager vhostManager;
    private AdResponseLogic adResponseLogic;
    private ImpressionService impressionService;
//    private MarkupGenerator markupGenerator;
    private AdEventFactory adEventFactory;
    private BackupLogger backupLogger;
    private Queue rtbPublicationPersistenceQueue;
    private Queue rtbAdSpaceFormatQueue;
    private JmsUtils jmsUtils;
    private RtbBidLogic rtbLogicImpl;

	@Before
	public void initTests(){
//		domainCache = mock(DomainCache.class);
//		adserverDomainCache = mock(AdserverDomainCache.class);
		targetingContextFactory = mock(TargetingContextFactory.class);
//        targetingContext = mock(TargetingContext.class);
		centralJmsTemplate = mock(JmsTemplate.class, "centralJmsTemplate");
		trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
		preProcessor = mock(PreProcessor.class);
		targetingEngine = mock(TargetingEngine.class);
		displayTypeUtils = mock(DisplayTypeUtils.class);
		rtbBidManager = mock(RtbBidManager.class);
        vhostManager = mock(VhostManager.class);
        adResponseLogic = mock(AdResponseLogic.class);
		impressionService = mock(ImpressionService.class);
//		markupGenerator = mock(MarkupGenerator.class);
		adEventFactory = mock(AdEventFactory.class);
        backupLogger = mock(BackupLogger.class);
		rtbPublicationPersistenceQueue = mock(Queue.class, "rtbPublicationPersistenceQueue");
		rtbAdSpaceFormatQueue = mock(Queue.class, "rtbAdSpaceFormatQueue");
        jmsUtils = mock(JmsUtils.class);

        rtbLogicImpl = new RtbBidLogicImpl();
        inject(rtbLogicImpl, "adEventFactory", adEventFactory);
        inject(rtbLogicImpl, "adResponseLogic", adResponseLogic);
        inject(rtbLogicImpl, "backupLogger", backupLogger);
        inject(rtbLogicImpl, "displayTypeUtils", displayTypeUtils);
        inject(rtbLogicImpl, "impressionService", impressionService);
        //inject(rtbLogicImpl, "markupGenerator", markupGenerator);
        inject(rtbLogicImpl, "preProcessor", preProcessor);
        inject(rtbLogicImpl, "centralJmsTemplate", centralJmsTemplate);
        inject(rtbLogicImpl, "rtbPublicationPersistenceQueue", rtbPublicationPersistenceQueue);
        inject(rtbLogicImpl, "rtbAdSpaceFormatQueue", rtbAdSpaceFormatQueue);
        inject(rtbLogicImpl, "rtbBidManager", rtbBidManager);
        inject(rtbLogicImpl, "targetingContextFactory", targetingContextFactory);
        inject(rtbLogicImpl, "targetingEngine", targetingEngine);
        inject(rtbLogicImpl, "trackingIdentifierLogic", trackingIdentifierLogic);
        inject(rtbLogicImpl, "vhostManager", vhostManager);
        inject(rtbLogicImpl, "jmsUtils", jmsUtils);
	}
	
// MAD-730 - Delete ignored tests in Adserver project
//	@Ignore
//    @Test
//    public void test01_handleWinNoticeAndGenerateXhtmlResponse() throws IOException {
//        final String impressionExternalID = randomAlphaNumericString(10);
//        final BigDecimal settlementPrice = new BigDecimal("1.234");
//        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
//        final RtbBidDetails bidDetails = mock(RtbBidDetails.class, "bidDetails");
//        final Impression impression = mock(Impression.class);
//        final long adSpaceId = randomLong();
//        final long creativeId = randomLong();
//        final TargetingContext context = mock(TargetingContext.class, "context");
//        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
//        final CreativeDto creative = mock(CreativeDto.class, "creative");
//        final long formatId = randomLong();
//        final FormatDto format = mock(FormatDto.class, "format");
//        final String displayTypeSystemName = randomAlphaNumericString(10);
//        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
//        final AdComponents adComponents = mock(AdComponents.class);
//        final String markup = randomAlphaNumericString(10);
//        final AdEvent event = mock(AdEvent.class, "event");
//        final Date eventTime = new Date();
//        final MockHttpServletResponse response = new MockHttpServletResponse();
//        expect(new Expectations() {{
//            oneOf (rtbBidManager).getBidDetailsForWin(impressionExternalID); will(returnValue(bidDetails));
//            oneOf (bidDetails).getImpression(); will(returnValue(impression));
//            allowing (impression).getExternalID(); will(returnValue(impressionExternalID));
//            allowing (impression).getAdSpaceId(); will(returnValue(adSpaceId));
//            allowing (impression).getCreativeId(); will(returnValue(creativeId));
//            oneOf (impression).setRtbBidPrice(settlementPrice);
//            oneOf (impression).setRtbSettlementPrice(settlementPrice);
//            oneOf (rtbBidManager).getTargetingContextFromBidDetails(bidDetails); will(returnValue(context));
//            allowing (context).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
//            oneOf (adserverDomainCache).getAdSpaceById(adSpaceId); will(returnValue(adSpace));
//            allowing (adSpace).getId(); will(returnValue(adSpaceId));
//            oneOf (context).setAdSpace(adSpace);
//            oneOf (adserverDomainCache).getCreativeById(creativeId); will(returnValue(creative));
//            allowing (creative).getId(); will(returnValue(creativeId));
//            allowing (context).getDomainCache(); will(returnValue(domainCache));
//            allowing (creative).getFormatId(); will(returnValue(formatId));
//            oneOf (domainCache).getFormatById(formatId); will(returnValue(format));
//            allowing (bidDetails).getDisplayTypeSystemName(); will(returnValue(displayTypeSystemName));
//            oneOf (domainCache).getDisplayTypeBySystemName(displayTypeSystemName); will(returnValue(displayType));
//            allowing (displayType).getSystemName(); will(returnValue(displayTypeSystemName));
//            ignoring (displayTypeUtils);
//            ignoring (adResponseLogic);
//            ignoring (impressionService);
//            oneOf (markupGenerator).generateMarkup(with(any(AdComponents.class)), with(context), with(creative), with(impression), with(true), with(false)); will(returnValue(markup));
//            oneOf (adEventFactory).newInstance(AdAction.AD_SERVED); will(returnValue(event));
//            oneOf (context).populateAdEvent(event, impression, creative);
//            oneOf (adEventLogger).logAdEvent(event, context);
//            oneOf (event).getEventTime(); will(returnValue(eventTime));
//            oneOf (backupLogger).logRtbWinSuccess(impression, settlementPrice, eventTime, context);
//
//        }});
//        rtbLogicImpl.handleWinNoticeAndWriteResponse(impressionExternalID, settlementPrice.toString(), request, response, null);
//        assertEquals(markup, response.getContentAsString());
//        
//    }
	
	
}
