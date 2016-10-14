package com.adfonic.domainserializer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class ShardTest {

    @Test
    public void createNewShard() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("AdserverDomainCache.shard.myshard.jmsTopic", "topic");
        properties.setProperty("AdserverDomainCache.shard.myshard.rtb.enabled", "true");
        properties.setProperty("AdserverDomainCache.shard.myshard.clusters", "c1,c2");
        DsShard shard = new DsShard("myshard", properties);

        assertThat(shard.getName(), is("myshard"));
        assertThat(shard.getShardMode(), is(AdserverDomainCache.ShardMode.exclude));
        assertThat(shard.isRtbEnabled(), is(true));

    }
}
