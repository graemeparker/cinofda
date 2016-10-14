package com.adfonic.tasks.config.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import com.adfonic.util.ConfUtils;

/**
 * @author mvanek
 * 
 * Replacement for adfonic-trackerdb-context.xml
 *
 */
@Configuration
public class TrackerDbSpringConfig {

    public static final String APPNAME = "tasks";

    @Autowired
    private Environment springEnv;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(TrackerDbSpringConfig.APPNAME);
    }

    @Bean(name = ConfUtils.TRACKER_DS, destroyMethod = "close")
    public DataSource trackerDataSource() {
        return ConfUtils.dbcpDataSource("tracker", springEnv);
    }

}
