package com.adfonic.adserver.controller.commands;

import java.util.Map;

import com.adfonic.adserver.test.util.TestUtils;

public class ClickThroughRequestCommandWeb extends RequestCommandWeb implements ClickThroughRequestCommand{

	public static final String CLICK_THROUGH_URL="/ct";
	@Override
	public String executeClickThroughCommand(String adSpaceExternalId,
			String impressionExternalId, Map<String, Object> queryMap)
			throws Exception {
		createDiagnosticUrl(adSpaceExternalId, queryMap);
		String queryString = TestUtils.createQueryString(queryMap);
		String url = BASE_ADFONIC_ADSERVER_URL + CLICK_THROUGH_URL+"/"+adSpaceExternalId+"/"+impressionExternalId;
		url=url+"?";
		url = url + queryString;
		String redirectedUrl = executeAndGetString(url, "");
		if(redirectedUrl != null){
			redirectedUrl = redirectedUrl.replaceAll(BASE_ADFONIC_ADSERVER_URL + CLICK_THROUGH_URL, "");
		}
		return redirectedUrl;
	}

}
