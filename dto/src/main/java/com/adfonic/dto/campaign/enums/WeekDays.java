package com.adfonic.dto.campaign.enums;

public enum WeekDays {
    MON("page.campaign.scheduling.controltime.weekday.mon.label", "Mon"), TUE("page.campaign.scheduling.controltime.weekday.tue.label", "Tue"), WED(
            "page.campaign.scheduling.controltime.weekday.wed.label", "Wed"), THU("page.campaign.scheduling.controltime.weekday.thu.label", "Thu"), FRI(
            "page.campaign.scheduling.controltime.weekday.fri.label", "Fri");
    private String i18n;
    private String key;

    private WeekDays(String i18n, String key) {
        this.i18n = i18n;
        this.key = key;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
