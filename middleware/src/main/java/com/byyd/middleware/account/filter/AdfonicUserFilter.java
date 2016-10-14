package com.byyd.middleware.account.filter;

import java.util.Collection;

public class AdfonicUserFilter {

    private Collection<Long> adfonicUserIds;

    public Collection<Long> getAdfonicUserIds() {
        return adfonicUserIds;
    }

    public AdfonicUserFilter setAdfonicUserIds(Collection<Long> adfonicUserIds) {
        this.adfonicUserIds = adfonicUserIds;
        return this;
    }

}
