package com.adfonic.email;

public class EmailException extends Exception {
    static final long serialVersionUID = 1L;
    
    public EmailException(String msg) {
        super(msg);
    }
    public EmailException(String msg, Throwable t) {
        super(msg, t);
    }
    public EmailException(Throwable t) {
        super(t);
    }
}
