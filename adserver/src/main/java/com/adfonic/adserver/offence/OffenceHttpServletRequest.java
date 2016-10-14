package com.adfonic.adserver.offence;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class OffenceHttpServletRequest {

    private final int contentLength;
    private final String characterEncoding;
    private final String contentType;
    private final Map<String, String[]> parameterMap;
    private final String protocol;
    private final String scheme;
    private final String serverName;
    private final int serverPort;
    private final String remoteAddr;
    private final String remoteHost;
    private final int remotePort;
    private final boolean secure;

    private final Enumeration<Locale> locales;
    private final Locale locale;

    private final String localName;
    private final String localAddr;
    private final int localPort;

    private final String servletPath;
    private final StringBuffer requestURL;
    private final String requestURI;
    private final String requestedSessionId;
    private final Principal userPrincipal;
    private final String remoteUser;
    private final String queryString;
    private final String contextPath;
    private final String pathTranslated;
    private final String pathInfo;
    private final String method;
    private final String authType;
    //private final DispatcherType dispatcherType;
    //private final boolean asyncStarted;
    //private final boolean asyncSupported;
    private final Map<String, String[]> headerMap;

    public OffenceHttpServletRequest(HttpServletRequest delegate) {
        this.characterEncoding = delegate.getCharacterEncoding();
        this.contentLength = delegate.getContentLength();
        this.contentType = delegate.getContentType();
        this.parameterMap = delegate.getParameterMap();
        this.protocol = delegate.getProtocol();
        this.scheme = delegate.getScheme();
        this.serverName = delegate.getServerName();
        this.serverPort = delegate.getServerPort();
        this.remoteAddr = delegate.getRemoteAddr();
        this.remoteHost = delegate.getRemoteHost();
        this.remotePort = delegate.getRemotePort();
        this.secure = delegate.isSecure();
        this.localAddr = delegate.getLocalAddr();
        this.localName = delegate.getLocalName();
        this.localPort = delegate.getLocalPort();

        this.servletPath = delegate.getServletPath();
        this.requestURL = delegate.getRequestURL();
        this.requestURI = delegate.getRequestURI();
        this.requestedSessionId = delegate.getRequestedSessionId();
        this.userPrincipal = delegate.getUserPrincipal();
        this.remoteUser = delegate.getRemoteUser();
        this.queryString = delegate.getQueryString();
        this.contextPath = delegate.getContextPath();
        this.pathTranslated = delegate.getPathTranslated();
        this.pathInfo = delegate.getPathInfo();
        this.method = delegate.getMethod();
        this.authType = delegate.getAuthType();
        //this.dispatcherType = delegate.getDispatcherType(); Servlet 3.0 abstract

        this.locales = delegate.getLocales();
        this.locale = delegate.getLocale();
        //this.asyncStarted = delegate.isAsyncStarted();
        //this.asyncSupported = delegate.isAsyncSupported();

        Enumeration<String> headerNames = delegate.getHeaderNames();
        this.headerMap = new HashMap<String, String[]>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = delegate.getHeaders(headerName);
            List<String> values = new LinkedList<String>();
            while (headerValues.hasMoreElements()) {
                values.add(headerValues.nextElement());
            }
            String[] valuesArray = values.toArray(new String[values.size()]);
            this.headerMap.put(headerName, valuesArray);
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public boolean isSecure() {
        return secure;
    }

    public Enumeration<Locale> getLocales() {
        return locales;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLocalName() {
        return localName;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getServletPath() {
        return servletPath;
    }

    public StringBuffer getRequestURL() {
        return requestURL;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getMethod() {
        return method;
    }

    public String getAuthType() {
        return authType;
    }

    public Map<String, String[]> getHeaderMap() {
        return headerMap;
    }

}
