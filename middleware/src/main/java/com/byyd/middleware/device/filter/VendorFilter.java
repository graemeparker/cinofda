package com.byyd.middleware.device.filter;

import java.util.List;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Platform;
import com.byyd.middleware.iface.dao.LikeSpec;

public class VendorFilter {

    private String name = null;
    private LikeSpec likeSpec = null;
    private boolean caseSensitive = false;
    private List<Platform> platforms = null;
    private DeviceGroup deviceGroup = null;

    public VendorFilter() {
    }

    public VendorFilter(String name, LikeSpec like, boolean caseSensitive, List<Platform> platforms, DeviceGroup deviceGroup) {
        this.name = name;
        this.likeSpec = like;
        this.caseSensitive = caseSensitive;
        this.platforms = platforms;
        this.deviceGroup = deviceGroup;
    }

    public String getName() {
        return name;
    }

    public VendorFilter setName(String name) {
        this.name = name;
        return this;
    }

    public LikeSpec getLikeSpec() {
        return likeSpec;
    }

    public VendorFilter setLikeSpec(LikeSpec likeSpec) {
        this.likeSpec = likeSpec;
        return this;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public VendorFilter setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public VendorFilter setName(String name, LikeSpec likeSpec, boolean caseSensitive) {
        this.name = name;
        this.likeSpec = likeSpec;
        this.caseSensitive = caseSensitive;
        return this;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public VendorFilter setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
        return this;
    }

    public DeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public VendorFilter setDeviceGroup(DeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
        return this;
    }

}
