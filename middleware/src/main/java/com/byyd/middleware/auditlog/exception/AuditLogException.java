package com.byyd.middleware.auditlog.exception;

public class AuditLogException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuditLogException() {
        super();
    }

    public AuditLogException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuditLogException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuditLogException(String message) {
        super(message);
    }

    public AuditLogException(Throwable cause) {
        super(cause);
    }
}
