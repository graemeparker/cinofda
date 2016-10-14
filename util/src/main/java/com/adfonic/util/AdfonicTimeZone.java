package com.adfonic.util;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

/**
 * This is a wrapper for time zones with a method to get a pretty description
 * and override TimeZone.LONG. The existing description is geared toward
 * unlimited space, eg web but we can add more as additional formats come up.
 *
 * Note: id should be a valid id for java and mysql tz.
 *
 * @author jon
 */
public enum AdfonicTimeZone {
    //
    GMT("GMT", null), EUROPE_LONDON("Europe/London", "Europe/London"), AFRICA_CASABLANCA("Africa/Casablanca", null), AFRICA_ALGIERS("Africa/Algiers", null), AFRICA_WINDHOEK(
            "Africa/Windhoek", null), EUROPE_AMSTERDAM("Europe/Amsterdam", null), AFRICA_HARARE("Africa/Harare", null), EUROPE_ATHENS("Europe/Athens", null), AFRICA_NAIROBI(
            "Africa/Nairobi", null), EUROPE_MOSCOW("Europe/Moscow", null), ASIA_TEHRAN("Asia/Tehran", null), ASIA_BAKU("Asia/Baku", null), ASIA_DUBAI("Asia/Dubai", null), ASIA_KABUL(
            "Asia/Kabul", null), ASIA_KARACHI("Asia/Karachi", null), ASIA_YEKATERINBURG("Asia/Yekaterinburg", null), ASIA_CALCUTTA("Asia/Calcutta", null), ASIA_KATMANDU(
            "Asia/Katmandu", null), ASIA_ALMATY("Asia/Almaty", null), ASIA_RANGOON("Asia/Rangoon", null), ASIA_BANGKOK("Asia/Bangkok", null), ASIA_KRASNOYARSK("Asia/Krasnoyarsk",
            null), ASIA_IRKUTSK("Asia/Irkutsk", null), AUSTRALIA_PERTH("Australia/Perth", null), ASIA_TOKYO("Asia/Tokyo", null), ASIA_YAKUTSK("Asia/Yakutsk", null), AUSTRALIA_ADELAIDE(
            "Australia/Adelaide", null), AUSTRALIA_DARWIN("Australia/Darwin", null), ASIA_VLADIVOSTOK("Asia/Vladivostok", null), AUSTRALIA_BRISBANE("Australia/Brisbane", null), AUSTRALIA_SYDNEY(
            "Australia/Sydney", null), AUSTRALIA_LORD_HOWE("Australia/Lord_Howe", null), ASIA_MAGADAN("Asia/Magadan", null), PACIFIC_GUADALCANAL("Pacific/Guadalcanal", null), PACIFIC_NORFOLK(
            "Pacific/Norfolk", null), PACIFIC_AUCKLAND("Pacific/Auckland", null), PACIFIC_FIJI("Pacific/Fiji", null), PACIFIC_CHATHAM("Pacific/Chatham", null), PACIFIC_ENDERBURY(
            "Pacific/Enderbury", null), PACIFIC_KIRITIMATI("Pacific/Kiritimati", null), PACIFIC_MIDWAY("Pacific/Midway", null), AMERICA_ADAK("America/Adak", null), PACIFIC_TAHITI(
            "Pacific/Tahiti", null), PACIFIC_MARQUESAS("Pacific/Marquesas", null), AMERICA_ANCHORAGE("America/Anchorage", null), PACIFIC_GAMBIER("Pacific/Gambier", null), AMERICA_LOS_ANGELES(
            "America/Los_Angeles", null), PACIFIC_PITCAIRN("Pacific/Pitcairn", null), AMERICA_DENVER("America/Denver", null), AMERICA_PHOENIX("America/Phoenix", null), AMERICA_CHICAGO(
            "America/Chicago", null), AMERICA_GUATEMALA("America/Guatemala", null), PACIFIC_EASTER("Pacific/Easter", null), AMERICA_BOGOTA("America/Bogota", null), AMERICA_NEW_YORK(
            "America/New_York", null), AMERICA_CARACAS("America/Caracas", null), AMERICA_HALIFAX("America/Halifax", null), AMERICA_SANTIAGO("America/Santiago", null), AMERICA_ST_JOHNS(
            "America/St_Johns", null), AMERICA_GODTHAB("America/Godthab", null), AMERICA_MONTEVIDEO("America/Montevideo", null), AMERICA_SAO_PAULO("America/Sao_Paulo", null), AMERICA_NORONHA(
            "America/Noronha", null), ATLANTIC_AZORES("Atlantic/Azores", null), ATLANTIC_CAPE_VERDE("Atlantic/Cape_Verde", null);
    ;

    private String id;
    private String description;

    private AdfonicTimeZone(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    /*
     * returns a description for the time zone in the form of: (GMT[+|-]#h:mm)
     * [non-null enum description|TimeZone.LONG]
     * 
     * uses the DST version if the current date for the given zone is using DST
     */
    public String getDescription() {
        TimeZone tz = TimeZoneUtils.getTimeZoneNonBlocking(id);
        if (tz == null) {
            return "";
        }

        // this gives us offset adjusted to dst if in use, in minutes
        try {
            Date now = new Date();
            int offset = tz.getOffset(now.getTime()) / (1000 * 60);
            DecimalFormat hoursFormat = new DecimalFormat("#0;#0");
            // - is supplied, + must be set
            hoursFormat.setPositivePrefix("+");
            DecimalFormat minutesFormat = new DecimalFormat("#00;#00");
            // no sign on minutes
            minutesFormat.setNegativePrefix("");

            StringBuilder bld = new StringBuilder("(GMT").append(hoursFormat.format(offset / 60)).append(':').append(minutesFormat.format(offset % 60)).append(") ");

            if (description == null) {
                bld.append(tz.getDisplayName(tz.inDaylightTime(now), TimeZone.LONG));
            } else {
                bld.append(description);
            }
            return bld.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static AdfonicTimeZone getAdfonicTimeZoneById(String tzId) {
        if (StringUtils.isEmpty(tzId)) {
            return null;
        }
        for (AdfonicTimeZone tz : AdfonicTimeZone.values()) {
            if (tz.getId().equals(tzId)) {
                return tz;
            }
        }
        return null;
    }

    public static String getAdfonicTimeZoneDescription(TimeZone tz) {
        if (tz == null || tz.getID() == null) {
            return "";
        }
        AdfonicTimeZone atz = getAdfonicTimeZoneById(tz.getID());
        return atz != null ? atz.getDescription() : "";
    }

}
