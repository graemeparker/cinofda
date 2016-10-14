package com.adfonic.tasks.combined.consumers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.tracking.TrackingMessage;
import com.adfonic.util.AbstractThreadSafeHttpClient;

@Component
public class TrackingMessageHandler extends AbstractThreadSafeHttpClient {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private final JsonResponseHandler JSON_RESPONSE_HANDLER;
    private final int trackerHttpRetryInterval;

    @Autowired
    protected TrackingMessageHandler(@Value("${tracker.base.url}") String baseUrl, @Value("${tracker.pool.connTtlMs:-1}") int connTtlMs,
            @Value("${tracker.pool.maxTotal:10}") int maxTotal, @Value("${tracker.pool.defaultMaxPerRoute:10}") int defaultMaxPerRoute,
            @Value("${tracker.http.retry.interval:1500}") int trackerHttpRetryInterval) {
        super(baseUrl, connTtlMs, maxTotal, defaultMaxPerRoute, new SleepingUnlimitedHttpRequestRetryHandler(trackerHttpRetryInterval));
        JSON_RESPONSE_HANDLER = new JsonResponseHandler();
        this.trackerHttpRetryInterval = trackerHttpRetryInterval;
    }

    private void logUnsuccessfulAction(String actionPath, Number successFlagVar, String errorVar) {
        LOG.warn("Action[{}] failed; success=[{}] error=[{}]", actionPath, successFlagVar, errorVar);
    }

    private void logErrorOnAction(String actionPath, Exception error) {
        LOG.error("Action[{}] error", actionPath, error);
    }

    public void onTrackingActionMessage(TrackingMessage actionMessage) {
        String actionPath = actionMessage.getTrackerPath();
        Map<?, ?> trackerResponse;
        try {
            trackerResponse = executeWithRetrySupport(new HttpGet(new URI(getBaseUrl() + actionPath)), JSON_RESPONSE_HANDLER);
        } catch (IOException | URISyntaxException e) {
            logErrorOnAction(actionPath, e);
            return;
        }

        Number successFlagVar = (Number) trackerResponse.get("success");
        if (successFlagVar == null || !(successFlagVar.intValue() == 0 || successFlagVar.intValue() == 1)) {
            logUnsuccessfulAction(actionPath, successFlagVar, (String) trackerResponse.get("error"));
        }
    }

    /**
     * ResponseHandler that expects a JSON response and transforms it into a JSONObject Map.
     */
    private final class JsonResponseHandler implements ResponseHandler<Map> {
        @Override
        public Map handleResponse(HttpResponse httpResponse) throws java.io.IOException {
            HttpEntity httpEntity = httpResponse.getEntity();
            try {
                StatusLine statusLine = httpResponse.getStatusLine();
                switch (statusLine.getStatusCode()) {
                case 200: // Should be a valid JSON response
                    return (Map) JSONValue.parseWithException(new InputStreamReader(httpEntity.getContent()));
                case 503:
                    throw new RetryRequestException(statusLine.getStatusCode(), statusLine.toString(), trackerHttpRetryInterval);
                default:
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.toString());
                }
            } catch (org.json.simple.parser.ParseException e) {
                LOG.error("Failed to parse JSON response {}", e);
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("success", 0);
                map.put("error", "Internal error");
                return map;
            } finally {
                // Ensure that the HttpEntity's InputStream gets closed
                EntityUtils.consumeQuietly(httpEntity);
            }
        }
    }

    private static class SleepingUnlimitedHttpRequestRetryHandler extends UnlimitedHttpRequestRetryHandler {

        private final long sleepTimeInMillis;

        private SleepingUnlimitedHttpRequestRetryHandler(long sleepTimeInMillis) {
            this.sleepTimeInMillis = sleepTimeInMillis;
        }

        @Override
        public boolean retryRequest(java.io.IOException exception, int executionCount, HttpContext context) {
            boolean doRetry = super.retryRequest(exception, executionCount, context);
            if (doRetry) {
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTimeInMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return doRetry;
        }

    }
}
