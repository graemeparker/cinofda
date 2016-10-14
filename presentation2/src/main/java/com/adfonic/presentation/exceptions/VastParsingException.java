package com.adfonic.presentation.exceptions;

public class VastParsingException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public VastParsingException() {
    }

    public VastParsingException(String message) {
        super(message);
    }

    public VastParsingException(Throwable cause) {
        super(cause);
    }

    public VastParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public VastParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
