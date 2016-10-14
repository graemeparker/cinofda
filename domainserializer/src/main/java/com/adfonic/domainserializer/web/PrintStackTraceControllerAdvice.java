package com.adfonic.domainserializer.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Shared for all @Controllers...
 */
@ControllerAdvice
public class PrintStackTraceControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public void exception(Exception exception, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        String queryString = httpRequest.getQueryString();
        String message = httpRequest.getRequestURI() + (queryString == null ? "" : "?" + queryString);
        logger.error(message, exception);
        exception.printStackTrace(httpResponse.getWriter());
    }
}
