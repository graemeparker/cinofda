package com.adfonic.dto.campaign;

import java.math.BigDecimal;

import org.jdto.annotation.Source;

import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Campaign.Status;

public abstract class CampaignBudgetInfoDto extends CampaignInfoDto {

    private static final long serialVersionUID = 1L;
    
    @Source("dailyBudget")
    protected BigDecimal dailyBudget; // null if none

    @Source("dailyBudgetWeekday")
    protected BigDecimal dailyBudgetWeekday; // null if none

    @Source("dailyBudgetWeekend")
    protected BigDecimal dailyBudgetWeekend; // null if none

    @Source("dailyBudgetAlertEnabled")
    protected boolean dailyBudgetAlertEnabled;

    @Source("overallBudget")
    protected BigDecimal overallBudget; // null if none

    @Source("overallBudgetAlertEnabled")
    protected boolean overallBudgetAlertEnabled;

    @Source("dailyBudgetImpressions")
    protected BigDecimal dailyBudgetImpressions; // null if none

    @Source("overallBudgetImpressions")
    protected BigDecimal overallBudgetImpressions; // null if none

    @Source("dailyBudgetClicks")
    protected BigDecimal dailyBudgetClicks; // null if none

    @Source("overallBudgetClicks")
    private BigDecimal overallBudgetClicks; // null if none

    @Source("dailyBudgetConversions")
    protected BigDecimal dailyBudgetConversions; // null if none

    @Source("overallBudgetConversions")
    protected BigDecimal overallBudgetConversions; // null if none

    @Source("budgetType")
    protected BudgetType budgetType;

    @Source("status")
    protected Status status;

    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public BigDecimal getDailyBudgetWeekday() {
        return dailyBudgetWeekday;
    }

    public void setDailyBudgetWeekday(BigDecimal dailyBudgetWeekday) {
        this.dailyBudgetWeekday = dailyBudgetWeekday;
    }

    public BigDecimal getDailyBudgetWeekend() {
        return dailyBudgetWeekend;
    }

    public void setDailyBudgetWeekend(BigDecimal dailyBudgetWeekend) {
        this.dailyBudgetWeekend = dailyBudgetWeekend;
    }

    public boolean getDailyBudgetAlertEnabled() {
        return dailyBudgetAlertEnabled;
    }

    public void setDailyBudgetAlertEnabled(boolean dailyBudgetAlertEnabled) {
        this.dailyBudgetAlertEnabled = dailyBudgetAlertEnabled;
    }

    public BigDecimal getOverallBudget() {
        return overallBudget;
    }

    public void setOverallBudget(BigDecimal overallBudget) {
        this.overallBudget = overallBudget;
    }

    public boolean getOverallBudgetAlertEnabled() {
        return overallBudgetAlertEnabled;
    }

    public void setOverallBudgetAlertEnabled(boolean overallBudgetAlertEnabled) {
        this.overallBudgetAlertEnabled = overallBudgetAlertEnabled;
    }

    public BigDecimal getDailyBudgetImpressions() {
        return dailyBudgetImpressions;
    }

    public void setDailyBudgetImpressions(BigDecimal dailyBudgetImpressions) {
        this.dailyBudgetImpressions = dailyBudgetImpressions;
    }

    public BigDecimal getOverallBudgetImpressions() {
        return overallBudgetImpressions;
    }

    public void setOverallBudgetImpressions(BigDecimal overallBudgetImpressions) {
        this.overallBudgetImpressions = overallBudgetImpressions;
    }

    public BigDecimal getDailyBudgetClicks() {
        return dailyBudgetClicks;
    }

    public void setDailyBudgetClicks(BigDecimal dailyBudgetClicks) {
        this.dailyBudgetClicks = dailyBudgetClicks;
    }

    public BigDecimal getOverallBudgetClicks() {
        return overallBudgetClicks;
    }

    public void setOverallBudgetClicks(BigDecimal overallBudgetClicks) {
        this.overallBudgetClicks = overallBudgetClicks;
    }

    public BigDecimal getDailyBudgetConversions() {
        return dailyBudgetConversions;
    }

    public void setDailyBudgetConversions(BigDecimal dailyBudgetConversions) {
        this.dailyBudgetConversions = dailyBudgetConversions;
    }

    public BigDecimal getOverallBudgetConversions() {
        return overallBudgetConversions;
    }

    public void setOverallBudgetConversions(BigDecimal overallBudgetConversions) {
        this.overallBudgetConversions = overallBudgetConversions;
    }

    public BudgetType getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(BudgetType budgetType) {
        this.budgetType = budgetType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
