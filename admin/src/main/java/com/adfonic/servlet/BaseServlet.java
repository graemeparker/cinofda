package com.adfonic.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.HttpRequestHandler;

import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailService;
import com.byyd.middleware.creative.service.AssetManager;

public abstract class BaseServlet implements HttpRequestHandler {
    @Autowired
    @Qualifier("emailService")
    protected EmailService emailService;

    @Autowired
    protected EmailAddressManager emailAddrMgr;

    @Autowired
    protected AssetManager assetManager;

    public abstract void handleRequest(HttpServletRequest request,
                                       HttpServletResponse response)
        throws javax.servlet.ServletException,
               java.io.IOException;
}
