package com.adfonic.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "CAMPAIGN_AUDIENCE")
@SQLDelete(sql = "UPDATE CAMPAIGN_AUDIENCE SET DELETED = 1 WHERE id = ?")
public class CampaignAudience extends BusinessKey {

    private static final long serialVersionUID = 1L;

    public enum RecencyType {
        NONE, RANGE, WINDOW;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUDIENCE_ID", nullable = false)
    private Audience audience;

    @Column(name = "INCLUDE", nullable = false)
    private boolean include;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAMPAIGN_ID", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENT_AUDIENCE_DATA_FEE_ID", nullable = false)
    private AudienceDataFee audienceDataFee;

    @Column(name = "RECENCY_FROM", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date recencyDateFrom;

    @Column(name = "RECENCY_TO", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date recencyDateTo;

    @Column(name = "NUM_DAYS_AGO_FROM", nullable = true)
    private Integer recencyDaysFrom;

    @Column(name = "NUM_DAYS_AGO_TO", nullable = true)
    private Integer recencyDaysTo;

    public Audience getAudience() {
        return audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public AudienceDataFee getAudienceDataFee() {
        return audienceDataFee;
    }

    public void setAudienceDataFee(AudienceDataFee audienceDataFee) {
        this.audienceDataFee = audienceDataFee;
    }

    public Date getRecencyDateFrom() {
        return recencyDateFrom;
    }

    public void setRecencyDateFrom(Date recencyDateFrom) {
        this.recencyDateFrom = (recencyDateFrom == null) ? null : new Date(recencyDateFrom.getTime());
    }

    public Date getRecencyDateTo() {
        return recencyDateTo;
    }

    public void setRecencyDateTo(Date recencyDateTo) {
        this.recencyDateTo = (recencyDateTo == null) ? null : setTimeTo_23_59_59(new Date(recencyDateTo.getTime()));
    }

    public Integer getRecencyDaysFrom() {
        return recencyDaysFrom;
    }

    public void setRecencyDaysFrom(Integer recencyDaysFrom) {
        this.recencyDaysFrom = recencyDaysFrom;
    }

    public Integer getRecencyDaysTo() {
        return recencyDaysTo;
    }

    public void setRecencyDaysTo(Integer recencyDaysTo) {
        this.recencyDaysTo = recencyDaysTo;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        if (id==0){
            result = prime * result + ((audience == null) ? 0 : audience.hashCode());
            result = prime * result + ((audienceDataFee == null) ? 0 : audienceDataFee.hashCode());
            result = prime * result + ((campaign == null) ? 0 : campaign.hashCode());
            result = prime * result + (deleted ? 1231 : 1237);
            result = prime * result + (include ? 1231 : 1237);
            result = prime * result + ((recencyDateFrom == null) ? 0 : recencyDateFrom.hashCode());
            result = prime * result + ((recencyDateTo == null) ? 0 : recencyDateTo.hashCode());
            result = prime * result + ((recencyDaysFrom == null) ? 0 : recencyDaysFrom.hashCode());
            result = prime * result + ((recencyDaysTo == null) ? 0 : recencyDaysTo.hashCode());
        }else{
            result = -1 * prime * result + (int) (id ^ (id >>> 32));
        }
        return result;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        final Class thisClass = getClass();
        final Class otherClass = o.getClass();
        if (!thisClass.isAssignableFrom(otherClass) && !otherClass.isAssignableFrom(thisClass)) {
            return false;
        }

        long caId = this.getId();
        CampaignAudience oCA = (CampaignAudience) o;
        if ((caId == 0) || (oCA.getId() == 0)) {
            // The object isn't persisted, it has no id.
            // Comparing fields
            if (this.audience.getId() != oCA.getAudience().getId()) {
                return false;
            }
            if (this.campaign.getId() != oCA.campaign.getId()) {
                return false;
            }
            if (this.include != oCA.isInclude()) {
                return false;
            }
            if (!ObjectUtils.equals(recencyDateFrom, oCA.getRecencyDateFrom())) {
                return false;
            }
            if (!ObjectUtils.equals(recencyDateTo, oCA.getRecencyDateTo())) {
                return false;
            }
            if (!ObjectUtils.equals(recencyDaysFrom, oCA.getRecencyDaysFrom())) {
                return false;
            }
            if (!ObjectUtils.equals(recencyDaysTo, oCA.getRecencyDaysTo())) {
                return false;
            }
            if (this.deleted != oCA.isDeleted()) {
                return false;
            }

            return true;
        }
        
        // Finally as they're both HasPrimaryKeyId, just see if the ids are equal, 
        // and if they are equal checks if it has assigned he same audience id
        return (caId == ((HasPrimaryKeyId) o).getId()) ? (this.audience.getId() == oCA.getAudience().getId()) : false;
    }
    
    // For AuditLog purposes
    public String getRecencyDateHumanReadable(){
        String result = null;
        if (this.recencyDateFrom!=null & this.recencyDateTo!=null){
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sb.append("From ");
            sb.append(sdf.format(this.recencyDateFrom));
            sb.append(" to ");
            sb.append(sdf.format(this.recencyDateTo));
            result = sb.toString();
        }
        return result;
    }
    
    public String getRecencyDaysHumanReadable(){
        String result = null;
        if (this.recencyDaysFrom!=null & this.recencyDaysTo!=null){
            StringBuilder sb = new StringBuilder();
            sb.append("From ");
            sb.append(this.recencyDaysFrom);
            sb.append(" to ");
            sb.append(this.recencyDaysTo);
            result = sb.toString();
        }
        return result;
    }
    
    /** Add time part with 23:59:59 */
    private Date setTimeTo_23_59_59(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.getTime();
    }

}
