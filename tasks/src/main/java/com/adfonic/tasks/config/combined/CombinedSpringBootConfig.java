package com.adfonic.tasks.config.combined;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.adfonic.jms.JmsResource;
import com.adfonic.tasks.config.db.AdmReportingDbSpringConfig;
import com.adfonic.tasks.config.db.OptDbSpringConfig;
import com.adfonic.tasks.config.db.OptOutDbSpringConfig;
import com.adfonic.tasks.config.db.ToolsDbSpringConfig;
import com.adfonic.tasks.config.db.TrackerDbSpringConfig;
import com.adfonic.tasks.config.single.SingleTasksSpringConfig;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.status.AppInfoServlet;
import com.adfonic.util.status.DataSourceCheck;
import com.adfonic.util.status.ResourceRegistry;
import com.adfonic.util.status.SendJmsCheck;

@Configuration
@EnableAutoConfiguration(exclude = { JmsAutoConfiguration.class, DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class })
@Import({ SingleTasksSpringConfig.class, CombinedTasksSpringConfig.class, ExternalAuditSpringConfig.class, CombinedJmsSpringConfig.class, CombinedKafkaSpringConfig.class, MiddlewareAuditLogConfig.class,
        SchedulingSpringConfig.class, ToolsDbSpringConfig.class, TrackerDbSpringConfig.class, AdmReportingDbSpringConfig.class, OptDbSpringConfig.class, OptOutDbSpringConfig.class })
@ImportResource(value = { /*"adfonic-toolsdb-context.xml","adfonic-admreportingdb-context.xml", "adfonic-optdb-context.xml", "adfonic-optoutdb-context.xml", "adfonic-trackerdb-context.xml"*/})
public class CombinedSpringBootConfig {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    @Qualifier(ConfUtils.TRACKER_DS)
    private DataSource trackerDataSource;

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_FACTORY)
    private ConnectionFactory centralConnectionFactory;

    @Bean
    public ServletRegistrationBean statusServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new AppInfoServlet(), "/status");
        Map<String, String> params = new HashMap<String, String>();
        registration.setInitParameters(params);
        return registration;
    }

    public static enum CtResources {
        TOOLS_DB, TRACKER_DB, JMS_CENTRAL;
    }

    @Bean
    public ResourceRegistry<CtResources> resourceRegistry() {
        ResourceRegistry<CtResources> registry = new ResourceRegistry<CtResources>(5);
        servletContext.setAttribute(AppInfoServlet.RR_DEFAULT_KEY, registry);
        registry.addResource(CtResources.TOOLS_DB, new DataSourceCheck<CtResources>(toolsDataSource, "SELECT 1"));
        registry.addResource(CtResources.TRACKER_DB, new DataSourceCheck<CtResources>(trackerDataSource, "SELECT 1"));
        registry.addResource(CtResources.JMS_CENTRAL, new SendJmsCheck<CtResources>(centralConnectionFactory, JmsResource.TEST_QUEUE));
        return registry;
    }
    /*
        @Bean
        public MetricRegistry metricRegistry() {
            MetricRegistry metricRegistry = new MetricRegistry();
            servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
            servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
            HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
            servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);
            return metricRegistry;
        }
    */
}
