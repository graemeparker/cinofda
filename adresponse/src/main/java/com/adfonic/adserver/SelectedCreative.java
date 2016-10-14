package com.adfonic.adserver;

import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public class SelectedCreative {
    private CreativeDto creative;
    private ProxiedDestination proxiedDestination;
    private double ecpmWeight;

    public SelectedCreative(MutableWeightedCreative mwc, ProxiedDestination proxiedDestination) {
        this.creative = mwc.getCreative();
        this.proxiedDestination = proxiedDestination;
        this.ecpmWeight = mwc.getEcpmWeight();
    }

    protected SelectedCreative() {
        //json
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public ProxiedDestination getProxiedDestination() {
        return proxiedDestination;
    }

    public double getEcpmWeight() {
        return ecpmWeight;
    }

    @Override
    public String toString() {
        return "SelectedCreative {creative=" + creative + ", proxiedDestination=" + proxiedDestination + ", ecpmWeight=" + ecpmWeight + "}";
    }

}
