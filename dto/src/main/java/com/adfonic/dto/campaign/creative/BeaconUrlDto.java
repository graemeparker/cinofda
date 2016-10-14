package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class BeaconUrlDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
