package com.adfonic.webservices.exception;

public class AuthenticationException extends Exception {
    private final int code;
    
    public AuthenticationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AuthenticationException(int code, String message, Throwable t) {
        super(message, t);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
