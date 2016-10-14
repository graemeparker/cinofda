package com.adfonic.presentation.exceptions;

public class NotContentTypeException extends Exception {
    
    private static final long serialVersionUID = 4188436945608530254L;

    public NotContentTypeException() {
    }

    public NotContentTypeException(String message) {
        super(message);
    }

    public NotContentTypeException(Throwable arg0) {
        super(arg0);
    }

    public NotContentTypeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
