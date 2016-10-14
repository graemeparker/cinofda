package com.adfonic.presentation.exceptions;

public class BigFileException extends Exception {
    
    private static final long serialVersionUID = 4188436945608530254L;

    public BigFileException() {
    }

    public BigFileException(String message) {
        super(message);
    }

    public BigFileException(Throwable arg0) {
        super(arg0);
    }

    public BigFileException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
