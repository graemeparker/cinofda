package com.adfonic.presentation.publication.model;

import java.math.BigDecimal;
import java.util.List;

import com.adfonic.dto.publication.enums.AdOpsStatus;
import com.adfonic.dto.publication.enums.PublicationSafetyLevel;
import com.adfonic.presentation.NameIdModel;

public class PublicationApprovalDetailModel extends PublicationApprovalModel {

    private static final long serialVersionUID = 1L;
    
    private String company;
    private String url;
    private BigDecimal revenueShare;
    private Boolean discloseIdentity;
    private PublicationSafetyLevel safetyLevel;
    private String statedCategory;
    private Boolean softFloor;
    private String samplingRate;
    
    private List<NameIdModel> excludedCategories;
    
    // Approval inputs    
    private AdOpsStatus adOpsStatus;
    private String comment;
    private List<Long> watchers;
    private Boolean notifyWatchers;
    private Boolean notifyPublisher;
    private Boolean learningsStatus;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigDecimal getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(BigDecimal revenueShare) {
        this.revenueShare = revenueShare;
    }

    public Boolean getDiscloseIdentity() {
        return discloseIdentity;
    }

    public void setDiscloseIdentity(Boolean discloseIdentity) {
        this.discloseIdentity = discloseIdentity;
    }

    public PublicationSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(PublicationSafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    public String getStatedCategory() {
        return statedCategory;
    }

    public void setStatedCategory(String statedCategory) {
        this.statedCategory = statedCategory;
    }

    public Boolean getSoftFloor() {
        return softFloor;
    }

    public void setSoftFloor(Boolean softFloor) {
        this.softFloor = softFloor;
    }

    public String getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(String samplingRate) {
        this.samplingRate = samplingRate;
    }
    
    public List<NameIdModel> getExcludedCategories() {
        return excludedCategories;
    }

    public void setExcludedCategories(List<NameIdModel> excludedCategories) {
        this.excludedCategories = excludedCategories;
    }

    public AdOpsStatus getAdOpsStatus() {
        return adOpsStatus;
    }

    public void setAdOpsStatus(AdOpsStatus adOpsStatus) {
        this.adOpsStatus = adOpsStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Long> getWatchers() {
        return watchers;
    }

    public void setWatchers(List<Long> watchers) {
        this.watchers = watchers;
    }

    public Boolean getNotifyWatchers() {
        return notifyWatchers;
    }

    public void setNotifyWatchers(Boolean notifyWatchers) {
        this.notifyWatchers = notifyWatchers;
    }

    public Boolean getNotifyPublisher() {
        return notifyPublisher;
    }

    public void setNotifyPublisher(Boolean notifyPublisher) {
        this.notifyPublisher = notifyPublisher;
    }

    public Boolean getLearningsStatus() {
        return learningsStatus;
    }

    public void setLearningsStatus(Boolean learningsStatus) {
        this.learningsStatus = learningsStatus;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationApprovalDetailModel [company=").append(company).append(", url=").append(url).append(", revenueShare=").append(revenueShare)
                .append(", discloseIdentity=").append(discloseIdentity).append(", safetyLevel=").append(safetyLevel).append(", statedCategory=").append(statedCategory)
                .append(", softFloor=").append(softFloor).append(", samplingRate=").append(samplingRate).append(", excludedCategories=").append(excludedCategories)
                .append(", adOpsStatus=").append(adOpsStatus).append(", comment=").append(comment).append(", watchers=").append(watchers).append(", notifyWatchers=")
                .append(notifyWatchers).append(", notifyPublisher=").append(notifyPublisher).append(", learningsStatus=").append(learningsStatus).append("]");
        return builder.append("\n").append(super.toString()).toString();
    }

}
