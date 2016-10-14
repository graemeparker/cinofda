package com.adfonic.domainserializer;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.cache.ext.AdserverDomainCache.ShardMode;

public final class DsShard {
    private final String name;
    private final Set<Long> publisherIds = new HashSet<Long>();
    private final ShardMode shardMode;
    private final boolean rtbEnabled;

    public DsShard(String name, ShardMode shardMode, Set<Long> publisherIds, boolean rtbEnabled) {
        this.name = name;
        this.shardMode = shardMode;
        this.rtbEnabled = rtbEnabled;
        this.publisherIds.addAll(publisherIds);
    }

    public DsShard(String name, Properties properties) {
        this.name = name;
        String prefix = "AdserverDomainCache.shard." + name;
        shardMode = ShardMode.valueOf(properties.getProperty(prefix + ".mode", "exclude"));
        String publisherIdsProperty = properties.getProperty(prefix + ".publisherIds");
        if (StringUtils.isBlank(publisherIdsProperty) && ShardMode.include.equals(shardMode)) {
            throw new IllegalArgumentException("You must specify " + prefix + ".publisherIds when ShardMode is " + shardMode);
        }
        if (!StringUtils.isBlank(publisherIdsProperty)) {
            for (String id : StringUtils.split(publisherIdsProperty, ',')) {
                publisherIds.add(Long.valueOf(id));
            }
        }
        rtbEnabled = BooleanUtils.toBoolean(properties.getProperty(prefix + ".rtb.enabled", "false"));
    }

    /**
     * Copy constructor with little twist to pass shardMode.
     * As shardMode is final and we can not have setter for it.So passing it in copy constructor. Used in JMX invocation code
     * @param copyShard
     * @param shardMode
     */
    public DsShard(DsShard copyShard, ShardMode shardMode) {
        super();
        this.name = copyShard.name;
        if (shardMode == null) {
            this.shardMode = copyShard.shardMode;
        } else {
            this.shardMode = shardMode;
        }

        this.rtbEnabled = copyShard.rtbEnabled;
        this.publisherIds.addAll(copyShard.publisherIds);
    }

    public String getName() {
        return name;
    }

    public Set<Long> getPublisherIds() {
        return publisherIds;
    }

    public ShardMode getShardMode() {
        return shardMode;
    }

    public boolean isRtbEnabled() {
        return rtbEnabled;
    }

    @Override
    public String toString() {
        return "Shard {name=" + name + ", publisherIds=" + publisherIds + ", shardMode=" + shardMode + ", rtbEnabled=" + rtbEnabled + "}";
    }
}
