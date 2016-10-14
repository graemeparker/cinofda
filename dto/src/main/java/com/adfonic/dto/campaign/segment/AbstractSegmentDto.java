package com.adfonic.dto.campaign.segment;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.jdto.annotation.Source;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.Medium;
import com.adfonic.domain.Segment.DayOfWeek;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.dto.category.CategoryDto;
import com.adfonic.dto.channel.ChannelDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.geotarget.GeotargetDto;
import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.dto.geotarget.LocationTargetDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.vendor.VendorDto;

public abstract class AbstractSegmentDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;
    
    protected static final int DAYS_IN_A_WEEK = 7;
    protected static final int HOURS_IN_A_DAY = 24;

    @Source(value = "name")
    private String name;
    @Source(value = "countries")
    private Set<CountryDto> countries = new HashSet<CountryDto>(0); // empty = don't care
    private int minAge;
    private int maxAge;

    private int daysOfWeek; // lowest 7 bits, bit 0 = Sunday
    private int hoursOfDay; // lowest 24 bits, bit 0 = 0:00-1:00 in target user's local time zone
    private int hoursOfDayWeekend; // same, but applies to Saturday/Sunday only
    private BigDecimal genderMix; // null = don't care, 1.0 = all male
    @Source(value = "browsers")
    private Set<BrowserDto> browsers = new HashSet<BrowserDto>(0); // empty = don't care
    @Source(value = "connectionType.bitValue")
    private String connectionType;
    @Source(value = "geotargets")
    private Set<GeotargetDto> geotargets = new HashSet<GeotargetDto>(0); // empty = don't care;
    @Source(value = "countryListIsWhitelist")
    private boolean countryListIsWhitelist = true; // true = targeting the countries set, false = targeting countries not in the set
    private Set<OperatorAutocompleteDto> mobileOperators = new HashSet<OperatorAutocompleteDto>(0); // empty = don't care
    private Set<OperatorAutocompleteDto> ispOperators = new HashSet<OperatorAutocompleteDto>(0); // empty = don't care

    private Set<VendorDto> vendors = new HashSet<VendorDto>(0); // empty = don't care

    // restricts a targeted Platform.
    private Set<ChannelDto> channels = new HashSet<ChannelDto>(0);
    private boolean channelEnabled;
    private Set<PlatformDto> platforms = new HashSet<PlatformDto>(0); // empty = don't care

    // Included categories
    @Source(value = "includedCategories")
    private Set<CategoryDto> includedCategories = new HashSet<CategoryDto>();

    private String showDeviceTargeting;
    @Source(value = "medium")
    private Medium medium;
    @Source(value = "includeAdfonicNetwork")
    private boolean includeAdfonicNetwork;

    @Source(value = "targettedPublishers")
    private Set<PublisherDto> targettedPublishers;

    @Source(value = "mobileOperatorListIsWhitelist")
    private boolean mobileOperatorListIsWhitelist;
    @Source(value = "ispOperatorListIsWhitelist")
    private boolean ispOperatorListIsWhitelist;

    @Source(value = "locationTargets")
    private Set<LocationTargetDto> locationTargets = new HashSet<LocationTargetDto>();

    @Source(value = "geotargetType")
    private GeotargetTypeDto geotargetType;

    @Source(value = "explicitGPSEnabled")
    private boolean explicitGPSEnabled = true;

    @Source(value = "deviceGroups")
    private Set<DeviceGroupDto> deviceGroups;

    @Source(value = "safetyLevel")
    private SegmentSafetyLevel safetyLevel;
    
    @Source(value = "ipAddresses")
    private Set<String> ipAddresses = new HashSet<String>();
    @Source(value = "ipAddressesListWhitelist")
    private boolean ipAddressesListWhitelist;

    public BigDecimal getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
    }

    public Set<CountryDto> getCountries() {
        return countries;
    }

    public void setCountries(Set<CountryDto> countries) {
        this.countries = countries;
    }

    public Set<OperatorAutocompleteDto> getMobileOperators() {
        return mobileOperators;
    }

    public void setMobileOperators(Set<OperatorAutocompleteDto> mobileOperators) {
        this.mobileOperators = mobileOperators;
    }
    
    public Set<OperatorAutocompleteDto> getIspOperators() {
        return ispOperators;
    }

    public void setIspOperators(Set<OperatorAutocompleteDto> ispOperators) {
        this.ispOperators = ispOperators;
    }


    public Set<VendorDto> getVendors() {
        return vendors;
    }

    public void setVendors(Set<VendorDto> vendors) {
        this.vendors = vendors;
    }

    public Set<ChannelDto> getChannels() {
        return channels;
    }

    public void setChannels(Set<ChannelDto> channels) {
        this.channels = channels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(int daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getHoursOfDay() {
        return hoursOfDay;
    }

    public void setHoursOfDay(int hoursOfDay) {
        this.hoursOfDay = hoursOfDay;
    }

    public int getHoursOfDayWeekend() {
        return hoursOfDayWeekend;
    }

    public void setHoursOfDayWeekend(int hoursOfDayWeekend) {
        this.hoursOfDayWeekend = hoursOfDayWeekend;
    }

    public boolean[] getHoursOfDayAsArray() {
        boolean[] result = new boolean[HOURS_IN_A_DAY];
        int value = hoursOfDay;
        for (int i = 0; i < HOURS_IN_A_DAY; i++) {
            result[i] = (value & 1) == 1;
            value = value >>> 1;
        }
        return result;
    }

    public boolean[] getDaysOfWeekAsArray() {
        boolean[] result = new boolean[DAYS_IN_A_WEEK];

        int pos = 0;
        for (DayOfWeek day : DayOfWeek.values()) {
            result[pos++] = (daysOfWeek & (1 << day.ordinal())) != 0;
        }
        return result;
    }

    public void setHoursOfDayAsArray(boolean[] hourBits) {
        int value = 0;
        for (int i = HOURS_IN_A_DAY-1; i >= 0; i--) {
            value = value << 1;
            if (hourBits[i]) {
                ++value;
            }
        }
        this.hoursOfDay = value;
    }

    public boolean[] getHoursOfDayWeekendAsArray() {
        boolean[] result = new boolean[HOURS_IN_A_DAY];
        int value = hoursOfDayWeekend;
        for (int i = 0; i < HOURS_IN_A_DAY; i++) {
            result[i] = (value & 1) == 1;
            value = value >>> 1;
        }
        return result;
    }

    public boolean getChannelEnabled() {
        return channelEnabled;
    }

    public void setChannelEnabled(boolean channelEnabled) {
        this.channelEnabled = channelEnabled;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public Set<BrowserDto> getBrowsers() {
        return browsers;
    }

    public void setBrowsers(Set<BrowserDto> browsers) {
        this.browsers = browsers;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Set<GeotargetDto> getGeotargets() {
        return geotargets;
    }

    public void setGeotargets(Set<GeotargetDto> geotargets) {
        this.geotargets = geotargets;
    }

    public boolean getCountryListIsWhitelist() {
        return countryListIsWhitelist;
    }

    public void setCountryListIsWhitelist(boolean countryListIsWhitelist) {
        this.countryListIsWhitelist = countryListIsWhitelist;
    }

    public Set<PlatformDto> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<PlatformDto> platforms) {
        this.platforms = platforms;
    }

    public String getShowDeviceTargeting() {
        if (showDeviceTargeting == null) {
            if (getPlatformTargeted()) {
                showDeviceTargeting = "PLATFORM";
            } else if (!getPlatformTargeted() && isModelsTargeted()) {
                showDeviceTargeting = "DEVICE";
            } else {
                showDeviceTargeting = "ALL";
            }
        }
        return showDeviceTargeting;
    }

    public void setShowDeviceTargeting(String showDeviceTargeting) {
        this.showDeviceTargeting = showDeviceTargeting;
    }

    public boolean getPlatformTargeted() {
        return !CollectionUtils.isEmpty(getPlatforms());
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public boolean isIncludeAdfonicNetwork() {
        return includeAdfonicNetwork;
    }

    public void setIncludeAdfonicNetwork(boolean includeAdfonicNetwork) {
        this.includeAdfonicNetwork = includeAdfonicNetwork;
    }

    public Set<PublisherDto> getTargettedPublishers() {
        return targettedPublishers;
    }

    public void setTargettedPublishers(Set<PublisherDto> targettedPublishers) {
        this.targettedPublishers = targettedPublishers;
    }

    public boolean getMobileOperatorListIsWhitelist() {
        return mobileOperatorListIsWhitelist;
    }

    public void setMobileOperatorListIsWhitelist(boolean mobileOperatorListIsWhitelist) {
        this.mobileOperatorListIsWhitelist = mobileOperatorListIsWhitelist;
    }
    
    public boolean getIspOperatorListIsWhitelist() {
        return ispOperatorListIsWhitelist;
    }

    public void setIspOperatorListIsWhitelist(boolean ispOperatorListIsWhitelist) {
        this.ispOperatorListIsWhitelist = ispOperatorListIsWhitelist;
    }

    public Set<CategoryDto> getIncludedCategories() {
        return includedCategories;
    }

    public void setIncludedCategories(Set<CategoryDto> includedCategories) {
        this.includedCategories = includedCategories;
    }

    public Set<LocationTargetDto> getLocationTargets() {
        return locationTargets;
    }

    public void setLocationTargets(Set<LocationTargetDto> locationTargets) {
        this.locationTargets = locationTargets;
    }

    public GeotargetTypeDto getGeotargetType() {
        return geotargetType;
    }

    public void setGeotargetType(GeotargetTypeDto geotargetType) {
        this.geotargetType = geotargetType;
    }

    public boolean isExplicitGPSEnabled() {
        return explicitGPSEnabled;
    }

    public void setExplicitGPSEnabled(boolean explicitGPSEnabled) {
        this.explicitGPSEnabled = explicitGPSEnabled;
    }

    public Set<DeviceGroupDto> getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(Set<DeviceGroupDto> deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

    public SegmentSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SegmentSafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel;
    }
    
    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public boolean isIpAddressesListWhitelist() {
        return ipAddressesListWhitelist;
    }

    public void setIpAddressesListWhitelist(boolean ipAddressesListWhitelist) {
        this.ipAddressesListWhitelist = ipAddressesListWhitelist;
    }

    public abstract boolean isModelsTargeted();
}
