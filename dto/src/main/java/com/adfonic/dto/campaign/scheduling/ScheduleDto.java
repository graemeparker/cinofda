package com.adfonic.dto.campaign.scheduling;

import java.io.Serializable;
import java.util.Arrays;

public class ScheduleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int TWENTY_FOUR = 24;
    private static final int SEVEN = 7;
    
    // Time section
    private Boolean[] hoursOfDay;
    private Boolean[] hoursOfDayWeekend;
    private Boolean[] daysOfWeek;


    // Target by time
    public Boolean[] getHoursOfDay() {
        if (hoursOfDay == null) {
            hoursOfDay = new Boolean[TWENTY_FOUR];
            // all to true
            for (int k = 0; k < hoursOfDay.length; k++) {
                hoursOfDay[k] = Boolean.TRUE;
            }
        }
        return hoursOfDay;
    }

    public void setHoursOfDay(Boolean[] hoursOfDay) {
        this.hoursOfDay = (hoursOfDay == null ? null : hoursOfDay.clone());
    }

    public Boolean[] getHoursOfDayWeekend() {
        if (hoursOfDayWeekend == null) {
            hoursOfDayWeekend = new Boolean[TWENTY_FOUR];
            Arrays.fill(hoursOfDayWeekend, Boolean.TRUE);
        }
        return hoursOfDayWeekend;
    }

    public void setHoursOfDayWeekend(Boolean[] hoursOfDayWeekend) {
        this.hoursOfDayWeekend =  (hoursOfDayWeekend == null ? null : hoursOfDayWeekend.clone());
    }

    public Boolean[] getDaysOfWeek() {
        if (daysOfWeek == null) {
            daysOfWeek = new Boolean[SEVEN];
            Arrays.fill(daysOfWeek, Boolean.TRUE);
        }
        return daysOfWeek;
    }

    public void setDaysOfWeek(Boolean[] daysOfWeek) {
        this.daysOfWeek = (daysOfWeek == null ? null : daysOfWeek.clone());
    }

}
