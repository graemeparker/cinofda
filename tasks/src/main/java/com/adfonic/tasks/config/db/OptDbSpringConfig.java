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
 * Replacement for adfonic-optdb-context.xml
 *
 */
@Configuration
public class OptDbSpringConfig {

    public static final String APPNAME = "tasks";

    @Autowired
    private Environment springEnv;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return ConfUtils.propertySourcesPlaceholderConfigurer(OptDbSpringConfig.APPNAME);
    }

    @Bean(name = ConfUtils.OPT_DS, destroyMethod = "close")
    public DataSource optDataSource() {
        return ConfUtils.dbcpDataSource("opt", springEnv);
    }

}
