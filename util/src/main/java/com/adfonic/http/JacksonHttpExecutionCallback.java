package com.adfonic.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectReader;

/**
 * 
 * @author mvanek
 *
 * @param <R>
 * @param <X>
 */
public class JacksonHttpExecutionCallback<R, X extends Exception> implements HttpExecutionCallback<R, X> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpErrorCallback<X> errorCallback;

    private final String expectedMimeType;

    private final int expectedStatusCode;

    private final ObjectReader objectReader;

    public JacksonHttpExecutionCallback(HttpErrorCallback<X> errorCallback, String expectedMimeType, int expectedStatusCode, ObjectReader objectReader) {
        Objects.requireNonNull(errorCallback);
        this.errorCallback = errorCallback;
        this.expectedMimeType = expectedMimeType;
        this.expectedStatusCode = expectedStatusCode;
        this.objectReader = objectReader;
    }

    @Override
    public void onResponseStatus(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws X {
        StatusLine statusLine = httpResponse.getStatusLine();
        if ((expectedStatusCode != -1 && statusLine.getStatusCode() != expectedStatusCode) || (expectedMimeType != null && !expectedMimeType.equals(mimeType))) {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity == null) {
                throw errorCallback.onResponseStatusException("Error response status: " + statusLine + ", mimeType: " + mimeType + ", message: <empty>");
            } else {
                long contentLength = httpEntity.getContentLength();
                if (logger.isDebugEnabled()) {
                    logger.debug("Unexpected response status: " + statusLine + ", mimeType: " + mimeType + ", length: " + contentLength);
                }
                try (InputStream stream = httpEntity.getContent()) {
                    // API can send some error payload along with error status. We need to read it to be able to reuse http connection 
                    String errorMessage = ApiClient.read(stream, contentLength, charset);
                    throw errorCallback.onResponseStatusException("Error response status: " + statusLine + ", mimeType: " + mimeType + ", message: " + errorMessage);
                } catch (IOException iox) {
                    throw errorCallback.onResponseStatusException("Failed to read error response. Status: " + statusLine + ", mimeType: " + mimeType + ", exception: " + iox);
                }
            }
        }
    }

    @Override
    public R onResponsePayload(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws X {
        StatusLine statusLine = httpResponse.getStatusLine();
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity == null) { // Null on HTTP 204 No Response 
            if (logger.isDebugEnabled()) {
                logger.debug("Returning null as response has no payload. Status: " + statusLine + ", mimeType: " + mimeType);
            }
            return null;
        }
        // When reponse is comprimed - Content-Encoding: gzip 
        // Then httpclient removes Content-Length header and following contentLength returns -1
        long contentLength = httpEntity.getContentLength();
        if (logger.isDebugEnabled()) {
            logger.debug("Processing response status: " + statusLine + ", mimeType: " + mimeType + ", length: " + contentLength);
        }
        try (InputStream stream = httpEntity.getContent()) {
            if (objectReader != null) {
                R response = objectReader.readValue(new InputStreamReader(stream, charset));
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning parsed response " + response);
                }
                return response;
            } else {
                ApiClient.trash(stream, contentLength); // not interested in response
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning null as response is consumed but ignored. Status: " + statusLine + ", mimeType: " + mimeType + ", length: " + contentLength);
                }
                return null;
            }
        } catch (IOException iox) {
            throw errorCallback.onResponsePayloadException(httpRequest, iox);
        }
    }
}
