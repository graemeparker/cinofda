package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.InvalidIpAddressException;

public class TestWhatsUpController extends BaseAdserverTest {

	//private CounterManager counterManager;
	private WhatsupController whatsupController;
	 
    @Before
	public void initTests() throws IOException, ServletException{
    	whatsupController = new WhatsupController();
    	
    	inject(whatsupController, "shardName", "shardName");
	}
	
	@Test
	public void testQuovaCheckController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        expect(new Expectations() {{
        	//oneOf(counterManager).incrementCounter(UnmappedRequestController.class, Counter.UNMAPPED_REQUEST_RECEIVED);
		}});
        
        whatsupController.handleRequest(request, response);
        String responseString = response.getContentAsString();
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertTrue(responseString.contains("shardName"));
	}
}
