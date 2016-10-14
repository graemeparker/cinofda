package com.adfonic.adserver.controller.rtb;

import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.rtb.RtbBidEventListener;

/**
 * 
 * @author mvanek
 *
 */
public interface RtbHttpExecutor {

    public RtbExecutionContext execute(RtbHttpContext http);

    public RtbExecutionContext execute(RtbHttpContext http, RtbBidEventListener bidEventListener, TargetingEventListener bidTargetListener);

}
