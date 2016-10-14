package com.adfonic.adserver.rtb.nativ;

import java.util.List;

public class ByydMarketPlace {

    private boolean privateDeal;

    private List<ByydDeal> deals;

    protected ByydMarketPlace() {
        //marshalling
    }

    public ByydMarketPlace(List<ByydDeal> deals, boolean isPrivate) {
        this.deals = deals;
        this.privateDeal = isPrivate;
    }

    public boolean isPrivateDeal() {
        return privateDeal;
    }

    public void setPrivateDeal(boolean privateDeal) {
        this.privateDeal = privateDeal;
    }

    public ByydDeal findDealById(String dealId) {
        if (dealId != null) {
            for (ByydDeal byydDeal : deals) {
                if (dealId.equals(byydDeal.getId())) {
                    return byydDeal;
                }
            }
        }
        return null;
    }

    public List<ByydDeal> getDeals() {
        return deals;
    }

    public void setDeals(List<ByydDeal> deals) {
        this.deals = deals;
    }

    @Override
    public String toString() {
        return "ByydMarketPlace { privateDeal=" + privateDeal + ", " + (deals != null ? "deals=" + deals : "") + "}";
    }
}
