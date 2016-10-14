package com.adfonic.adserver;

public class BlacklistedException extends Exception {

    private static final long serialVersionUID = 1L;

    public BlacklistedException(String msg) {
        super(msg);
    }
}