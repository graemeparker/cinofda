package net.byyd.archive.transform;

public interface EventFeed<M> {
	
	void onEvent(M model);

	void finish();
}
