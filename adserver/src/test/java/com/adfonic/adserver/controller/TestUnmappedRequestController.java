package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.util.stats.CounterManager;

public class TestUnmappedRequestController extends BaseAdserverTest {

	private CounterManager counterManager;
	private UnmappedRequestController unmappedController;
	 
    @Before
	public void initTests() throws IOException, ServletException{
    	counterManager = mock(CounterManager.class);
    	unmappedController = new UnmappedRequestController();
    	
    	inject(unmappedController, "counterManager", counterManager);
	}
	
	@Test
	public void testQuovaCheckController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        unmappedController.handleRequest(request, response);
        String responseString = response.getContentAsString();
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertTrue(responseString.contains("Nothing here! Revise your integration."));
	}
}
