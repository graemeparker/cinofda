package com.adfonic.adserver.controller.dbg.dto;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author mvanek
 *
 */
public class DbgContainerDto {

    @JsonProperty("startedAt")
    private Date startedAt;

    @JsonProperty("snapshotAt")
    private Date snapshotAt;

    @JsonProperty("inetAddress")
    private Map<String, String> inetAddress;

    @JsonProperty("systemProperties")
    private Map<String, String> systemProperties;

    @JsonProperty("environmentVariables")
    private Map<String, String> environmentVariables;

    @JsonProperty("servletContext")
    private Map<String, Object> servletContext;

    @JsonProperty("springContext")
    private DbgSpringInfoDto springContext;

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date started) {
        this.startedAt = started;
    }

    public Date getSnapshotAt() {
        return snapshotAt;
    }

    public void setSnapshotAt(Date snapshotAt) {
        this.snapshotAt = snapshotAt;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> sysprop) {
        this.systemProperties = sysprop;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> sysenv) {
        this.environmentVariables = sysenv;
    }

    public Map<String, String> getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(Map<String, String> address) {
        this.inetAddress = address;
    }

    public Map<String, Object> getServletContext() {
        return servletContext;
    }

    public void setServletContext(Map<String, Object> contextMap) {
        this.servletContext = contextMap;
    }

    public void setSpringContext(DbgSpringInfoDto contextMap) {
        this.springContext = contextMap;
    }

    public DbgSpringInfoDto getSpringContext() {
        return springContext;
    }

}
