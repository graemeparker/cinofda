package com.adfonic.webservices.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "audience")
public class AudienceDTO {

    private String id;

    private String name;

    private Set<AudienceVendorDTO> vendors;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @XmlElementWrapper(name = "segments")
    @XmlElement(name = "vendor")
    public Set<AudienceVendorDTO> getVendors() {
        return vendors;
    }


    public void setVendors(Set<AudienceVendorDTO> vendors) {
        this.vendors = vendors;
    }

}
