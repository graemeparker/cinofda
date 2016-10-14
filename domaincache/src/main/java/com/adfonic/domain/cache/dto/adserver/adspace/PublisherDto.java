package com.adfonic.domain.cache.dto.adserver.adspace;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.BidType;
import com.adfonic.domain.PendingAdType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublisherDto extends BusinessKeyDto {
    private static final long serialVersionUID = 7L;

    private CompanyDto company;
    private PendingAdType pendingAdType;
    private Map<Long, Long> defaultIntegrationTypeIdsByPublicationTypeId = new HashMap<Long, Long>();
    private Long defaultAdRequestTimeout;
    private Map<BidType, RateCardDto> defaultRateCardMap = new HashMap<BidType, RateCardDto>();
    private double currentRevShare;
    private RtbConfigDto rtbConfig;
    private String externalId;
    private RateCardDto ecpmTargetRateCard;
    private double buyerPremium;
    private boolean requiresRealDestination;
    private Long operatingPublisherId;

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }

    public PendingAdType getPendingAdType() {
        return pendingAdType;
    }

    public void setPendingAdType(PendingAdType pendingAdType) {
        this.pendingAdType = pendingAdType;
    }

    public Map<Long, Long> getDefaultIntegrationTypeIdsByPublicationTypeId() {
        return defaultIntegrationTypeIdsByPublicationTypeId;
    }

    public Long getDefaultIntegrationTypeId(Long publicationTypeId) {
        return defaultIntegrationTypeIdsByPublicationTypeId.get(publicationTypeId);
    }

    public Long getDefaultAdRequestTimeout() {
        return defaultAdRequestTimeout;
    }

    public void setDefaultAdRequestTimeout(Long defaultAdRequestTimeout) {
        this.defaultAdRequestTimeout = defaultAdRequestTimeout;
    }

    public Map<BidType, RateCardDto> getDefaultRateCardMap() {
        return defaultRateCardMap;
    }

    public double getCurrentRevShare() {
        return currentRevShare;
    }

    public void setCurrentRevShare(double currentRevShare) {
        this.currentRevShare = currentRevShare;
    }

    public RtbConfigDto getRtbConfig() {
        return rtbConfig;
    }

    public void setRtbConfig(RtbConfigDto rtbConfig) {
        this.rtbConfig = rtbConfig;
    }

    public boolean isRtbEnabled() {
        return rtbConfig != null;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public RateCardDto getEcpmTargetRateCard() {
        return ecpmTargetRateCard;
    }

    public void setEcpmTargetRateCard(RateCardDto ecpmTargetRateCard) {
        this.ecpmTargetRateCard = ecpmTargetRateCard;
    }

    public double getBuyerPremium() {
        return buyerPremium;
    }

    public void setBuyerPremium(double buyerPremium) {
        this.buyerPremium = buyerPremium;
    }

    public boolean doesRequireRealDestination() {
        return requiresRealDestination;
    }

    public void setRequiresRealDestination(boolean requiresRealDestination) {
        this.requiresRealDestination = requiresRealDestination;
    }

    public long getOperatingPublisherId() {
        return operatingPublisherId == null ? getId() : operatingPublisherId;
    }

    public void setOperatingPublisherId(long operatingPublisherId) {
        this.operatingPublisherId = operatingPublisherId;
    }

    @Override
    public String toString() {
        return "PublisherDto {" + getId() + ", company=" + company + ", pendingAdType=" + pendingAdType + ", defaultIntegrationTypeIdsByPublicationTypeId="
                + defaultIntegrationTypeIdsByPublicationTypeId + ", defaultAdRequestTimeout=" + defaultAdRequestTimeout + ", defaultRateCardMap=" + defaultRateCardMap
                + ", currentRevShare=" + currentRevShare + ", rtbConfig=" + rtbConfig + ", externalId=" + externalId + ", ecpmTargetRateCard=" + ecpmTargetRateCard
                + ", buyerPremium=" + buyerPremium + ", requiresRealDestination=" + requiresRealDestination + ", operatingPublisherId=" + operatingPublisherId + "}";
    }

}
