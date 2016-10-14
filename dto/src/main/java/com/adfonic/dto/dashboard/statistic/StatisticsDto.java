package com.adfonic.dto.dashboard.statistic;

import com.adfonic.dto.campaign.enums.BidType;

public class StatisticsDto extends AdvertiserHeadlineStatsDto {

    private static final long serialVersionUID = 1L;

    private String status;
    private String campaignName;
    private double bidPrice;
    private double cpa;
    private double cpm;
    private double cvr;
    private double budgetSpent;
    private long totalBudgetToDate;
    private long campaignId;
    private BidType bidType;
    private double averageBidPrice;
    private Double budgetRemaining;
    private Double totalBudget;
    private Double spendYesterday;
    private Double totalSpend;
    private Double dailyCap;
    private boolean selected;
    private String budgetUnit;
    private Double budgetDeliveredToday;
    private boolean evenDistributionOverallBudget;
    private boolean evenDistributionDailyBudget;
    private boolean priceOverridden;
    private boolean allCreativeRejected;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public double getCpa() {
        return cpa;
    }

    public void setCpa(double cpa) {
        this.cpa = cpa;
    }

    public double getBudgetSpent() {
        return budgetSpent;
    }

    public void setBudgetSpent(double budgetSpent) {
        this.budgetSpent = budgetSpent;
    }

    public long getTotalBudgetToDate() {
        return totalBudgetToDate;
    }

    public void setTotalBudgetToDate(long totalBudgetToDate) {
        this.totalBudgetToDate = totalBudgetToDate;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public BidType getBidType() {
        return bidType;
    }

    public void setBidType(BidType bidType) {
        this.bidType = bidType;
    }

    /**
     * @return the averageBidPrice
     */
    public double getAverageBidPrice() {
        return averageBidPrice;
    }

    /**
     * @param averageBidPrice
     *            the averageBidPrice to set
     */
    public void setAverageBidPrice(double averageBidPrice) {
        this.averageBidPrice = averageBidPrice;
    }

    public Double getBudgetRemaining() {
        return budgetRemaining;
    }

    public void setBudgetRemaining(Double budgetRemaining) {
        this.budgetRemaining = budgetRemaining;
    }

    public Double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(Double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public Double getSpendYesterday() {
        return spendYesterday;
    }

    public void setSpendYesterday(Double spendYesterday) {
        this.spendYesterday = spendYesterday;
    }

    public Double getDailyCap() {
        return dailyCap;
    }

    public void setDailyCap(Double dailyCap) {
        this.dailyCap = dailyCap;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public double getCpm() {
        return cpm;
    }

    public void setCpm(double cpm) {
        this.cpm = cpm;
    }

    public double getCvr() {
        return cvr;
    }

    public void setCvr(double cvr) {
        this.cvr = cvr;
    }

    public Double getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(Double totalSpend) {
        this.totalSpend = totalSpend;
    }

    public String getBudgetUnit() {
        return budgetUnit;
    }

    public void setBudgetUnit(String budgetUnit) {
        this.budgetUnit = budgetUnit;
    }

    public Double getBudgetDeliveredToday() {
        return budgetDeliveredToday;
    }

    public void setBudgetDeliveredToday(Double budgetDeliveredToday) {
        this.budgetDeliveredToday = budgetDeliveredToday;
    }

    public Double getRemainingLifetimeBudget() {
        if (totalBudget != null && totalSpend != null) {
            return totalBudget - totalSpend;
        }
        return null;
    }

    public boolean isEvenDistributionOverallBudget() {
        return evenDistributionOverallBudget;
    }

    public void setEvenDistributionOverallBudget(boolean evenDistributionOverallBudget) {
        this.evenDistributionOverallBudget = evenDistributionOverallBudget;
    }

    public boolean isEvenDistributionDailyBudget() {
        return evenDistributionDailyBudget;
    }

    public void setEvenDistributionDailyBudget(boolean evenDistributionDailyBudget) {
        this.evenDistributionDailyBudget = evenDistributionDailyBudget;
    }
    
    public boolean isPriceOverridden() {
        return priceOverridden;
    }

    public void setPriceOverridden(boolean priceOverridden) {
        this.priceOverridden = priceOverridden;
    }
    
    public boolean isAllCreativeRejected() {
        return allCreativeRejected;
    }

    public void setAllCreativeRejected(boolean allCreativeRejected) {
        this.allCreativeRejected = allCreativeRejected;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StatisticsDto [status=");
        builder.append(status);
        builder.append(", campaignName=");
        builder.append(campaignName);
        builder.append(", campaignId=");
        builder.append(campaignId);
        builder.append(", bidPrice=");
        builder.append(bidPrice);
        builder.append(", cpa=");
        builder.append(cpa);
        builder.append(", budgetSpent=");
        builder.append(budgetSpent);
        builder.append(", totalBudgetToDate=");
        builder.append(totalBudgetToDate);
        builder.append(", bidType=");
        builder.append(bidType);
        builder.append(", averageBidPrice=");
        builder.append(averageBidPrice);
        builder.append(", priceOverridden=");
        builder.append(priceOverridden);
        builder.append(", allCreativeRejected=");
        builder.append(allCreativeRejected);
        toStringFields(builder);
        builder.append("]");
        return builder.toString();
    }

}
