package com.adfonic.webservices.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asset")
public class AssetDTO {

    private String id;// externalID

    private String creativeId;// creative.externalID

    private String contentType;// contentType.mimeType

    private byte[] data;

    // private String dimensions;
    // - Suggested DISPLAY_TYPE.systemname is not good enough to map to the placeholder. using contentSpec below

    private String contentSpec;// indirect mapping based on creative.format


    public String getContentSpec() {
        return contentSpec;
    }


    public void setContentSpec(String contentSpec) {
        this.contentSpec = contentSpec;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getCreativeId() {
        return creativeId;
    }


    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public byte[] getData() {
        return data;
    }


    public void setData(byte[] data) {
        this.data = data;
    }

}
