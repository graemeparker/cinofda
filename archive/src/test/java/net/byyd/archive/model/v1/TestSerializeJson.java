package net.byyd.archive.model.v1;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.byyd.archive.mapping.AnnotationNamePropertyNamingStrategy;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.util.Range;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class TestSerializeJson {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	AdEvent a;
	com.adfonic.adserver.AdEvent o;

	@Before
	public void setUp() throws ParseException {
		a = new AdEvent();
		o = new com.adfonic.adserver.AdEvent();
	
		o.setAdAction(com.adfonic.domain.AdAction.AD_SERVED);
		o.setActionValue(12);
		o.setAdSpaceId(1235L);
		o.setAgeRange(new Range<Integer>(14, 29, true));
		o.setDateOfBirth(df.parse("2014-02-06"));		
	}
	
	@Test
	public void test() throws Exception {
		V1DomainModelMapper dmv1 = new V1DomainModelMapper();
		a = dmv1.map(o); 
		
		ObjectMapper om = new ObjectMapper();
		om.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());

		String json = om.writeValueAsString(a);

		AdEvent r = om.readValue(json, AdEvent.class);

		assertEquals(a.getAdAction(), r.getAdAction());
		assertEquals(a.getActionValue(), r.getActionValue());
		assertEquals(a.getAdSpaceId(), r.getAdSpaceId());

		assertEquals("{\"a\":\"A\",\"arf\":14,\"ari\":true,\"art\":29,\"as\":1235,"
				+ "\"av\":12,\"dob\":" + o.getDateOfBirth().getTime() +",\"rtb\":0}", json);
	}

	@Test
	public void testRead() throws JsonParseException, JsonMappingException, IOException {
		String in = "{\"a\":\"A\",\"as\":18545401,\"acm\":0E-50,\"adc\":0.003250,"
				+ "\"adm\":0.0016379999999999999893418589635984972119331359863281250,"
				+ "\"bp\":0.62117978699999987224344977221335284411907196044921875,"
				+ "\"c\":307918,\"co\":156,\"di\":{\"1\":\"5fe6a541c734b25cd792d5bb0abb54c2708c612f\"},"
				+ "\"dti\":7,\"gcn\":\"DE\",\"h\":\"89.204.153.114\",\"it\":181,\"m\":16192,\"rc\":8,\"ro\":9,\"t\":1410268019032,"
				+ "\"ua\":\"Mozilla\\/5.0 (Linux; Android 4.4.2; SM-G900F Build\\/KOT49H) AppleWebKit\\/537.36 (KHTML, like Gecko) "
				+ "Version\\/4.0 Chrome\\/30.0.0.0 Mobile Safari\\/537.36\"}";
		ObjectMapper om = new ObjectMapper();
		om.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());
		AdEvent ae = om.readValue(in, AdEvent.class);
		// campaingId, adOpsOwnerId, advertiserId, adomain
		assertEquals(307918L, ae.getCreativeId().longValue());
	}

	@Test
	public void testPerformance() throws Exception {
		int n = 5_000_000;
		
		ObjectMapper om = new ObjectMapper();
		om.setPropertyNamingStrategy(new AnnotationNamePropertyNamingStrategy());

		V1DomainModelMapper dmv1 = new V1DomainModelMapper();
		
		long b = System.currentTimeMillis();
		a = dmv1.map(o);
		for(int i = 0; i < n; i++) {
			om.writeValueAsString(a);
		}
		long c = System.currentTimeMillis() - b;
		
		System.out.println("F: " + c);
	}
	
	@Test
	public void testPerformanceProtoStuff() throws Exception {
		int n = 5_000_000;
		
		Schema<AdEvent> schema = RuntimeSchema.getSchema(AdEvent.class);
		V1DomainModelMapper dmv1 = new V1DomainModelMapper();
		
		LinkedBuffer buffer = LinkedBuffer.allocate(1000);
		
		byte[] byteArray = null;
		long b = System.currentTimeMillis();
		a = dmv1.map(o);
		for(int i = 0; i < n; i++) {
			//byteArray = JsonIOUtil.toByteArray(a, schema, false);
			buffer.clear();
			byteArray = ProtobufIOUtil.toByteArray(a, schema, buffer);
		}
		long c = System.currentTimeMillis() - b;
		
		System.out.println("PS F: " + c );
	}
	
}
