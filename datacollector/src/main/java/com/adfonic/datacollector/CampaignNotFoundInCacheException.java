package com.adfonic.datacollector;

public class CampaignNotFoundInCacheException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CampaignNotFoundInCacheException() {
        super();
    }

    public CampaignNotFoundInCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CampaignNotFoundInCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CampaignNotFoundInCacheException(String message) {
        super(message);
    }

    public CampaignNotFoundInCacheException(Throwable cause) {
        super(cause);
    }
}
