package com.adfonic.adserver;


public interface FrequencyCounter {

    enum FrequencyEntity {
        CREATIVE, CAMPAIGN
    };

    /**
     * Simply return a frequency counter representing the number of times a
     * given end user has been served a given Creative within a given period
     * of time.  This method does not increment the counter.
     * @param uniqueIdentifier a String that uniquely identifies the end user
     * @param creativeId the id of the impression candidate Creative
     * @param windowSeconds the sliding window period in seconds
     * @return the count, or zero if not found
     */
    int getFrequencyCount(String uniqueIdentifier, long entityId, int windowSeconds, FrequencyEntity frequencyEntity);

    /**
     * Atomically increment a frequency counter representing the number of
     * times a given end user has been served a given Creative within a given
     * period of time.
     * @param uniqueIdentifier a String that uniquely identifies the end user
     * @param creativeId the id of the impression candidate Creative
     * @param windowSeconds the sliding window period in seconds
     * @return the new count after being incremented
     */
    int incrementFrequencyCount(String uniqueIdentifier, long creativeId, int windowSeconds, FrequencyEntity frequencyEntity);

    /**
     * Atomically decrement a frequency counter representing the number of
     * times a given end user has been served a given Creative within a given
     * period of time.
     * @param uniqueIdentifier a String that uniquely identifies the end user
     * @param creativeId the id of the impression candidate Creative
     * @param windowSeconds the sliding window period in seconds
     * @return the new count after being decremented
     */
    int decrementFrequencyCount(String uniqueIdentifier, long creativeId, int windowSeconds, FrequencyEntity frequencyEntity);

}