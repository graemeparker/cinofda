package com.adfonic.presentation.exceptions;

public class StoredProcedureException extends Exception {

    private static final long serialVersionUID = 1L;

    public StoredProcedureException(String message) {
        super(message);
    }

    public StoredProcedureException(String message, Throwable cause) {
        super(message, cause);
    }

}
