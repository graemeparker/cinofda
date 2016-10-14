package com.adfonic.retargeting;

public class RetargetingException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RetargetingException(String message) {
        super(message);
    }

    public RetargetingException(String message, Throwable t) {
        super(message, t);
    }
}