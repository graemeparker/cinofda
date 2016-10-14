package com.adfonic.webservices.dto;

import java.util.Date;

public class Campaign extends BaseTO {
    public String id;
    public String name;
    public String status;
    // reuse from base class error description
    // public String description;
    public Date startDate;
    public String endDate;

    // 3 fields not in spec
    public String advertiser;
    public String activationDate;
    public String deactivationDate;

    public CurrencyValue dailyBudget;
    public CurrencyValue overallBudget;
    public CurrencyValue dailyBudgetWeekday;
    public CurrencyValue dailyBudgetWeekend;
}
