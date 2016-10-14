package com.adfonic.datacollector.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import kafka.consumer.ConsumerConfig;
import net.byyd.archive.model.v1.AdAction;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.KryoManager;
import com.adfonic.datacollector.BatchManager;
import com.adfonic.datacollector.kafka.KafkaTopicStream;
import com.adfonic.datacollector.kafka.KafkaTopics;
import com.adfonic.domain.cache.DataCollectorDomainCacheManager;
import com.adfonic.jms.JmsUtils;
import com.adfonic.tracker.jdbc.TrackerMultiServiceJdbcImpl;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.TimeZoneUtils;
import com.adfonic.util.stats.CounterJmxManager;
import com.adfonic.util.stats.CounterManager;

/**
 * @author mvanek
 * 
 * Replacememnt for adfonic-datacollector-context.xml
 *
 */
@Configuration
@EnableAutoConfiguration(exclude = { JmsAutoConfiguration.class, DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })
@ComponentScan("com.adfonic.datacollector")
@ImportResource(value = { "adfonic-datacollector-db-context.xml", "file:${adfonic.config.home:/usr/local/adfonic/config}/adfonic-datacollector-routes.xml",
        "adfonic-datacollector-kafka-context.xml" })
public class DcSpringBootConfig {

    static {
        TimeZoneUtils.getTimeZoneNonBlocking("Europe/London");
    }

    @Autowired
    BatchManager batchManager;

    @Scheduled(fixedRateString = "${BatchManager.batchDurationMs}")
    public void batchManager() {
        batchManager.flushBatches();
    }

    @Bean
    public DataCollectorDomainCacheManager dataCollectorDomainCacheManager(@Value("${adfonic.cache.home:/usr/local/adfonic/cache}") File cacheHome,
            @Value("${DataCollectorDomainCache.label}") String cacheLabel, @Value("${DataCollectorDomainCache.useMemory}") Boolean useMemory) {
        return new DataCollectorDomainCacheManager(cacheHome, cacheLabel, useMemory);
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(new ClassPathResource("ehcache-datacollector.xml"));
        return bean;
    }

    @Bean(name = "campaignStoppageCache")
    public Ehcache campaignStoppageCache(CacheManager ehCacheManager) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManager);
        bean.setCacheName("campaignStoppage");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean(name = "advertiserStoppageCache")
    public Ehcache advertiserStoppageCache(CacheManager ehCacheManager) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManager);
        bean.setCacheName("advertiserStoppage");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean(name = "missingCampaignsCache")
    public Ehcache missingCampaignsCache(CacheManager ehCacheManager) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManager);
        bean.setCacheName("missingCampaigns");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean(name = "uaHeaderIdCache")
    public Ehcache uaHeaderIdCache(CacheManager ehCacheManager) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManager);
        bean.setCacheName("uaHeaderId");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public com.adfonic.tracker.jdbc.TrackerMultiServiceJdbcImpl trackerMultiServiceJdbcImpl(@Qualifier(ConfUtils.TRACKER_DS) DataSource dataSource,
            @Qualifier("uaHeaderIdCache") Ehcache uaHeaderIdCache) {
        TrackerMultiServiceJdbcImpl bean = new TrackerMultiServiceJdbcImpl(dataSource);
        bean.setUaHeaderIdCache(uaHeaderIdCache);
        return bean;
    }

    @Bean
    public KryoManager kryoManager() {
        return new KryoManager();
    }

    @Bean
    public AdEventFactory adEventFactory(KryoManager kryoManager) {
        return new AdEventFactory(kryoManager);
    }

    @Bean
    public JmsUtils jmsUtils() {
        return new JmsUtils();
    }

    @Bean
    CounterManager counterManager() {
        return new CounterManager();
    }

    @Bean(name = "AdfonicCounters:mbean=GenericCounters")
    public CounterJmxManager counterJmxManager(CounterManager counterManager) {
        return new CounterJmxManager(counterManager);
    }
    
    
    @Bean
    public KafkaTopics kafkaTopics(@Value("${kafka.topic.streams.low:1}") int streamsLow, @Value("${kafka.topic.streams.shard1.adserved:1}") int streamsAdservedShard1, @Value("${kafka.topic.streams.shard2.adserved:1}") int streamsAdservedShard2,
            @Value("${kafka.topic.streams.shard1.impression:1}") int streamsImpressionShard1, @Value("${kafka.topic.streams.shard1.impression:1}") int streamsImpressionShard2, @Value("${kafka.topic.streams.shard1.click:1}") int streamsClickShard1,
            @Value("${kafka.topic.streams.shard2.click:1}") int streamsClickShard2, @Value("${kafka.topic.streams.shard1.rtblost:3}") int streamsRtblostShard1, @Value("${kafka.topic.streams.shard2.rtblost:2}") int streamsRtblostShard2,
            @Value("${kafka.topic.streams.shard1.unfilled}") int streamsUnfilledShard1, @Value("${kafka.topic.streams.shard2.unfilled}") int streamsUnfilledShard2,
            @Value("${kafka.topic.prefix}") String topicPrefix, @Value("${kafka.topic.postfix}") String topicPosfix, @Value("${kafka.environment}") String environment,
            @Value("${kafka.topic.shard1}") String shard1, @Value("${kafka.topic.shard2}") String shard2, @Value("${kafka.topic.failed}") String failedTopic,
            @Value("${kafka.topic.prefix.sampled}") String sampledPrefix) {
        KafkaTopics kafkaTopics = new KafkaTopics();
        List<KafkaTopicStream> topics = new ArrayList<KafkaTopicStream>();
        //First shard related topics
        //AD_SERVED Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.AD_SERVED.toString() + "_" + shard1 + "_" + environment + "_" + topicPosfix, streamsAdservedShard1));
        if (!StringUtils.isEmpty(shard2)) {
            topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.AD_SERVED.toString() + "_" + shard2 + "_" + environment + "_" + topicPosfix, streamsAdservedShard2));
        }
        //IMPRESSION Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.IMPRESSION.toString() + "_" + shard1 + "_" + environment + "_" + topicPosfix, streamsImpressionShard1));
        if (!StringUtils.isEmpty(shard2)) {
            topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.IMPRESSION.toString() + "_" + shard2 + "_" + environment + "_" + topicPosfix, streamsImpressionShard2));
        }
        //CLICK Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.CLICK.toString() + "_" + shard1 + "_" + environment + "_" + topicPosfix, streamsClickShard1));
        if (!StringUtils.isEmpty(shard2)) {
            topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.CLICK.toString() + "_" + shard2 + "_" + environment + "_" + topicPosfix, streamsClickShard2));
        }
        //RTB_LOST Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.RTB_LOST.toString() + "_" + shard1 + "_" + environment + "_" + topicPosfix, streamsRtblostShard1));
        if (!StringUtils.isEmpty(shard2)) {
            topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.RTB_LOST.toString() + "_" + shard2 + "_" + environment + "_" + topicPosfix, streamsRtblostShard2));
        }
        //Common topics (now shard related)
        //SAMPLED UNFILLED Events
        topics.add(new KafkaTopicStream(sampledPrefix + "." + AdAction.UNFILLED_REQUEST.toString() + "_" + shard1 + "_" + environment + "_" + topicPosfix, streamsUnfilledShard1));
        if (!StringUtils.isEmpty(shard2)) {
            topics.add(new KafkaTopicStream(sampledPrefix + "." + AdAction.UNFILLED_REQUEST.toString() + "_" + shard2 + "_" + environment + "_" + topicPosfix, streamsUnfilledShard2));
        }
        
        //CONVERSION Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.CONVERSION.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //INSTALL Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.INSTALL.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //COMPLETED VIEW Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.COMPLETED_VIEW.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //VIEW Q1 Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.VIEW_Q1.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //VIEW Q2 Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.VIEW_Q2.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //VIEW Q3 Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.VIEW_Q3.toString() + "_" + environment + "_" + topicPosfix, streamsLow));
        //VIEW Q4 Events
        topics.add(new KafkaTopicStream(topicPrefix + "." + AdAction.VIEW_Q4.toString() + "_" + environment + "_" + topicPosfix, streamsLow)); 
        

        //Failed topics
        topics.add(new KafkaTopicStream(failedTopic, streamsLow));
        kafkaTopics.setTopics(topics);
        return kafkaTopics;
    }

    @Bean
    public ConsumerConfig consumerConfig(@Value("${kafka.zk.connection}") String zookeeper, @Value("${kafka.zk.groupid}") String groupId,
            @Value("${kafka.zk.connection.timeout}") String zkSessionTimeout, @Value("${kafka.zk.sync.time}") String zkSyncTime,
            @Value("${kafka.auto.offset.reset}") String autoOffsetReset, @Value("${kafka.socket.receive.buffer.bytes}") String receiveBufferBytes,
            @Value("${kafka.fetch.message.max.bytes}") String fetchMssgMax, @Value("${kafka.auto.commit.interval.ms}") String autoCommitInterval) {

        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", zkSessionTimeout);
        props.put("zookeeper.sync.time.ms", zkSyncTime);
        props.put("auto.offset.reset", autoOffsetReset);
        props.put("socket.receive.buffer.bytes", receiveBufferBytes);
        props.put("fetch.message.max.bytes", fetchMssgMax);
        props.put("auto.commit.interval.ms", autoCommitInterval);

        return new ConsumerConfig(props);
    }

}
