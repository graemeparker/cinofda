package com.byyd.middleware.creative.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;

public class CreativeFilter {
    private Campaign campaign;
    private Collection<Long> campaignIds;
    private Collection<Creative.Status> statuses;
    
    private String containsName;
    private boolean containsNameCaseSensitive;
    private String name;
    private boolean nameCaseSensitive;
    private Collection<Long> excludedIds;
    //Filter by advertiser for restricted admins
    private Collection<Advertiser> advertisers;
    private boolean filterByAdvertisers;

    // These funky filters are used by the creative approvals dashboard
    private Collection<Long> includedIds;
    private String assignedToFullNameContains;
    private String destinationContains;
    private String companyNameContains;
    private Boolean advertiserIsKey;
    private String countryNameContains;
    private Boolean countryTargetingGlobal;
    private String externalIdContains;
    private String accountManagerEmailContains;
    private String campaignNameContains;
    private String campaignAdvertiserDomainContains;
    
    private Collection<PublisherAuditedCreativeFilter> publisherAuditedCreativeFilters;

    public Campaign getCampaign() {
        return campaign;
    }
    public CreativeFilter setCampaign(Campaign campaign) {
        this.campaign = campaign;
        return this;
    }

    public Collection<Long> getCampaignIds() {
        return campaignIds;
    }
    public CreativeFilter setCampaignIds(Collection<Long> campaignIds) {
        this.campaignIds = campaignIds;
        return this;
    }

    public Collection<Creative.Status> getStatuses() {
        return statuses;
    }
    public CreativeFilter setStatuses(Collection<Creative.Status> statuses) {
        this.statuses = statuses;
        return this;
    }
    public CreativeFilter setStatuses(Creative.Status ... statuses) {
        Set<Creative.Status> set = new LinkedHashSet<Creative.Status>();
        for (Creative.Status status : statuses) {
            set.add(status);
        }
        this.statuses = set;
        return this;
    }
    
    public String getContainsName() {
        return containsName;
    }
    public CreativeFilter setContainsName(String containsName, boolean caseSensitive) {
        this.containsName = containsName;
        this.containsNameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isContainsNameCaseSensitive() {
        return containsNameCaseSensitive;
    }
    public CreativeFilter setContainsNameCaseSensitive(boolean containsNameCaseSensitive) {
        this.containsNameCaseSensitive = containsNameCaseSensitive;
        return this;
    }

    public String getName() {
        return name;
    }
    public CreativeFilter setName(String name, boolean caseSensitive) {
        this.name = name;
        this.nameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public CreativeFilter setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public Collection<Long> getExcludedIds() {
        return excludedIds;
    }
    public CreativeFilter setExcludedIds(Collection<Long> excludedIds) {
        this.excludedIds = excludedIds;
        return this;
    }

    public Collection<Long> getIncludedIds() {
        return includedIds;
    }
    public CreativeFilter setIncludedIds(Collection<Long> includedIds) {
        this.includedIds = includedIds;
        return this;
    }
    public CreativeFilter setIncludedIds(Long ... includedIds) {
        this.includedIds = Arrays.asList(includedIds);
        return this;
    }

    public String getAssignedToFullNameContains() {
        return assignedToFullNameContains;
    }
    public CreativeFilter setAssignedToFullNameContains(String assignedToFullNameContains) {
        this.assignedToFullNameContains = assignedToFullNameContains;
        return this;
    }

    public String getDestinationContains() {
        return destinationContains;
    }
    public CreativeFilter setDestinationContains(String destinationContains) {
        this.destinationContains = destinationContains;
        return this;
    }

    public String getCompanyNameContains() {
        return companyNameContains;
    }
    public CreativeFilter setCompanyNameContains(String companyNameContains) {
        this.companyNameContains = companyNameContains;
        return this;
    }

    public Boolean getAdvertiserIsKey() {
        return advertiserIsKey;
    }
    public CreativeFilter setAdvertiserIsKey(Boolean advertiserIsKey) {
        this.advertiserIsKey = advertiserIsKey;
        return this;
    }

    public String getCountryNameContains() {
        return countryNameContains;
    }
    public CreativeFilter setCountryNameContains(String countryNameContains) {
        this.countryNameContains = countryNameContains;
        return this;
    }

    public Boolean getCountryTargetingGlobal() {
        return countryTargetingGlobal;
    }
    public CreativeFilter setCountryTargetingGlobal(Boolean countryTargetingGlobal) {
        this.countryTargetingGlobal = countryTargetingGlobal;
        return this;
    }

    public String getExternalIdContains() {
        return externalIdContains;
    }
    public CreativeFilter setExternalIdContains(String externalIdContains) {
        this.externalIdContains = externalIdContains;
        return this;
    }

    public String getAccountManagerEmailContains() {
        return accountManagerEmailContains;
    }
    public CreativeFilter setAccountManagerEmailContains(String accountManagerEmailContains) {
        this.accountManagerEmailContains = accountManagerEmailContains;
        return this;
    }

    public String getCampaignNameContains() {
        return campaignNameContains;
    }
    
    public CreativeFilter setCampaignNameContains(String campaignNameContains) {
        this.campaignNameContains = campaignNameContains;
        return this;
    }
    
    public String getCampaignAdvertiserDomainContains() {
        return campaignAdvertiserDomainContains;
    }
    
    public CreativeFilter setCampaignAdvertiserDomainContains(String campaignAdvertiserDomainContains) {
        this.campaignAdvertiserDomainContains = campaignAdvertiserDomainContains;
        return this;
    }
    
    public Collection<Advertiser> getAdvertisers() {
        return advertisers;
    }
    
    public CreativeFilter setAdvertisers(Collection<Advertiser> advertisers) {
        this.advertisers = advertisers;
        return this;
    }
    
    public boolean isFilterByAdvertisers() {
        return filterByAdvertisers;
    }
    
    public void setFilterByAdvertisers(boolean filterByAdvertisers) {
        this.filterByAdvertisers = filterByAdvertisers;
    }
    
    public Collection<PublisherAuditedCreativeFilter> getPublisherAuditedCreativeFilters() {
        return publisherAuditedCreativeFilters;
    }
    public CreativeFilter addPublisherAuditedCreativeFilter(String sortingAlias, 
                                                            Collection<Publisher> publishers, 
                                                            Boolean excludeStatuses, 
                                                            Collection<PublisherAuditedCreative.Status> statuses, 
                                                            Boolean retrieveNullValuesAlso){
        if (this.publisherAuditedCreativeFilters==null){
            this.publisherAuditedCreativeFilters = new HashSet<>();
        }
        this.publisherAuditedCreativeFilters.add(new PublisherAuditedCreativeFilter().setSortingAlias(sortingAlias)
                                                                                     .setPublishers(publishers)
                                                                                     .setExcludeStatuses(excludeStatuses)
                                                                                     .setStatuses(statuses)
                                                                                     .setRetrieveNullValuesAlso(retrieveNullValuesAlso));
        
        return this;
    }
    public PublisherAuditedCreativeFilter getPublisherAuditedCreativeFilter(String alias){
        if (this.publisherAuditedCreativeFilters!=null){
            for (PublisherAuditedCreativeFilter filter : this.publisherAuditedCreativeFilters){
                if (filter.getSortingAlias().equals(alias)){
                    return filter;
                }
            }
        }
        return null;
    }
    
}
