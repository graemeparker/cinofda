package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="DMP_SELECTOR")
public class DMPSelector extends BusinessKey implements Named, HasExternalID {

    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="DMP_VENDOR_ID",nullable=false)
    private Long dmpVendorId;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DMP_ATTRIBUTE_ID",nullable=false)
    private DMPAttribute dmpAttribute;
    @Column(name="HIDDEN",nullable=false)
    private Boolean hidden;
    @Column(name="MUID_SEGMENT_ID")
    private Long muidSegmentId;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="DISPLAY_ORDER",nullable=false)
    private Integer displayOrder;
    @Embedded
    private AudiencePrices audiencePrices;
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PUBLISHER_ID", nullable = true)
	private Publisher publisher;
    
	public Long getDmpVendorId() {
        return dmpVendorId;
    }
    public void setDmpVendorId(Long dmpVendorId) {
        this.dmpVendorId = dmpVendorId;
    }
    
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DMPAttribute getDmpAttribute() {
		return dmpAttribute;
	}
	public void setDmpAttribute(DMPAttribute dmpAttribute) {
		this.dmpAttribute = dmpAttribute;
	}
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public String getExternalID() {
		return externalID;
	}
	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}
	public Integer getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}
	public long getId() {
		return id;
	}
	public Long getMuidSegmentId() {
		return muidSegmentId;
	}
	public void setMuidSegmentId(Long muidSegmentId) {
		this.muidSegmentId = muidSegmentId;
	}
	public BigDecimal getDataRetail() {
		if (audiencePrices==null){
			return null;
		}
		return audiencePrices.getDataRetail();
	}
	public void setDataRetail(BigDecimal dataRetail) {
		if (audiencePrices==null){
			audiencePrices = new AudiencePrices();
		}
		this.audiencePrices.setDataRetail(dataRetail);
	}
	public BigDecimal getDataWholesale() {
		if (audiencePrices==null){
			return null;
		}
		return audiencePrices.getDataWholesale();
	}
	public void setDataWholesale(BigDecimal dataWholesale) {
		if (audiencePrices==null){
			audiencePrices = new AudiencePrices();
		}
		this.audiencePrices.setDataWholesale(dataWholesale);
	}
	public AudiencePrices getAudiencePrices() {
		return audiencePrices;
	}
	public void setAudiencePrices(AudiencePrices audiencePrices) {
		this.audiencePrices = audiencePrices;
	}
	public Publisher getPublisher() {
		return publisher;
	}
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	// Leaving this methods to guarantee backward compatibility
	/**
	 * @deprecated Use {@link getDataRetail()} instead
	 */
	@Deprecated
	
	public BigDecimal getPrice() {
		return getDataRetail();
	}
	/**
	 * @deprecated Use {@link setDataRetail(Bigdecimal dataRetail)} instead
	 */
	@Deprecated
	public void setPrice(BigDecimal price) {
		setDataRetail(price);
	}
}
