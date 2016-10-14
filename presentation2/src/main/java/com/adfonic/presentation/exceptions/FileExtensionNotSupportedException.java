package com.adfonic.presentation.exceptions;

public class FileExtensionNotSupportedException extends Exception {
    
    private static final long serialVersionUID = 4188436945608530254L;

    public FileExtensionNotSupportedException() {
    }

    public FileExtensionNotSupportedException(String message) {
        super(message);
    }

    public FileExtensionNotSupportedException(Throwable arg0) {
        super(arg0);
    }

    public FileExtensionNotSupportedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
