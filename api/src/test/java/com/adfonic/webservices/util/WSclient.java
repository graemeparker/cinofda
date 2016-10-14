package com.adfonic.webservices.util;

import com.adfonic.webservices.util.WSFixture.Format;

public interface WSclient {

    public String postForm(String url, com.adfonic.webservices.util.Form form) throws Exception;

    public String postForm(String url, String user, String password, com.adfonic.webservices.util.Form form) throws Exception;
    
    public String get(String url, String user, String password) throws Exception;

    public Response getResponse(String url, String user, String password) throws Exception;
    
    public String post(String url, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception;
    
    public String put(String url, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception;
    
    public void delete(String url, Format format, int expectedStatus) throws Exception;
}
