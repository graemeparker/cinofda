package com.byyd.middleware.iface.exception;


public class FetchStrategyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FetchStrategyException() {
        super();
    }

    public FetchStrategyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FetchStrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchStrategyException(String message) {
        super(message);
    }

    public FetchStrategyException(Throwable cause) {
        super(cause);
    }
}
