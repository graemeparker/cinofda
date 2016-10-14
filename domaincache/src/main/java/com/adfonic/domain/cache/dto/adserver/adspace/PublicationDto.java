package com.adfonic.domain.cache.dto.adserver.adspace;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication.AdOpsStatus;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublicationDto extends BusinessKeyDto {
    private static final long serialVersionUID = 9L;

    private String externalID;
    private PublisherDto publisher;
    private String name;
    private Publication.Status status;
    private Long publicationTypeId;
    private Set<Long> languageIds = new HashSet<Long>();
    private TransparentNetworkDto transparentNetwork;
    private boolean installTrackingDisabled;
    private TrackingIdentifierType trackingIdentifierType;
    private Long adRequestTimeout;
    private Long defaultIntegrationTypeId;
    private String rtbId;
    private Map<BidType, RateCardDto> rateCardMap = new HashMap<BidType, RateCardDto>();
    private Long categoryId;
    private AdOpsStatus adOpsStatus;
    private RateCardDto ecpmTargetRateCard;
    private Integer samplingRate = 25;// Allowed values 0 - 255
    //adserver doesnt need approvedate its only for DomainSerializer so making it transient
    private transient Date approveDate;
    private boolean useSoftFloor;
    private String bundleName;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Publication.Status getStatus() {
        return status;
    }

    public void setStatus(Publication.Status status) {
        this.status = status;
    }

    public Long getPublicationTypeId() {
        return publicationTypeId;
    }

    public void setPublicationTypeId(Long publicationTypeId) {
        this.publicationTypeId = publicationTypeId;
    }

    public Set<Long> getLanguageIds() {
        return languageIds;
    }

    public TransparentNetworkDto getTransparentNetwork() {
        return transparentNetwork;
    }

    public void setTransparentNetwork(TransparentNetworkDto transparentNetwork) {
        this.transparentNetwork = transparentNetwork;
    }

    public boolean isInstallTrackingDisabled() {
        return installTrackingDisabled;
    }

    public void setInstallTrackingDisabled(boolean installTrackingDisabled) {
        this.installTrackingDisabled = installTrackingDisabled;
    }

    public TrackingIdentifierType getTrackingIdentifierType() {
        return trackingIdentifierType;
    }

    public void setTrackingIdentifierType(TrackingIdentifierType trackingIdentifierType) {
        this.trackingIdentifierType = trackingIdentifierType;
    }

    public Long getAdRequestTimeout() {
        return adRequestTimeout;
    }

    public void setAdRequestTimeout(Long adRequestTimeout) {
        this.adRequestTimeout = adRequestTimeout;
    }

    public Long getEffectiveAdRequestTimeout() {
        return adRequestTimeout == null ? publisher.getDefaultAdRequestTimeout() : adRequestTimeout;
    }

    public Long getDefaultIntegrationTypeId() {
        return defaultIntegrationTypeId;
    }

    public void setDefaultIntegrationTypeId(Long defaultIntegrationTypeId) {
        this.defaultIntegrationTypeId = defaultIntegrationTypeId;
    }

    public String getRtbId() {
        return rtbId;
    }

    public void setRtbId(String rtbId) {
        this.rtbId = rtbId;
    }

    public Map<BidType, RateCardDto> getRateCardMap() {
        return rateCardMap;
    }

    public RateCardDto getEffectiveRateCard(BidType bidType) {
        RateCardDto rateCard = rateCardMap.get(bidType);
        // Fall back on the Publisher's default RateCard map if the pub doesn't have it set.
        return rateCard != null ? rateCard : publisher.getDefaultRateCardMap().get(bidType);
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public AdOpsStatus getAdOpsStatus() {
        return adOpsStatus;
    }

    public void setAdOpsStatus(AdOpsStatus adOpsStatus) {
        this.adOpsStatus = adOpsStatus;
    }

    public RateCardDto getEcpmTargetRateCard() {
        return ecpmTargetRateCard;
    }

    public void setEcpmTargetRateCard(RateCardDto ecpmTargetRateCard) {
        this.ecpmTargetRateCard = ecpmTargetRateCard;
    }

    public Date getApproveDate() {
        return approveDate;
    }

    public void setApproveDate(Date approveDate) {
        this.approveDate = approveDate;
    }

    public Integer getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(Integer samplingRate) {
        this.samplingRate = samplingRate;
    }

    public boolean isUseSoftFloor() {
        return useSoftFloor;
    }

    public void setUseSoftFloor(boolean useSoftFloor) {
        this.useSoftFloor = useSoftFloor;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    @Override
    public String toString() {
        return "PublicationDto {" + getId() + ", externalID=" + externalID + ", publisher=" + publisher + ", name=" + name + ", status=" + status + ", publicationTypeId="
                + publicationTypeId + ", languageIds=" + languageIds + ", transparentNetwork=" + transparentNetwork + ", installTrackingDisabled=" + installTrackingDisabled
                + ", trackingIdentifierType=" + trackingIdentifierType + ", adRequestTimeout=" + adRequestTimeout + ", defaultIntegrationTypeId=" + defaultIntegrationTypeId
                + ", rtbId=" + rtbId + ", rateCardMap=" + rateCardMap + ", categoryId=" + categoryId + ", adOpsStatus=" + adOpsStatus + ", ecpmTargetRateCard="
                + ecpmTargetRateCard + ", samplingRate=" + samplingRate + ", useSoftFloor=" + useSoftFloor + ", bundleName=" + bundleName + "}";
    }

}
