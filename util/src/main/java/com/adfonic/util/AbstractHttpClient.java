package com.adfonic.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractHttpClient {
    
    private static final transient Logger LOG = Logger.getLogger(AbstractPoolingClientConnectionManager.class.getName());
    
    protected final PoolingClientConnectionManager connMgr;
    protected final DefaultHttpClient httpClient;
    
    public AbstractHttpClient(int connTtlMs, int maxTotal, int defaultMaxPerRoute){
        // Set up the thread-safe HTTP ClientConnectionManager
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Initializing ThreadSafeClientConnManager, connTTL=" + connTtlMs + "ms, maxTotal=" + maxTotal + ", defaultMaxPerRoute=" + defaultMaxPerRoute);
        }
        connMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), connTtlMs, TimeUnit.MILLISECONDS);
        connMgr.setMaxTotal(maxTotal);
        connMgr.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // Set up the shared HttpClient
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Initializing DefaultHttpClient");
        }
        httpClient = new DefaultHttpClient(connMgr);
    }
    
    /**
     * Execute an HTTP request with retry support. If the response handler
     * throws a RetryRequestException, the request will be retried. This is
     * useful in case you want to invoke a retry upon encountering a 503.
     */
    protected <T> T executeWithRetrySupport(HttpUriRequest request, ResponseHandler<T> responseHandler) throws java.io.IOException {
        return executeWithRetrySupport(request, responseHandler, new BasicHttpContext());
    }

    /**
     * Execute an HTTP request with retry support. If the response handler
     * throws a RetryRequestException, the request will be retried. This is
     * useful in case you want to invoke a retry upon encountering a 503.
     */
    protected <T> T executeWithRetrySupport(HttpUriRequest request, ResponseHandler<T> responseHandler, HttpContext context) throws java.io.IOException {
        while (true) {
            try {
                return httpClient.execute(request, responseHandler, context);
            } catch (RetryRequestException e) {
                LOG.warning("Retrying " + request.getURI() + " due to: " + e.getMessage());
                if (e.getSleepBeforeRetrying() > 0) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Sleeping " + e.getSleepBeforeRetrying() + " before retrying");
                    }
                    try {
                        Thread.sleep(e.getSleepBeforeRetrying());
                    } catch (InterruptedException e2) {
                        LOG.warning("Interrupted");
                        throw new HttpResponseException(e.getStatusCode(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * This implementation of the request retry handler places no limit on the
     * number of times a given request may be retried. This provides support for
     * "waiting forever" for a server to respond, allowing the server to be down
     * and not accepting connections for a period of time.
     */
    protected static class UnlimitedHttpRequestRetryHandler implements HttpRequestRetryHandler {
        public UnlimitedHttpRequestRetryHandler() {
        }

        @Override
        public boolean retryRequest(java.io.IOException exception, int executionCount, HttpContext context) {
            // These checks are copied from DefaultHttpRequestRetryHandler
            if (exception == null) {
                throw new IllegalArgumentException("Exception parameter may not be null");
            } else if (context == null) {
                throw new IllegalArgumentException("HTTP context may not be null");
            } else if (exception instanceof java.net.UnknownHostException) {
                return false; // There's no recovering from this, should never
                              // be retried
            } else if (exception instanceof javax.net.ssl.SSLException) {
                return false; // SSL handshake exceptions should never be
                              // retried
            }

            HttpRequest httpRequest = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
            String requestDescription;
            if (httpRequest != null) {
                RequestLine requestLine = httpRequest.getRequestLine();
                if (requestLine != null) {
                    requestDescription = requestLine.getMethod() + " " + requestLine.getUri();
                } else {
                    requestDescription = "unknown request";
                }
            } else {
                requestDescription = "unknown request";
            }

            HttpHost httpHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            String hostDescription;
            if (httpHost != null) {
                hostDescription = httpHost.toString();
            } else {
                hostDescription = "unknown host";
            }

            LOG.info("Retrying " + requestDescription + " on " + hostDescription);
            return true; // allow the retry
        }
    }

    /**
     * Special exception that a ResponseHandler can throw in order to signal
     * that the request should be retried...i.e. when a 503 is encountered.
     * 
     * @see executeWithRetrySupport
     */
    protected static final class RetryRequestException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        private final int statusCode;
        private final long sleepBeforeRetrying;

        public RetryRequestException(int statusCode, String message, long sleepBeforeRetrying) {
            super(message);
            this.statusCode = statusCode;
            this.sleepBeforeRetrying = sleepBeforeRetrying;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public long getSleepBeforeRetrying() {
            return sleepBeforeRetrying;
        }
    }
}
