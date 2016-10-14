package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class ContentTypeDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "MIMEType")
    private String mimeType;

    @Source(value = "animated")
    private String animated;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getAnimated() {
        return animated;
    }

    public void setAnimated(String animated) {
        this.animated = animated;
    }

}
