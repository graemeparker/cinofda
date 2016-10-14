package com.adfonic.adserver.impl;

import com.adfonic.adserver.FrequencyCounter;

public class SharedNamespaceFrequencyCounterTestImpl implements FrequencyCounter {

    public SharedNamespaceFrequencyCounterTestImpl() {
        //System.out.println("creating TestSharedNamespaceFrequencyCounter");
    }

    @Override
    public int getFrequencyCount(String uniqueIdentifier, long creativeId, int windowSeconds, FrequencyEntity frequencyEntity) {
        //System.out.println("getFrequencyCount returning 0");
        return 0;
    }

    @Override
    public int incrementFrequencyCount(String uniqueIdentifier, long creativeId, int windowSeconds, FrequencyEntity frequencyEntity) {
        //System.out.println("setFrequencyCount = 0");
        return 0;
    }

    @Override
    public int decrementFrequencyCount(String uniqueIdentifier, long creativeId, int windowSeconds, FrequencyEntity frequencyEntity) {
        //System.out.println("setFrequencyCount = 0");
        return 0;
    }

}
