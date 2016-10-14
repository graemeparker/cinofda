package com.adfonic.adserver.controller.dbg;

/**
 * 
 * @author mvanek
 *
 */
public class DebugBidContext {

    public static enum CreativePurpose {
        Prefer, // Only prefer when it comes to weighting eligible AND targeted creatives
        Enforce; // Enforce eligibility, targeting and selection 
    }

    private Long creativeId;

    private CreativePurpose creativePurpose = CreativePurpose.Prefer;

    private boolean skipThrottling = true;

    public DebugBidContext(Long creativeId, CreativePurpose creativePurpose) {
        this.creativeId = creativeId;
        this.creativePurpose = creativePurpose;
    }

    public Long getCreativeId() {
        return creativeId;
    }

    public CreativePurpose getCreativePurpose() {
        return creativePurpose;
    }

    public boolean isSkipThrottling() {
        return skipThrottling;
    }

    public void setSkipThrottling(boolean skipThrottling) {
        this.skipThrottling = skipThrottling;
    }

    public void setCreativeId(Long creativeId) {
        this.creativeId = creativeId;
    }

    public void setCreativePurpose(CreativePurpose creativePurpose) {
        this.creativePurpose = creativePurpose;
    }

}
