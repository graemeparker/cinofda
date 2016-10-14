package com.adfonic.webservices.dto;

import com.adfonic.domain.DestinationType;

public class DestinationDTO {

    private DestinationType type;// destinationType

    private String data;


    public DestinationType getType() {
        return type;
    }


    public void setType(DestinationType type) {
        this.type = type;
    }


    public String getData() {
        return data;
    }


    public void setData(String data) {
        this.data = data;
    }
}
