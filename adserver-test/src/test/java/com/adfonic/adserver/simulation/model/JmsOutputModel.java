package com.adfonic.adserver.simulation.model;

public class JmsOutputModel {
	
	private String queueName;
	private String event;

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
}
