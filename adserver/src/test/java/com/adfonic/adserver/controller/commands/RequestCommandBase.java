package com.adfonic.adserver.controller.commands;

import java.util.Map;

import com.adfonic.adserver.test.util.TestUtils;

public class RequestCommandBase {
	protected final String SERVER = "http://localhost:8080";
	protected final String BASE_ADFONIC_ADSERVER_URL = SERVER+"/adfonic-adserver";
	protected final String DIAGNOSTIC_REQUEST_URL = BASE_ADFONIC_ADSERVER_URL+"/internal/doDiagnostic.jsp";

	
	protected String createDiagnosticUrl(String adSpaceExternalId,Map<String,Object> queryMap){
		String queryString = TestUtils.createDiagnosticUrlParams(adSpaceExternalId, queryMap);
		String url = DIAGNOSTIC_REQUEST_URL;
		url=url+"?";
		url = url + queryString;
		System.out.println("Diagnostic URL : "+ url);
		return url;

	}
}
