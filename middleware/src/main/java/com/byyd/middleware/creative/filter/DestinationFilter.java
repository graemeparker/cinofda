package com.byyd.middleware.creative.filter;

import java.util.ArrayList;
import java.util.Collection;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.DestinationType;

public class DestinationFilter {
    private Advertiser advertiser;
    private Collection<DestinationType> destinationTypes;
    private String data;

    public Advertiser getAdvertiser() {
        return advertiser;
    }
    public DestinationFilter setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        return this;
    }

    public Collection<DestinationType> getDestinationTypes() {
        return destinationTypes;
    }
    public DestinationFilter setDestinationTypes(Collection<DestinationType> destinationTypes) {
        this.destinationTypes = destinationTypes;
        return this;
    }
    public DestinationFilter addDestinationType(DestinationType destinationType) {
        if(this.destinationTypes == null) {
            this.destinationTypes = new ArrayList<DestinationType>();
        }
        this.destinationTypes.add(destinationType);
        return this;
    }

    public String getData() {
        return data;
    }
    public DestinationFilter setData(String data) {
        this.data = data;
        return this;
    }
}
