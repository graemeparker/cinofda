package com.adfonic.dto.campaign.creative;


public class AssetInfoDto {
    private byte[] data;
    private ContentTypeDto contentType;

    public AssetInfoDto() {
    }

    public AssetInfoDto(byte[] data, ContentTypeDto contentType) {
        super();
        this.data = (data == null ? null : data.clone());
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = (data == null ? null : data.clone());
    }

    public ContentTypeDto getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeDto contentType) {
        this.contentType = contentType;
    }
}
