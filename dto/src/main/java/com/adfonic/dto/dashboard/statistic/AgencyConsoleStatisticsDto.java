package com.adfonic.dto.dashboard.statistic;

public class AgencyConsoleStatisticsDto extends AdvertiserHeadlineStatsDto {

    private static final long serialVersionUID = 1L;

    private String status;
    private String advertiserName;
    private long advertiserId;
    private Double spendYesterday;
    private boolean selected;
    private Double balance;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public void setAdvertiserName(String advertiserName) {
        this.advertiserName = advertiserName;
    }

    public long getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(long advertiserId) {
        this.advertiserId = advertiserId;
    }

    public Double getSpendYesterday() {
        return spendYesterday;
    }

    public void setSpendYesterday(Double spendYesterday) {
        this.spendYesterday = spendYesterday;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StatisticsDto [status=");
        builder.append(status);
        builder.append(", advertiserName=");
        builder.append(advertiserName);
        builder.append(", advertiserId=");
        builder.append(advertiserId);
        toStringFields(builder);
        builder.append("]");
        return builder.toString();
    }

}
