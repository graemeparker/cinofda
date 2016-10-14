package com.byyd.middleware.creative.filter;

import java.util.List;

import com.adfonic.domain.Creative;
import com.adfonic.domain.DisplayType;

public class AssetBundleFilter {

    private Creative creative;
    private List<DisplayType> includeDisplayTypes;
    private List<DisplayType> excludeDisplayTypes;
    public Creative getCreative() {
        return creative;
    }
    public AssetBundleFilter setCreative(Creative creative) {
        this.creative = creative;
        return this;
    }
    public List<DisplayType> getIncludeDisplayTypes() {
        return includeDisplayTypes;
    }
    public AssetBundleFilter setIncludeDisplayTypes(List<DisplayType> includeDisplayTypes) {
        this.includeDisplayTypes = includeDisplayTypes;
        return this;
    }
    public List<DisplayType> getExcludeDisplayTypes() {
        return excludeDisplayTypes;
    }
    public AssetBundleFilter setExcludeDisplayTypes(List<DisplayType> excludeDisplayTypes) {
        this.excludeDisplayTypes = excludeDisplayTypes;
        return this;
    }


}
