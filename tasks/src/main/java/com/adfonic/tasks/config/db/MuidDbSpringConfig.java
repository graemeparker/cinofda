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
 * Replacement for adfonic-muiddb-context.xml
 *
 */
@Configuration
public class MuidDbSpringConfig {

    public static final String APPNAME = "tasks";

    @Autowired
    private Environment springEnv;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(MuidDbSpringConfig.APPNAME);
    }

    @Bean(name = ConfUtils.MUID_DS, destroyMethod = "close")
    public DataSource muidDataSource() {
        return ConfUtils.dbcpDataSource("muid", springEnv);
    }

}
