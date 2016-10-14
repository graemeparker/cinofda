package com.adfonic.retargeting;

public class CacheException extends Exception {
    
    private static final long serialVersionUID = 1L;

    private final String customMessage;
    
    public CacheException() {
        super();
        this.customMessage = "Cache operation";
    }

    public CacheException(String message, String customMessage) {
        super(message);
        this.customMessage = customMessage;
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
        this.customMessage = "Cache operation";
    }

    public String getCustomMessage() {
        return customMessage;
    }

}