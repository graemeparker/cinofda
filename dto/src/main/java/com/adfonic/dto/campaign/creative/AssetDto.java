package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class AssetDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "creative")
    private ContentTypeDto creative;

    @DTOCascade
    @Source(value = "contentType")
    private ContentTypeDto contentType;

    @Source(value = "data")
    private byte[] data;

    @Source(value = "externalID")
    private String externalID;

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

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public ContentTypeDto getCreative() {
        return creative;
    }

    public void setCreative(ContentTypeDto creative) {
        this.creative = creative;
    }

    /**
     * Returns a String containing the textual data of this asset, or null if
     * the data is binary.
     */
    public String getDataAsString() {
        if (data != null && isText()) {
            return new String(data);
        }
        return null;
    }

    /**
     * Returns a UTF-8 String containing the textual data of this asset, or null
     * if the data is binary.
     */
    public String getDataAsUtf8String() {
        if (data != null && isText()) {
            try {
                return new String(data, "utf-8");
            } catch (java.io.UnsupportedEncodingException e) {
                throw new UnsupportedOperationException("Man, if you don't know utf-8 then what DO you know?", e);
            }
        }
        return null;
    }

    private boolean isText() {
        return contentType.getMimeType().startsWith("text/");
    }
}
