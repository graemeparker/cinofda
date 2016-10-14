package com.adfonic.http;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

/**
 * 
 * @author mvanek
 *
 * @param <X>
 */
public interface HttpErrorCallback<X extends Exception> {

    X onRequestException(HttpRequest httpRequest, HttpHost httpHost, Exception x);

    // X onResponseStatusException(HttpRequest httpRequest, StatusLine status);

    X onResponseStatusException(String message);

    X onResponsePayloadException(HttpRequest httpRequest, IOException iox);
}