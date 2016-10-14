package com.byyd.adsquare.v2;

import java.util.Date;
import java.util.List;

public class AmpAudience {

    private Long audienceId;
    private String name;
    private Date lastModified;
    private String companyId;
    private Double cpm;
    private String currency;
    private List<String> customTags;

    public Long getAudienceId() {
        return audienceId;
    }

    public void setAudienceId(Long audienceId) {
        this.audienceId = audienceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Double getCpm() {
        return cpm;
    }

    public void setCpm(Double cpm) {
        this.cpm = cpm;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getCustomTags() {
        return customTags;
    }

    public void setCustomTags(List<String> customTags) {
        this.customTags = customTags;
    }

    @Override
    public String toString() {
        return "AmpAudience {audienceId=" + audienceId + ", name=" + name + ", lastModified=" + lastModified + ", companyId=" + companyId + ", cpm=" + cpm + ", currency="
                + currency + ", customTags=" + customTags + "}";
    }

}
