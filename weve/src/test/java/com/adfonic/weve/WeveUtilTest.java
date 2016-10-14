package com.adfonic.weve;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class WeveUtilTest {

	@Test
	public void shouldFormatDeviceIdsIntoReadableString() {
		List<String> deviceStrings = Arrays.asList("deviceId3r2r234~type4", "deviceId43uh4~type2", "deviceIdjoubo9u43~type1");
		String printableDeviceIds = WeveUtil.printableDeviceIds(deviceStrings);
		assertThat(printableDeviceIds, equalTo("deviceId3r2r234, deviceId43uh4, deviceIdjoubo9u43"));
	}
	
	@Test
	public void shouldFlattenListOfDeviceIdsIntoDbReadyString() throws Exception {
		List<String> deviceStrings = Arrays.asList("123abc~type4", "5a5a5a~type2");
		assertThat(WeveUtil.normalizeDeviceIdList(deviceStrings), equalTo("123abc~type4|5a5a5a~type2"));
	}
	
	@Test
	public void shouldHandleEmptyListOfDeviceIds() {
		assertThat(WeveUtil.normalizeDeviceIdList(Collections.<String> emptyList()), equalTo(""));
	}
	
}
