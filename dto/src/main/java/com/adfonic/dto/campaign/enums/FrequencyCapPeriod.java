package com.adfonic.dto.campaign.enums;

public enum FrequencyCapPeriod {
    HOUR(60 * 60, "page.dashboard.labels.charts.filter.options.hour"), 
    DAY(60 * 60 * 24, "page.dashboard.labels.charts.filter.options.day"), 
    WEEK(60 * 60 * 24 * 7, "page.dashboard.labels.charts.filter.options.week"),
    MONTH(60*60*24*30, "page.dashboard.labels.charts.filter.options.month");

    private int seconds;
    private String description;

    private FrequencyCapPeriod(int seconds, String description) {
        this.seconds = seconds;
        this.description = description;
    }
    
    public int getSeconds() {
        return this.seconds;
    }

    public String getDescription() {
        return this.description;
    }

    public static FrequencyCapPeriod getFrequencyCapPeriodBySeconds(int seconds) {
        for (FrequencyCapPeriod f : FrequencyCapPeriod.values()) {
            if (f.seconds == seconds) {
                return f;
            }
        }
        return null;
    }
}
