package com.byyd.middleware.campaign.filter;

import com.byyd.middleware.iface.dao.LikeSpec;

public class ChannelFilter {

    private String name;
    private LikeSpec likeSpec;
    private boolean caseSensitive = false;
    private Boolean excludeUncategorized;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Boolean getExcludeUncategorized() {
        return excludeUncategorized;
    }
    public ChannelFilter setExcludeUncategorized(Boolean excludeUncategorized) {
        this.excludeUncategorized = excludeUncategorized;
        return this;
    }
    public LikeSpec getLikeSpec() {
        return likeSpec;
    }
    public ChannelFilter setLikeSpec(LikeSpec likeSpec) {
        this.likeSpec = likeSpec;
        return this;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public ChannelFilter setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }


}
