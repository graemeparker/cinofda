package com.adfonic.adserver.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.http.client.ResponseHandler;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.quova.QuovaClient;
import com.adfonic.util.LoadBalancingHttpClient;
import com.quova.data._1.CountryDataType;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.LocationType;

@SuppressWarnings("unchecked")
public class TestQuovaCheckController extends BaseAdserverTest {

	private QuovaCheckController quovaCheckController;
    private QuovaClient quovaClient;
    private LoadBalancingHttpClient loadBalancingHttpClient;

    @Before
	public void initTests() throws IOException, ServletException{
		loadBalancingHttpClient = mock(LoadBalancingHttpClient.class);
		quovaClient = new QuovaClient(loadBalancingHttpClient,"something");
		quovaCheckController = new QuovaCheckController(quovaClient);
	}
	
	@Test
	public void testQuovaCheckController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(returnValue(null));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("ERROR: No response from Quova"));
	}
	
	@Test
	public void testQuovaCheckController02_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final Ipinfo ipInfo = mock(Ipinfo.class);
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(returnValue(ipInfo));
	    	oneOf (ipInfo).getLocation(); will(returnValue(null));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("ERROR: no location"));
	}
	
	@Test
	public void testQuovaCheckController03_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final Ipinfo ipInfo = mock(Ipinfo.class);
		final LocationType locationType = mock(LocationType.class);
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(returnValue(ipInfo));
	    	allowing (ipInfo).getLocation(); will(returnValue(locationType));
	    	oneOf (locationType).getCountryData(); will(returnValue(null));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("ERROR: no country data"));
	}
	
	@Test
	public void testQuovaCheckController04_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final Ipinfo ipInfo = mock(Ipinfo.class);
		final LocationType locationType = mock(LocationType.class);
		final CountryDataType countryDataType = mock(CountryDataType.class);
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(returnValue(ipInfo));
	    	allowing (ipInfo).getLocation(); will(returnValue(locationType));
	    	allowing (locationType).getCountryData(); will(returnValue(countryDataType));
	    	oneOf (countryDataType).getCountryCode(); will(returnValue("someth"));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("ERROR: expected country=us"));
	}
	
	@Test
	public void testQuovaCheckController05_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final Ipinfo ipInfo = mock(Ipinfo.class);
		final LocationType locationType = mock(LocationType.class);
		final CountryDataType countryDataType = mock(CountryDataType.class);
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(returnValue(ipInfo));
	    	allowing (ipInfo).getLocation(); will(returnValue(locationType));
	    	allowing (locationType).getCountryData(); will(returnValue(countryDataType));
	    	oneOf (countryDataType).getCountryCode(); will(returnValue("us"));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("Quova OK"));
	}
	
	@Test
	public void testQuovaCheckController06_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String ip = "98.67.157.168";
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        expect(new Expectations() {{
        	oneOf (loadBalancingHttpClient).execute(with(any(String.class)), with(any(String.class)), with.is(any(ResponseHandler.class))); will(throwException(new Exception()));
		}});
        
        quovaCheckController.handleRequest(request, response, ip);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("ERROR"));
	}
}
