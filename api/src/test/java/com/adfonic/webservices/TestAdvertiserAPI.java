package com.adfonic.webservices;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;

public class TestAdvertiserAPI {
	
	protected final static String XML_FORMAT = "xml";
	protected final static String JSON_FORMAT = "json";
	
	protected String hostName = "localhost";
	protected String hostPort = "8080";
	protected String prefix = "/adfonic-webservices";
	protected String userEmail = "tatyanaisoft@gmail.com";
	protected String userDeveloperKey = "thisismykey";
	protected String wrongUserDeveloperKey = "__thisismykey__";
	protected String authorizedAdvertiserId = "a06476c6-03f3-4c6e-b010-30ec432734da";
	protected String unauthorizedAdvertiserId = "c39f62f4-9a82-482b-bfdc-70b5ddc8f3b8";
	protected String authorizedCampaignId = "2420dd46-b743-49df-9029-8e67d5ce7756";
	protected String unauthorizedCampaignId = "d565beca-82fb-4873-8081-699e8c08778d";
	
	public TestAdvertiserAPI() throws Exception {
		super();
	}

	protected String buildUrl(String serviceUrl, String format) throws Exception {
		return Util.buildUrl(serviceUrl, format, null, null, hostName, hostPort, prefix);
	}
	
	protected void testAdvertisersForCompany(String username, String password, String format) throws Exception {
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String url = buildUrl("/advertisers/list", format);
		String response = Util.getWebServiceResponse(url, username, password);
		Util.logDecodedJsonWebServiceResponse(response);
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	protected void testAdvertiser(String username, String password, String advertiserId, String format) throws Exception {
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String url = buildUrl("/advertiser/" + advertiserId, format);
		String response = Util.getWebServiceResponse(url, username, password);
		Util.logDecodedJsonWebServiceResponse(response);
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	protected void testCampaignsForAdvertiser(String username, String password, String advertiserId, String format) throws Exception {
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String url = buildUrl("/advertiser/" + advertiserId + "/campaigns/list", format);
		String response = Util.getWebServiceResponse(url, username, password);
		Util.logDecodedJsonWebServiceResponse(response);
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	protected void testCampaign(String username, String password, String campaignId, String format) throws Exception {
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		String url = buildUrl("/campaign/" + campaignId, format);
		String response = Util.getWebServiceResponse(url, username, password);
		Util.logDecodedJsonWebServiceResponse(response);
		log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	protected static void log(String s) {
		System.out.println(s);
	}
	

	@Ignore
	@Test
	public void run() throws Exception {
		log("Testing Advertiser for company with proper developper key");
		testAdvertisersForCompany(userEmail, userDeveloperKey, JSON_FORMAT);
		log("Testing Advertiser for company with wrong developper key");
		testAdvertisersForCompany(userEmail, wrongUserDeveloperKey, JSON_FORMAT);
		log("Testing Authorized Advertiser");
		testAdvertiser(userEmail, userDeveloperKey, authorizedAdvertiserId, JSON_FORMAT);
		log("Testing Unauthorized Advertiser");
		testAdvertiser(userEmail, userDeveloperKey, unauthorizedAdvertiserId, JSON_FORMAT);
		log("Testing campaign for authorized advertiser");
		testCampaignsForAdvertiser(userEmail, userDeveloperKey, authorizedAdvertiserId, JSON_FORMAT);
		log("Testing campaign for unauthorized advertiser");
		testCampaignsForAdvertiser(userEmail, userDeveloperKey, unauthorizedAdvertiserId, JSON_FORMAT);
		log("Testing authorized Campaign");
		testCampaign(userEmail, userDeveloperKey, authorizedCampaignId, JSON_FORMAT);
		log("Testing unauthorized Campaign");
		testCampaign(userEmail, userDeveloperKey, unauthorizedCampaignId, JSON_FORMAT);
	}
	
	public void shutdown() {
	}
	
	public static void main(String[] args) {
		TestAdvertiserAPI instance = null;
		try {
			instance = new TestAdvertiserAPI();
			instance.run();
		} catch(Exception e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
		} finally {
			instance.shutdown();
		}
	}

}
