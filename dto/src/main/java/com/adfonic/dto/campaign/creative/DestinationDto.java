package com.adfonic.dto.campaign.creative;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.campaign.enums.DestinationType;

public class DestinationDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "destinationType")
    private DestinationType destinationType;

    @Source(value = "data")
    private String data;

    @DTOCascade
    @Source(value = "beaconUrls")
    private List<BeaconUrlDto> beaconUrls = new ArrayList<BeaconUrlDto>();

    @Source(value = "dataIsFinalDestination")
    private boolean dataIsFinalDestination;

    @Source(value = "finalDestination")
    private String finalDestination;

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

    public List<BeaconUrlDto> getBeaconUrls() {
        return beaconUrls;
    }

    public void setBeaconUrls(List<BeaconUrlDto> beaconUrls) {
        this.beaconUrls = beaconUrls;
    }

    public boolean isDataIsFinalDestination() {
        return dataIsFinalDestination;
    }

    public void setDataIsFinalDestination(boolean dataIsFinalDestination) {
        this.dataIsFinalDestination = dataIsFinalDestination;
    }

    public String getFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
    }
}
