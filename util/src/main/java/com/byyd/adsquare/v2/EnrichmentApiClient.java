package com.byyd.adsquare.v2;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.adfonic.http.ApiClient;
import com.adfonic.http.DefaultHttpErrorCallback;
import com.adfonic.http.HttpErrorCallback;
import com.adfonic.http.JacksonHttpExecutionCallback;
import com.byyd.adsquare.AdsquareApiException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Enrichment Integration API Documentation
 * http://docs.adsquare.com/
 * 
 * @author mvanek
 *
 */
public class EnrichmentApiClient implements Closeable {

    private static final AdsqrEnrichQueryResponse EMPTY = new AdsqrEnrichQueryResponse(Collections.EMPTY_LIST);

    private static final ObjectMapper jackson = new ObjectMapper();
    static {
        jackson.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Do NOT write null fields
        jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    // Create Jackson readers/writers AFTER Jackson's ObjectMapper was configured
    private final ObjectWriter requestWriter = jackson.writerFor(AdsqrEnrichQueryRequest.class);

    private final ApiClient apiClient;
    private final AdsquareErrorCallback errorCallback = new AdsquareErrorCallback();
    private final AdsquareHttpExecutionCallback queryCallback = new AdsquareHttpExecutionCallback(errorCallback, ApiClient.APPLICATION_JSON, HttpURLConnection.HTTP_OK,
            jackson.readerFor(AdsqrEnrichQueryResponse.class));

    public EnrichmentApiClient(String connectionString, int connectTimeout, int readTimeout, int poolTargetSize, int poolTtlSeconds, int failThreshold, int failLockdownMillis) {
        this(new ApiClient("adsqrich", connectionString, connectTimeout, readTimeout, poolTargetSize, poolTtlSeconds, failThreshold, failLockdownMillis));
    }

    public EnrichmentApiClient(ApiClient apiClient) {
        Objects.requireNonNull(apiClient);
        this.apiClient = apiClient;
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

    public AdsqrEnrichQueryResponse query(double latitude, double longitude) {
        return query(new AdsqrEnrichQueryRequest(latitude, longitude));
    }

    /**
     * @param latitude required
     * @param longitude required
     * @param deviceIdRaw optional
     * @param ipAddress optional
     * @return
     * @throws AdsquareApiException
     */
    public AdsqrEnrichQueryResponse query(Double latitude, Double longitude, String deviceIdRaw, String deviceIdSha1, String deviceIdMd5, String deviceType, Integer sspId)
            throws AdsquareApiException {
        AdsqrEnrichQueryRequest request = new AdsqrEnrichQueryRequest(latitude, longitude, deviceIdRaw, deviceIdSha1, deviceIdMd5, deviceType, sspId);
        return query(request);
    }

    public AdsqrEnrichQueryResponse query(AdsqrEnrichQueryRequest request) throws AdsquareApiException {
        HttpPost httpPost = new HttpPost("/api/v2/audience/query");
        /*
        EntityTemplate entityTemplate = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                requestWriter.writeValue(outstream, request);
            }
        });
        entityTemplate.setContentType(APPLICATION_JSON_UTF8);
        entityTemplate.setChunked(false);
        httpPost.setEntity(entityTemplate);
        */
        try {
            httpPost.setEntity(new StringEntity(requestWriter.writeValueAsString(request), ContentType.APPLICATION_JSON));
        } catch (Exception x) {
            throw new IllegalArgumentException("Failed to marshall request: " + request, x);
        }

        return apiClient.execute(httpPost, queryCallback, errorCallback);
    }

    static class AdsquareHttpExecutionCallback extends JacksonHttpExecutionCallback<AdsqrEnrichQueryResponse, AdsquareApiException> {

        public AdsquareHttpExecutionCallback(HttpErrorCallback<AdsquareApiException> errorCallback, String expectedMimeType, int expectedStatusCode, ObjectReader objectReader) {
            super(errorCallback, expectedMimeType, expectedStatusCode, objectReader);
        }

        @Override
        public void onResponseStatus(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset) throws AdsquareApiException {
            // To save traffic Adsquare is now sending 204 No Content response instead of empty JSON array 
            if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return;
            } else {
                super.onResponseStatus(httpRequest, httpHost, httpResponse, mimeType, charset);
            }
        }

        @Override
        public AdsqrEnrichQueryResponse onResponsePayload(HttpRequest httpRequest, HttpHost httpHost, HttpResponse httpResponse, String mimeType, Charset charset)
                throws AdsquareApiException {
            if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return EMPTY;
            } else {
                return super.onResponsePayload(httpRequest, httpHost, httpResponse, mimeType, charset);
            }
        }
    }

    static class AdsquareErrorCallback extends DefaultHttpErrorCallback<AdsquareApiException> {

        @Override
        public AdsquareApiException newException(String message) {
            return new AdsquareApiException(message);
        }

        @Override
        public AdsquareApiException newException(String message, Exception x) {
            return new AdsquareApiException(message + " " + x);
        }

    }
}