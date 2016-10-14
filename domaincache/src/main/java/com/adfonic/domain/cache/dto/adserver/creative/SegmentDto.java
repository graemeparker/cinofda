package com.adfonic.domain.cache.dto.adserver.creative;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment.DayOfWeek;
import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.util.NonBlockingCalendarPool;
import com.adfonic.util.TimeZoneUtils;

public class SegmentDto extends BusinessKeyDto {
    private static final long serialVersionUID = 5L;

    private Set<Long> countryIds = new HashSet<Long>();
    private BigDecimal genderMix;
    private int minAge;
    private int maxAge;
    private Set<Long> vendorIds = new HashSet<Long>();
    private Set<Long> modelIds = new HashSet<Long>();
    private Set<Long> deviceGroupIds = new HashSet<Long>();
    private Set<Long> browserIds = new HashSet<Long>();
    private Set<Long> platformIds = new HashSet<Long>();
    private Map<Long, Boolean> capabilityIdMap = new HashMap<Long, Boolean>();
    private ConnectionType connectionType;
    private boolean mobileOperatorListIsWhitelist;
    private boolean ispOperatorListIsWhitelist;
    private Set<Long> mobileOperatorIds = new HashSet<Long>();
    private Set<Long> ispOperatorIds = new HashSet<Long>();
    private boolean countryListIsWhitelist;
    private Set<Long> geotargetIds = new HashSet<Long>();
    private Set<LocationTargetDto> locationTargets = new HashSet<LocationTargetDto>();
    private Set<String> ipAddresses = new HashSet<String>();
    private boolean ipAddressesListWhitelist;
    private Set<Long> excludedModelIds = new HashSet<Long>();
    private Medium medium;
    private boolean includeAdfonicNetwork;
    private Map<Integer, Integer> dayToHourMap = new HashMap<Integer, Integer>();
    private boolean explicitGPSEnabled;

    public Set<Long> getCountryIds() {
        return countryIds;
    }

    public BigDecimal getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(BigDecimal genderMix) {
        this.genderMix = genderMix;
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

    public Set<Long> getVendorIds() {
        return vendorIds;
    }

    public Set<Long> getModelIds() {
        return modelIds;
    }

    public Set<Long> getBrowserIds() {
        return browserIds;
    }

    public Set<Long> getPlatformIds() {
        return platformIds;
    }

    public Map<Long, Boolean> getCapabilityIdMap() {
        return capabilityIdMap;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
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

    public Set<Long> getMobileOperatorIds() {
        return mobileOperatorIds;
    }

    public Set<Long> getIspOperatorIds() {
        return ispOperatorIds;
    }

    public boolean getCountryListIsWhitelist() {
        return countryListIsWhitelist;
    }

    public void setCountryListIsWhitelist(boolean countryListIsWhitelist) {
        this.countryListIsWhitelist = countryListIsWhitelist;
    }

    public Set<Long> getGeotargetIds() {
        return geotargetIds;
    }

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public boolean isIpAddressesListWhitelist() {
        return ipAddressesListWhitelist;
    }

    public void setIpAddressesListWhitelist(boolean ipAddressesListWhitelist) {
        this.ipAddressesListWhitelist = ipAddressesListWhitelist;
    }

    public Set<Long> getExcludedModelIds() {
        return excludedModelIds;
    }

    // Helper methods to check if various types of targeting are enabled
    /**
     * Method to check if this campaign is being targetted every day and every hour, 24X7
     * @return
     */
    public boolean isEveryDayEveryHourTargeted() {
        //Map will have all days always, even if some days are not targeted
        for (Entry<Integer, Integer> oneEntry : dayToHourMap.entrySet()) {
            //check for each day if all hours are being targeted
            //if any day any hour is not targeted that means its not targeting 24 X 7
            if (oneEntry.getValue() != Segment.ALL_HOURS) {
                return false;
            }
        }
        return true;
    }

    /**
     * Logic method to determine if the segment is enabled for
     * the given time in any time zone in the given country.
     */
    public boolean isTimeEnabled(String countryIsoCode, Date date) {
        if (countryIsoCode == null) {
            // Without a country, all we can do is see if this segment
            // is enabled for all days/hours.
            return isEveryDayEveryHourTargeted();
        }

        // Use TimeZoneUtils since it caches for us
        String[] tzIDs = TimeZoneUtils.getAvailableIDs(countryIsoCode);
        for (String tzID : tzIDs) {
            TimeZone tz = TimeZoneUtils.getTimeZoneNonBlocking(tzID);
            if (tz != null && isTimeEnabled(tz, date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logic method to determine if the segment is enabled for the
     * given time in the given time zone.
     */
    public boolean isTimeEnabled(TimeZone timeZone, Date date) {
        Calendar c = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        try {
            // DAY_OF_WEEK constants are SUNDAY=1 through SATURDAY=7
            int userDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            int userHourOfDay = c.get(Calendar.HOUR_OF_DAY);

            int targatedHoursOfDay = dayToHourMap.get(userDayOfWeek);

            return (targatedHoursOfDay & (1 << userHourOfDay)) != 0;
        } finally {
            NonBlockingCalendarPool.releaseCalendar(c);
        }
    }

    public void addDayHour(int day, int hour) {
        dayToHourMap.put(day, hour);
    }

    public void addDayHour(String day, int hour) {
        addDayHour(Segment.DayOfWeek.valueOf(day), hour);
    }

    public void addDayHour(DayOfWeek day, int hour) {
        //ordinal of sunday(first element in enum) is 0, so adding 1 to it to make it compatible with Calendar
        //where week start from sunday with value as 1
        int dayCount = day.ordinal() + 1;
        dayToHourMap.put(dayCount, hour);
    }

    public Map<Integer, Integer> getDayToHourMap() {
        return dayToHourMap;
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

    public boolean isExplicitGPSEnabled() {
        return explicitGPSEnabled;
    }

    public void setExplicitGPSEnabled(boolean explicitGPSEnabled) {
        this.explicitGPSEnabled = explicitGPSEnabled;
    }

    public Set<LocationTargetDto> getLocationTargets() {
        return locationTargets;
    }

    public void setLocationTargets(Set<LocationTargetDto> locationTargets) {
        this.locationTargets = locationTargets;
    }

    public Set<Long> getDeviceGroupIds() {
        return deviceGroupIds;
    }

    public void setDeviceGroupIds(Set<Long> deviceGroupIds) {
        this.deviceGroupIds = deviceGroupIds;
    }

    @Override
    public String toString() {
        return "SegmentDto {" + getId() + ", countryIds=" + countryIds + ", genderMix=" + genderMix + ", minAge=" + minAge + ", maxAge=" + maxAge
                + ", vendorIds=" + vendorIds + ", modelIds=" + modelIds + ", deviceGroupIds=" + deviceGroupIds + ", browserIds=" + browserIds + ", platformIds=" + platformIds
                + ", capabilityIdMap=" + capabilityIdMap + ", connectionType=" + connectionType + ", mobileOperatorListIsWhitelist=" + mobileOperatorListIsWhitelist + ", ispOperatorListIsWhitelist=" + ispOperatorListIsWhitelist
                + ", mobileOperatorIds=" + mobileOperatorIds + ", ispOperatorIds=" + ispOperatorIds + ", countryListIsWhitelist=" + countryListIsWhitelist + ", geotargetIds=" + geotargetIds + ", locationTargets=" + locationTargets + ", ipAddresses=" + ipAddresses
                + ", ipAddressesListWhitelist=" + ipAddressesListWhitelist + ", excludedModelIds=" + excludedModelIds + ", medium=" + medium + ", includeAdfonicNetwork="
                + includeAdfonicNetwork + ", dayToHourMap=" + dayToHourMap + ", explicitGPSEnabled=" + explicitGPSEnabled + "}";
    }

}
