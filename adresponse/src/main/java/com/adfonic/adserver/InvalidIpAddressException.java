package com.adfonic.adserver;

public class InvalidIpAddressException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidIpAddressException(String msg) {
        super(msg);
    }

    public InvalidIpAddressException(String msg, Throwable t) {
        super(msg, t);
    }
}