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
 * Replacement for adfonic-optoutdb-context.xml
 *
 */
@Configuration
public class OptOutDbSpringConfig {

    public static final String APPNAME = "tasks";

    @Autowired
    private Environment springEnv;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(OptOutDbSpringConfig.APPNAME);
    }

    @Bean(name = ConfUtils.OPTOUT_DS, destroyMethod = "close")
    public DataSource optoutDataSource() {
        return ConfUtils.dbcpDataSource("optout", springEnv);
    }

}
