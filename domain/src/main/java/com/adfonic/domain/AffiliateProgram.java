package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.*;

/**
 * Affiliate program is used for co-branding registration and
 * linking to a company at user sign-up if referred by a registered
 * affiliate
 */
@Entity
@Table(name="AFFILIATE_PROGRAM")
public class AffiliateProgram extends BusinessKey implements Named {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;

    /**
     * Registration co-branding image
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="LOGO_CONTENT_ID",nullable=true)
    private UploadedContent logo;

    /**
     * value that will be used on urls etc for referral detection
     */
    @Column(name="AFFILIATE_ID",length=64,nullable=false)
    private String affiliateId;

    /**
     * percentage that will be credited to top ups if non-zero
     */
    @Column(name="DEPOSIT_BONUS",nullable=false)
    private BigDecimal depositBonus;

    @Column(name="DESCRIPTION",nullable=true)
    @Lob
    private String description;

    public long getId() { return id; };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UploadedContent getLogo() {
        return logo;
    }

    public void setLogo(UploadedContent logo) {
        this.logo = logo;
    }

    public String getAffiliateId() {
        return affiliateId;
    }

    public void setAffiliateId(String affiliateId) {
        this.affiliateId = affiliateId;
    }

    public BigDecimal getDepositBonus() {
        return depositBonus;
    }

    public void setDepositBonus(BigDecimal depositBonus) {
        this.depositBonus = depositBonus;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

}
