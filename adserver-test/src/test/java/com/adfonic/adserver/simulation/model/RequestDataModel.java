package com.adfonic.adserver.simulation.model;

import java.util.Map;
import java.util.TreeMap;

public class RequestDataModel {

	private Map<String, String> headers = new TreeMap<String, String>();
	private String uri;
	private String method;
	private ContentModel content;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public ContentModel getContent() {
		return content;
	}

	public void setContent(ContentModel content) {
		this.content = content;
	}
}
