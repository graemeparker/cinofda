package com.byyd.middleware.campaign.filter;

public class CampaignStateSyncingFilter {
    
    public static final CampaignStateSyncingFilter FOR_DTO_COPY = new CampaignStateSyncingFilter().setSyncName(true)
                                                                                                  .setSyncStatus(true)
                                                                                                  .setSyncCategory(true);
    public static final CampaignStateSyncingFilter FOR_NEW_INSTANCE = new CampaignStateSyncingFilter();

    // Default values as defined by the original Campaign.copyFrom()
    Boolean syncName = false;
    Boolean syncBudgetData = true;
    Boolean syncLanguageData = true;
    Boolean syncCategory = false;
    Boolean syncTransparentNetworks = true;
    Boolean syncDeviceIdentifierTypes = true;
    Boolean syncStatus = false;

    public Boolean getSyncName() {
        return syncName;
    }
    public CampaignStateSyncingFilter setSyncName(Boolean syncName) {
        this.syncName = syncName;
        return this;
    }
    public Boolean getSyncBudgetData() {
        return syncBudgetData;
    }
    public CampaignStateSyncingFilter setSyncBudgetData(Boolean syncBudgetData) {
        this.syncBudgetData = syncBudgetData;
        return this;
    }
    public Boolean getSyncLanguageData() {
        return syncLanguageData;
    }
    public CampaignStateSyncingFilter setSyncLanguageData(Boolean syncLanguageData) {
        this.syncLanguageData = syncLanguageData;
        return this;
    }
    public Boolean getSyncCategory() {
        return syncCategory;
    }
    public CampaignStateSyncingFilter setSyncCategory(Boolean syncCategory) {
        this.syncCategory = syncCategory;
        return this;
    }
    public Boolean getSyncTransparentNetworks() {
        return syncTransparentNetworks;
    }
    public CampaignStateSyncingFilter setSyncTransparentNetworks(Boolean syncTransparentNetworks) {
        this.syncTransparentNetworks = syncTransparentNetworks;
        return this;
    }
    public Boolean getSyncDeviceIdentifierTypes() {
        return syncDeviceIdentifierTypes;
    }
    public CampaignStateSyncingFilter setSyncDeviceIdentifierTypes(Boolean syncDeviceIdentifierTypes) {
        this.syncDeviceIdentifierTypes = syncDeviceIdentifierTypes;
        return this;
    }
    public Boolean getSyncStatus() {
        return syncStatus;
    }
    public CampaignStateSyncingFilter setSyncStatus(Boolean syncStatus) {
        this.syncStatus = syncStatus;
        return this;
    }
}
