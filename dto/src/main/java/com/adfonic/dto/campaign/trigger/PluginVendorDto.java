package com.adfonic.dto.campaign.trigger;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class PluginVendorDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "apiUser")
    private String apiUser;

    @Source(value = "apiPassword")
    private String apiPassword;

    public String getApiUser() {
        return apiUser;
    }

    public void setApiUser(String apiUser) {
        this.apiUser = apiUser;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }
}
