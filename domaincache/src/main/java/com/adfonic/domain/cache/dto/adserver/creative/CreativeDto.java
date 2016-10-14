package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.domain.cache.dto.adserver.ContentTypeDto;

public class CreativeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 10L;

    private String externalID;
    private String name;
    private CampaignDto campaign;
    private SegmentDto segment;
    private Long formatId;
    private Set<Integer> creativeAttributes; // open-rtb-2 creative-attribute-ids
    private Map<Long, Map<Long, AssetDto>> assetsByDisplayTypeIdAndComponentId = new HashMap<Long, Map<Long, AssetDto>>();
    private Map<Long, Map<Long, AssetDto>> assetsByDisplayTypeIdAndContentTypeId = new HashMap<Long, Map<Long, AssetDto>>();
    private DestinationDto destination;
    private Long languageId;
    private boolean pluginBased;
    private int priority;
    private Date endDate;
    private Long extendedCreativeTypeId;
    private Map<String, String> extendedData = new HashMap<String, String>();
    private Creative.Status status;
    // This is a shortcut for determining if any of the creative's assets
    // are of an animated ContentType
    private boolean animated = false;
    private Map<ContentForm, String> extendedCreativeTemplates = new HashMap<>();
    private Map<Long, String> externalCreativeReferenceByPublisherId;

    private boolean closedMode;
    private boolean allowExternalAudit;
    private boolean sslCompliant;
    private Date creationDate;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CampaignDto getCampaign() {
        return campaign;
    }

    public void setCampaign(CampaignDto campaign) {
        this.campaign = campaign;
    }

    public SegmentDto getSegment() {
        return segment;
    }

    public void setSegment(SegmentDto segment) {
        this.segment = segment;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean hasAssets(Long displayTypeId) {
        return assetsByDisplayTypeIdAndComponentId.containsKey(displayTypeId);
    }

    public boolean hasAssets(Long displayTypeId, List<String> mimeTypes, Boolean isAnimated, DomainCache domainCache) {
        Map<Long, AssetDto> assetsByContentTypeId = assetsByDisplayTypeIdAndContentTypeId.get(displayTypeId);

        return assetsByContentTypeId != null
                && (mimeTypes == null || CollectionUtils.containsAny(assetsByContentTypeId.keySet(), getContentTypes(mimeTypes, isAnimated, domainCache)));
    }

    private Set<Long> getContentTypes(List<String> mimeTypes, Boolean isAnimated, DomainCache domainCache) {
        Set<Long> contentTypes = new HashSet<Long>();
        for (String mimeType : mimeTypes) {
            if (BooleanUtils.isNotFalse(isAnimated)) {
                contentTypes.add(ContentTypeDto.getContentTypeId(mimeType, true, domainCache));
            }

            if (BooleanUtils.isNotTrue(isAnimated)) {
                contentTypes.add(ContentTypeDto.getContentTypeId(mimeType, false, domainCache));
            }
        }
        //Won't harm to leave nulls in. contentTypes.remove(null);
        return contentTypes;
    }

    public AssetDto getAsset(Long displayTypeId, Long componentId) {
        Map<Long, AssetDto> assetsByComponentId = assetsByDisplayTypeIdAndComponentId.get(displayTypeId);
        if (assetsByComponentId == null) {
            return null;
        } else {
            return assetsByComponentId.get(componentId);
        }
    }

    public void setAsset(Long displayTypeId, Long componentId, AssetDto asset, Long contentTypeId) {
        Map<Long, AssetDto> assetsByContentTypeId, assetsByComponentId = assetsByDisplayTypeIdAndComponentId.get(displayTypeId);
        if (assetsByComponentId == null) {
            assetsByComponentId = new HashMap<Long, AssetDto>();
            assetsByDisplayTypeIdAndComponentId.put(displayTypeId, assetsByComponentId);
            assetsByDisplayTypeIdAndContentTypeId.put(displayTypeId, assetsByContentTypeId = new HashMap<Long, AssetDto>());
        } else {
            assetsByContentTypeId = assetsByDisplayTypeIdAndContentTypeId.get(displayTypeId);
        }
        assetsByContentTypeId.put(contentTypeId, asset);
        assetsByComponentId.put(componentId, asset);
    }

    public DestinationDto getDestination() {
        return destination;
    }

    public void setDestination(DestinationDto destination) {
        this.destination = destination;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public boolean isPluginBased() {
        return pluginBased;
    }

    public void setPluginBased(boolean pluginBased) {
        this.pluginBased = pluginBased;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getExtendedCreativeTypeId() {
        return extendedCreativeTypeId;
    }

    public void setExtendedCreativeTypeId(Long extendedCreativeTypeId) {
        this.extendedCreativeTypeId = extendedCreativeTypeId;
    }

    public Map<String, String> getExtendedData() {
        return extendedData;
    }

    public Creative.Status getStatus() {
        return status;
    }

    public void setStatus(Creative.Status status) {
        this.status = status;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public Map<ContentForm, String> getExtendedCreativeTemplates() {
        return extendedCreativeTemplates;
    }

    public boolean hasDynamicExtendedTemplate() {
        return !extendedCreativeTemplates.isEmpty();
    }

    public boolean isClosedMode() {
        return closedMode;
    }

    public void setClosedMode(boolean closedMode) {
        this.closedMode = closedMode;
    }

    public boolean isAllowExternalAudit() {
        return allowExternalAudit;
    }

    public void setAllowExternalAudit(boolean allowExternalAudit) {
        this.allowExternalAudit = allowExternalAudit;
    }

    public Set<Integer> getCreativeAttributes() {
        return creativeAttributes == null ? Collections.<Integer> emptySet() : creativeAttributes;
    }

    public void addCreativeAttribute(int creativeAttributeId) {
        if (this.creativeAttributes == null) {
            this.creativeAttributes = new HashSet<>();
        }

        this.creativeAttributes.add(creativeAttributeId);
    }

    public String getExternalCreativeReferenceByPublisherId(Long publisherId) {
        return externalCreativeReferenceByPublisherId == null ? null : externalCreativeReferenceByPublisherId.get(publisherId);
    }

    public void addExternalCreativeReferenceForPublisher(Long publisherId, String externalReference) {
        if (externalCreativeReferenceByPublisherId == null) {
            externalCreativeReferenceByPublisherId = new HashMap<>();
        }

        externalCreativeReferenceByPublisherId.put(publisherId, externalReference);
    }

    public boolean isSslCompliant() {
        return sslCompliant;
    }

    public void setSslCompliant(boolean sslCompliant) {
        this.sslCompliant = sslCompliant;
    }

    @Override
    public String toString() {
        return "CreativeDto:" + getId();
    }

}
