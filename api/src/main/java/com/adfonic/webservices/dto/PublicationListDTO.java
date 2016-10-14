package com.adfonic.webservices.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="publications")
public class PublicationListDTO {

    private List<String> publications;

    @XmlElement(name="publication")
    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

}
