package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.Date;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignTimePeriodDto extends BusinessKeyDto implements Comparable {
    private static final long serialVersionUID = 1L;

    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /** True if the time period is current as of the time argument */
    public boolean isCurrent(long timeMillis) {
        return (startDate == null || startDate.getTime() <= timeMillis) && (endDate == null || endDate.getTime() > timeMillis);
    }

    /** True if the time period is current as of the system's current time */
    public boolean isCurrent() {
        return isCurrent(System.currentTimeMillis());
    }

    /** True if the time period is in the future as of the time argument */
    public boolean isFuture(long timeMillis) {
        return startDate != null && startDate.getTime() > timeMillis;
    }

    /** True if the time period is in the future as of the system's current time */
    public boolean isFuture() {
        return isFuture(System.currentTimeMillis());
    }

    @Override
    public int compareTo(Object o) {
        CampaignTimePeriodDto other = (CampaignTimePeriodDto) o;
        if (this == other) {
            return 0;
        }
        if (this.startDate == null) {
            if (other.startDate != null) {
                return -1; // we have no start date, so we're earlier
            }
            // Fall through to endDate comparison
        } else if (other.startDate == null) {
            return 1; // the other one has no start date, so it's earlier
        } else {
            // Compare start dates
            int x = this.startDate.compareTo(other.startDate);
            if (x != 0) {
                return x;
            }
        }

        if (this.endDate == null) {
            if (other.endDate != null) {
                return 1; // We're open-ended, so we're later
            } else {
                return 0; // Both open-ended, same startDate, they're the same
            }
        } else if (other.endDate == null) {
            return -1; // The other one is open-ended, so we're earlier
        } else {
            // Compare end dates
            return this.endDate.compareTo(other.endDate);
        }
    }

    @Override
    public String toString() {
        return "CampaignTimePeriodDto {" + getId() + ", startDate=" + startDate + ", endDate=" + endDate + "}";
    }

}
