package com.byyd.middleware.creative.filter;

public class CreativeStateSyncingFilter {
    
    public static final CreativeStateSyncingFilter FOR_DTO_COPY = new CreativeStateSyncingFilter().setSyncDateUpdated(true);
    public static final CreativeStateSyncingFilter FOR_NEW_INSTANCE = new CreativeStateSyncingFilter();

    // Default values as defined by the original Creative.copyFrom()
    Boolean syncName = true;
    Boolean syncDateUpdated = false; // new Date() by default

    public Boolean getSyncName() {
        return syncName;
    }
    public CreativeStateSyncingFilter setSyncName(Boolean syncName) {
        this.syncName = syncName;
        return this;
    }
    public Boolean getSyncDateUpdated() {
        return syncDateUpdated;
    }
    public CreativeStateSyncingFilter setSyncDateUpdated(Boolean syncDateUpdated) {
        this.syncDateUpdated = syncDateUpdated;
        return this;
    }

}
