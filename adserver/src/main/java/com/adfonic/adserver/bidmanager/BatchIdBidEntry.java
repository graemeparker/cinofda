package com.adfonic.adserver.bidmanager;

public class BatchIdBidEntry<T> {

    private long batchId;
    private T element;

    public BatchIdBidEntry(long batchId, T element) {
        this.batchId = batchId;
        this.element = element;
    }

    public long getBatchId() {
        return batchId;
    }

    public T getElement() {
        return element;
    }

}
