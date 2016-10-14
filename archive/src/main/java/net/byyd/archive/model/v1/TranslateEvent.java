package net.byyd.archive.model.v1;

import net.byyd.archive.transform.EventFeed;

public class TranslateEvent implements EventFeed<AdEvent> {
	
	private EventFeed<AdEvent> sink;
	private AdAction from;
	private AdAction to;

	public TranslateEvent(EventFeed<AdEvent> sink, AdAction from, AdAction to) {
		this.sink = sink;
		this.from = from;
		this.to = to;
	}

	@Override
	public void onEvent(AdEvent model) {
		sink.onEvent(model);
		
		if (model.getAdAction() == from) {
			AdEvent copy = (AdEvent) model.copy();
			if (copy != null) {
				copy.setAdAction(to);
				sink.onEvent(copy);
			}
		}
	}

	@Override
	public void finish() {
	}
}
