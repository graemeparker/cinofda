package net.byyd.archive.transform;

public class ManualFinishFeed<T> implements EventFeed<T> {

	private EventFeed<T> underlaying;

	public ManualFinishFeed(EventFeed<T> underlaying) {
		this.underlaying = underlaying;
	}

	@Override
	public void onEvent(T model) {
		underlaying.onEvent(model);
	}

	@Override
	public void finish() {
	}

	public void performFinish() {
		underlaying.finish();
	}
}
