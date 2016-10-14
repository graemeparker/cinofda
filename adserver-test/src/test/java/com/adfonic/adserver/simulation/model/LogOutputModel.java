package com.adfonic.adserver.simulation.model;

import java.util.List;

public class LogOutputModel {
	private String event;
	private List<String> requiredAttributes;
	private List<String> deniedAttributes;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public List<String> getRequiredAttributes() {
		return requiredAttributes;
	}

	public void setRequiredAttributes(List<String> requiredAttributes) {
		this.requiredAttributes = requiredAttributes;
	}

	public List<String> getDeniedAttributes() {
		return deniedAttributes;
	}

	public void setDeniedAttributes(List<String> deniedAttributes) {
		this.deniedAttributes = deniedAttributes;
	}
}
