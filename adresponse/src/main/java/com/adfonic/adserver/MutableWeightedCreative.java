package com.adfonic.adserver;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.util.Weighted;

public class MutableWeightedCreative implements Weighted {

    private long adSpaceId;
    private CreativeDto creative;
    private double ecpmWeight;

    public MutableWeightedCreative() {
    }

    /**
     * Copy constructor used by the diagnostic tool
     */
    public MutableWeightedCreative(MutableWeightedCreative mwc) {
        this.adSpaceId = mwc.getAdSpaceId();
        this.creative = mwc.getCreative();
        this.ecpmWeight = mwc.getEcpmWeight();
    }

    /**
     * @param adspace
     *            the AdSpace
     * @param wc
     *            the Creative
     * @param campaignBoostFactor
     *            the camapign boost factor to use when calculating the weight
     */
    public MutableWeightedCreative(AdSpaceDto adspace, CreativeDto wc) {
        copyFrom(adspace, wc);
    }

    /**
     * @param adspace
     *            the AdSpace
     * @param wc
     *            the Creative
     * @param campaignBoostFactor
     *            the camapign boost factor to use when calculating the weight
     */
    public final void copyFrom(AdSpaceDto adspace, CreativeDto wc) {
        this.adSpaceId = adspace.getId();
        this.creative = wc;
    }

    public final void copyFrom(MutableWeightedCreative mwc) {
        this.adSpaceId = mwc.getAdSpaceId();
        this.creative = mwc.getCreative();
        this.ecpmWeight = mwc.getEcpmWeight();
    }

    public long getAdSpaceId() {
        return adSpaceId;
    }

    public void setAdSpaceId(long adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public void setCreative(CreativeDto creative) {
        this.creative = creative;
    }

    public double getEcpmWeight() {
        return ecpmWeight;
    }

    public void setEcpmWeight(double ecpmWeight) {
        this.ecpmWeight = ecpmWeight;
    }

    @Override
    public double getWeight() {
        return ecpmWeight;
    }

    @Override
    public String toString() {
        return "MutableWeightedCreative {adSpaceId=" + adSpaceId + ", creativeId=" + creative.getId() + ", ecpmWeight=" + ecpmWeight + ", priority=" + creative.getPriority() + "}";
    }
}
