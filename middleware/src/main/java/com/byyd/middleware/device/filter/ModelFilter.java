package com.byyd.middleware.device.filter;

import java.util.List;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Platform;
import com.byyd.middleware.iface.dao.LikeSpec;

public class ModelFilter {

    private String name = null;
    private LikeSpec likeSpec = null;
    private boolean caseSensitive = false;
    private boolean prependVendorName = false;
    private Boolean deleted = null;
    private Boolean hidden = null;
    private List<Platform> platforms = null;
    private DeviceGroup deviceGroup = null;
    private boolean justVendorName = false;

    public ModelFilter() {
    }
    
    public ModelFilter(Boolean deleted, Boolean hidden) {
        this.deleted = deleted;
        this.hidden = hidden;
    }

    public ModelFilter(String name, boolean caseSensitive, Boolean deleted, Boolean hidden) {
        this.name = name;
        this.deleted = deleted;
        this.caseSensitive = caseSensitive;
        this.hidden = hidden;
    }
    
    public ModelFilter(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName) {
        this.name = name;
        this.likeSpec = like;
        this.deleted = deleted;
        this.caseSensitive = caseSensitive;
        this.prependVendorName = prependVendorName;
        this.hidden = hidden;
    }
    
    public ModelFilter(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms) {
        this.name = name;
        this.likeSpec = like;
        this.caseSensitive = caseSensitive;
        this.deleted = deleted;
        this.prependVendorName = prependVendorName;
        this.platforms = platforms;
        this.hidden = hidden;
    }
    
    public ModelFilter(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup) {
        this.name = name;
        this.likeSpec = like;
        this.caseSensitive = caseSensitive;
        this.deleted = deleted;
        this.prependVendorName = prependVendorName;
        this.platforms = platforms;
        this.hidden = hidden;
        this.deviceGroup = deviceGroup;
    }
    
    public ModelFilter(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, boolean justVendorName) {
        this.name = name;
        this.likeSpec = like;
        this.caseSensitive = caseSensitive;
        this.deleted = deleted;
        this.prependVendorName = prependVendorName;
        this.platforms = platforms;
        this.hidden = hidden;
        this.deviceGroup = deviceGroup;
        this.justVendorName = justVendorName;
    }
    
    public String getName() {
        return name;
    }
    public ModelFilter setName(String name) {
        this.name = name;
        return this;
    }
    public LikeSpec getLikeSpec() {
        return likeSpec;
    }
    public ModelFilter setLikeSpec(LikeSpec likeSpec) {
        this.likeSpec = likeSpec;
        return this;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public ModelFilter setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    public ModelFilter setName(String name, LikeSpec likeSpec, boolean caseSensitive) {
        this.name = name;
        this.likeSpec = likeSpec;
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    public boolean isPrependVendorName() {
        return prependVendorName;
    }
    public ModelFilter setPrependVendorName(boolean prependVendorName) {
        this.prependVendorName = prependVendorName;
        return this;
    }
    public Boolean getDeleted() {
        return deleted;
    }
    public ModelFilter setDeleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }
    public List<Platform> getPlatforms() {
        return platforms;
    }
    public ModelFilter setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
        return this;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public ModelFilter setHidden(Boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public DeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public ModelFilter setDeviceGroup(DeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

    public boolean isJustVendorName() {
        return justVendorName;
    }

    public void setJustVendorName(boolean justVendorName) {
        this.justVendorName = justVendorName;
    }

}
