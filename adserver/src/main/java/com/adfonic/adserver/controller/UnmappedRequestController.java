package com.adfonic.adserver.controller;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.util.stats.CounterManager;

@Controller
public class UnmappedRequestController extends AbstractAdServerController {

    private static final transient Logger LOG = Logger.getLogger(UnmappedRequestController.class.getName());

    @Autowired
    private CounterManager counterManager;

    enum Counter {
        UNMAPPED_REQUEST_RECEIVED
    }

    @RequestMapping("/ad")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("No mapping found for HTTP request with URI [/ad/] in DispatcherServlet with name 'dispatcher'");
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "handleRequest",
                    "No mapping found for HTTP request with URI [/ad/] in DispatcherServlet with name 'dispatcher'");
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/plain");
        PrintStream out = new PrintStream(response.getOutputStream());
        out.print("Nothing here! Revise your integration.");
        out.close();
    }
}
