package com.adfonic.http;


public interface HttpExecutionCallback<R, X extends Exception> extends HttpStatusCallback<X>, HttpPayloadCallback<R, X> {

}