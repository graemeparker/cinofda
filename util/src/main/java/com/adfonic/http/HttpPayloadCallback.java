package com.adfonic.http;

import java.nio.charset.Charset;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public interface HttpPayloadCallback<R, X extends Exception> {

    R onResponsePayload(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws X;
}
