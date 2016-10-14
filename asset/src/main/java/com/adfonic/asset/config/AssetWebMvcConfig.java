package com.adfonic.asset.config;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.adfonic.asset.controller.AssetController;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.status.AppInfoServlet;
import com.adfonic.util.status.DataSourceCheck;
import com.adfonic.util.status.ResourceRegistry;

/**
 *
 * Supersedes dispatcher-servlet.xml
 * 
 */
@Configuration
@ComponentScan(basePackageClasses = AssetController.class)
public class AssetWebMvcConfig extends WebMvcConfigurationSupport {

    public static enum AssetResources {
        TOOLS_DB;
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
        return ConfUtils.propertySourcesPlaceholderConfigurer(AssetSpringConfig.APPNAME);
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

    @Bean
    public ResourceRegistry<AssetResources> resourceRegistry() {
        ResourceRegistry<AssetResources> registry = new ResourceRegistry<AssetResources>(5);
        servletContext.setAttribute(AppInfoServlet.RR_DEFAULT_KEY, registry);
        registry.addResource(AssetResources.TOOLS_DB, new DataSourceCheck<AssetResources>(toolsDataSource, "SELECT 1"));
        return registry;
    }

}
