package com.byyd.middleware.iface.exception;


public class BusinessKeyDaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessKeyDaoException() {
        super();
    }

    public BusinessKeyDaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BusinessKeyDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessKeyDaoException(String message) {
        super(message);
    }

    public BusinessKeyDaoException(Throwable cause) {
        super(cause);
    }
}
