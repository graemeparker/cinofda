package com.adfonic.tasks.combined;

import java.util.Objects;

import javax.sql.DataSource;

import com.adfonic.adserver.DynamicProperties;
import com.adfonic.data.cache.util.Properties;
import com.adfonic.data.cache.util.PropertiesFactory;

/**
 * 
 * @author mvanek
 *
 */
public class DynaPropertiesReloader implements DynamicProperties {

    private final PropertiesFactory propertiesFactory;
    // Field assignment is atomic operation in Java so there is no need for fancy atomic stuff
    private Properties dynaProperties;

    public DynaPropertiesReloader(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        propertiesFactory = new PropertiesFactory(dataSource);
        reload();
    }

    // Schedule externaly in spring configuration
    public void reload() {
        dynaProperties = propertiesFactory.getProperties();
    }

    public Properties getDynaProperties() {
        return dynaProperties;
    }

    @Override
    public String getProperty(DcProperty keyName) {
        return dynaProperties.getProperty(keyName.getDbKey());
    }

    @Override
    public String getProperty(DcProperty keyName, String defaultValue) {
        String value = dynaProperties.getProperty(keyName.getDbKey());
        return value != null ? value : defaultValue;
    }

}
