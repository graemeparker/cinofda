package com.adfonic.dto.dashboard.statistic;

import java.io.Serializable;

public class AdvertiserHeadlineStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected long impressions;
    protected double ctr;
    protected long clicks;
    protected long conversions;
    protected double costPerConversion;
    protected double spend;

    public AdvertiserHeadlineStatsDto() {
        super();
    }

    /**
     * @return the impressions
     */
    public long getImpressions() {
        return impressions;
    }

    /**
     * @param impressions the impressions to set
     */
    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }

    /**
     * @return the ctr
     */
    public double getCtr() {
        return ctr;
    }

    /**
     * @param ctr the ctr to set
     */
    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    /**
     * @return the clicks
     */
    public long getClicks() {
        return clicks;
    }

    /**
     * @param clicks the clicks to set
     */
    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    /**
     * @return the conversions
     */
    public long getConversions() {
        return conversions;
    }

    /**
     * @param conversions the conversions to set
     */
    public void setConversions(long conversions) {
        this.conversions = conversions;
    }

    /**
     * @return the costPerConversion
     */
    public double getCostPerConversion() {
        return costPerConversion;
    }

    /**
     * @param costPerConversion the costPerConversion to set
     */
    public void setCostPerConversion(double costPerConversion) {
        this.costPerConversion = costPerConversion;
    }

    /**
     * @return the spend
     */
    public double getSpend() {
        return spend;
    }

    /**
     * @param spend the spend to set
     */
    public void setSpend(double spend) {
        this.spend = spend;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AdvertiserHeadlineStatsDto [impressions=");
        toStringFields(builder);
        builder.append("]");
        return builder.toString();
    }

    protected void toStringFields(StringBuilder builder) {
        builder.append(impressions);
        builder.append(", ctr=");
        builder.append(ctr);
        builder.append(", clicks=");
        builder.append(clicks);
        builder.append(", conversions=");
        builder.append(conversions);
        builder.append(", costPerConversion=");
        builder.append(costPerConversion);
        builder.append(", spend=");
        builder.append(spend);
    }
}