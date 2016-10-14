package com.adfonic.retargeting.citrusleaf;

import com.adfonic.retargeting.CacheException;

public class CitrusleafException extends CacheException {
    private static final long serialVersionUID = 1L;

    public CitrusleafException(String message) {
        super(message, "Aerospike operation");
    }

    public CitrusleafException(String message, Throwable t) {
        super(message, t);
    }
}