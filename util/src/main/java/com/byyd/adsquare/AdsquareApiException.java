package com.byyd.adsquare;

public class AdsquareApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * By default do not fill stacktrace
     */
    private static boolean fillStackTrace = Boolean.parseBoolean(System.getProperty(AdsquareApiException.class.getSimpleName() + ".fillStackTrace", "false"));

    public AdsquareApiException(String message) {
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
