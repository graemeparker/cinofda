package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class DestinationDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private DestinationType destinationType;
    private String data;
    private List<String> beaconUrls = new ArrayList<String>();
    private boolean dataIsFinalDestination;
    private String finalDestination;

    public void setDataIsFinalDestination(boolean dataIsFinalDestination) {
        this.dataIsFinalDestination = dataIsFinalDestination;
    }

    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
    }

    public String getRealDestination() {
        return dataIsFinalDestination ? data : finalDestination;
    }

    public boolean hasRealDestination() {
        return dataIsFinalDestination || finalDestination != null;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getBeaconUrls() {
        return beaconUrls;
    }

    public void setBeaconUrls(List<String> beaconUrls) {
        this.beaconUrls = beaconUrls;
    }

    @Override
    public String toString() {
        return "DestinationDto {" + getId() + ", destinationType=" + destinationType + ", data=" + data + ", beaconUrls=" + beaconUrls.toString() + ", dataIsFinalDestination="
                + dataIsFinalDestination + ", finalDestination=" + finalDestination + "}";
    }

}
