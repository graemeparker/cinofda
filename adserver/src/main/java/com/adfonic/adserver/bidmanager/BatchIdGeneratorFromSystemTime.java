package com.adfonic.adserver.bidmanager;

public class BatchIdGeneratorFromSystemTime implements BatchIdGenerator {
    private int batchDurationSeconds;

    public BatchIdGeneratorFromSystemTime(int batchDurationSeconds) {
        this.batchDurationSeconds = batchDurationSeconds;
    }

    @Override
    public long getBatchId() {
        return System.currentTimeMillis() / (batchDurationSeconds * 1000L);
    }
}
