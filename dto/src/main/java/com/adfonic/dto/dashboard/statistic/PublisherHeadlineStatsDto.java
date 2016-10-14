package com.adfonic.dto.dashboard.statistic;

import java.io.Serializable;

public class PublisherHeadlineStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected long requests;
    protected long impressions;
    protected double fillRate;
    protected double revenue;
    protected double ecpm;

    public PublisherHeadlineStatsDto() {
        super();
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getImpressions() {
        return impressions;
    }

    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }

    public double getFillRate() {
        return fillRate;
    }

    public void setFillRate(double fillRate) {
        this.fillRate = fillRate;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getEcpm() {
        return ecpm;
    }

    public void setEcpm(double ecpm) {
        this.ecpm = ecpm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublisherHeadlineStatsDto [requests=");
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