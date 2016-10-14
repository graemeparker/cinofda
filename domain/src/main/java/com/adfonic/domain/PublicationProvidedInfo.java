package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PUBLICATION_PROVIDED_INFO")
public class PublicationProvidedInfo extends BusinessKey {
    private static final long serialVersionUID = 7L;

    public enum InfoType {
        SellerNetworkId,
    }

    @Id @GeneratedValue @Column(name="ID")
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUBLICATION_ID", nullable=false)
    private Publication publication;
    
    @Column(name="PUBLICATION_PROVIDED_INFO_TYPE",length=255, nullable=false)
    @Enumerated(EnumType.STRING)
    private InfoType infoType;
    
    @Column(name="varchar_value",length=250,nullable=true)
    private String stringValue;

    @Column(name="integer_value",nullable=true)
    private Integer integerValue;
    
    @Column(name="decimal_value",nullable=true)
    private BigDecimal decimalValue;
    
    public long getId() { return id; }

    PublicationProvidedInfo(){};
    
    public PublicationProvidedInfo(Publication publication) {
        this.publication = publication;
    }
    
    public Publication getPublication() {
        return publication;
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public void setInfoType(InfoType infoType) {
        this.infoType = infoType;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }
    
}
