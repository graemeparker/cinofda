package net.byyd.archive.transform;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.ContinuousPushAWStoMMX;
import net.byyd.archive.model.v1.Gender;
import net.byyd.archive.transform.util.TransformUtil;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class MVELTransformerTest {

	private static final String V1_OUT = "/net/byyd/archive/transform/mapADv1Out.json";
	private static final String V1_1L_OUT = "/net/byyd/archive/transform/mapADv1Out1L.json";
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	

	@Test
	public void testTransform() throws ParseException, IOException {
	    df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(AdEvent.VERSION);
		AdEvent ade = new AdEvent();
		
		ade.setAdomain("http://adomain.com");
		ade.setActionValue(1);
		ade.setAdAction(AdAction.AD_SERVED);
		ade.setAdSpaceId(100);
		ade.setPublicationDomain("bundle-name");
		ade.setCampaignId(200L);
		ade.setCountryId(300L);
		ade.setCreativeId(400L);
		ade.setDateOfBirth(df.parse("1990-01-02"));
		ade.setEventTime(df.parse("2014-06-30"));
		ade.setGender(Gender.FEMALE);
		ade.setGeotargetId(500L);
		ade.setHost("byyd-host-01");
		ade.setImpressionExternalID("7c83af6c-4f33-d0b5-0a8f-02d3ca3b7b4d");
		ade.setIpAddress("80.80.80.80");
		ade.setModelId(600L);
		ade.setOperatorId(700L);
		ade.setPublicationId(800L);
		ade.setRtbBidPrice(new BigDecimal("0.80"));
		ade.setRtbSettlementPrice(new BigDecimal("0.60"));
		ade.setUserAgentHeader("Mozilla/5.0 (iPad; CPU OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D257");

		ade.setAccountingBuyerPremium(new BigDecimal("0.10"));
		ade.setAccountingCampaignDiscount(new BigDecimal("0.15"));
		ade.setAccountingCustMargin(new BigDecimal("0.20"));
		ade.setAccountingDataRetail(new BigDecimal("0.25"));
		ade.setAccountingDataWholesale(new BigDecimal("0.30"));
		ade.setAccountingDirectCost(new BigDecimal("0.35"));
		ade.setAccountingDspMargin(new BigDecimal("0.40"));
		ade.setAccountingPayout(new BigDecimal("0.45"));
		ade.setAccountingTechFee(new BigDecimal("0.50"));

		String expected = IOUtils.toString(getClass().getResource(V1_OUT));
		String actual = tr.transform(ade);
        assertEquals(expected,actual );
        
        String expected1 = IOUtils.toString(getClass().getResource(V1_1L_OUT));
        String actual1 = TransformUtil.oneLineJson(tr.transform(ade));
        assertEquals(expected1, actual1);
	}

	@Test
	public void testExtractHostnameAndShard() {
		assertEquals("adserver02.nca1", ContinuousPushAWStoMMX.retrieveHostname("/events/2014/10/16/adserver02.aws.adf.local/"
				+ "adevent-v1-adserver02.aws.adf.local-2014-10-16-18-01-01-00001.json.gz"));
		assertEquals("nca1", ContinuousPushAWStoMMX.retrieveShard("/events/2014/10/16/adserver02.aws.adf.local/"
				+ "adevent-v1-adserver02.aws.adf.local-2014-10-16-18-01-01-00001.json.gz"));
		assertEquals("lon2adserver13", ContinuousPushAWStoMMX.retrieveHostname("/events/2014/10/16/lon2adserver13.adfonic.com/"
				+ "adevent-v1-lon2adserver13.adfonic.com-2014-10-16-18-01-01-00001.json.gz"));
		assertEquals("lon2", ContinuousPushAWStoMMX.retrieveShard("/events/2014/10/16/lon2adserver13.adfonic.com/"
				+ "adevent-v1-lon2adserver13.adfonic.com-2014-10-16-18-01-01-00001.json.gz"));
	}
}
