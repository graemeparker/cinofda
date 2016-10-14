package com.byyd.celtra;


/**
 * 
 * @author mvanek
 *
 */
public class CeltraApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * By default do not fill stacktrace
     */
    private static boolean fillStackTrace = Boolean.parseBoolean(System.getProperty(CeltraApiException.class.getSimpleName() + ".fillStackTrace", "false"));

    public CeltraApiException(String message) {
        super(message);
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
