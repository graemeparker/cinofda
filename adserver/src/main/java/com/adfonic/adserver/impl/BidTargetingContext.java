package com.adfonic.adserver.impl;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

/**
 * @author mvanek
 * 
 * TargetingContext is used in 3 different situations
 * 1. RTB Bidding & Ad markup on rendering (Bid request populated)
 * 2. Managed traffic / Non RTB (HTTP request populated) 
 * 3. Win Notification Ad markup on rendering (BidDetails populated)
 * 4. External audit creative Ad markup rendering
 * 
 * 2. 3. 4. have no access to ByydRequest and ByydResponse
 * 
 * 
 */
public interface BidTargetingContext extends TargetingContext {

    public void setByydRequest(ByydRequest byydRequest);

    public ByydRequest getByydRequest();

    public void setByydImp(ByydImp byydImp);

    public ByydImp getByydImp();

    void setByydBid(ByydBid byydBid);

    public ByydBid getByydBid();

    public void setByydRespose(ByydResponse byydResponse);

    public ByydResponse getByydResponse();
}
