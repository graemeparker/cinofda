package com.adfonic.presentation.exceptions;

public class SizeNotSupportedException extends Exception {
    
    private static final long serialVersionUID = 4188436945608530254L;

    public SizeNotSupportedException() {
    }

    public SizeNotSupportedException(String message) {
        super(message);
    }

    public SizeNotSupportedException(Throwable arg0) {
        super(arg0);
    }

    public SizeNotSupportedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
