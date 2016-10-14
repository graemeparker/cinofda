package com.adfonic.domainserializer;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import com.adfonic.domain.cache.DataCollectorDomainCacheLoader;
import com.adfonic.domain.cache.DomainCacheLoader;
import com.adfonic.domainserializer.loader.AdSpaceLoader;
import com.adfonic.domainserializer.loader.AdserverDomainCacheLoader;
import com.adfonic.domainserializer.loader.CampaignAudienceLoader;
import com.adfonic.domainserializer.loader.CreativeLoader;
import com.adfonic.domainserializer.xaudit.AuditCheckJmsSender;
import com.adfonic.domainserializer.xaudit.AuditEligibilityCheck;
import com.adfonic.domainserializer.xaudit.AuditEligibilityCheckImpl;
import com.adfonic.jms.JmsResource;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.StringUtils;

/**
 * 
 * @author mvanek
 * 
 * This replaced xml congigs adfonic-domainserializer-context.xml and adfonic-domainserializer-db-context.xml
 *
 */
@Configuration
//Enable JMX export as @ManagedResource annotated classes are used and monitored
@EnableMBeanExport
public class DsSpringConfig {

    @Autowired
    private Environment springEnv;

    /**
     * Injects @Value(...)
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer("domainserializer");
    }

    @Bean(name = "domainSerializerProperties")
    public Properties domainSerializerProperties(@Value(ConfUtils.CONFIG_DIR_CONFIG) String configHome) throws IOException {
        PropertiesFactoryBean factoryBean = new PropertiesFactoryBean();
        factoryBean.setLocation(new FileSystemResource(configHome + "/adfonic-domainserializer.properties"));
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean
    public DomainSerializerS3 domainSerializer() {
        return new DomainSerializerS3();
    }

    @Bean
    public DsCacheManager s3CacheManager() {
        return new DsCacheManager();
    }

    @Bean
    public DomainCacheLoader domainCacheLoader(@Qualifier(ConfUtils.TOOLS_DS) DataSource toolsReadDataSource) {
        return new DomainCacheLoader(toolsReadDataSource);
    }

    @Bean
    public DataCollectorDomainCacheLoader dataCollectorDomainCacheLoader(@Qualifier(ConfUtils.TOOLS_DS) DataSource toolsReadDataSource) {
        return new DataCollectorDomainCacheLoader(toolsReadDataSource);
    }

    @Bean
    public AdserverDomainCacheLoader adserverDomainCacheLoader(@Qualifier(ConfUtils.TOOLS_DS) DataSource toolsReadDataSource, AuditEligibilityCheck auditedCreativesFilter) {

        String etProperty = System.getProperty("DomainSerializer.ELIGIBILITY_THREADS");
        int threadCount;
        if (etProperty != null) {
            threadCount = Integer.parseInt(etProperty);
        } else {
            // leave one core unoccupied
            threadCount = Runtime.getRuntime().availableProcessors() - 1;
            if (threadCount < 1) {
                threadCount = 1;
            }
        }

        boolean showProgress = "true".equals(System.getProperty("com.adfonic.progress.enabled"));
        EligibilityChecker eligibilityChecker = new EligibilityChecker(auditedCreativesFilter, threadCount, showProgress);

        return new AdserverDomainCacheLoader(toolsReadDataSource, adspaceLoader(toolsReadDataSource), creativeLoader(toolsReadDataSource), eligibilityChecker);
    }

    @Bean
    public AdSpaceLoader adspaceLoader(@Qualifier(ConfUtils.TOOLS_DS) DataSource toolsReadDataSource) {
        return new AdSpaceLoader(toolsReadDataSource);
    }

    @Bean
    public CreativeLoader creativeLoader(@Qualifier(ConfUtils.TOOLS_DS) DataSource toolsReadDataSource) {
        return new CreativeLoader(toolsReadDataSource, new CampaignAudienceLoader());
    }

    @Bean
    public AuditEligibilityCheck auditedCreativesFilter(@Value("${external.auditing.publishers}") String auditingPublishers,
            @Value("${appnxs.allow.audit}") String appNexusAllowAuditPublishers, @Value("${adx.publisher.id}") long adxPublisherId, AuditCheckJmsSender creativeSyncService) {
        Set<Long> allAuditingPublishers = StringUtils.toSetOfLongs(auditingPublishers, ",");
        Set<Long> apnxAuditingPublishers = StringUtils.toSetOfLongs(appNexusAllowAuditPublishers, ",");
        return new AuditEligibilityCheckImpl(allAuditingPublishers, apnxAuditingPublishers, creativeSyncService);
    }

    @Bean
    public AuditCheckJmsSender publisherCreativeSyncNotifier(ConnectionFactory jmsConnectionFactory) {
        return new AuditCheckJmsSender(jmsConnectionFactory, JmsResource.EXCHANGE_CREATIVE_AUDIT);
    }

    @Bean(name = JmsResource.CENTRAL_JMS_FACTORY, initMethod = "start", destroyMethod = "stop")
    public ConnectionFactory jmsConnectionFactory(@Value("${jms.broker.url}") String brokerUrl, @Value("${jms.pool.maxConnections}") Integer maxConnections) {
        PooledConnectionFactory jmsPooledFactory = new PooledConnectionFactory(brokerUrl);
        jmsPooledFactory.setMaxConnections(maxConnections);
        return jmsPooledFactory;
    }

    @Bean(name = ConfUtils.TOOLS_DS, destroyMethod = "close")
    public BasicDataSource toolsDbDataSource() {
        return ConfUtils.dbcpDataSource("tools.read", springEnv);
    }
    /*
    @Bean(name = "executor")
    public ThreadPoolTaskScheduler scheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setDaemon(true);
        scheduler.setThreadNamePrefix("domser-schexec");
        scheduler.setPoolSize(5);
        return scheduler;
    }
    */
}
