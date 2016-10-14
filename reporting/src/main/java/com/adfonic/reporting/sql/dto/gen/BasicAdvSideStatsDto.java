package com.adfonic.reporting.sql.dto.gen;

import java.io.Serializable;
import java.lang.reflect.Field;

public class BasicAdvSideStatsDto implements OrderedStatistics, Serializable {

    private static final long serialVersionUID = 1L;

    private long impressions;
    private long clicks;
    private double ctr;
    private double ecpm;
    private double ecpc;
    private long conversions;
    private double costPerConversion;
    private double spend;

    private double clickConversion;


    @Override
    public Object[] asObjectArray() {
        return new Object[] { impressions, clicks, conversions, clickConversion, ctr, ecpm, costPerConversion, spend, ecpc };
    }

    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }


    public void setClicks(long clicks) {
        this.clicks = clicks;
    }


    public void setCtr(double ctr) {
        this.ctr = ctr;
    }


    public void setEcpm(double ecpm) {
        this.ecpm = ecpm;
    }


    public void setEcpc(double ecpc) {
        this.ecpc = ecpc;
    }


    public void setConversions(long conversions) {
        this.conversions = conversions;
    }


    public void setCostPerConversion(double costPerConversion) {
        this.costPerConversion = costPerConversion;
    }


    public void setSpend(double spend) {
        this.spend = spend;
    }


    public void setClickConversion(double clickConversion) {
        this.clickConversion = clickConversion;
    }

    @Override
    public Object getStatisticByName(String name) {
        try {
            Field field = BasicAdvSideStatsDto.class.getDeclaredField(name);
            return field.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException  e) {
            e.printStackTrace();
        }
        return null;
    }

}
