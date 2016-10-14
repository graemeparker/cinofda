package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name="CAMPAIGN_TRIGGER")
@SQLDelete(sql = "UPDATE CAMPAIGN_TRIGGER SET DELETED = 1 WHERE id = ?")
public class CampaignTrigger extends BusinessKey {
    private static final long serialVersionUID = 1L;

    public enum PluginType {FINANCE_EVENT, SPORT_EVENT, TIMING_EVENT, TV_AD_EVENT, TV_GUIDE_EVENT, WEATHER_EVENT};
    
    @Id 
    @GeneratedValue 
    @Column(name="ID")
    private long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAMPAIGN_ID", nullable = false)
    private Campaign campaign;
    
    @ManyToOne
    @JoinColumn(name = "PLUGIN_VENDOR_ID", nullable = false)
    private PluginVendor pluginVendor;
 
    @Column(name="PLUGIN_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private PluginType pluginType;
    
    @Column(name="DELETED",nullable=false)
    private boolean deleted;

    public long getId() {
        return id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public PluginVendor getPluginVendor() {
        return pluginVendor;
    }

    public void setPluginVendor(PluginVendor pluginVendor) {
        this.pluginVendor = pluginVendor;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }
    
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes" })
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        
        final Class thisClass = getClass();
        final Class otherClass = o.getClass();
        if (!thisClass.isAssignableFrom(otherClass) &&
            !otherClass.isAssignableFrom(thisClass)) return false;

        long id = this.getId();
        CampaignTrigger oCT = (CampaignTrigger) o;
        if ((id == 0) || (oCT.getId()==0)) {
            // The object isn't persisted, it has no id.
            // Comparing fields
            if (this.campaign.getId()!=oCT.campaign.getId()) return false;
            if (this.pluginVendor.getId()!=oCT.pluginVendor.getId()) return false;
            if (this.pluginType!=oCT.pluginType) return false;
            if (this.deleted!=oCT.isDeleted()) return false;
            
            return true;
        }else {
            // If they're both HasPrimaryKeyId, just see if the ids are equal
            return id == ((HasPrimaryKeyId)o).getId();
        }
    }
}
