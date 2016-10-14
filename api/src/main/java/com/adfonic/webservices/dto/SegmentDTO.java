package com.adfonic.webservices.dto;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.adfonic.domain.ConnectionType;

public class SegmentDTO {

    // No point. Updating spec
    //private String name;

    private Integer daysOfWeek; // lowest 7 bits, bit 0 = Sunday

    private Integer hoursOfDay; // lowest 24 bits, bit 0 = 0:00-1:00 in target user's local time zone

    private Integer hoursOfDayWeekend; // same, but applies to Saturday/Sunday only

    private Set<String> countries;// Country->isoCode

    private Boolean countryWhitelist; // countryListIsWhitelist

    private Set<String> operators;// Operator->name

    private Boolean operatorWhitelist; // operatorListIsWhitelist

    private BigDecimal genderMix; // null = don't care, 1.0 = all male

    private Integer minAge;

    private Integer maxAge;

    private Set<String> vendors; // Vendor->name

    private Set<String> models; // Model->name
    // Model.exernalID looks to be a good candidate but going with name after confirmation

    //private Set<String> browsers; // Browser->name
    
    private Boolean excludeOperaMini;

    private Set<String> platforms; // Platform->systemName

    // private Map<Capability, Boolean> capabilityMap; // true = must have, false = must not have; nulls get removed from map
    // - Decided not to implement the spec

    private ConnectionType connectionType;

    //private Set<String> categories;// Category->name

    private Set<GeoTargetDTO> geotargets;

    private Set<String> ipAddresses;
    
    private Boolean ipAddressesWhitelist;

    private Set<String> excludedModels; //Model->name

    //private Set<String> channels;// Channel->name

    // AI-249 - inventory targeting
    
    private Set<String> targetedPublishers;

    private Set<String> includedCategories;

    public Integer getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Integer daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public Integer getHoursOfDay() {
        return hoursOfDay;
    }

    public void setHoursOfDay(Integer hoursOfDay) {
        this.hoursOfDay = hoursOfDay;
    }

    public Integer getHoursOfDayWeekend() {
        return hoursOfDayWeekend;
    }

    public void setHoursOfDayWeekend(Integer hoursOfDayWeekend) {
        this.hoursOfDayWeekend = hoursOfDayWeekend;
    }

    @XmlElementWrapper(name="countries")
    @XmlElement(name="country")
    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }

    public Boolean isCountryWhitelist() {
        return countryWhitelist;
    }

    public void setCountryWhitelist(Boolean countryWhitelist) {
        this.countryWhitelist = countryWhitelist;
    }

    @XmlElementWrapper(name="operators")
    @XmlElement(name="operator")
    public Set<String> getOperators() {
        return operators;
    }

    public void setOperators(Set<String> operators) {
        this.operators = operators;
    }

    public Boolean isOperatorWhitelist() {
        return operatorWhitelist;
    }

    public void setOperatorWhitelist(Boolean operatorWhitelist) {
        this.operatorWhitelist = operatorWhitelist;
    }

    public BigDecimal getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    @XmlElementWrapper(name="vendors")
    @XmlElement(name="vendor")
    public Set<String> getVendors() {
        return vendors;
    }

    public void setVendors(Set<String> vendors) {
        this.vendors = vendors;
    }

    @XmlElementWrapper(name="models")
    @XmlElement(name="model")
    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    public Boolean isExcludeOperaMini() {
        return excludeOperaMini;
    }

    public void setExcludeOperaMini(Boolean excludeOperaMini) {
        this.excludeOperaMini = excludeOperaMini;
    }

    @XmlElementWrapper(name="platforms")
    @XmlElement(name="platform")
    public Set<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<String> platforms) {
        this.platforms = platforms;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /*@XmlElementWrapper(name="categories")
    @XmlElement(name="category")
    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }*/

    @XmlElementWrapper(name="geotargets")
    @XmlElement(name="geotarget")
    public Set<GeoTargetDTO> getGeotargets() {
        return geotargets;
    }

    public void setGeotargets(Set<GeoTargetDTO> geotargets) {
        this.geotargets = geotargets;
    }

    @XmlElementWrapper(name="ipAddresses")
    @XmlElement(name="ipAddress")
    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public Boolean isIpAddressesWhitelist() {
        return (ipAddressesWhitelist == null ? true : ipAddressesWhitelist);
    }

    public void setIpAddressesWhitelist(Boolean ipAddressesWhitelist) {
        this.ipAddressesWhitelist = ipAddressesWhitelist;
    }

    @XmlElementWrapper(name="excludedModels")
    @XmlElement(name="model")
    public Set<String> getExcludedModels() {
        return excludedModels;
    }

    public void setExcludedModels(Set<String> excludedModels) {
        this.excludedModels = excludedModels;
    }

    /*@XmlElementWrapper(name="channels")
    @XmlElement(name="channel")
    public Set<String> getChannels() {
        return channels;
    }

    public void setChannels(Set<String> channels) {
        this.channels = channels;
    }*/

    public void nullizeCollectionProperties() {
        countries = null;
        operators = null;
        vendors = null;
        models = null;
        platforms = null;
        //categories = null;
        geotargets = null;
        ipAddresses = null;
        excludedModels = null;
        //channels = null;
        targetedPublishers = null;
        includedCategories = null;
    }

    @XmlElementWrapper(name="targetedPublishers")
    @XmlElement(name="publisher")
    public Set<String> getTargetedPublishers() {
        return targetedPublishers;
    }

    public void setTargetedPublishers(Set<String> targetedPublishers) {
        this.targetedPublishers = targetedPublishers;
    }

    @XmlElementWrapper(name="includedCategories")
    @XmlElement(name="category")
    public Set<String> getIncludedCategories() {
        return includedCategories;
    }

    public void setIncludedCategories(Set<String> includedCategories) {
        this.includedCategories = includedCategories;
    }
}
