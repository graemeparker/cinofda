package com.adfonic.dto;

import java.io.Serializable;

public abstract class AbstractPaginationSearch implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final long DEFAULT_NUMBER_OF_RECORDS = 10L;
    
    /** total number or rows in database */
    protected Long numTotalRecords;

    /** Pagination purpose */
    protected Long start = 0L;

    protected Long numberOfRecords = DEFAULT_NUMBER_OF_RECORDS;
    
    public Long getNumTotalRecords() {
        return numTotalRecords;
    }

    public void setNumTotalRecords(Long numTotalRecords) {
        this.numTotalRecords = numTotalRecords;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(Long numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }
}
