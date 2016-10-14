package com.adfonic.adserver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adfonic.adserver.FrequencyCounter;

public abstract class AbstractFrequencyCounter implements FrequencyCounter {

    private static final transient Logger LOG = Logger.getLogger(AbstractFrequencyCounter.class.getName());
    private static final String SEPARATOR = "/";
    private static final Pattern VALUE_PATTERN = Pattern.compile("^(\\d+)" + SEPARATOR + "(\\d+)$");

    protected String makeKey(final String uniqueIdentifier, final long entityId, FrequencyEntity frequencyEntity) {
        String key;
        
        switch (frequencyEntity) {
            case CAMPAIGN:
                key = uniqueIdentifier + ".c." + entityId;
                break;
            default:  // CREATIVE
                key = uniqueIdentifier + "." + entityId;
                break;
        }
        
        return key;
    }

    /** Get a value from cache */
    protected abstract String getValue(String key);

    /** Set a value in cache */
    protected abstract void setValue(String key, String value, long expireTimestamp);

    /** {@inheritDoc} */
    @Override
    public int getFrequencyCount(String uniqueIdentifier, long entityId, int windowSeconds, FrequencyEntity frequencyEntity) {
        String key = makeKey(uniqueIdentifier, entityId, frequencyEntity);
        String value;
        try {
            value = getValue(key);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get value for \"" + key + "\"", e);
            return 0;
        }

        if (value == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For key=" + key + ", value not found");
            }
            return 0;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("For key=" + key + ", found cached value=" + value);
        }
        Matcher matcher = VALUE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            LOG.warning("For key=" + key + ", unexpected value format: " + value);
            return 0;
        }

        // See if the expiryTime has elapsed already
        long expireTime = Long.parseLong(matcher.group(2));
        if (expireTime <= System.currentTimeMillis()) {
            // It expired...
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For key=" + key + ", expired");
            }
            return 0;
        }

        // Hasn't expired yet...grab the count
        return Integer.parseInt(matcher.group(1));
    }

    /** {@inheritDoc} */
    @Override
    public int incrementFrequencyCount(String uniqueIdentifier, long entityId, int windowSeconds, FrequencyEntity frequencyEntity) {
        String key = makeKey(uniqueIdentifier, entityId, frequencyEntity);
        String value = null;
        try {
            value = getValue(key);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get value for \"" + key + "\"", e);
        }

        int count = 0;
        long expireTime = 0;
        if (value != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For key=" + key + ", found cached value=" + value);
            }
            Matcher matcher = VALUE_PATTERN.matcher(value);
            if (matcher.matches()) {
                // See if the expiryTime has elapsed already
                expireTime = Long.parseLong(matcher.group(2));
                if (expireTime <= System.currentTimeMillis()) {
                    // It expired...reset the count
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("For key=" + key + ", expired, resetting count");
                    }
                } else {
                    // Hasn't expired yet...grab the count and increment it
                    count = Integer.parseInt(matcher.group(1)) + 1;
                }
            } else {
                LOG.warning("For key=" + key + ", unexpected value format: " + value);
            }
        }

        if (count == 0) {
            // Initialize the count
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For key=" + key + ", not cached, initializing count");
            }
            count = 1;
            expireTime = System.currentTimeMillis() + (windowSeconds * 1000L);
        }

        // Update it in cache
        value = count + SEPARATOR + expireTime;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("For key=" + key + ", setting value=" + value);
        }
        setValue(key, value, expireTime);
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public int decrementFrequencyCount(String uniqueIdentifier, long entityId, int windowSeconds, FrequencyEntity frequencyEntity) {
        String key = makeKey(uniqueIdentifier, entityId, frequencyEntity);
        String value = null;
        try {
            value = getValue(key);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get value for \"" + key + "\"", e);
        }

        int count = 0;
        long expireTime = 0;
        if (value != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("For key=" + key + ", found cached value=" + value);
            }
            Matcher matcher = VALUE_PATTERN.matcher(value);
            if (matcher.matches()) {
                // See if the expiryTime has elapsed already
                expireTime = Long.parseLong(matcher.group(2));
                if (expireTime <= System.currentTimeMillis()) {
                    // It expired...reset the count
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("For key=" + key + ", expired, resetting count");
                    }
                } else {
                    // Hasn't expired yet...grab the count and decrement it if > 0
                    count = Integer.parseInt(matcher.group(1));
                    if (count > 0)
                        count--;
                }
            } else {
                LOG.warning("For key=" + key + ", unexpected value format: " + value);
            }
        }

        if (count == 0) {
            expireTime = System.currentTimeMillis() + (windowSeconds * 1000L);
        }
        // Update it in cache
        value = count + SEPARATOR + expireTime;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("For key=" + key + ", setting value=" + value);
        }
        setValue(key, value, expireTime);
        return count;
    }
}
