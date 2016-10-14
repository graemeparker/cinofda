package net.byyd.archive.model.v1;

import net.byyd.archive.transform.EventFeed;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamplingUnfilledEventFilterSinkTest {
    
    @Mock
    private EventFeed<AdEvent> sink;
    
    @Test
    public void testShouldProcessAllAdServed() {
        SamplingUnfilledEventFilterSink testObj = new SamplingUnfilledEventFilterSink(sink, AdAction.AD_SERVED, AdAction.UNFILLED_REQUEST);
        
        for( int j=0;j<1_000;j++) {            
            AdEvent adEvent = new AdEvent();
            adEvent.setAdAction(AdAction.AD_SERVED);
            testObj.onEvent(adEvent);
        }
        
        Mockito.verify(sink, Mockito.times(1_000)).onEvent(Mockito.any(AdEvent.class));
    }
    
    @Test
    public void testShouldProcessSmallFractionOfUnfilled() {
        SamplingUnfilledEventFilterSink testObj = new SamplingUnfilledEventFilterSink(sink, AdAction.AD_SERVED, AdAction.UNFILLED_REQUEST, AdAction.RTB_FAILED);
        
 
        for( int j=0;j<10_000;j++) {            
            AdEvent adEvent = new AdEvent();
            adEvent.setAdAction(AdAction.UNFILLED_REQUEST);
            testObj.onEvent(adEvent);
        }
        
        // probability to fail the test = 0.99 ^ 10_000 = 2.2488e-44
        Mockito.verify(sink, Mockito.atLeastOnce()).onEvent(Mockito.any(AdEvent.class));
        Mockito.verify(sink, Mockito.atMost(1_000)).onEvent(Mockito.any(AdEvent.class));
    }
    
    @Test
    public void testShouldProcessSmallFractionOfFailed() {
        SamplingUnfilledEventFilterSink testObj = new SamplingUnfilledEventFilterSink(sink, AdAction.AD_SERVED, AdAction.UNFILLED_REQUEST, AdAction.RTB_FAILED);
        
        
        for( int j=0;j<10_000;j++) {            
            AdEvent adEvent = new AdEvent();
            adEvent.setAdAction(AdAction.RTB_FAILED);
            adEvent.setDetailReason("no creative");
            testObj.onEvent(adEvent);
        }
        
        // probability to fail the test = 0.99 ^ 10_000 = 2.2488e-44
        Mockito.verify(sink, Mockito.atLeastOnce()).onEvent(Mockito.any(AdEvent.class));
        Mockito.verify(sink, Mockito.atMost(1_000)).onEvent(Mockito.any(AdEvent.class));
    }

}
