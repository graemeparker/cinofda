package net.byyd.archive.transform;

public class PublishEventFeed<T> implements EventFeed<T> {

	private EventFeed<T>[] publish;

	@SafeVarargs
	public PublishEventFeed(EventFeed<T> ... publish) {
		this.publish = publish;
	}
	
	@Override
	public void onEvent(T model) {
		for (EventFeed<T> ev : publish) {
			ev.onEvent(model);
		}
	}

	@Override
	public void finish() {
		for (EventFeed<T> ev : publish) {
			ev.finish();
		}
	}
}
