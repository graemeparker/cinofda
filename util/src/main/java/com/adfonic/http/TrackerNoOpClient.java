package com.adfonic.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;

public class TrackerNoOpClient implements Closeable {

    private final ApiClient apiClient;
    private static final TrackerNoOpExecutionCallback execCallback = new TrackerNoOpExecutionCallback();
    private static final TrackerNoOpErrorCallback errorCallback = new TrackerNoOpErrorCallback() {

    };

    public TrackerNoOpClient(String connectionString) {
        this(connectionString, 200, 100, 10, 60, 10, 10_000);
    }

    public TrackerNoOpClient(String connectionString, int connectTimeoutMs, int readTimeoutMs, int poolTargetMax, int poolTtlSeconds, int failThreshold, int failLockdownMs) {
        apiClient = new ApiClient("TNOC", connectionString, connectTimeoutMs, readTimeoutMs, poolTargetMax, poolTtlSeconds, failThreshold, failLockdownMs);
    }

    @Override
    public void close() {
        apiClient.close();
    }

    public void reset() {
        apiClient.reset();
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * /**
     * Caller is responsible for correct url encoding of @param urlSuffix
     */
    public void track(String urlSuffix) {
        StringBuilder urlPath = new StringBuilder("/x");
        if (urlSuffix.charAt(0) != '/') {
            urlPath.append('/');
        }
        urlPath.append(urlSuffix);
        HttpGet httpGet = new HttpGet(urlPath.toString());
        apiClient.execute(httpGet, execCallback, errorCallback);
    }

    static class TrackerNoOpExecutionCallback implements HttpExecutionCallback<Void, IllegalStateException> {

        @Override
        public void onResponseStatus(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws IllegalStateException {
            StatusLine statusLine = httpResponse.getStatusLine();
            if (statusLine.getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return;
            } else {
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity == null) {
                    throw errorCallback.onResponseStatusException("Error response status: " + statusLine + ", mimeType: " + mimeType + ", message: <empty>");
                } else {
                    long contentLength = httpEntity.getContentLength();
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
        public Void onResponsePayload(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws IllegalStateException {
            return null; // There is no response expected
        }

    }

    static class TrackerNoOpErrorCallback extends DefaultHttpErrorCallback<IllegalStateException> {

        @Override
        public IllegalStateException newException(String message) {
            return new IllegalStateException(message);
        }

        @Override
        public IllegalStateException newException(String message, Exception x) {
            return new IllegalStateException(message + " " + x);
        }

    }
}
