package com.adfonic.dto.report;

import java.io.Serializable;
import java.math.BigDecimal;

public class PublicationReportRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private String name;
    private String platform;
    private String approval;
    private Boolean backfill;
    private Long requests;
    private Long impressions;
    private Float fillRate;
    private BigDecimal revenue;
    private BigDecimal ecpm;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public Boolean getBackfill() {
        return backfill;
    }

    public void setBackfill(Boolean backfill) {
        this.backfill = backfill;
    }

    public Long getRequests() {
        return requests;
    }

    public void setRequests(Long requests) {
        this.requests = requests;
    }

    public Long getImpressions() {
        return impressions;
    }

    public void setImpressions(Long impressions) {
        this.impressions = impressions;
    }

    public Float getFillRate() {
        return fillRate;
    }

    public void setFillRate(Float fillRate) {
        this.fillRate = fillRate;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getEcpm() {
        return ecpm;
    }

    public void setEcpm(BigDecimal ecpm) {
        this.ecpm = ecpm;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationReportRowDTO [status=");
        builder.append(status);
        builder.append(", name=");
        builder.append(name);
        builder.append(", platform=");
        builder.append(platform);
        builder.append(", approval=");
        builder.append(approval);
        builder.append(", backfill=");
        builder.append(backfill);
        builder.append(", requests=");
        builder.append(requests);
        builder.append(", impressions=");
        builder.append(impressions);
        builder.append(", fillRate=");
        builder.append(fillRate);
        builder.append(", revenue=");
        builder.append(revenue);
        builder.append(", ecpm=");
        builder.append(ecpm);
        builder.append("]");
        return builder.toString();
    }

}
