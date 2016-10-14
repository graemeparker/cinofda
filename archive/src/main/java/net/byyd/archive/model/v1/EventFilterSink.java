package net.byyd.archive.model.v1;

import java.util.Arrays;
import java.util.HashSet;

import net.byyd.archive.transform.EventFeed;

public class EventFilterSink implements EventFeed<AdEvent> {

	protected EventFeed<AdEvent> sink;
	private HashSet<AdAction> events = new HashSet<AdAction>();

	public EventFilterSink(EventFeed<AdEvent> sink, AdAction... events) {
		this.sink = sink;
		this.events.addAll(Arrays.asList(events));
	}

	@Override
	public void onEvent(AdEvent model) {
		if (events.contains(model.getAdAction())) {
			if (model.getAdAction() != AdAction.RTB_FAILED || model.isNoCreative()) {
				sink.onEvent(model);
			}
		}
	}

	@Override
	public void finish() {
		sink.finish();
	}

}
