package com.adfonic.webservices.exception;

import com.adfonic.webservices.ErrorCode;

public class AuthorizationException extends ServiceException {

    public AuthorizationException() {
        super(ErrorCode.AUTH_NO_AUTHORIZATION, "Not authorized!");
    }

}
