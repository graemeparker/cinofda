package com.adfonic.adserver.controller.commands;

import java.util.Map;

import com.adfonic.adserver.test.util.TestUtils;

public class AdRequestCommandWeb extends RequestCommandWeb implements AdRequestCommand {

	final String AD_REQUEST_URL = "/ad/";
	

	@Override
	public String executeGetAdCommand(String adSpaceExternalId,Map<String,Object> queryMap)
			throws Exception {
		createDiagnosticUrl(adSpaceExternalId, queryMap);
		String queryString = TestUtils.createQueryString(queryMap);
		String url = BASE_ADFONIC_ADSERVER_URL + AD_REQUEST_URL+adSpaceExternalId;
		url=url+"?";
		url = url + queryString;
		
		return executeAndGetString(url, "");
	}
	
	
	

}
