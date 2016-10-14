package net.byyd.archive.model.v1;

import java.util.concurrent.ThreadLocalRandom;

import net.byyd.archive.transform.EventFeed;

public class SamplingUnfilledEventFilterSink extends EventFilterSink {

    public SamplingUnfilledEventFilterSink(EventFeed<AdEvent> sink, AdAction... events) {
        super(sink, events);
    }

    @Override
    public void onEvent(AdEvent model) {

        // sampling randomly only 1% of RTB_FAILED and/or UNFILLED_REQUEST(s)
        if (AdAction.UNFILLED_REQUEST == model.getAdAction() //
               || AdAction.RTB_FAILED == model.getAdAction()) {
            
            int v = ThreadLocalRandom.current().nextInt(100);
            if (v != 0) {
                return;
            }
        }

        super.onEvent(model);
    }

}
