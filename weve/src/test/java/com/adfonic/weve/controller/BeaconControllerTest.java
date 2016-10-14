package com.adfonic.weve.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.weve.dto.WeveOperatorDto;
import com.adfonic.weve.service.BeaconService;
import com.adfonic.weve.service.CorrelationService;
import com.adfonic.weve.service.WeveService;

@SuppressWarnings("unchecked")
public class BeaconControllerTest extends AbstractAdfonicTest {

	BeaconController controller = new BeaconController();
	HttpServletRequest request;
	MockHttpServletResponse response;
	BeaconService beaconService;
	WeveService weveService;
	CorrelationService correlationService;
	ServletContext servletContext;
	
	@Before
	public void setUp() {
		beaconService = mock(BeaconService.class, "beaconService");
		weveService = mock(WeveService.class, "weveService");
		correlationService = mock(CorrelationService.class, "correlationService");
		inject(controller, "beaconService", beaconService);
		inject(controller, "weveService", weveService);
		inject(controller, "correlationService", correlationService);
		request = mock(HttpServletRequest.class, "request");
		//request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		servletContext = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
		servletContext.getResourceAsStream("WEB-INF/images/clear.gif");
		controller.setServletContext(servletContext);
	}
	
	@Test
	public void shouldReturnOKWhenOperatorNotFound() throws IOException {
		expect(new Expectations() {{
			oneOf(request).getRemoteAddr(); will(returnValue("127.0.0.1"));
			oneOf(beaconService).retrieveOperatorInfoByIpAddressLookup("127.0.0.1"); will(returnValue(new WeveOperatorDto(-1, "Error, operator not found")));
		}});
		
		controller.handleRequest(request, response, "dummyAdSpaceId", "dummyClickId", "dummyPublicationId");
		assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
		assertThat(response.getContentType(), equalTo("image/gif"));
		assertThat(response.getContentAsByteArray().length, equalTo(43));
	}
	
	@Test
	public void shouldReturnOKWhenOperatorFoundWeveIdNotFound() throws IOException {
		inject(controller, "logUserIdEnabled", false);
				
		expect(new Expectations() {{
			oneOf(request).getRemoteAddr(); will(returnValue("200.200.200.157"));
			oneOf(beaconService).retrieveOperatorInfoByIpAddressLookup("200.200.200.157"); will(returnValue(new WeveOperatorDto(2, 3368601757L, 3368601757L, "x-test-header", 0L, true, false)));
			oneOf(request).getHeader("x-test-header"); will(returnValue("aTestEndUserId"));
			oneOf(weveService).logHeaders(request);
			oneOf(beaconService).checkWeveIdExists(2, "aTestEndUserId"); will(returnValue(0L));
			oneOf(weveService).getDeviceIds(request); will(returnValue(Collections.<String>emptyList()));
		}});
		
		controller.handleRequest(request, response, "anythingGoesHere", "noCheckingRequired", null);
		assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
		assertThat(response.getContentType(), equalTo("image/gif"));
		assertThat(response.getContentAsByteArray().length, equalTo(43));
	}

    @Test
	public void shouldRecordDeviceIdsAndReturnOKWhenWeveIdFound() throws Exception {
		final List<String> deviceIdList = Arrays.asList("1234~1", "5678~7");
		expect(new Expectations() {{
			oneOf(request).getRemoteAddr(); will(returnValue("200.200.200.157"));
			oneOf(beaconService).retrieveOperatorInfoByIpAddressLookup("200.200.200.157"); will(returnValue(new WeveOperatorDto(1, 3368601757L, 3368601757L, "x-weve-test", 0L, true, false)));
			oneOf(request).getHeader("x-weve-test"); will(returnValue("testing123"));
			oneOf(weveService).logHeaders(request);
			oneOf(beaconService).checkWeveIdExists(1, "testing123"); will(returnValue(1L));
			oneOf(weveService).getDeviceIds(request); will(returnValue(deviceIdList));
			oneOf(correlationService).correlateDeviceIdsWithEndUser(with(any(Long.class)), with(any(List.class)), with(any(String.class)), with(any(String.class)));
		}});
		
		controller.handleWeirdNewRequest(request, response, "something", "somethingElse", "anotherThing");
		assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
		assertThat(response.getContentType(), equalTo("image/gif"));
		assertThat(response.getContentAsByteArray().length, equalTo(43));		
	}
	
	@Test
	public void shouldRecordAdspaceAndCreativeIdsAgainstUnknownUser() throws Exception {
		inject(controller, "logUserIdEnabled", true);
		final List<String> deviceIdList = Arrays.asList("1111~1", "2222~7");
		expect(new Expectations() {{
			oneOf(request).getRemoteAddr(); will(returnValue("200.200.200.157"));
			oneOf(beaconService).retrieveOperatorInfoByIpAddressLookup("200.200.200.157"); will(returnValue(new WeveOperatorDto(9, 3368601757L, 3368601757L, "x-weve-test", 0L, true, false)));
			oneOf(request).getHeader("x-weve-test"); will(returnValue("someUnknownUserId"));
			oneOf(weveService).logHeaders(request);
			oneOf(beaconService).checkWeveIdExists(9, "someUnknownUserId"); will(returnValue(0L));
			oneOf(weveService).getDeviceIds(request); will(returnValue(deviceIdList));
			oneOf(correlationService).recordDeviceIdsForUnknownUser(with(any(String.class)), with(any(Integer.class)), with(any(List.class)), with(any(String.class)), with(any(String.class)));
		}});
		
		controller.handleWeirdNewRequest(request, response, "testAdSpaceId1", "somethingElse", "testCreativeId1");
		assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
		assertThat(response.getContentType(), equalTo("image/gif"));
		assertThat(response.getContentAsByteArray().length, equalTo(43));				
	}
}
