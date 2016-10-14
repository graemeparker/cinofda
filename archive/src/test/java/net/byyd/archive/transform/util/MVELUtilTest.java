package net.byyd.archive.transform.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MVELUtilTest {

	@Test
	public void test() {
		assertEquals("test-/bar", MVELUtil.resolve("test-/@{arg0}", "bar"));

		// assertEquals("test-/bar-null/2014-08",
		// MVELUtil.resolve("test-/@{arg0}-@{env('HOSTNAME')}/@{date('yyyy-MM')}",
		// "bar"));
	}

}
