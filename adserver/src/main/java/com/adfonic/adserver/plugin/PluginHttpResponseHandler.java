package com.adfonic.adserver.plugin;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

public class PluginHttpResponseHandler {
    /**
     * Handle the response as needed, and either throw an exception or return the response content.
     * This method may be overridden as necessary.  The default version simply enforces that the
     * response status wasn't 300 or greater, and returns the response content.
     * @param httpResponse the given HttpResponse
     * @param httpEntity the given HttpEntity, which will be closed for you
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String handleResponse(HttpResponse httpResponse, HttpEntity httpEntity) throws java.io.IOException, PluginException {
        // Require a non-error HTTP status response
        checkResponse(httpResponse);
            
        // Grab the response content
        return getContent(httpEntity);
    }

    /**
     * Get the response content for a given HttpEntity
     * @param HttpEntity the given httpEntity
     * @return the response content
     */
    protected final String getContent(HttpEntity httpEntity) throws java.io.IOException {
        return IOUtils.toString(httpEntity.getContent());
    }

    /**
     * Require that the response status code was less than 300
     * @throws HttpResponseException if the status code was 300 or over
     */
    protected final void checkResponse(HttpResponse httpResponse) throws HttpResponseException {
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.toString());
        }
    }

    /**
     * Get an HTTP response header
     * @param httpResponse the HttpResponse object
     * @param name the name of the header
     * @return the first available value of the given header, or null if not found
     */
    protected final String getHeader(HttpResponse httpResponse, String name) {
        Header header = httpResponse.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }
}
