package com.byyd.breaker;

/**
 * 
 * @author mvanek
 *
 */
public class CircuitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final boolean fillStackTrace = Boolean.parseBoolean(System.getProperty(CircuitException.class.getSimpleName() + ".fillStackTrace", "false"));

    private final Object resource;

    public CircuitException(Object resource, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
    }

    public CircuitException(Object resource, String message) {
        super(message);
        this.resource = resource;
    }

    public Object getResource() {
        return resource;
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
