package com.adfonic.tasks.combined.truste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.tasks.combined.truste.dto.BatchPreference;
import com.adfonic.tasks.combined.truste.dto.RefreshToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class BatchPreferenceService {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${Truste.preference.url}")
    private String preferenceUrl;

    @Value("${Truste.batchcount.url}")
    private String batchCountUrl;

    @Value("${Truste.refreshtoken.url}")
    private String refreshTokenUrl;

    @Value("${Truste.accesstoken}")
    private String accessToken;
    @Value("${Truste.refreshtoken}")
    private String refreshToken;

    private ObjectMapper objectMapper = new ObjectMapper();

    HttpClient httpClient;

    private DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");

    // HTTP auto-retry settings
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_INTERVAL_MS = 30000;
    private boolean retry;

    @PostConstruct
    public void initialize() {
        if (objectMapper != null) {
            SimpleModule module = new SimpleModule("TrusteDateDeserializer", new Version(1, 0, 0, null, null, null));
            module.addDeserializer(Date.class, new TrusteDateDeserializer());
            objectMapper.registerModule(module);
        }

        if (httpClient == null) {
            httpClient = new AutoRetryHttpClient(new DefaultServiceUnavailableRetryStrategy(MAX_RETRIES, RETRY_INTERVAL_MS));
        }
    }

    public List<BatchPreference> getBatch(int from, int to, DateTime changeAfter, DateTime changeBefore) throws TrusteUnreachableException {
        LOG.info("Starting getting batch preferences from Truste {} - {},  changeAfter: {} changeBefore: {}", from, to, changeAfter, changeBefore);

        try {
            String url = String.format(preferenceUrl, from, to, format.print(changeAfter), format.print(changeBefore), accessToken);

            // log only first url
            if (from == 0) {
                LOG.info("url: {}", url);
            }

            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                EntityUtils.consume(httpResponse.getEntity());
                LOG.error("failed to getBatch httpResponse:{} url:{}", httpResponse, url, httpResponse.getEntity());
                return Collections.emptyList();
            }
            List<BatchPreference> preferences = objectMapper.readValue(httpResponse.getEntity().getContent(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, BatchPreference.class));

            LOG.info("End getting batch preferences from Truste changeAfter: {} changeBefore: {}", changeAfter, changeBefore);
            return preferences;
        } catch (Exception e) {
            throw new TrusteUnreachableException("failed to get batch", e);
        }
    }

    /**
     * Refreshes the token in case the active token has expired
     * 
     * @return RefreshToken new token
     */
    private RefreshToken refreshToken() {
        LOG.info("Refreshing access token.");
        HttpGet httpGet = new HttpGet(String.format(refreshTokenUrl, refreshToken));
        LOG.debug("{} {}", httpGet.getMethod(), httpGet.getURI());
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                RefreshToken token = objectMapper.readValue(httpResponse.getEntity().getContent(), RefreshToken.class);
                LOG.info("Access token refreshed to: {}", token.getToken());
                return token;
            }
        } catch (ClientProtocolException e) {
            LOG.error("Something went wrong while refreshing token {}", e);
        } catch (Exception e) {
            LOG.error("Something went wrong while reading refresh token {}", e);
        }
        LOG.warn("Problem refreshing access token.");
        return null;
    }

    /**
     * Get the count for preferences between changeAfter and changeBefore time periods
     * 
     * @param changeAfter
     * @param changeBefore
     * @return int count
     */
    public int getCount(DateTime changeAfter, DateTime changeBefore) throws TrusteUnreachableException {
        LOG.info("getCount changeAfter:{} changeBefore {}", changeAfter, changeBefore);

        String url = String.format(batchCountUrl, format.print(changeAfter), format.print(changeBefore), accessToken);
        LOG.info("url: {}", url);

        HttpGet httpGet = new HttpGet(url);
        LOG.debug("{} {}", httpGet.getMethod(), httpGet.getURI());
        HttpResponse httpResponse;
        int totalCount = 0;
        try {
            httpResponse = httpClient.execute(httpGet);
            String content = getContent(httpResponse);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                try {
                    totalCount = Integer.parseInt(content.trim());
                    return totalCount;
                } catch (Exception e) {
                    LOG.error("error parsing totalCount {}", content, e);
                }

            } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {

                LOG.warn("SC_FORBIDDEN {}", httpResponse);

                RefreshToken token = refreshToken();
                if (token != null) {
                    accessToken = token.getToken();
                }
                // retrying batch count once
                if (!retry) {
                    LOG.debug("Retrying batch count!");
                    getCount(changeAfter, changeBefore);
                    retry = true;
                } else {
                    LOG.debug("Out of Retry attempts");
                }
            } else {
                LOG.error("httpResponse {}", httpResponse);
            }
        } catch (IOException e) {
            throw new TrusteUnreachableException("failed to get count", e);
        }
        return totalCount;
    }

    private String getContent(HttpResponse httpResponse) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))//
        ) {
            String string = IOUtils.toString(in);
            return string;
        } catch (Exception e) {
            LOG.error("error getting httpResponse content", e);
        }
        return null;
    }

}
