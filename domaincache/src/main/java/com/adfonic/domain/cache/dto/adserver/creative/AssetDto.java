package com.adfonic.domain.cache.dto.adserver.creative;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class AssetDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private String externalID;
    private byte[] data;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AssetDto {" + getId() + ", externalID=" + externalID + ", data=" + data.length + " b}";
    }

}
