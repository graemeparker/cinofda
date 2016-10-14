package com.adfonic.adx.client;

public class AdXClientException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public AdXClientException(String message) {
        super(message);
    }
    
    public AdXClientException(String message, Throwable t) {
        super(message, t);
    }
}