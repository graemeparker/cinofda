package com.adfonic.dto.optimisation;

import java.util.UUID;

public class OptimisationUserInterfaceLivePublicationDto {

    private String publicationName;
    private Long publicationId;
    private String publicationBundle;
    private String publicationType;
    private String creativeName;
    private Long creativeId;
    private String publicationExternalId;
    private String iabCategory;
    private String inventorySource;
    private Integer bids;
    private Integer impressions;
    private Double winRate;
    private Integer clicks;
    private Double ctr;
    private Integer conversions;
    private Double cvr;
    private Double spend;
    private Double ecpm;
    private Double ecpc;
    private Double ecpa;
    private Boolean partiallyRemoved = false;

    private String id;

    public OptimisationUserInterfaceLivePublicationDto() {
        super();
        id = UUID.randomUUID().toString();
    }

    public String getIabCategory() {
        return iabCategory;
    }

    public void setIabCategory(String iabCategory) {
        this.iabCategory = iabCategory;
    }

    public String getInventorySource() {
        return inventorySource;
    }

    public void setInventorySource(String inventorySource) {
        this.inventorySource = inventorySource;
    }

    public Integer getBids() {
        return bids;
    }

    public void setBids(Integer bids) {
        this.bids = bids;
    }

    public Integer getImpressions() {
        return impressions;
    }

    public void setImpressions(Integer impressions) {
        this.impressions = impressions;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public Double getCtr() {
        return ctr;
    }

    public void setCtr(Double ctr) {
        this.ctr = ctr;
    }

    public Integer getConversions() {
        return conversions;
    }

    public void setConversions(Integer conversions) {
        this.conversions = conversions;
    }

    public Double getCvr() {
        return cvr;
    }

    public void setCvr(Double cvr) {
        this.cvr = cvr;
    }

    public Double getEcpm() {
        return ecpm;
    }

    public void setEcpm(Double ecpm) {
        this.ecpm = ecpm;
    }

    public Double getEcpc() {
        return ecpc;
    }

    public void setEcpc(Double ecpc) {
        this.ecpc = ecpc;
    }

    public Double getEcpa() {
        return ecpa;
    }

    public void setEcpa(Double ecpa) {
        this.ecpa = ecpa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getSpend() {
        return spend;
    }

    public void setSpend(Double spend) {
        this.spend = spend;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public String getPublicationBundle() {
        return publicationBundle;
    }

    public void setPublicationBundle(String publicationBundle) {
        this.publicationBundle = publicationBundle;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getCreativeName() {
        return creativeName;
    }

    public void setCreativeName(String creativeName) {
        this.creativeName = creativeName;
    }

    public Long getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(Long creativeId) {
        this.creativeId = creativeId;
    }

    public String getPublicationExternalId() {
        return publicationExternalId;
    }

    public void setPublicationExternalId(String publicationExternalId) {
        this.publicationExternalId = publicationExternalId;
    }

    public Boolean getPartiallyRemoved() {
        return partiallyRemoved;
    }

    public void setPartiallyRemoved(Boolean partiallyRemoved) {
        this.partiallyRemoved = partiallyRemoved;
    }

}
