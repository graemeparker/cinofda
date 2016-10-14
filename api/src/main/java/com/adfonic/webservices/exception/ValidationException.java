package com.adfonic.webservices.exception;

import com.adfonic.webservices.ErrorCode;

public class ValidationException extends ServiceException{

    public ValidationException(String message){
        super(ErrorCode.VALIDATION_ERROR, message);
    }
    
}
