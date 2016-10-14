package com.adfonic.dto.audience;

/**
 * This DTO is meant to hold the collated results of Audiences' fields and
 * population counts
 *
 * @author pierre
 *
 */
public class MyAudienceDto {

    public enum Status {
        NEW, ACTIVE, PAUSED, STATIC
    }

    public enum Type {
        CLICKERS, CONVERTERS, DMP, DEVICE_IDS, INSTALLERS, SITE_APP_VISITOR, LOCATION
    }

    private String externalId;
    private Status status;
    private String name;
    private Type type;
    private long population;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

}
