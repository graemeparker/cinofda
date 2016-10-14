package com.adfonic.cache;

public class CacheException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable t) {
        super(message, t);
    }
}