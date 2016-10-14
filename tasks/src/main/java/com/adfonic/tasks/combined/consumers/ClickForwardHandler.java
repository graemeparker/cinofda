package com.adfonic.tasks.combined.consumers;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simply take the URL and try it - See AI-65
 * 
 */
@Component
public class ClickForwardHandler {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${ClickForwardHandler.3pconn.timeout:3000}")
    private int timeout;

    public void onClickForwardRequest(String forwardUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(forwardUrl).openConnection();
            // We don't want to go places. Calling per instance as opposite to changing the global default
            conn.setInstanceFollowRedirects(false);

            conn.setConnectTimeout(timeout);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOG.warn("!200! - Response code:[{}] on url:[{}]", responseCode, forwardUrl);
            }
        } catch (MalformedURLException e) { // Should not happen - already verified by adserver
            LOG.error("Click forward failed on url:[{}] with exception:[{}]", forwardUrl, e.getMessage());
        } catch (Throwable e) {
            LOG.warn("Click forward failed on url:[{}] with exception:[{}]", forwardUrl, e.getMessage());
        }
    }

}
