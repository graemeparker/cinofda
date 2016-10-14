package com.adfonic.http;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.util.Pair;
import com.byyd.breaker.CircuitTargetTemplate;
import com.byyd.breaker.CircuitTargetTemplate.TargetResource;

/**
 * 
 * @author mvanek
 *
 */
public class ApiClient implements Closeable {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON_UTF8 = APPLICATION_JSON + "; charset=UTF-8";
    public static final Charset UTF_8 = Charset.forName("utf-8");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String name;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int poolTargetMax;
    private final int poolTtlSeconds;

    private final HttpHost[] httpHosts;

    private final CircuitTargetTemplate<HttpHost> breakerTemplate;

    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private StaleConnectionMonitorThread killerThread;

    public ApiClient(String name, String connectionString, int connectTimeoutMs, int readTimeoutMs, int poolTargetMax, int poolTtlSeconds, int failThreshold, int failLockdownMs) {
        this.name = name;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.poolTargetMax = poolTargetMax;
        this.poolTtlSeconds = poolTtlSeconds;

        this.httpHosts = toHttpHosts(connectionString.split(","));
        this.breakerTemplate = new CircuitTargetTemplate<HttpHost>(Arrays.asList(httpHosts), failThreshold, failLockdownMs);

        Pair<CloseableHttpClient, PoolingHttpClientConnectionManager> pair = buildHttpClient(name, connectTimeoutMs, readTimeoutMs, httpHosts.length, poolTargetMax, poolTtlSeconds);
        this.httpClient = pair.first;
        this.connectionManager = pair.second;

        this.killerThread = new StaleConnectionMonitorThread(name + "-http-close", 5000, poolTtlSeconds, connectionManager);
        this.killerThread.start();

        logger.info("server: " + connectionString);
    }

    /**
     * Circuit breaker managed execution of http HttpRequest 
     */
    public <R, X extends Exception> R execute(HttpRequest httpRequest, HttpExecutionCallback<R, X> execCallback, HttpErrorCallback<X> errorCallback) {
        return breakerTemplate.execute(new HttpResource<R>(httpRequest) {

            @Override
            public R call(HttpHost httpHost) throws X {
                return execute(httpHost, httpRequest, execCallback, errorCallback);
            }
        });
    }

    /**
     * Execute HttpRequest using specific HttpHost
     */
    protected <R, X extends Exception> R execute(HttpHost httpHost, HttpRequest httpRequest, HttpExecutionCallback<R, X> execCallback, HttpErrorCallback<X> errorCallback) throws X {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + httpHost + " " + httpRequest.getRequestLine());
        }

        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpHost, httpRequest);
        } catch (IOException iox) {
            throw errorCallback.onRequestException(httpRequest, httpHost, iox);
        }

        Object[] contentType = digContentType(httpResponse);
        String mimeType = (String) contentType[0];
        Charset charset = (Charset) contentType[1];
        // Header encodingHeader = entity.getContentEncoding(); // gzip

        execCallback.onResponseStatus(httpRequest, httpHost, httpResponse, mimeType, charset);

        return execCallback.onResponsePayload(httpRequest, httpHost, httpResponse, mimeType, charset);
    }

    private final Object[] digContentType(HttpResponse httpResponse) {
        String mimeType;
        Charset charset;
        Header header = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        if (header != null) {
            String contentTypeValue = header.getValue();
            int indexOfCharset = contentTypeValue.indexOf("charset=");
            if (indexOfCharset != -1) {
                mimeType = contentTypeValue.substring(0, contentTypeValue.indexOf(';'));
                charset = Charset.forName(contentTypeValue.substring(indexOfCharset + 8));
            } else {
                mimeType = contentTypeValue;
                charset = ApiClient.UTF_8;
            }
        } else {
            mimeType = null;
            charset = ApiClient.UTF_8;
        }
        return new Object[] { mimeType, charset };
    }

    /**
     * Leak internal pool stats for monitoring 
     */
    public PoolStats getTotalStats() {
        return connectionManager.getTotalStats();
    }

    public HttpHost[] getHttpHosts() {
        return httpHosts;
    }

    public CircuitTargetTemplate<HttpHost> getBreakerTemplate() {
        return breakerTemplate;
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException iox) {
            // let it be...
        }
        killerThread.shutdown();
    }

    /**
     * If it happen that connection pool get stuck and all calls end with ConnectionPoolTimeoutException (seen it happening)
     */
    public void reset() {
        try {
            httpClient.close();
        } catch (IOException iox) {
            // ignore
        }
        Pair<CloseableHttpClient, PoolingHttpClientConnectionManager> pair = buildHttpClient(name, connectTimeoutMs, readTimeoutMs, httpHosts.length, poolTargetMax, poolTtlSeconds);
        httpClient = pair.first;
        connectionManager = pair.second;

        killerThread.shutdown();
        killerThread = new StaleConnectionMonitorThread("factual-http-close", 5000, poolTtlSeconds, connectionManager);
        killerThread.start();
    }

    public static HttpHost[] toHttpHosts(String[] strings) {
        HttpHost[] retval = new HttpHost[strings.length];
        for (int i = 0; i < strings.length; ++i) {
            String string = strings[i];
            if (!string.startsWith("http")) {
                string = "http://" + string;
            }
            URL url;
            try {
                url = new URL(string);
            } catch (MalformedURLException mux) {
                throw new IllegalArgumentException("Unparseable " + string, mux);
            }
            int port = url.getPort();
            if (port == -1) {
                port = url.getProtocol().equals("https") ? 443 : 80;
            }
            retval[i] = new HttpHost(url.getHost(), port, url.getProtocol());
        }
        return retval;
    }

    /**
     * Build simple HttpClient for single target host 
     */
    public static Pair<CloseableHttpClient, PoolingHttpClientConnectionManager> buildHttpClient(String userAgent, int connectTimeout, int readTimeout, int targetCount,
            int targetPoolSize, int poolTtlSeconds) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(poolTtlSeconds, TimeUnit.SECONDS);
        connectionManager.setMaxTotal(targetPoolSize * targetCount);
        connectionManager.setDefaultMaxPerRoute(targetPoolSize);
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout + (connectTimeout / 4))
                .setSocketTimeout(readTimeout).setRedirectsEnabled(false).build();

        CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(connectionManager).setUserAgent(userAgent).setDefaultRequestConfig(requestConfig).build();
        return Pair.of(client, connectionManager);
    }

    public static class StaleConnectionMonitorThread extends Thread {

        private volatile boolean shutdown;
        private final int checkPeriodSeconds;
        private final PoolingHttpClientConnectionManager connectionManager;
        private int poolTtlSeconds;

        public StaleConnectionMonitorThread(String threadName, int checkPeriodSeconds, int poolTtlSeconds, PoolingHttpClientConnectionManager connectionManager) {
            this.checkPeriodSeconds = checkPeriodSeconds;
            this.poolTtlSeconds = poolTtlSeconds;
            this.connectionManager = connectionManager;
            setName(threadName);
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(checkPeriodSeconds);
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(poolTtlSeconds, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                shutdown();
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public static void trash(InputStream stream, long contentLenght) throws IOException {
        int bufferSize = contentLenght < 4096 && contentLenght > 0 ? (int) contentLenght : 4096;
        byte[] buffer = new byte[bufferSize];
        while (stream.read(buffer) != -1) {
            // throw away content...
        }
    }

    public static String read(InputStream stream, long contentLenght, Charset charset) throws IOException {
        int bufferSize = contentLenght < 4096 && contentLenght > 0 ? (int) contentLenght : 4096;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset), bufferSize);
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    public abstract static class HttpResource<R> implements TargetResource<HttpHost, R> {

        private final HttpRequest httpRequest;

        public HttpResource(HttpRequest request) {
            this.httpRequest = request;
        }

        @Override
        public String toString() {
            return httpRequest.toString();
        }
    }

}
