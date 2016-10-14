package com.adfonic.tasks.config.single;

import java.util.Properties;

import javax.el.ExpressionFactory;
import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.DynamicProperties;
import com.adfonic.audit.AuditorConfig;
import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Publication;
import com.adfonic.email.impl.JmsBasedEmailService;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.tasks.combined.DynaPropertiesReloader;
import com.adfonic.util.ConfUtils;
import com.byyd.middleware.domainlog.EntityAuditorJpaImpl;
import com.byyd.middleware.domainlog.service.AuditLogManager;
import com.byyd.middleware.iface.dao.FetchStrategyFactory;
import com.byyd.middleware.iface.service.NotAutoScan;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

import de.odysseus.el.ExpressionFactoryImpl;

/**
 * 
 * @author mvanek
 *
 * Replacement for adfonic-tasks-context.xml
 */
@Configuration
@ComponentScan(basePackages = "com.byyd.middleware", excludeFilters = @ComponentScan.Filter(value = NotAutoScan.class, type = FilterType.ANNOTATION))
public class SingleTasksSpringConfig {

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Bean
    @Qualifier(ConfUtils.TOOLS_JDBC_TEMPLATE)
    JdbcTemplate toolsJdbcTemplate() {
        return new JdbcTemplate(toolsDataSource);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        String configFile = System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT) + "/" + "adfonic-tasks.properties";
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setFileEncoding("utf-8");
        configurer.setLocation(new FileSystemResource(configFile));
        return configurer;
    }

    @Bean
    public AdfonicBeanDispatcher adfonicBeanDispatcher() {
        return new AdfonicBeanDispatcher();
    }

    @Bean(initMethod = "init")
    public FetchStrategyFactory fetchStrategyFactory() {
        return new FetchStrategyFactory();
    }

    /**
     * @ComponentScan in com.byyd.middleware should discover domainLogManager bean
     */
    @Bean
    public EntityAuditor entityAuditor(@Qualifier("domainLogManager") AuditLogManager domainLogManager) {
        AuditorConfig config = new AuditorConfig();
        Properties properties = new Properties();
        properties.put(Campaign.class.getName(), "status");
        properties.put(Publication.class.getName(), "name,publicationType,autoApproval,status,category,statedCategoriesAsString,rtbId,URLString");
        properties.put(AdSpace.class.getName(), "name,status,backfillEnabled,formatsAsString");
        config.setAuditedProperties(properties);

        return new EntityAuditorJpaImpl(config, domainLogManager, "tasks");
    }

    /**
     * Blody email sending needs EL Factory 
     */
    @Bean
    public ExpressionFactory expressionFactory() {
        return new ExpressionFactoryImpl();
    }

    @Bean
    public JmsBasedEmailService emailService(@Qualifier(JmsResource.CENTRAL_JMS_FACTORY) ConnectionFactory connectionFactory) {
        JmsBasedEmailService service = new JmsBasedEmailService(connectionFactory);
        service.setOutboundEmailQueue(JmsResource.EMAIL_OUTBOUND_QUEUE);
        return service;
    }

    @Bean(name = JmsResource.CENTRAL_JMS_FACTORY, initMethod = "start", destroyMethod = "stop")
    public ConnectionFactory centralActiveMqConnectionFactory(@Value("${central.jms.broker.url}") String brokerUrl,
            @Value("${central.jms.pool.maxConnections}") Integer maxConnections) {
        ActiveMQConnectionFactory activemqFactory = new ActiveMQConnectionFactory(brokerUrl);
        PooledConnectionFactory pooledFactory = new PooledConnectionFactory(activemqFactory);
        pooledFactory.setMaxConnections(maxConnections);
        return pooledFactory;
    }

    @Bean(name = JmsResource.CENTRAL_JMS_TEMPLATE)
    public JmsTemplate centralJmsTemplate(@Qualifier(JmsResource.CENTRAL_JMS_FACTORY) ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    public JmsUtils jmsUtils() {
        return new JmsUtils();
    }

    @Bean
    DynamicProperties dynaPropertiesReloader(@Qualifier(ConfUtils.TOOLS_DS) DataSource dataSource) {
        return new DynaPropertiesReloader(dataSource);
    }
}
