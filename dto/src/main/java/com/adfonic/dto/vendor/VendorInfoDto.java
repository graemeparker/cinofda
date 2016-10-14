package com.adfonic.dto.vendor;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class VendorInfoDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "reviewed")
    private boolean reviewed = false;

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

}
