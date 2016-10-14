package com.adfonic.adserver.simulation.model;

import java.util.Map;
import java.util.TreeMap;

public class ResponseDataModel {

	private int code;

	private Map<String, String> headersRequired = new TreeMap<String, String>();
	private Map<String, String> headersNotAllowed = new TreeMap<String, String>();

	private ContentModel contentRequired;
	private boolean verifyEmptyContent;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Map<String, String> getHeadersRequired() {
		return headersRequired;
	}

	public void setHeadersRequired(Map<String, String> headersRequired) {
		this.headersRequired = headersRequired;
	}

	public Map<String, String> getHeadersNotAllowed() {
		return headersNotAllowed;
	}

	public void setHeadersNotAllowed(Map<String, String> headersNotAllowed) {
		this.headersNotAllowed = headersNotAllowed;
	}

	public ContentModel getContentRequired() {
		return contentRequired;
	}

	public void setContentRequired(ContentModel contentRequired) {
		this.contentRequired = contentRequired;
	}

	public boolean isVerifyEmptyContent() {
		return verifyEmptyContent;
	}

	public void setVerifyEmptyContent(boolean verifyEmptyContent) {
		this.verifyEmptyContent = verifyEmptyContent;
	}
}
