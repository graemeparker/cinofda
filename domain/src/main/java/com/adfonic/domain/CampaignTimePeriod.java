package com.adfonic.domain;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="CAMPAIGN_TIME_PERIOD")
public class CampaignTimePeriod extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=false)
    private Campaign campaign;

    // This date is INCLUSIVE (at least it is in adserver)
    @Column(name="START_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    // This date is EXCLUSIVE (at least it is in adserver)
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    public CampaignTimePeriod() {}

    public CampaignTimePeriod(Campaign campaign) {
        this.campaign = campaign;
    }

    public CampaignTimePeriod(Campaign campaign,
                              Date startDate,
                              Date endDate)
    {
        this.campaign = campaign;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getId() { return id; };

    public Campaign getCampaign() {
        return campaign;
    }
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

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
        return (startDate == null || startDate.getTime() <= timeMillis) &&
            (endDate == null || endDate.getTime() > timeMillis);
    }

    /** True if the time period is current as of the system's current time */
    public boolean isCurrent() {
        return isCurrent(System.currentTimeMillis());
    }

    /** True if the time period is in the future as of the time argument */
    public boolean isFuture(long timeMillis) {
        return startDate != null &&
            startDate.getTime() > timeMillis;
    }

    /** True if the time period is in the future as of the system's current time */
    public boolean isFuture() {
        return isFuture(System.currentTimeMillis());
    }

    public boolean overlaps(CampaignTimePeriod other) {
        if (other == null) {
            return false;
        }

        // If either one is double-open-ended, then they implicitly overlap
        if ((this.startDate == null && this.endDate == null) ||
            (other.startDate == null && other.endDate == null)) {
            return true;
        }

        // If both have open-ended start or end, then they implicitly overlap
        if ((this.startDate == null && other.startDate == null) ||
            (this.endDate == null && other.endDate == null)) {
            return true;
        }

        if (this.startDate == null) {
            return this.endDate.after(other.startDate);
        }
        else if (this.endDate == null) {
            return this.startDate.before(other.endDate);
        }
        else if (other.startDate == null) {
            return other.endDate.after(this.startDate);
        }
        else if (other.endDate == null) {
            return other.startDate.before(this.endDate);
        }
        else {
            return (!this.startDate.before(other.startDate) &&
                    this.startDate.before(other.endDate)) ||
                (!other.startDate.before(this.startDate) &&
                 other.startDate.before(this.endDate)) ||
                (this.endDate.after(other.startDate) &&
                 !this.endDate.after(other.endDate)) ||
                (other.endDate.after(this.startDate) &&
                 !other.endDate.after(this.endDate));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        }
        final CampaignTimePeriod other = (CampaignTimePeriod)obj;
        return
            //this.campaign.equals(other.campaign) &&
            (
                    (this.endDate == null && other.endDate == null) ||
                    //(this.endDate != null && this.endDate.equals(other.endDate))
                    (this.endDate != null && other.endDate != null && this.endDate.getTime() == other.endDate.getTime())
            )
            &&
            (
                    (this.startDate == null && other.startDate == null) ||
                    //(this.startDate != null && this.startDate.equals(other.startDate))
                    (this.startDate != null && other.startDate != null && this.startDate.getTime() == other.startDate.getTime())
            );
    }

    @Override
    public int compareTo(Object o) {
        CampaignTimePeriod other = (CampaignTimePeriod)o;
        if (this == other) return 0;
        if (this.startDate == null) {
            if (other.startDate != null) {
                return -1; // we have no start date, so we're earlier
            }
            // Fall through to endDate comparison
        }
        else if (other.startDate == null) {
            return 1; // the other one has no start date, so it's earlier
        }
        else {
            // Compare start dates
            int x = this.startDate.compareTo(other.startDate);
            if (x != 0) {
                return x;
            }
        }

        if (this.endDate == null) {
            if (other.endDate != null) {
                return 1; // We're open-ended, so we're later
            }
            else {
                return 0; // Both open-ended, same startDate, they're the same
            }
        }
        else if (other.endDate == null) {
            return -1; // The other one is open-ended, so we're earlier
        }
        else {
            // Compare end dates
            return this.endDate.compareTo(other.endDate);
        }
    }

    public String toString() {
        return "CampaignTimePeriod[startDate=" + startDate + ",endDate=" + endDate + "]";
    }
}
