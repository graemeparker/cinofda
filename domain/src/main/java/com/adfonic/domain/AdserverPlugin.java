package com.adfonic.domain;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;
import org.apache.commons.lang.StringUtils;

@Entity
@Table(name="ADSERVER_PLUGIN")
public class AdserverPlugin extends BusinessKey {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=64,nullable=false)
    private String name;
    @Column(name="SYSTEM_NAME",length=64,nullable=false)
    private String systemName;
    @Column(name="ENABLED",nullable=false)
    private boolean enabled;
    @Column(name="EXPECTED_RESPONSE_TIME_MILLIS",nullable=false)
    private long expectedResponseTimeMillis;

    @ElementCollection(fetch=FetchType.EAGER,targetClass=String.class)
    @CollectionTable(name="ADSERVER_PLUGIN_PROPERTY",joinColumns=@JoinColumn(name="ADSERVER_PLUGIN_ID",referencedColumnName="ID"))
    @MapKeyColumn(name="NAME",length=64,nullable=false)
    @MapKeyClass(String.class)
    @Column(name="VALUE",length=1024,nullable=false)
    private Map<String,String> properties;

    {
        properties = new HashMap<String,String>();
    }

    public long getId() { return id; };

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getExpectedResponseTimeMillis() {
        return expectedResponseTimeMillis;
    }
    public void setExpectedResponseTimeMillis(long expectedResponseTimeMillis) {
        this.expectedResponseTimeMillis = expectedResponseTimeMillis;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name, String defaultValue) {
        return StringUtils.defaultString(properties.get(name), defaultValue);
    }

    public String getPropertyRequired(String name) {
        String value = properties.get(name);
        if (value != null) {
            return value;
        } else {
            throw new PropertyNotDefinedException(name);
        }
    }

    public static final class PropertyNotDefinedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private PropertyNotDefinedException(String name) {
            super(name);
        }
    }
}
