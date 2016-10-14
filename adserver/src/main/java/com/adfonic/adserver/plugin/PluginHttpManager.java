package com.adfonic.adserver.plugin;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TimeLimit;
import com.adfonic.util.HttpUtils;

@Component
public class PluginHttpManager {
    private static final transient Logger LOG = Logger.getLogger(PluginHttpManager.class.getName());

    private PoolingClientConnectionManager connMgr;
    private HttpClient httpClient;
    private final int connectionTimeout;
    private final int soTimeout;
    private static PluginHttpResponseHandler defaultResponseHandler = new PluginHttpResponseHandler();
    
    public static void setPluginHttpResponseHandler(PluginHttpResponseHandler pluginHttpResponseHandler){
    	defaultResponseHandler = pluginHttpResponseHandler;
    }
    
    public void setThreadSafeClientConnManager(PoolingClientConnectionManager threadSafeClientConnManager){
    	connMgr = threadSafeClientConnManager;
    }
    
    public void setDefaultHttpClient(HttpClient defaultHttpClient){
    	httpClient = defaultHttpClient;
    }
    
    @Autowired
    public PluginHttpManager(@Value("${plugin.http.pool.connTtlMs}")
                             int connTtlMs,
                             @Value("${plugin.http.pool.maxTotal}")
                             int maxTotal,
                             @Value("${plugin.http.pool.defaultMaxPerRoute}")
                             int defaultMaxPerRoute,
                             @Value("${plugin.http.connectTimeout}")
                             int connectionTimeout,
                             @Value("${plugin.http.readTimeout}")
                             int soTimeout)
    {
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        
        // Set up the thread-safe HTTP ClientConnectionManager
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Initializing ThreadSafeClientConnManager, connTTL=" + connTtlMs + "ms, maxTotal=" + maxTotal + ", defaultMaxPerRoute=" + defaultMaxPerRoute);
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
     * Shut down the connection manager and release any associated resources
     */
    @PreDestroy
    public void destroy() {
        LOG.info("Shutting down ThreadSafeClientConnManager");
        connMgr.shutdown();
    }

    /**
     * Execute an HTTP GET
     * @param url the URL to GET
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @param responseHandler the response handler that should be invoked
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String executeGet(String url, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit, PluginHttpResponseHandler responseHandler) throws java.io.IOException, PluginException {
        return execute(new HttpGet(url), headers, handleRedirects, timeLimit, responseHandler);
    }
    
    /**
     * Execute an HTTP GET
     * @param url the URL to GET
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String executeGet(String url, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit) throws java.io.IOException, PluginException {
        return executeGet(url, headers, handleRedirects, timeLimit, defaultResponseHandler);
    }
    
    /**
     * Execute an HTTP GET
     * @param url the URL to GET
     * @param params the parameters from which to build a query string
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String executeGet(String url, Map<String,String> params, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit) throws java.io.IOException, PluginException {
        if (params == null || params.isEmpty()) {
            return executeGet(url, headers, handleRedirects, timeLimit, defaultResponseHandler);
        } else {
            return executeGet(url + "?" + HttpUtils.encodeParams(params), headers, handleRedirects, timeLimit, defaultResponseHandler);
        }
    }
    
    /**
     * Execute an HTTP POST
     * @param url the URL to POST
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param params the parameters to post
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @param responseHandler the response handler that should be invoked
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String executePost(String url, Map<String,String> params, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit, PluginHttpResponseHandler responseHandler) throws java.io.IOException, PluginException {
        HttpPost httpPost = new HttpPost(url);
        if (params != null) {
            httpPost.setEntity(new UrlEncodedFormEntity(HttpUtils.toNameValuePairList(params), "UTF-8"));
        }
        return execute(httpPost, headers, handleRedirects, timeLimit, responseHandler);
    }

    /**
     * Execute an HTTP POST
     * @param url the URL to POST
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param params the parameters to post
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String executePost(String url, Map<String,String> params, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit) throws java.io.IOException, PluginException {
        return executePost(url, params, headers, handleRedirects, timeLimit, defaultResponseHandler);
    }

    /**
     * Execute an HTTP request
     * @param httpRequest the HTTP request
     * @param headers any additional headers to pass on the request (optional, may be null)
     * @param handleRedirects whether redirects should be handled automatically
     * @param timeLimit any TimeLimit that should be enforced in form of timeouts
     * @param responseHandler the response handler that should be invoked
     * @return the response content
     * @throws java.io.IOException
     * @throws PluginException
     */
    public String execute(HttpUriRequest httpRequest, Map<String,String> headers, boolean handleRedirects, TimeLimit timeLimit, PluginHttpResponseHandler responseHandler) throws java.io.IOException, PluginException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(httpRequest.getMethod() + " " + httpRequest.getURI() + " (handleRedirects=" + handleRedirects + ", timeLimit=" + timeLimit + ")");
        }
        
        BasicHttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientPNames.HANDLE_REDIRECTS, Boolean.valueOf(handleRedirects));
        
        // Use the remaining time in the TimeLimit as our connect and read
        // timeouts.  This is not an exact science, since it could take
        // limit-1 to connect, and then limit-1 to read...and then we're
        // at 2*limit-2, which is already over the limit.  But we can at
        // least try to avoid going over...better than nothing.
        int timeLeft  = 0;
        if(timeLimit != null){
        	timeLeft = (int)timeLimit.getTimeLeft();
        }

        // Be sure to set the timeout params on the HttpUriRequest, not on
        // the HttpContext (which is how we did it originally and it wasn't
        // working as expected).
        // http://old.nabble.com/Timeouts-not-being-obeyed-td33679313.html
        if (timeLimit != null && timeLeft != 0) {            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Using TimeLimit.timeLeft=" + timeLeft + " for HTTP timeouts");
            }
            httpRequest.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(timeLeft));
            httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(timeLeft));
        } else {
            // No time limit, so just use the default configured timeouts
            httpRequest.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
            httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(soTimeout));
        }

        if (headers != null) {
            for (Map.Entry<String,String> entry : headers.entrySet()) {
                httpRequest.setHeader(entry.getKey(), entry.getValue());
            }
        }
        
        HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            return responseHandler.handleResponse(httpResponse, httpEntity);
        } finally {
            // Ensure that the HttpEntity's InputStream gets closed
            EntityUtils.consumeQuietly(httpEntity);
        }
    }
}
