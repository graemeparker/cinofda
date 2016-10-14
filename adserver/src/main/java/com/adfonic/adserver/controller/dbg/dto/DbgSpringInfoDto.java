package com.adfonic.adserver.controller.dbg.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author mvanek
 *
 */
public class DbgSpringInfoDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("applicationName")
    private String applicationName;

    @JsonProperty("beanDefinitionCount")
    private Integer beanDefinitionCount;

    @JsonProperty("environment")
    private DbgSpringEnvironmentDto environment;

    @JsonProperty("beanDefinitionMap")
    private Map<String, String> beanDefinitionMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getBeanDefinitionCount() {
        return beanDefinitionCount;
    }

    public void setBeanDefinitionCount(Integer beanDefinitionCount) {
        this.beanDefinitionCount = beanDefinitionCount;
    }

    public DbgSpringEnvironmentDto getEnvironment() {
        return environment;
    }

    public void setEnvironment(DbgSpringEnvironmentDto environment) {
        this.environment = environment;
    }

    public Map<String, String> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    public void setBeanDefinitionMap(Map<String, String> definitionMap) {
        this.beanDefinitionMap = definitionMap;
    }

    public static class DbgSpringEnvironmentDto {

        @JsonProperty("activeProfiles")
        private String[] activeProfiles;

        @JsonProperty("defaultProfiles")
        private String[] defaultProfiles;

        @JsonProperty("propertySources")
        private Map<String, String> propertySources;

        public String[] getActiveProfiles() {
            return activeProfiles;
        }

        public void setActiveProfiles(String[] activeProfiles) {
            this.activeProfiles = activeProfiles;
        }

        public String[] getDefaultProfiles() {
            return defaultProfiles;
        }

        public void setDefaultProfiles(String[] defaultProfiles) {
            this.defaultProfiles = defaultProfiles;
        }

        public Map<String, String> getPropertySources() {
            return propertySources;
        }

        public void setPropertySources(Map<String, String> propertySources) {
            this.propertySources = propertySources;
        }

    }
}
