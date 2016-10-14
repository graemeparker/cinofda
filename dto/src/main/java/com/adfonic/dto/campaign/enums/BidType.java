package com.adfonic.dto.campaign.enums;

public enum BidType {

    ALL("All", null), CPA("CPA", "CPA"), CPM("CPM", "CPM"), CPC("CPC", "CPC"), CPI("CPI", "CPI");

    private String bidType;
    private String id;

    private BidType(String bidType, String id) {
        this.bidType = bidType;
        this.id = id;
    }

    public String getBidType() {
        return bidType;
    }

    public String getId() {
        return id;
    }

    public static BidType value(String id) {
        if (id == null) {
            return BidType.ALL;
        } else if (BidType.CPC.getId().equals(id)) {
            return BidType.CPC;
        } else if (BidType.CPM.getId().equals(id)) {
            return BidType.CPM;
        } else if (BidType.CPA.getId().equals(id)) {
            return BidType.CPA;
        } else {
            return BidType.CPI;
        }
    }
}
