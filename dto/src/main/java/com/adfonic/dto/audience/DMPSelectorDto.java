package com.adfonic.dto.audience;

import java.math.BigDecimal;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.publisher.PublisherDto;

public class DMPSelectorDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    public enum DMPSelectorSortBy {
        NAME, DISPLAY_ORDER
    }

    @Source("muidSegmentId")
    private Long muidSegmentId;
    
    @Source("externalID")
    private String externalID;
    
    @Source("dmpVendorId")
    private Long dmpVendorId;
    
    @Source("dataWholesale")
    private BigDecimal dataWholesale;
    
    @Source("dataRetail")
    private BigDecimal dataRetail;
    
    @Source("hidden")
    private Boolean hidden;
    
    @DTOCascade
    @Source("publisher")
    private PublisherDto publisher;

    public Long getMuidSegmentId() {
        return muidSegmentId;
    }

    public void setMuidSegmentId(Long muidSegmentId) {
        this.muidSegmentId = muidSegmentId;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public Long getDmpVendorId() {
        return dmpVendorId;
    }

    public void setDmpVendorId(Long dmpVendorId) {
        this.dmpVendorId = dmpVendorId;
    }

    public BigDecimal getDataWholesale() {
        return dataWholesale;
    }

    public void setDataWholesale(BigDecimal dataWholesale) {
        this.dataWholesale = dataWholesale;
    }

    public BigDecimal getDataRetail() {
        return dataRetail;
    }

    public void setDataRetail(BigDecimal dataRetail) {
        this.dataRetail = dataRetail;
    }

	public PublisherDto getPublisher() {
		return publisher;
	}

	public void setPublisher(PublisherDto publisher) {
		this.publisher = publisher;
	}

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
	
}
