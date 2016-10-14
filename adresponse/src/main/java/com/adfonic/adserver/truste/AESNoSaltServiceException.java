package com.adfonic.adserver.truste;

public class AESNoSaltServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AESNoSaltServiceException() {
    }

    public AESNoSaltServiceException(String message) {
        super(message);
    }

    public AESNoSaltServiceException(Throwable cause) {
        super(cause);
    }

    public AESNoSaltServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AESNoSaltServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
