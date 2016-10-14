package com.adfonic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeZoneUtils {
    private static final transient Logger LOG = Logger.getLogger(TimeZoneUtils.class.getName());

    // This is the TimeZone ID that explicitly controls which TimeZone is
    // considered our "application default," irrespective of the system tz.
    // This is package protected in order to enable a unit test...
    static final String DEFAULT_TIME_ZONE_ID = System.getProperty("com.adfonic.util.TimeZoneUtils.defaultTimeZoneID", "GMT");

    private static final TimeZone DEFAULT_TIME_ZONE;

    private static final Map<String, String> IDS_BY_LOWERCASE;
    private static final Map<String, String[]> IDS_BY_COUNTRY_ISOCODE;
    private static final Map<String, TimeZone> TIMEZONES_BY_ID;

    static {
        com.ibm.icu.util.TimeZone.setDefaultTimeZoneType(com.ibm.icu.util.TimeZone.TIMEZONE_JDK);

        LOG.info("Initializing TimeZone ID caches");
        IDS_BY_LOWERCASE = new HashMap<String, String>();
        TIMEZONES_BY_ID = new HashMap<String, TimeZone>();
        for (String id : TimeZone.getAvailableIDs()) {
            IDS_BY_LOWERCASE.put(id.toLowerCase(), id);
            TIMEZONES_BY_ID.put(id, TimeZone.getTimeZone(id));
        }
        LOG.info("Cached " + TIMEZONES_BY_ID.size() + " TimeZones by ID");

        IDS_BY_COUNTRY_ISOCODE = new HashMap<String, String[]>();

        DEFAULT_TIME_ZONE = TIMEZONES_BY_ID.get(DEFAULT_TIME_ZONE_ID);
    }

    private TimeZoneUtils() {
    }

    /**
     * Get the default application TimeZone. This is NOT the same as calling
     * TimeZone.getDefault(). This method is deliberate about which TimeZone it
     * considers to be the "application default."
     */
    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TIME_ZONE;
    }

    /**
     * Did you know TimeZone.getTimeZone(id) is synchronized?!!? Well I
     * didn't...until now obviously. Please use this method instead of
     * TimeZone.getTimeZone() in any highly concurrent setting. We ran into a
     * major thread contention issue on lon2adserver* where that new hardware is
     * so fast that synchronization is actually becoming a real bottleneck.
     * We're eliminating this point of contention, at least...
     */
    public static TimeZone getTimeZoneNonBlocking(String id) {
        return TIMEZONES_BY_ID.get(id);
    }

    /**
     * This method is preferable to using TimeZone directly, since this method
     * will cache the results. After running a profiler on adserver, we
     * discovered that Segment.isTimeEnabled was wastefully calling
     * TimeZone.getAvailableIDs. Caching will help speed that up.
     */
    public static String[] getAvailableIDs(String countryIsoCode) {
        String[] ids;
        if ((ids = IDS_BY_COUNTRY_ISOCODE.get(countryIsoCode)) == null) {
            synchronized (IDS_BY_COUNTRY_ISOCODE) {
                if ((ids = IDS_BY_COUNTRY_ISOCODE.get(countryIsoCode)) == null) {
                    ids = com.ibm.icu.util.TimeZone.getAvailableIDs(countryIsoCode);
                    IDS_BY_COUNTRY_ISOCODE.put(countryIsoCode, ids);
                }
            }
        }
        return ids;
    }

    public static TimeZone getTimeZoneByFreeformInput(String input) {
        if (input == null) {
            return null;
        }

        // See if it's a TimeZone ID...use our lowercase map to look it up
        String id = IDS_BY_LOWERCASE.get(input.toLowerCase());
        if (id != null) {
            // Yup, it's an ID...just return the respective TimeZone by ID
            return getTimeZoneNonBlocking(id);
        }

        // Let's see if it's a numeric offset
        try {
            double offset = Double.parseDouble(input);
            // It parsed out as a number ok, let's see if we can find it
            TimeZone timeZone = getTimeZoneByOffset(offset);
            if (timeZone != null) {
                return timeZone;
            }
        } catch (java.lang.NumberFormatException ignored) {
            //do nothing
        }

        LOG.warning("Unrecognized TimeZone: " + input);
        return null;
    }

    /**
     * This method tries to resolve a TimeZone by a provided numeric offset. The
     * "scale" of the offset is interpreted automatically based on the size of
     * the value. i.e. -23.0 to 23.0 would represent hours, -720.0 to 720.0
     * would represent minutes, -43200.0 to 43200.0 would represent seconds, and
     * anything larger would represent milliseconds.
     */
    public static TimeZone getTimeZoneByOffset(double offset) {
        double localOffset = offset;
        // Convert the input to milliseconds
        int offsetMs;

        // Sometimes we're passed oddball offsets, like -13.0.
        // Coerce them into the -12.0 to +12.0 range.
        if (localOffset < -12.0 && localOffset > -24.0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Coercing offset " + localOffset + " into range");
            }
            localOffset += 24.0;
        } else if (localOffset > 12.0 && localOffset < 24.0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Coercing offset " + localOffset + " into range");
            }
            localOffset -= 24.0;
        }

        if (localOffset <= 12.0 && localOffset >= -12.0) {
            // It's hours
            offsetMs = (int) (localOffset * 3600000);
        } else if (localOffset <= 720.0 && localOffset >= -720.0) {
            // It's minutes
            offsetMs = (int) (localOffset * 60000);
        } else if (localOffset <= 43200.0 && localOffset >= -43200.0) {
            // It's seconds
            offsetMs = (int) (localOffset * 1000);
        } else {
            // It's milliseconds
            offsetMs = (int) localOffset;
        }

        // First let's see if we have a "preferred" TimeZone ID based on
        // the hours offset (i.e. 5.0 -> US/Eastern, 6.0 -> US/Central)
        final double hours = offsetMs / 3600000.0;
        final String id = getPreferredTimeZoneIDByHoursOffset(hours);
        if (id != null) {
            return getTimeZoneNonBlocking(id);
        }

        // Fall back on looking up a TimeZone with the given raw offset
        final String[] ids = TimeZone.getAvailableIDs(offsetMs);
        if (ids == null || ids.length == 0) {
            LOG.warning("No TimeZone with offset=" + localOffset);
            return null;
        }

        // Just use the first available one...it's understood that the
        // first available TimeZone won't necessarily map to the correct
        // country. i.e. if you take -6.0 hours as the offset, you'll
        // get a whole mess of stuff for South America as well as North
        // America. And if you just take the first available one you're
        // not likely to get the *exact* one you want. But really, all
        // we're after here is to use a TimeZone that has the same offset,
        // so we can do "get current time in this time zone" operations.
        return getTimeZoneNonBlocking(ids[0]);
    }

    public static String getPreferredTimeZoneIDByHoursOffset(double hours) {
        String preferredTimeZone = null;
        
        final int hoursInt = (int) Math.floor(hours);
        if (hoursInt == hours) {
            switch (hoursInt) {
                case 2:
                    preferredTimeZone = "EET";
                    break;
                case 1:
                    preferredTimeZone = "CET";
                    break;
                case 0:
                    preferredTimeZone = "WET";
                    break;
                case -4:
                    preferredTimeZone = "Canada/Atlantic";
                    break;
                case -5:
                    preferredTimeZone = "US/Eastern";
                    break;
                case -6:
                    preferredTimeZone = "US/Central";
                    break;
                case -7:
                    preferredTimeZone = "US/Mountain";
                    break;
                case -8:
                    preferredTimeZone = "US/Pacific";
                    break;
                case -9:
                    preferredTimeZone = "US/Alaska";
                    break;
                case -10:
                    preferredTimeZone = "US/Hawaii";
                    break;
                case -11:
                    preferredTimeZone = "US/Samoa";
                    break;
                default:
                    break;
            }
        }
        
        return preferredTimeZone;
    }
}
