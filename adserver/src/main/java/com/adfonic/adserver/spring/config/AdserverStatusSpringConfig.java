package com.adfonic.adserver.spring.config;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.monitor.AdserverMonitor;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.util.AdsquareServiceCheck;
import com.adfonic.adserver.rtb.util.GdsServerCheck;
import com.adfonic.adserver.rtb.util.SimpleCassandraCheck;
import com.adfonic.adserver.rtb.util.SimpleRedisCheck;
import com.adfonic.http.TrackerNoOpClient;
import com.adfonic.quova.QuovaClient;
import com.adfonic.retargeting.redis.ThreadLocalClientFactory;
import com.adfonic.util.stats.CounterJmxManager;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.status.AppInfoServlet;
import com.adfonic.util.status.DataSourceCheck;
import com.adfonic.util.status.ResourceRegistry;
import com.byyd.adsquare.v2.EnrichmentApiClient;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.datastax.driver.core.Session;

@Configuration
public class AdserverStatusSpringConfig {

    public static enum AdServerResource {
        AWS_S3, CACHE_DB, GDS_HTTP, GEO_REDIS, DID_REDIS, ADSQUARE, CASSANDRA;
    }

    @Autowired
    private ServletContext servletContext;

    @Autowired
    @Qualifier("cachedbDataSource")
    private DataSource dataSource;

    @Autowired
    private QuovaClient quovaClient;

    @Autowired
    private EnrichmentApiClient adsquareClient;

    @Autowired
    @Qualifier("GeoRedisFactory")
    private ThreadLocalClientFactory geoRedisFactory;

    @Autowired
    @Qualifier("DmpRedisFactory")
    private ThreadLocalClientFactory dmpRedisFactory;

    @Autowired
    @Qualifier("AdsquareCassandraSession")
    private Session session;

    /**
     * Used from newrelic adserver plugin to access counters via JMX
     */
    @Bean(name = "AdfonicCounters:mbean=GenericCounters")
    public CounterJmxManager counterJmxManager(CounterManager counterManager) {
        return new CounterJmxManager(counterManager);
    }

    @Bean
    public CounterManager counterManager(@Value("${counter.publishers}") String counterPublishers) {
        /*
        String[] split = counterPublishers.split(",");
        HashSet<Long> publishers = new HashSet<Long>();
        for (String item : split) {
            publishers.add(Long.parseLong(item));
        }
        return new CounterManager(publishers);
        */
        return new CounterManager();
    }

    @Bean
    public OffenceRegistry offenceRegistry() {
        return new OffenceRegistry(100, 10);
    }

    @Bean
    public RtbFisherman RtbFisherman() {
        return new RtbFisherman();
    }

    @Bean
    public AdserverMonitor adserverMonitor() {
        return new AdserverMonitor();
    }

    @Bean
    public TrackerNoOpClient trackerNoOpClient(DynamicProperties dprops) {
        String trackerUrl = dprops.getProperty(DcProperty.TrackerBaseUrl);
        return new TrackerNoOpClient(trackerUrl);
    }

    @Bean
    public ResourceRegistry<AdServerResource> resourceRegistry() {
        ResourceRegistry<AdServerResource> registry = new ResourceRegistry<AdServerResource>(5);
        servletContext.setAttribute(AppInfoServlet.RR_DEFAULT_KEY, registry);

        registry.addResource(AdServerResource.CACHE_DB, "MySQL CacheDB", new DataSourceCheck<AdServerResource>(dataSource, "SELECT 1"));
        registry.addResource(AdServerResource.GDS_HTTP, "GDS/Quova/Neustar service", new GdsServerCheck(quovaClient, "50.57.33.35"));
        registry.addResource(AdServerResource.GEO_REDIS, "Geo Redis", new SimpleRedisCheck(geoRedisFactory, "TestGeoKey"));
        registry.addResource(AdServerResource.DID_REDIS, "Dmp Redis", new SimpleRedisCheck(dmpRedisFactory, "TestDmpKey"));
        registry.addResource(AdServerResource.CASSANDRA, "Cassandra", new SimpleCassandraCheck(session));
        registry.addResource(AdServerResource.ADSQUARE, "Adsquare", new AdsquareServiceCheck(adsquareClient));
        return registry;
    }

    @Bean
    public MetricRegistry metricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry();
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
        HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);
        return metricRegistry;
    }
}
