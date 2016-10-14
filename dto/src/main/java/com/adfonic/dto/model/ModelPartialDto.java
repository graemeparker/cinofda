package com.adfonic.dto.model;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.vendor.VendorDto;

public class ModelPartialDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "externalID")
    private String externalID;
    
    @DTOCascade
    @Source(value = "vendor")
    private VendorDto vendor;
    
    @Source(value = "deleted")
    private boolean deleted;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public VendorDto getVendor() {
        return vendor;
    }

    public void setVendor(VendorDto vendor) {
        this.vendor = vendor;
    }
}