package com.byyd.middleware.iface.exception;


public class SortOrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SortOrderException() {
        super();
    }

    public SortOrderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SortOrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SortOrderException(String message) {
        super(message);
    }

    public SortOrderException(Throwable cause) {
        super(cause);
    }
}
