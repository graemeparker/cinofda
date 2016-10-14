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
public abstract class DefaultHttpErrorCallback<X extends Exception> implements HttpErrorCallback<X> {

    public abstract X newException(String message);

    public abstract X newException(String message, Exception x);

    @Override
    public X onRequestException(HttpRequest httpRequest, HttpHost httpHost, Exception x) {
        if (x instanceof java.net.SocketTimeoutException || x instanceof org.apache.http.conn.ConnectTimeoutException) {
            // For timeout exceptions print only one-liner... 
            return newException("Failed to execute " + httpRequest + " on " + httpHost + " - " + x);
        } else {
            // For the other exceptions, subclass will decide how to handle it...
            return newException("Failed to execute " + httpRequest + " on " + httpHost, x);
        }
    }

    @Override
    public X onResponseStatusException(String message) {
        return newException(message);
    }

    @Override
    public X onResponsePayloadException(HttpRequest httpRequest, IOException iox) {
        return newException("Failed to process response: " + iox);
    }

}