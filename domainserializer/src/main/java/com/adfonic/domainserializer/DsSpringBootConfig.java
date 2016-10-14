package com.adfonic.domainserializer;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.adfonic.jms.JmsResource;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.status.AppInfoServlet;
import com.adfonic.util.status.DataSourceCheck;
import com.adfonic.util.status.ResourceRegistry;
import com.adfonic.util.status.SendJmsCheck;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

/**
 * Spring Boot configurator
 * 
 * Automatically discovers @Component @Controller @Configuration annotated classes (exanine @SpringBootApplication source code)
 * If it becomes messy or uncomprehensive, more traditional manual but fine-grained apporoach can be taken instead
 * 
 * http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready
 * http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-actuator
 * 
 * @author mvanek
 *   
 */
@SpringBootApplication
public class DsSpringBootConfig {

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_FACTORY)
    private ConnectionFactory centralJmsFactory;

    @Autowired
    private ServletContext servletContext;

    @Bean
    public ServletRegistrationBean statusServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new AppInfoServlet(), "/status");
        Map<String, String> params = new HashMap<String, String>();
        // params.put("application-class", DomainSerializerS3.class.getName());
        registration.setInitParameters(params);
        return registration;
    }

    public static enum DsResources {
        TOOLS_DB, /*ECPM_DB,*/JMS_CENTRAL;
    }

    @Bean
    public ResourceRegistry<DsResources> resourceRegistry() {
        ResourceRegistry<DsResources> registry = new ResourceRegistry<DsResources>(5);
        servletContext.setAttribute(AppInfoServlet.RR_DEFAULT_KEY, registry);
        registry.addResource(DsResources.TOOLS_DB, new DataSourceCheck<DsResources>(toolsDataSource, "SELECT 1"));
        //        registry.addResource(DsResources.ECPM_DB, new DataSourceCheck<DsResources>(ecpmDataSource, "SELECT 1"));
        registry.addResource(DsResources.JMS_CENTRAL, new SendJmsCheck<DsResources>(centralJmsFactory, JmsResource.TEST_QUEUE));
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
