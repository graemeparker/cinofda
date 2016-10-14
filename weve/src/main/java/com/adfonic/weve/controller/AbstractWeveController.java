package com.adfonic.weve.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.support.WebApplicationObjectSupport;

public abstract class AbstractWeveController extends WebApplicationObjectSupport {
    private static final transient Logger LOG = LogManager.getLogger(AbstractWeveController.class.getName());

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleHttpMessageNotWritable() {
        // Ignore these exceptions that were occurring when the client
        // disconnects prior to us writing our response
        // The cause is usually:
        // 
        // java.net.SocketException: Connection reset
        //
        LOG.info("HTTP message not writable...ignoring");
    }
}
