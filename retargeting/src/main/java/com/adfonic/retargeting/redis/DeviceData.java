package com.adfonic.retargeting.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.Instant;

import com.adfonic.dmp.cache.OptOutType;

public class DeviceData {

    private OptOutType optOutType = OptOutType.noOptout;
    private Set<Long> audienceIds = new HashSet<>();
    private Map<Long, Instant> recencyByAudience = new HashMap<Long, Instant>();

    public DeviceData() {
    }

    public DeviceData(DeviceData dd) {
        this.optOutType = dd.optOutType;
        this.audienceIds = new HashSet<>(dd.audienceIds);
        this.recencyByAudience = new HashMap<>(dd.recencyByAudience);
    }

    public OptOutType getOptOutType() {
        return optOutType;
    }

    public void setOptOutType(OptOutType optOutType) {
        this.optOutType = optOutType;
    }

    public Set<Long> getAudienceIds() {
        return audienceIds;
    }

    public void setAudienceIds(Set<Long> audienceIds) {
        this.audienceIds = audienceIds;
    }

    public Map<Long, Instant> getRecencyByAudience() {
        return recencyByAudience;
    }

    public void setRecencyByAudience(Map<Long, Instant> recencyByAudience) {
        this.recencyByAudience = recencyByAudience;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((audienceIds == null) ? 0 : audienceIds.hashCode());
        result = prime * result + ((optOutType == null) ? 0 : optOutType.hashCode());
        result = prime * result + ((recencyByAudience == null) ? 0 : recencyByAudience.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceData other = (DeviceData) obj;
        if (audienceIds == null) {
            if (other.audienceIds != null) {
                return false;
            }
        } else if (!audienceIds.equals(other.audienceIds)) {
            return false;
        }
        if (optOutType != other.optOutType) {
            return false;
        }
        if (recencyByAudience == null) {
            if (other.recencyByAudience != null) {
                return false;
            }
        } else if (!recencyByAudience.equals(other.recencyByAudience)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DeviceData {optOutType=" + optOutType + ", audienceIds=" + audienceIds + ", recencyByAudience=" + recencyByAudience + "}";
    }

}
