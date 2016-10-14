package com.byyd.factual;


public class FactualApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * By default do not fill stacktrace
     */
    private static boolean fillStackTrace = Boolean.parseBoolean(System.getProperty(FactualApiException.class.getSimpleName() + ".fillStackTrace", "false"));

    public FactualApiException(String message) {
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