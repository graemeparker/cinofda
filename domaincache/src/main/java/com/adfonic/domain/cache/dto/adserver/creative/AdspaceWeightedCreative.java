package com.adfonic.domain.cache.dto.adserver.creative;

import java.io.Serializable;
import java.util.Arrays;

public class AdspaceWeightedCreative implements Comparable<AdspaceWeightedCreative>, Serializable {

    private static final long serialVersionUID = 2L;
    private int priority;
    private Long[] creativeIds;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Long[] getCreativeIds() {
        return creativeIds;
    }

    public void setCreativeIds(Long[] creativeIds) {
        this.creativeIds = creativeIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + priority;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdspaceWeightedCreative other = (AdspaceWeightedCreative) obj;
        if (priority != other.priority)
            return false;
        return true;
    }

    @Override
    public int compareTo(AdspaceWeightedCreative o) {
        return ((Integer) this.priority).compareTo(o.priority);
    }

    @Override
    public String toString() {
        return "AdspaceWeightedCreative {priority=" + priority + ", creatives=" + Arrays.asList(creativeIds) + "}";
    }
}
