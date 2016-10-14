package com.adfonic.cache.citrusleaf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.citrusleaf.CitrusleafClient;
import net.citrusleaf.CitrusleafClient.ClOptions;
import net.citrusleaf.CitrusleafClient.ClResult;
import net.citrusleaf.CitrusleafClient.ClResultCode;
import net.citrusleaf.CitrusleafClient.ClWriteOptions;
import net.citrusleaf.CitrusleafInfo;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.adfonic.cache.AbstractCacheManager;
import com.adfonic.cache.CacheException;

/**
 * Citrusleaf implementation of CacheManager
 */
public class CitrusleafCacheManagerImpl extends AbstractCacheManager {
    private static final transient Logger LOG = Logger.getLogger(CitrusleafCacheManagerImpl.class.getName());

    private static final String DEFAULT_NAMESPACE = "Adfonic";
    private static final int DEFAULT_OPERATION_TIMEOUT_MS = 5000;
    private static final int MILLIS_100 = 100;
    private static final String NO_SET = "";
    private static final String NO_BIN = "";
    private static final String[] INFO_SERVER_PROPS = { "version", "build", "features" };
    private static final String[] INFO_NAMESPACE_PROPS = { "objects", "expired-objects", "evicted-objects", "set-deleted-objects", "set-evicted-objects" };

    private final CitrusleafClient client;
    private final String hostname;
    private final Integer port;
    private final ClOptions clOptions;
    private final String namespace;

    /**
     * Constructor.  Ensure the provided CitrusleafClient connects before
     * returning.  This version of the constructor does not time out, it
     * will wait forever for the connection to be established.  It does not
     * set the default namespace, relying on Citrusleaf defaults for that.
     * @param client the CitrusleafClient to use
     */
    public CitrusleafCacheManagerImpl(CitrusleafClient client, String hostname, Integer port) {
        this(client, hostname, port, DEFAULT_NAMESPACE, -1, DEFAULT_OPERATION_TIMEOUT_MS);
    }

    /**
     * Constructor.  Ensure the provided CitrusleafClient connects before
     * returning, waiting up to connectTimeoutMs for the connection to be
     * established.
     * @param client the CitrusleafClient to use
     * @param namespace the optional namespace to use (will use the Citrusleaf
     * default namespace if this is null)
     * @param connectTimeoutMs milliseconds to wait for the connection to be established
     * @param operationTimeoutMs milliseconds to wait for each cache operation
     */
    public CitrusleafCacheManagerImpl(CitrusleafClient client, String hostname, Integer port, String namespace, int connectTimeoutMs, int operationTimeoutMs) {
        this.client = client;
        this.hostname = hostname;
        this.port = port;

        // Make sure the client is connected
        connect(connectTimeoutMs);

        LOG.fine("Using namespace: " + namespace);
        this.namespace = namespace;

        // Set up the single shared instance of ClOptions that will control timeout
        // on the various cache operations.
        LOG.fine("Using cache operation timeout: " + operationTimeoutMs + "ms");
        clOptions = new ClOptions(operationTimeoutMs);
    }

    void connect(int connectTimeoutMs) {
        // Wait for the client to connect to the cluster
        Long timeToBail = null;
        if (connectTimeoutMs >= 0) {
            timeToBail = System.currentTimeMillis() + connectTimeoutMs;
        }
        while (!client.isConnected()) {
            if (timeToBail != null && System.currentTimeMillis() >= timeToBail) {
                throw new IllegalStateException("Timed out while connecting to Citrusleaf");
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Waiting for the CitrusleafClient to connect...");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(MILLIS_100);
            } catch (InterruptedException e) {
                LOG.warning("Interrupted");
                throw new IllegalStateException("Connect interrupted", e);
            }
        }
    }

    @Override
    /** {@inheritDoc} */
    public <T> T get(String key, Class<T> clazz) throws CacheException {
        return get(key, (String) null, clazz);
    }

    @Override
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String cacheName, Class<T> clazz) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("get namespace: " + namespace + ", cacheName: " + cacheName + ", key: " + key + ", class: " + clazz);
        }
        // cacheName is used as the "set"
        String set = StringUtils.isEmpty(cacheName) ? NO_SET : cacheName;
        ClResult result = client.get(namespace, set, key, NO_BIN, clOptions);
        switch (result.resultCode) {
        case OK:
            return (T) result.result;
        case SERVER_ERROR:
        case TIMEOUT:
        case CLIENT_ERROR:
        case SERVER_MEM_ERROR:
        case SERVER_NOT_AVAILABLE:
        case RECORD_TOO_BIG:
        case KEY_BUSY:
            LOG.severe("CitrusleafClient.get(" + namespace + ", " + set + ", " + key + ") failed: " + result);
            throw new CacheException("CitrusleafClient.get(" + namespace + ", " + set + ", " + key + ") failed: " + result);
        default: // "soft" errors don't throw, the key just doesn't exist
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("CitrusleafClient.get(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(result.resultCode));
            }
            return null;
        }
    }

    @Override
    /** {@inheritDoc} */
    public void set(String key, Object value, String cacheName, int ttlSeconds) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("set namespace: " + namespace + ", cacheName: " + cacheName + ", key: " + key + ", ttlSeconds: " + ttlSeconds);
        }
        ClWriteOptions writeOptions = new ClWriteOptions();
        writeOptions.expiration = ttlSeconds;
        // cacheName is used as the "set"
        String set = StringUtils.isEmpty(cacheName) ? NO_SET : cacheName;
        ClResultCode resultCode = client.set(namespace, set, key, NO_BIN, value, clOptions, writeOptions);
        switch (resultCode) {
        case OK:
            break;
        default: // anything other than OK is considered a hard failure
            LOG.severe("CitrusleafClient.set(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(resultCode));
            throw new CacheException("CitrusleafClient.set(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(resultCode));
        }
    }

    @Override
    /** {@inheritDoc} */
    public void set(String key, Object value, int ttlSeconds) {
        set(key, value, null, ttlSeconds);
    }

    @Override
    /** {@inheritDoc} */
    public void set(String key, Object value, String cacheName, Date expiry) {
        int ttlSeconds = (int) ((expiry.getTime() - System.currentTimeMillis()) / MILLISECONDS_IN_SECOND);
        if (ttlSeconds > 0) {
            set(key, value, cacheName, ttlSeconds);
        }
    }

    @Override
    /** {@inheritDoc} */
    public void set(String key, Object value, Date expiry) {
        set(key, value, null, expiry);
    }

    @Override
    /** {@inheritDoc} */
    public boolean remove(String key) {
        return remove(key, (String) null);
    }

    @Override
    /** {@inheritDoc} */
    public boolean remove(String key, String cacheName) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("remove namespace: " + namespace + ", cacheName: " + cacheName + ", key: " + key);
        }
        // cacheName is used as the "set"
        String set = StringUtils.isEmpty(cacheName) ? NO_SET : cacheName;
        ClResultCode resultCode = client.delete(namespace, set, key, clOptions, null);
        switch (resultCode) {
        case OK:
            return true;
        case SERVER_ERROR:
        case TIMEOUT:
        case CLIENT_ERROR:
        case SERVER_MEM_ERROR:
        case SERVER_NOT_AVAILABLE:
        case RECORD_TOO_BIG:
        case KEY_BUSY:
            LOG.severe("CitrusleafClient.delete(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(resultCode));
            throw new CacheException("CitrusleafClient.delete(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(resultCode));
        default: // "soft" errors don't throw, the key just doesn't exist
            LOG.fine("CitrusleafClient.delete(" + namespace + ", " + set + ", " + key + ") failed: " + ClResult.resultCodeToString(resultCode));
            return false;
        }
    }

    @Override
    /** {@inheritDoc} */
    public boolean remove(String key, Class<?> clazz) {
        return remove(key, (String) null);
    }

    @Override
    /** {@inheritDoc} */
    public long cacheCount() throws CacheException {
        return cacheCount((String) null);
    }

    @Override
    /** {@inheritDoc} */
    public long cacheCount(String cacheName) throws CacheException {
        throw new NotImplementedException("Method not yet implemented");
    }

    @Override
    /** {@inheritDoc} */
    public List<String> cacheInfo() throws CacheException {
        List<String> cacheInfo = new ArrayList<String>();

        // Get server info
        Map<String, String> map = CitrusleafInfo.get(this.hostname, this.port);
        if (map == null) {
            throw new CacheException(String.format("Failed to get server info: host=%s port=%d", this.hostname, this.port));
        }
        for (String serverPropKey : INFO_SERVER_PROPS) {
            String value = map.get(serverPropKey);
            if (value != null) {
                cacheInfo.add("Server " + serverPropKey + '=' + value);
            }
        }

        String filter = "namespace/" + this.namespace;
        String tokens = CitrusleafInfo.get(this.hostname, this.port, filter);
        if (tokens == null) {
            throw new CacheException(String.format("Failed to get namespace info: host=%s port=%d namespace=%s", this.hostname, this.port, this.namespace));
        }
        Map<String, String> valuesMap = new HashMap<String, String>();
        for (String value : tokens.split(";")) {
            String[] valueInfo = value.split("=");
            if (valueInfo != null && valueInfo.length == 2) {
                valuesMap.put(valueInfo[0], valueInfo[1]);
            }
        }
        for (String namespacePropKey : INFO_NAMESPACE_PROPS) {
            String value = valuesMap.get(namespacePropKey);
            if (value != null) {
                cacheInfo.add(this.namespace + " " + namespacePropKey + '=' + value);
            }
        }

        return cacheInfo;
    }
}
