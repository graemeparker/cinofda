package com.adfonic.tasks.xaudit.exception;

public class ExternalSystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalSystemException(Throwable t) {
        super(t);
    }

    public ExternalSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalSystemException(String message) {
        super(message);
    }

}
