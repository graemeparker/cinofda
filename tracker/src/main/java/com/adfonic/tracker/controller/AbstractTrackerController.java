package com.adfonic.tracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.support.WebApplicationObjectSupport;

public abstract class AbstractTrackerController extends WebApplicationObjectSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(AbstractTrackerController.class.getName());

    protected static final String RESPONSE_PARAM_ERROR = "error";
    protected static final String RESPONSE_PARAM_SUCCESS = "success";

    protected static final String INTERNAL_ERROR = "Internal error";
    protected static final String DUPLICATE_ERROR = "Duplicate";
    protected static final String UNKNOWN_UNIQUE_IDENTIFIER_ERROR = "Unknown unique identifier";
    protected static final String UNRECOGNIZED_DEVICE_IDENTIFIERS_ERROR = "Unrecognized device identifier(s)";

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleHttpMessageNotWritable() {
        // SC-181 - Ignore these exceptions were client disconnects
        LOG.warn("HTTP message not writable...ignoring");
    }
}
