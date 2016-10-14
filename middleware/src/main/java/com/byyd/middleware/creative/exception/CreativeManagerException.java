package com.byyd.middleware.creative.exception;


public class CreativeManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CreativeManagerException() {
        super();
    }

    public CreativeManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CreativeManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreativeManagerException(String message) {
        super(message);
    }

    public CreativeManagerException(Throwable cause) {
        super(cause);
    }
}
