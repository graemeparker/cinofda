package net.byyd.archive.transform;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.byyd.archive.mapping.AnnotationNamePropertyNamingStrategy;
import net.byyd.archive.model.v1.AdEvent;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MVELMMXTemplatesTest {

	private static final String AD_SERVED_IN = "/net/byyd/archive/transform/mapAD_SERVEDv2In.json";
	private static final String AD_SERVED_OUT = "/net/byyd/archive/transform/mapAD_SERVEDv2Out.json";

	private static final String AD_SERVED_IN_INV = "/net/byyd/archive/transform/mapAD_SERVEDv3In.json";
	private static final String AD_SERVED_OUT_INV = "/net/byyd/archive/transform/mapAD_SERVEDv3Out.json";

	private static final String CLICK_IN = "/net/byyd/archive/transform/mapCLICKv2In.json";
	private static final String CLICK_OUT = "/net/byyd/archive/transform/mapCLICKv2Out.json";

	private static final String IMPRESSION_IN = "/net/byyd/archive/transform/mapIMPRESSIONv2In.json";
	private static final String IMPRESSION_OUT = "/net/byyd/archive/transform/mapIMPRESSIONv2Out.json";

	private static final String UNFILLED_IN = "/net/byyd/archive/transform/mapUNFILLED_REQUESTv2In.json";
	private static final String UNFILLED_OUT = "/net/byyd/archive/transform/mapUNFILLED_REQUESTv2Out.json";

	private static final String RTB_FAILED_IN = "/net/byyd/archive/transform/mapRTB_FAILEDv2In.json";
	private static final String RTB_FAILED_OUT = "/net/byyd/archive/transform/mapRTB_FAILEDv2Out.json";

	private static final String UNFILLED_NC_IN = "/net/byyd/archive/transform/mapUNFILLED_REQUESTv3In.json";
	private static final String UNFILLED_NC_OUT = "/net/byyd/archive/transform/mapUNFILLED_REQUESTv3Out.json";

	private static final String RTB_FAILED_NC_IN = "/net/byyd/archive/transform/mapRTB_FAILEDv3In.json";
	private static final String RTB_FAILED_NC_OUT = "/net/byyd/archive/transform/mapRTB_FAILEDv3Out.json";

	private static final String RTB_LOST_IN = "/net/byyd/archive/transform/mapRTB_LOSTv1In.json";
	private static final String RTB_LOST_OUT = "/net/byyd/archive/transform/mapRTB_LOSTv1Out.json";

	private ObjectMapper om = new ObjectMapper();

	@Before
	public void setup() throws ParseException {
		om = new ObjectMapper();
		om.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());
		MVELTransformer.TEST_DATE = new SimpleDateFormat("yyyy-MM-dd")
				.parse("2014-01-01");
	}

	@Test
	public void testAdServed() throws ParseException, IOException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(AD_SERVED_IN);

		assertEquals(read(AD_SERVED_OUT), tr.transform(ae));
	}

	@Test
	public void testRtbLost() throws ParseException, IOException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(RTB_LOST_IN);

		assertEquals(read(RTB_LOST_OUT), tr.transform(ae));
	}

	@Test
	public void testAdServedInvalid() throws ParseException, IOException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>("v2");

		AdEvent ae = readEvent(AD_SERVED_IN_INV);

		assertEquals(read(AD_SERVED_OUT_INV), tr.transform(ae));
	}

	@Test
	public void testClick() throws ParseException, IOException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(CLICK_IN);

		assertEquals(read(CLICK_OUT), tr.transform(ae));
	}

	@Test
	@Ignore("FIXME!!! whitespace in array")
	public void testImpression() throws ParseException, IOException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(IMPRESSION_IN);

		assertEquals(read(IMPRESSION_OUT), tr.transform(ae));
	}

	@Test
	public void testUnfilledRequest() throws ParseException, IOException,
			NoSuchAlgorithmException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(UNFILLED_IN);
		ae.setMessageHash(MVELTransformer.hashMD5(IOUtils.toString(getClass()
				.getResource(UNFILLED_IN))));
		assertEquals(read(UNFILLED_OUT), tr.transform(ae));
	}

	@Test
	public void testUnfilledRequestNc() throws ParseException, IOException,
			NoSuchAlgorithmException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);

		AdEvent ae = readEvent(UNFILLED_NC_IN);
		ae.setMessageHash(MVELTransformer.hashMD5(IOUtils.toString(getClass()
				.getResource(UNFILLED_NC_IN))));
		assertEquals(read(UNFILLED_NC_OUT), tr.transform(ae));
	}

	@Test
	public void testRtbFailedRequest() throws ParseException, IOException,
			NoSuchAlgorithmException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);
		MVELTransformer.TEST_DATE = new SimpleDateFormat("yyyy-MM-dd")
				.parse("2014-01-01");

		AdEvent ae = readEvent(RTB_FAILED_IN);
		ae.setMessageHash(MVELTransformer.hashMD5(IOUtils.toString(getClass()
				.getResource(RTB_FAILED_IN))));
		assertEquals(read(RTB_FAILED_OUT), tr.transform(ae));
	}
	
	@Test
	public void testRtbFailedRequestNc() throws ParseException, IOException,
			NoSuchAlgorithmException {
		MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(
				AdEvent.VERSION);
		MVELTransformer.TEST_DATE = new SimpleDateFormat("yyyy-MM-dd")
				.parse("2014-01-01");

		AdEvent ae = readEvent(RTB_FAILED_NC_IN);
		ae.setMessageHash(MVELTransformer.hashMD5(IOUtils.toString(getClass()
				.getResource(RTB_FAILED_NC_IN))));
		assertEquals(read(RTB_FAILED_NC_OUT), tr.transform(ae));
	}

	private String read(String res) throws IOException {
		return IOUtils.toString(getClass().getResource(res));
	}

	private AdEvent readEvent(String res) throws IOException {
		String in = IOUtils.toString(getClass().getResource(res));

		AdEvent event = om.readValue(in.replaceAll("\\\\0", "\\u0"), AdEvent.class);
		event.setServerName("ch1adserver13");
		event.setShard("ch1");
		
		return event;
	}

}
