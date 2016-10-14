package com.adfonic.webservices.util;

import java.io.InputStream;

public class Response {

    private int status;
    
    private InputStream content;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
