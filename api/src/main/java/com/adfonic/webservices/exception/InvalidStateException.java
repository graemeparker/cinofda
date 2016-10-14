package com.adfonic.webservices.exception;

import com.adfonic.webservices.ErrorCode;

public class InvalidStateException extends ServiceException {

    public InvalidStateException(String message) {
        super(ErrorCode.INVALID_STATE, message);
    }


    public InvalidStateException() {
        this("Invalid state for operation!");
    }

}
