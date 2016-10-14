package com.adfonic.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;

/**
 * Primary/failover load balancing wrapper around a shared HttpClient and
 * thread-safe reusable HTTP connection manager.
 */
public class LoadBalancingHttpClient {
    private static final transient Logger LOG = Logger.getLogger(LoadBalancingHttpClient.class.getName());

    private final long retryPrimaryIntervalMs;
    private final PoolingClientConnectionManager connMgr;
    private final DefaultHttpClient httpClient;
    private final boolean failoverSupported;
    private final Object failoverModeMutex = new Object();
    private final HttpHost primaryHttpHost;
    private final HttpHost failoverHttpHost;
    private volatile boolean failoverMode = false;
    private volatile long retryPrimaryAtTimestamp = -1;

    /**
     * Constructor
     * 
     * @param serverList
     *            a comma-separated list of "host[:port]" (port 80 assumed if
     *            omitted)
     * @param retryCount
     *            the maximum number of times a method will be retried before
     *            failing
     * @param requestSentRetryEnabled
     *            whether methods that have successfully sent their request
     *            should be retried
     * @param retryPrimaryIntervalMs
     *            how often, during failover mode, the primary server should be
     *            retried
     * @param connTtlMs
     *            max connection lifetime, &lt;=0 implies "infinity"
     * @param maxTotal
     *            the maximum total number of connections
     * @param defaultMaxPerRoute
     *            the default maximum number of connections per HTTP route
     * @param useHttps
     *            whether HTTPS should be used for all requests
     * @see org.apache.http.impl.client.DefaultHttpRequestRetryHandler
     */
    public LoadBalancingHttpClient(String serverList, int retryCount, boolean requestSentRetryEnabled, long retryPrimaryIntervalMs, int connTtlMs, int maxTotal,
            int defaultMaxPerRoute, boolean useHttps) {
        this(serverList, -1, retryCount, requestSentRetryEnabled, retryPrimaryIntervalMs, connTtlMs, maxTotal, defaultMaxPerRoute, useHttps);
    }

    public LoadBalancingHttpClient(String serverList, int clientIndex, int retryCount, boolean requestSentRetryEnabled, long retryPrimaryIntervalMs, int connTtlMs, int maxTotal,
            int defaultMaxPerRoute, boolean useHttps) {
        this(serverList, clientIndex, retryCount, requestSentRetryEnabled, retryPrimaryIntervalMs, connTtlMs, maxTotal, defaultMaxPerRoute, useHttps, 3000, 5000);
    }

    /**
     * Constructor
     * 
     * @param serverList
     *            a comma-separated list of "host[:port]" (port 80 assumed if
     *            omitted)
     * @param clientIndex
     *            a specifically assigned index into the server list (-1 means
     *            derive)
     * @param retryCount
     *            the maximum number of times a method will be retried before
     *            failing
     * @param requestSentRetryEnabled
     *            whether methods that have successfully sent their request
     *            should be retried
     * @param retryPrimaryIntervalMs
     *            how often, during failover mode, the primary server should be
     *            retried
     * @param connTtlMs
     *            max connection lifetime, &lt;=0 implies "infinity"
     * @param maxTotal
     *            the maximum total number of connections
     * @param defaultMaxPerRoute
     *            the default maximum number of connections per HTTP route
     * @param useHttps
     *            whether HTTPS should be used for all requests
     * @see org.apache.http.impl.client.DefaultHttpRequestRetryHandler
     */
    public LoadBalancingHttpClient(String serverList, int clientIndex, int retryCount, boolean requestSentRetryEnabled, long retryPrimaryIntervalMs, int connTtlMs, int maxTotal,
            int defaultMaxPerRoute, boolean useHttps, int connectionTimeout, int readTimeout) {
        int localClientIndex = clientIndex;

        this.retryPrimaryIntervalMs = retryPrimaryIntervalMs;

        String[] servers = StringUtils.split(serverList, ',');

        if (localClientIndex != -1 && LOG.isLoggable(Level.FINE)) {
            LOG.fine("Using assigned clientIndex=" + localClientIndex);
        }

        // See if we were passed more than one server in the list. If there's
        // only
        // one, then failover isn't supported.
        if (servers.length == 1) {
            LOG.warning("Only primary server specified, failover disabled");
            failoverSupported = false;
            primaryHttpHost = parseHttpHost(servers[0], useHttps);
            failoverHttpHost = null;
        } else {
            failoverSupported = true;

            // If we haven't been assigned a specific client index, derive it
            // now
            if (localClientIndex == -1) {
                // This pattern picks the "host number" out of the hostname, in
                // either short
                // or fully qualified form. i.e. "rfadserver23" or
                // "rfadserver23.adfonic.com".
                // It allows digits in the name prefix, i.e. "gs2foobar07".
                Pattern pattern = Pattern.compile("^(?:[\\w]+)[A-Za-z]+(\\d+)\\.?.*$");
                String hostName = HostUtils.getHostName();
                Matcher matcher = pattern.matcher(hostName);
                if (matcher.matches()) {
                    localClientIndex = Integer.parseInt(matcher.group(1)) - 1; // convert
                    // to
                    // zero-based
                } else {
                    LOG.warning("Can't determine clientIndex from hostname \"" + hostName + "\", defaulting to clientIndex=0");
                    localClientIndex = 0; // just assume we're the first client node
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("For hostName=" + hostName + ", clientIndex=" + localClientIndex);
                }
            }

            // Determine our primary host
            int primaryIndex = LoadBalancingUtils.getPrimaryServerIndex(localClientIndex, servers.length);
            primaryHttpHost = parseHttpHost(servers[primaryIndex], useHttps);

            // Determine our failover host
            int failoverIndex = LoadBalancingUtils.getFailoverServerIndex(localClientIndex, servers.length);
            failoverHttpHost = parseHttpHost(servers[failoverIndex], useHttps);
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Primary HttpHost: " + primaryHttpHost);
            if (failoverHttpHost != null) {
                LOG.info("Failover HttpHost: " + failoverHttpHost);
            }
        }

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

        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeout);

        if (retryCount > 0) {
            // Set up the request retry handler
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Initializing DefaultHttpRequestRetryHandler, retryCount=" + retryCount + ", requestSentRetryEnabled=" + requestSentRetryEnabled);
            }
            httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, requestSentRetryEnabled));
        }
    }

    /**
     * Shut down the connection manager and release any associated resources
     */
    @PreDestroy
    public void destroy() {
        LOG.info("Shutting down ThreadSafeClientConnManager");
        connMgr.shutdown();
    }

    /**
     * Execute an HTTP request using our pooled connection manager, with
     * primary/failover handled automatically and transparently.
     * 
     * @param method
     *            the HTTP request method (i.e. "GET" or "POST")
     * @param uri
     *            the URI being requested, including a leading slash
     * @param responseHandler
     *            the ResponseHandler implementation that will process the
     *            response
     * @return the object returned by the ResponseHandler
     * @throws java.io.IOException
     *             if the primary and failover servers are both failing
     */
    public <T> T execute(String method, String uri, ResponseHandler<T> responseHandler) throws java.io.IOException {
        // First things first, if we're currently in failover mode but it's time
        // to retry the primary, take care of that.
        exitFailoverNodeIfNecessary();

        // Store the value of failoverMode at the start of the request, since
        // another
        // thread might flip the value while we're doing our thing...and we'll
        // need to
        // know the "just prior to the request" value in case we catch an
        // exception.
        boolean alreadyInFailoverMode = failoverMode;

        // Pick the primary or failover host, as applicable, and construct the
        // request
        HttpHost httpHost = alreadyInFailoverMode ? failoverHttpHost : primaryHttpHost;
        HttpRequest httpRequest = new BasicHttpRequest(method, uri);
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(method + " " + httpHost.toString() + uri);
            }
            // Execute the request, returning the result transformed by the
            // given response handler
            return httpClient.execute(httpHost, httpRequest, responseHandler, new BasicHttpContext());
        } catch (java.io.IOException e) {
            // The request failed, so let's see how we need to handle this...
            if (!failoverSupported || alreadyInFailoverMode) {
                // Either failover isn't supported, or we were already in
                // failover mode
                // at the start of the request. In either case, this is a hard
                // failure.
                throw e;
            }

            // Log a warning about this, but only do so if we're not already in
            // failover mode.
            // What happens is...when the primary goes down, all of the clients
            // in the pool
            // hit a primary failure around the same time. The first one to get
            // there does
            // the actual effective enterFailoverMode() (below), and the rest
            // just end up
            // passing through that method. But there's no real reason for every
            // client in
            // the pool to log the primary failure warning once the first one on
            // the scene
            // has flipped us into failover mode. It's totally possible that by
            // the time
            // we get to this logger call, the first on the scene has already
            // taken care
            // of it. This synchronized block isn't necessary for the
            // enterFailoverMode()
            // call, but it helps reduce logging.
            synchronized (this) {
                if (failoverMode) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Primary failed " + method + " " + httpHost.toString() + uri + ", but another client already put us in failover mode");
                    }
                } else {
                    // We're the first on the scene
                    LOG.warning("Primary failed " + method + " " + httpHost.toString() + uri + ", entering failover mode");
                }

                // Enter failover mode now in a thread-safe way
                enterFailoverMode();
            }

            // Retry now that we're in failover mode
            return execute(method, uri, responseHandler);
        }
    }

    /**
     * Enter failover mode in a thread-safe manner, if we're not there already.
     */
    private void enterFailoverMode() {
        // Only one thread, first on the scene, can cause the switch to failover
        // mode
        synchronized (failoverModeMutex) {
            // We have the lock, but another thread may already have forced us
            // to
            // enter failover mode...let's see
            if (!failoverMode) {
                // We're not yet in failover mode...so switch to it now
                failoverMode = true;
                retryPrimaryAtTimestamp = System.currentTimeMillis() + retryPrimaryIntervalMs;
                LOG.warning("Entering failover mode");
            } else {
                // Some other thread got there first and already handled it
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Another thread already entered failover mode");
                }
            }
        }
    }

    /**
     * Exit failover mode if it's time to retry the primary node, and do so in a
     * thread-safe manner.
     */
    private void exitFailoverNodeIfNecessary() {
        // See if we're in failover mode and it's time to retry the primary
        if (failoverMode && System.currentTimeMillis() >= retryPrimaryAtTimestamp) {
            // Yup, looks like it's time to retry the primary...but only the
            // first
            // thread on the scene is permitted to flip us back to primary.
            synchronized (failoverModeMutex) {
                // We have the lock...check again to make sure we got there
                // first
                if (failoverMode) {
                    // Yup, exit failover mode to force a primary retry
                    failoverMode = false;
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Exiting failover mode to retry primary");
                    }
                } else {
                    // Some other thread got there first and already handled it
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Another thread already exited failover mode");
                    }
                }
            }
        }
    }

    private static HttpHost parseHttpHost(String server, boolean useHttps) {
        String[] tokens = StringUtils.split(server.trim(), ':');
        int port;
        switch (tokens.length) {
        case 1:
            port = useHttps ? 443 : 80;
            break;
        case 2:
            port = Integer.parseInt(tokens[1]);
            break;
        default:
            throw new IllegalArgumentException("Illegal server format (expected host[:port]): " + server);
        }
        return new HttpHost(tokens[0], port, useHttps ? "https" : "http");
    }
}
