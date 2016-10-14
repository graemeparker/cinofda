package com.byyd.middleware.audience.filter;

import com.adfonic.domain.DMPAttribute;
import com.adfonic.domain.DMPAudience;
import com.byyd.middleware.iface.dao.LikeSpec;

public class DMPSelectorFilter {

    private DMPAttribute dmpAttribute;
    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;
    private Boolean hidden;
    private Long muidSegmentId;
    private DMPAudience dmpAudience;
    private String externalId;
    private LikeSpec externalIdLikeSpec;
    private boolean externalIdCaseSensitive;
    private Long dmpVendorId;

    public DMPSelectorFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }

    public DMPSelectorFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
        this.name = name;
        this.nameLikeSpec = nameLikeSpec;
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public DMPSelectorFilter setExternalId(String externalId, boolean externalIdCaseSensitive) {
        return this.setExternalId(externalId, null, externalIdCaseSensitive);
    }

    public DMPSelectorFilter setExternalId(String externalId, LikeSpec externalIdLikeSpec, boolean externalIdCaseSensitive) {
        this.externalId = externalId;
        this.externalIdLikeSpec = externalIdLikeSpec;
        this.externalIdCaseSensitive = externalIdCaseSensitive;
        return this;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public LikeSpec getExternalIdLikeSpec() {
        return this.externalIdLikeSpec;
    }

    public boolean isExternalIdCaseSensitive() {
        return this.externalIdCaseSensitive;
    }

    public DMPAttribute getDMPAttribute() {
        return dmpAttribute;
    }

    public DMPSelectorFilter setDMPAttribute(DMPAttribute dmpAttribute) {
        this.dmpAttribute = dmpAttribute;
        return this;
    }

    public Long getDmpVendorId() {
        return dmpVendorId;
    }

    public DMPSelectorFilter setDmpVendorId(Long dmpVendorId) {
        this.dmpVendorId = dmpVendorId;
        return this;
    }

    public String getName() {
        return name;
    }

    public LikeSpec getNameLikeSpec() {
        return nameLikeSpec;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Long getMuidSegmentId() {
        return muidSegmentId;
    }

    public void setMuidSegmentId(Long muidSegmentId) {
        this.muidSegmentId = muidSegmentId;
    }

    public DMPAudience getDMPAudience() {
        return dmpAudience;
    }

    public void setDMPAudience(DMPAudience dmpAudience) {
        this.dmpAudience = dmpAudience;
    }

}
