package com.adfonic.tracker.config;

import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import com.adfonic.tracker.controller.ConversionController;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.status.AppInfoServlet;
import com.adfonic.util.status.DataSourceCheck;
import com.adfonic.util.status.ResourceRegistry;

/**
 * 
 * Replacement for dispatcher-servlet.xml 
 *
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackageClasses = ConversionController.class)
public class TrackerWebMvcConfig extends WebMvcConfigurationSupport {

    public static enum TrackerResources {
        TOOLS_DB, JMS_ADEVENT;
    }

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    private ServletContext servletContext;

    /**
     * Some controllers have @Value injected configuration properties 
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(TrackerSpringConfig.APPNAME);
    }

    @Bean
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        /**
         * We need these next two beans with their alwaysUseFullPath set to true
         * so that "<context>/foo/bar" ends up being treated as "/foo/bar"
         * when it comes to request mappings.
         */
        handlerMapping.setAlwaysUseFullPath(true);
        return handlerMapping;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //jacksonHttpMessageConverter.getObjectMapper().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        converters.add(jackson2HttpMessageConverter());
    }

    @Bean
    public ViewResolver viewResolver() {
        VelocityViewResolver velocityResolver = new VelocityViewResolver();
        velocityResolver.setExposeSpringMacroHelpers(true);
        return velocityResolver;
    }

    @Bean
    public VelocityConfigurer velocityConfigurer() {
        VelocityConfigurer bean = new VelocityConfigurer();
        bean.setResourceLoaderPath("/"); //this is strange and probably wrong. It should be /WEB-INF/velocity where is adtruth.vm
        return bean;
    }

    /**
     * It is also injected into Byyd's JsonView 
     */
    @Bean
    MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Bean
    public ResourceRegistry<TrackerResources> resourceRegistry() {
        ResourceRegistry<TrackerResources> registry = new ResourceRegistry<TrackerResources>(5);
        servletContext.setAttribute(AppInfoServlet.RR_DEFAULT_KEY, registry);
        registry.addResource(TrackerResources.TOOLS_DB, new DataSourceCheck<TrackerResources>(toolsDataSource, "SELECT 1"));
        return registry;
    }

}
