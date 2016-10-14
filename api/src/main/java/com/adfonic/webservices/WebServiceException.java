package com.adfonic.webservices;

public class WebServiceException extends Exception {
    private final int code;
    private final String responseFormat;
    private Integer responseStatus;
    
    public WebServiceException(int code, String msg, String responseFormat) {
        super(msg);
        this.code = code;
        this.responseFormat = responseFormat;
    }

    public WebServiceException(int code, String msg, int responseStatus, String responseFormat) {
        this(code, msg, responseFormat);
        this.responseStatus=Integer.valueOf(responseStatus);
    }

    public WebServiceException(int code, String msg, String responseFormat, Throwable t) {
        super(msg, t);
        this.code = code;
        this.responseFormat = responseFormat;
    }

    public int getCode() {
        return code;
    }
    
    public String getResponseFormat() {
        return responseFormat;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }
}
