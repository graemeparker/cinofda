package com.adfonic.adserver.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.jms.AdserverJms;
import com.adfonic.jms.ClickMessage;

public class SimulationAdserverJmsImpl implements AdserverJms {

	private List<AdEvent> events = new ArrayList<>();
	private List<ClickMessage> clicks = new ArrayList<>();

	@Override
	public void logAdEvent(AdEvent adEvent) {
		events.add(adEvent);
	}

	@Override
	public void logAdEventBatch(AdEvent[] adEvents) {
		for (AdEvent a : adEvents) {
			events.add(a);
		}
	}

	@Override
	public void logClickMessage(ClickMessage clickMessage) {
		clicks.add(clickMessage);
	}

	public List<AdEvent> getEvents() {
		return events;
	}

	public void setEvents(List<AdEvent> events) {
		this.events = events;
	}

	public List<ClickMessage> getClicks() {
		return clicks;
	}

	public void setClicks(List<ClickMessage> clicks) {
		this.clicks = clicks;
	}
}
