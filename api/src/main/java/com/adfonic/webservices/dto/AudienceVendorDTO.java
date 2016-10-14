package com.adfonic.webservices.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;

public class AudienceVendorDTO {

    private String name;

    private Set<AudienceSegmentDTO> segment;


    @XmlAttribute
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Set<AudienceSegmentDTO> getSegment() {
        return segment;
    }


    public void setSegment(Set<AudienceSegmentDTO> segment) {
        this.segment = segment;
    }

}
