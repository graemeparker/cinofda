package com.byyd.middleware.account.exception;

public class AdvertiserCloudManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AdvertiserCloudManagerException() {
        super();
    }

    public AdvertiserCloudManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AdvertiserCloudManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvertiserCloudManagerException(String message) {
        super(message);
    }

    public AdvertiserCloudManagerException(Throwable cause) {
        super(cause);
    }
}
