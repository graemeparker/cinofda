package net.byyd.archive.model.v1;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

public class ArchiveV1JsonWriterTest {

	ArchiveV1JsonWriter writer;

	@Before
	public void before() {
		writer = new ArchiveV1JsonWriter();
	}

	@Test
	public void testWriteAdEventStringBuilder() {
		StringBuilder sb = new StringBuilder();
		AdEvent a = new AdEvent();

		writer.write(a, sb);
		assertEquals("{}", sb.toString());
		sb.setLength(0);

		a.setAdAction(AdAction.AD_SERVED);
		writer.write(a, sb);
		assertEquals("{\"a\":\"A\"}", sb.toString());
		sb.setLength(0);

		a.setFormatId(12L);
		writer.write(a, sb);
		assertEquals("{\"a\":\"A\",\"fmi\":12}", sb.toString());
		sb.setLength(0);

		Map<Long, String> m = new TreeMap<Long, String>();
		a.setDeviceIdentifiers(m);
		m.put(4L, "abcdef-0047");
		m.put(1L, "fedcba-8102");

		writer.write(a, sb);
		assertEquals("{\"a\":\"A\",\"di\":{\"1\":\"fedcba-8102\","
				+ "\"4\":\"abcdef-0047\"},\"fmi\":12}",
				sb.toString());
		sb.setLength(0);

		a.setDeviceIdentifiers(new TreeMap<Long, String>());
		a.setDetailReason("\"foo\\bar/{}");
		writer.write(a, sb);
		assertEquals(
				"{\"a\":\"A\",\"fmi\":12,\"mdr\":\"\\\"foo\\\\bar\\/{}\"}",
				sb.toString());
		sb.setLength(0);

		int n = 5_000_000;
		long b = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			writer.write(a, sb);
			sb.setLength(0);
		}
		long af = System.currentTimeMillis();

		System.out.println("Perf: " + (af - b));
	}
}
