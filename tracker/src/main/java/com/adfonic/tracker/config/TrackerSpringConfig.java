package com.adfonic.tracker.config;

import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.KryoManager;
import com.adfonic.tracker.VideoViewAdEventLogic;
import com.adfonic.tracker.jdbc.TrackerMultiServiceJdbcImpl;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.TimeZoneUtils;
import com.byyd.middleware.iface.dao.FetchStrategyFactory;
import com.byyd.middleware.iface.service.NotAutoScan;

@Configuration
@EnableMBeanExport
@Import({ TrackerDatabaseConfig.class, TrackerKafkaConfig.class })
@ComponentScan(basePackages = "com.byyd.middleware", excludeFilters = @ComponentScan.Filter(value = NotAutoScan.class, type = FilterType.ANNOTATION))
public class TrackerSpringConfig {

    public static final String APPNAME = "tracker";

    static {
        TimeZoneUtils.getTimeZoneNonBlocking("Europe/London");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(TrackerSpringConfig.APPNAME);
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(new ClassPathResource("ehcache-tracker.xml"));
        return bean;
    }

    @Bean(name = "uaHeaderIdCache")
    public Ehcache uaHeaderIdCache(CacheManager ehCacheManager) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManager);
        bean.setCacheName("uaHeaderId");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    /**
     * Implements ClickService, InstallService, ConversionService, VideoViewService
     */
    @Bean
    public TrackerMultiServiceJdbcImpl trackerMultiServiceJdbcImpl(@Qualifier(ConfUtils.TRACKER_DS) DataSource dataSource) {
        return new TrackerMultiServiceJdbcImpl(dataSource);
    }

    @Bean
    public VideoViewAdEventLogic videoViewAdEventLogic(AdEventFactory adEventFactory) {
        return new VideoViewAdEventLogic(adEventFactory);
    }

    @Bean
    public AdEventFactory adEventFactory() {
        return new AdEventFactory(new KryoManager());
    }

    @Bean(initMethod = "init")
    public FetchStrategyFactory fetchStrategyFactory() {
        return new FetchStrategyFactory();
    }

}
