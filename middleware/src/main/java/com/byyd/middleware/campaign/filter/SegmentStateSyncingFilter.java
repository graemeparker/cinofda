package com.byyd.middleware.campaign.filter;

public class SegmentStateSyncingFilter {
    
    public static final SegmentStateSyncingFilter FOR_DTO_COPY = new SegmentStateSyncingFilter().setCreateNewLocationTargets(false);
    public static final SegmentStateSyncingFilter FOR_NEW_INSTANCE = new SegmentStateSyncingFilter();

    Boolean createNewLocationTargets = true;
    
    public Boolean getCreateNewLocationTargets() {
        return createNewLocationTargets;
    }
    public SegmentStateSyncingFilter setCreateNewLocationTargets(Boolean createNewLocationTargets) {
        this.createNewLocationTargets = createNewLocationTargets;
        return this;
    }
}
