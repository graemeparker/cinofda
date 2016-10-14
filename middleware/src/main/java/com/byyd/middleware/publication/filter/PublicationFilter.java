package com.byyd.middleware.publication.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.adfonic.domain.Publication;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.LikeSpec;

public class PublicationFilter {
    
    private String name;
    private boolean nameCaseSensitive;
    private String nameLike;
    private boolean nameLikeCaseSensitive;
    private Publisher publisher;
    private Collection<Publication.Status> statuses;
    private Collection<Publication.AdOpsStatus> adOpsStatuses;
    private String rtbId;
    private String friendlyName;
    private LikeSpec friendlyNameLikeSpec;
    private boolean friendlyNameCaseSensitive = false;
    private boolean friendlyNameAppliesToName = false;
    private boolean rtbIdNotNull = false;
    private Date greaterThanOrEqualToApprovalDate;
    private Collection<Publication.PublicationSafetyLevel> safetyLevels;

    // These funky filters are used by the publication approvals dashboard
    private Collection<Long> includedIds;
    private String assignedToFullNameContains;
    private String companyNameContains;
    private Boolean publisherIsKey;
    private String externalIdContains;
    private String accountManagerEmailContains;
    private String publicationTypeNameContains;
    private Boolean autoApproval;

    public String getName() {
        return name;
    }
    public PublicationFilter setName(String name, boolean caseSensitive) {
        this.name = name;
        this.nameCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    public PublicationFilter setNameCaseSensitive(boolean nameCaseSensitive) {
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public String getNameLike() {
        return nameLike;
    }
    public PublicationFilter setNameLike(String nameLike, LikeSpec likeSpec, boolean caseSensitive) {
        this.nameLike = (likeSpec == null ? LikeSpec.CONTAINS : likeSpec).getPattern(nameLike);
        this.nameLikeCaseSensitive = caseSensitive;
        return this;
    }

    public boolean isNameLikeCaseSensitive() {
        return nameLikeCaseSensitive;
    }
    public PublicationFilter setNameLikeCaseSensitive(boolean nameLikeCaseSensitive) {
        this.nameLikeCaseSensitive = nameLikeCaseSensitive;
        return this;
    }

    public Publisher getPublisher() {
        return publisher;
    }
    public PublicationFilter setPublisher(Publisher publisher) {
        this.publisher = publisher;
        return this;
    }

    public Collection<Publication.Status> getStatuses() {
        return statuses;
    }
    public PublicationFilter setStatuses(Collection<Publication.Status> statuses) {
        this.statuses = statuses;
        return this;
    }
    public PublicationFilter setStatuses(Publication.Status ... statuses) {
        Set<Publication.Status> set = new LinkedHashSet<Publication.Status>();
        for (Publication.Status status : statuses) {
            set.add(status);
        }
        this.statuses = set;
        return this;
    }

    public Collection<Publication.AdOpsStatus> getAdOpsStatuses() {
        return adOpsStatuses;
    }
    public PublicationFilter setAdOpsStatuses(Collection<Publication.AdOpsStatus> adOpsStatuses) {
        this.adOpsStatuses = adOpsStatuses;
        return this;
    }

    public String getRtbId() {
        return rtbId;
    }
    public PublicationFilter setRtbId(String rtbId) {
        this.rtbId = rtbId;
        return this;
    }
    
    public Collection<Long> getIncludedIds() {
        return includedIds;
    }
    public PublicationFilter setIncludedIds(Collection<Long> includedIds) {
        this.includedIds = includedIds;
        return this;
    }
    public PublicationFilter setIncludedIds(Long ... includedIds) {
        this.includedIds = Arrays.asList(includedIds);
        return this;
    }

    public String getAssignedToFullNameContains() {
        return assignedToFullNameContains;
    }
    public PublicationFilter setAssignedToFullNameContains(String assignedToFullNameContains) {
        this.assignedToFullNameContains = assignedToFullNameContains;
        return this;
    }

    public String getCompanyNameContains() {
        return companyNameContains;
    }
    public PublicationFilter setCompanyNameContains(String companyNameContains) {
        this.companyNameContains = companyNameContains;
        return this;
    }

    public Boolean getPublisherIsKey() {
        return publisherIsKey;
    }
    public PublicationFilter setPublisherIsKey(Boolean publisherIsKey) {
        this.publisherIsKey = publisherIsKey;
        return this;
    }

    public String getExternalIdContains() {
        return externalIdContains;
    }
    public PublicationFilter setExternalIdContains(String externalIdContains) {
        this.externalIdContains = externalIdContains;
        return this;
    }

    public String getAccountManagerEmailContains() {
        return accountManagerEmailContains;
    }
    public PublicationFilter setAccountManagerEmailContains(String accountManagerEmailContains) {
        this.accountManagerEmailContains = accountManagerEmailContains;
        return this;
    }

    public String getPublicationTypeNameContains() {
        return publicationTypeNameContains;
    }
    public PublicationFilter setPublicationTypeNameContains(String publicationTypeNameContains) {
        this.publicationTypeNameContains = publicationTypeNameContains;
        return this;
    }
    
    public Boolean isAutoApproval() {
        return autoApproval;
    }
    public void setAutoApproval(Boolean autoApproval) {
        this.autoApproval = autoApproval;
    }
    
    // Will trigger and "equals" applied only to friendlyName
    public PublicationFilter setFriendlyName(String friendlyName, boolean friendlyNameCaseSensitive) {
        return this.setFriendlyName(friendlyName, null, friendlyNameCaseSensitive);
    }
    
    // Will trigger a "like" applied only to friendlyName
    public PublicationFilter setFriendlyName(String friendlyName, LikeSpec friendlyNameLikeSpec, boolean friendlyNameCaseSensitive) {
        return this.setFriendlyName(friendlyName, friendlyNameLikeSpec, friendlyNameCaseSensitive, false);
    }
    
    // If friendlyNameAppliesToName is true, will trigger a "like" applied friendlyName OR name
    public PublicationFilter setFriendlyName(String friendlyName, LikeSpec friendlyNameLikeSpec, boolean friendlyNameCaseSensitive, boolean friendlyNameAppliesToName) {
        this.friendlyName = friendlyName;
        this.friendlyNameCaseSensitive = friendlyNameCaseSensitive;
        this.friendlyNameLikeSpec = friendlyNameLikeSpec;
        this.friendlyNameAppliesToName = friendlyNameAppliesToName;
        return this;
    }
    public String getFriendlyName() {
        return friendlyName;
    }
    public LikeSpec getFriendlyNameLikeSpec() {
        return friendlyNameLikeSpec;
    }
    public boolean isFriendlyNameCaseSensitive() {
        return friendlyNameCaseSensitive;
    }
    public boolean isFriendlyNameAppliesToName() {
        return friendlyNameAppliesToName;
    }
    public boolean isRtbIdNotNull() {
        return rtbIdNotNull;
    }
    public PublicationFilter setRtbIdNotNull(boolean rtbIdNotNull) {
        this.rtbIdNotNull = rtbIdNotNull;
        return this;
    }
    public Date getGreaterThanOrEqualToApprovalDate() {
        return greaterThanOrEqualToApprovalDate;
    }
    public PublicationFilter setGreaterThanOrEqualToApprovalDate(Date date) {
        this.greaterThanOrEqualToApprovalDate = (date==null ? null : new Date(date.getTime()));
        return this;
    }
    
    public Collection<Publication.PublicationSafetyLevel> getSafetyLevels() {
        return this.safetyLevels;
    }
    public PublicationFilter setSafetyLevels(Publication.PublicationSafetyLevel ... safetyLevels) {
        Set<Publication.PublicationSafetyLevel> set = new LinkedHashSet<Publication.PublicationSafetyLevel>();
        for (Publication.PublicationSafetyLevel safetyLevel : safetyLevels) {
            set.add(safetyLevel);
        }
        this.safetyLevels = set;
        return this;
    }
    
}
