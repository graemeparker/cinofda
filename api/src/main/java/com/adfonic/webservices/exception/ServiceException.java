package com.adfonic.webservices.exception;

public class ServiceException extends RuntimeException {

    int errorCode;

    public ServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return (errorCode);
    }
}
