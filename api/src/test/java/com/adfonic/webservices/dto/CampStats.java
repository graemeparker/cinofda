package com.adfonic.webservices.dto;

public class CampStats extends BaseTO {
    public int impressions;
    public int clicks;
    public int conversions;
    public float conversionsPercent;// as implemented. not in spec

    // Actually Payouts - reusing Earnings
    public CurrencyValue CTR;
    public CurrencyValue ECPM;
    public CurrencyValue costPerConversion;
    public CurrencyValue spend;

}
