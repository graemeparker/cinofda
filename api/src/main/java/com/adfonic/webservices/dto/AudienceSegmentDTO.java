package com.adfonic.webservices.dto;

import javax.xml.bind.annotation.XmlAttribute;

public class AudienceSegmentDTO {

    private String vendor;

    private String id;


    @XmlAttribute
    public String getVendor() {
        return vendor;
    }


    public void setVendor(String vendor) {
        this.vendor = vendor;
    }


    @XmlAttribute
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

}
