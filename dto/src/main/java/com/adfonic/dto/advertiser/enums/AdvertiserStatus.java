package com.adfonic.dto.advertiser.enums;

import com.adfonic.domain.Advertiser;

public enum AdvertiserStatus {

    ALL("page.dashboard.labels.table.filter.status.options.all", null), ACTIVE("page.dashboard.labels.table.filter.status.options.active", Advertiser.Status.ACTIVE), INACTIVE(
            "page.dashboard.labels.table.filter.status.options.inactive", Advertiser.Status.INACTIVE);

    private String advertiserStatusStr;
    private Advertiser.Status status;

    private AdvertiserStatus(String advertiserStatusStr, Advertiser.Status status) {
        this.advertiserStatusStr = advertiserStatusStr;
        this.status = status;
    }

    public String getAdvertiserStatusStr() {
        return advertiserStatusStr;
    }

    public Advertiser.Status getStatus() {
        return status;
    }

    public static AdvertiserStatus fromString(String text) {
        if (text != null) {
            for (AdvertiserStatus b : AdvertiserStatus.values()) {
                if (text.equalsIgnoreCase(b.advertiserStatusStr)) {
                    return b;
                }
            }
        }
        return null;
    }
}
