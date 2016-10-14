package com.adfonic.adserver.rtb.nativ;

import org.junit.Assert;
import org.junit.Test;

public class BidRequestTest {

    ByydRequest testObj = new ByydRequest();
    
    @Test
    public void testSetSellerNetworkId() {
        
        Integer sellerNetworkId = testObj.getSellerNetworkId();
        Assert.assertNull(sellerNetworkId);
        
        testObj.setSellerNetworkId(123);
        Assert.assertEquals(Integer.valueOf(123), testObj.getSellerNetworkId());
    }

}
