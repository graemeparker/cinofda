package com.byyd.breaker;

public class CircuitTargetException extends CircuitException {

    private static final long serialVersionUID = 1L;

    /**
     * By default do not fill stacktrace
     */
    private static final boolean fillStackTrace = Boolean.parseBoolean(System.getProperty(CircuitTargetException.class.getSimpleName() + ".fillStackTrace", "false"));

    // Exception class cannot be generic and we do not havecommon class for target... 
    private final Object target;

    public CircuitTargetException(Object resource, Object target, String message, Throwable cause) {
        super(resource, message, cause);
        this.target = target;
    }

    public CircuitTargetException(Object resource, Object target, String message) {
        super(resource, message);
        this.target = target;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (fillStackTrace) {
            return super.fillInStackTrace();
        } else {
            return this;
        }
    }
}
