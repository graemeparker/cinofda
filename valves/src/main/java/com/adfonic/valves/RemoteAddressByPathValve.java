package com.adfonic.valves;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;

public class RemoteAddressByPathValve extends RequestFilterValve {
    private Pattern pathPattern;
    private Pattern ipPattern;

    @Override
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        String requestPath = request.getRequestURI();
        // we only check for IP pattern if the requestPath matches our path pattern
        if(pathPattern.matcher(requestPath).matches()) {
            String remoteAddr = request.getRemoteAddr();
            if (ipPattern.matcher(remoteAddr).matches()) {
                getNext().invoke(request, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        getNext().invoke(request, response);
    }

    @Override
    public void setAllow(String allow) {
        this.ipPattern = Pattern.compile(allow);
    }

    public void setPath(String path) {
        this.pathPattern = Pattern.compile(path);
    }
}
