package com.byyd.adsquare.v2;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.adfonic.http.ApiClient;
import com.adfonic.http.JacksonHttpExecutionCallback;
import com.byyd.adsquare.AdsquareApiException;
import com.byyd.adsquare.v2.EnrichmentApiClient.AdsquareErrorCallback;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * Clinet for http://amp.adsquare.com API,Credantials: enrichment-demo@adsquare.com / #enrichment-demo1
 * Docs http://docs.adsquare.com/ Credantials: demo / integration 
 * 
 * @author mvanek
 *
 */
public class AmpApiClient implements Closeable {

    public static enum TrackType {
        IMPRESSION, CLICK;
    }

    private static final String AUTH_HEADER = "X-AUTH-TOKEN";
    private ApiClient apiClient;

    static final ObjectMapper jackson = new ObjectMapper();
    static {
        jackson.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Do NOT write null fields
        jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // Create Jackson readers/writers AFTER Jackson's ObjectMapper was configured

    private AdsquareErrorCallback errorCallback = new AdsquareErrorCallback();
    private JacksonHttpExecutionCallback<Map, AdsquareApiException> loginCallback = new JacksonHttpExecutionCallback<Map, AdsquareApiException>(errorCallback,
            ApiClient.APPLICATION_JSON, 200, jackson.readerFor(Map.class));
    private JacksonHttpExecutionCallback<AmpAudienceResponse, AdsquareApiException> audiencesCallback = new JacksonHttpExecutionCallback<AmpAudienceResponse, AdsquareApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, 200, jackson.readerFor(AmpAudienceResponse.class));
    private JacksonHttpExecutionCallback<List<AmpCompany>, AdsquareApiException> companiesCallback = new JacksonHttpExecutionCallback<List<AmpCompany>, AdsquareApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, 200, jackson.readerFor(new TypeReference<List<AmpCompany>>() {
            }));
    private JacksonHttpExecutionCallback<List<AmpSupplySidePlatform>, AdsquareApiException> sspsCallback = new JacksonHttpExecutionCallback<List<AmpSupplySidePlatform>, AdsquareApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, 200, jackson.readerFor(new TypeReference<List<AmpSupplySidePlatform>>() {
            }));
    private JacksonHttpExecutionCallback<Void, AdsquareApiException> trackingCallback = new JacksonHttpExecutionCallback<Void, AdsquareApiException>(errorCallback, null, 204, null);

    public AmpApiClient(String connectionString, int connectTimeout, int readTimeout, int poolTargetSize, int poolTtlSeconds, int failThreshold, int failLockdownMillis) {
        this(new ApiClient("adsqrapm", connectionString, connectTimeout, readTimeout, poolTargetSize, poolTtlSeconds, failThreshold, failLockdownMillis));
    }

    public AmpApiClient(ApiClient apiClient) {
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

    public String login(String username, String password) {
        HttpPost httpPost = new HttpPost("/api/v1/auth/login");
        httpPost.setEntity(new StringEntity("{ \"username\":\"" + username + "\", \"password\":\"" + password + "\" }", ContentType.APPLICATION_JSON));
        return (String) apiClient.execute(httpPost, loginCallback, errorCallback).get("token");
    }

    public String refreshToken(String authToken) {
        HttpGet httpGet = new HttpGet("/api/v1/auth/refreshToken");
        httpGet.setHeader(AUTH_HEADER, authToken);
        return (String) apiClient.execute(httpGet, loginCallback, errorCallback).get("token");
    }

    public List<AmpAudience> audiences(String authToken, String dspId) {
        HttpGet httpGet = new HttpGet("/api/v1/enrichmentMeta/audiences/" + dspId);
        httpGet.setHeader(AUTH_HEADER, authToken);
        return apiClient.execute(httpGet, audiencesCallback, errorCallback).getAudiences();
    }

    public List<AmpCompany> companies(String authToken, String dspId) {
        HttpGet httpGet = new HttpGet("/api/v1/enrichmentMeta/companies/" + dspId);
        httpGet.setHeader(AUTH_HEADER, authToken);
        return apiClient.execute(httpGet, companiesCallback, errorCallback);
    }

    public List<AmpSupplySidePlatform> ssps(String authToken) {
        HttpGet httpGet = new HttpGet("/api/v1/enrichmentMeta/ssps");
        httpGet.setHeader(AUTH_HEADER, authToken);
        return apiClient.execute(httpGet, sspsCallback, errorCallback);
    }

    /*
    {
        "dspId": "5655c3dde4b0b70902221001",
        "audienceId": 0,
        "appId": "string",
        "latitude": 0,
        "longitude": 0,
        "ifa": "string",
        "impressionTime": "2016-01-22T18:55:53.060Z",
        "sspId": 0
      }
      */
    public void trackClick(String authToken, String dspId, int audienceId, String appId, Double latitude, Double longitude, String ifa, Date impressionTime, Integer sspId) {
        HttpPost httpPost = new HttpPost("/api/v1/tracking/singleClick");
        httpPost.setHeader(AUTH_HEADER, authToken);
        String payload = buildTrackingJson(TrackType.CLICK, dspId, audienceId, appId, latitude, longitude, ifa, impressionTime, sspId);
        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        apiClient.execute(httpPost, trackingCallback, errorCallback);
    }

    /*
    {
        "dspId": "5655c3dde4b0b70902221001",
        "audienceId": 0,
        "appId": "string",
        "latitude": 0,
        "longitude": 0,
        "ifa": "string",
        "impressionTime": "2016-01-22T18:55:53.061Z",
        "sspId": 0
      }
      */
    public void trackImpression(String authToken, String dspId, int audienceId, String appId, Double latitude, Double longitude, String ifa, Date impressionTime, Integer sspId) {
        HttpPost httpPost = new HttpPost("/api/v1/tracking/singleImpression");
        httpPost.setHeader(AUTH_HEADER, authToken);
        String payload = buildTrackingJson(TrackType.IMPRESSION, dspId, audienceId, appId, latitude, longitude, ifa, impressionTime, sspId);
        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        apiClient.execute(httpPost, trackingCallback, errorCallback);
    }

    private final String buildTrackingJson(TrackType type, String dspId, int audienceId, String appId, Double latitude, Double longitude, String ifa, Date actionTime, Integer sspId) {
        StringBuilder sb = new StringBuilder("{ \"dspId\":\"" + dspId + "\", \"audienceId\":" + audienceId);
        if (type == TrackType.IMPRESSION) {
            sb.append(",\"impressionTime\":\"");
        } else {
            sb.append(",\"clickTime\":\"");
        }
        sb.append(ISO8601Utils.format(actionTime)).append("\"");

        if (appId != null) {
            sb.append(",\"appId\":\"").append(appId).append("\"");
        }
        if (latitude != null) {
            sb.append(",\"latitude\":").append(latitude);
            sb.append(",\"longitude\":").append(longitude);
        }
        if (ifa != null) {
            sb.append(",\"ifa\":\"").append(ifa).append("\"");
        }
        if (sspId != null) {
            sb.append(",\"sspId\":").append(sspId);
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Just a wrapper class for audiences request
     */
    static class AmpAudienceResponse {

        private List<AmpAudience> audiences;

        public List<AmpAudience> getAudiences() {
            return audiences;
        }

        public void setAudiences(List<AmpAudience> audiences) {
            this.audiences = audiences;
        }
    }

    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient("adsqrapm", "https://amp.adsquare.com", 2000, 5000, 3, 30, 5, 30_000);
        AmpApiClient client = new AmpApiClient(apiClient);
        String token = client.login("enrichment-demo@adsquare.com", "#enrichment-demo1");
        System.out.println(token);
        List<AmpAudience> audiences = client.audiences(token, "5655c3dde4b0b70902221001");
        for (AmpAudience ampAudience : audiences) {
            System.out.println(ampAudience);
        }
        //System.out.println(audiences);
        List<AmpSupplySidePlatform> ssps = client.ssps(token);
        for (AmpSupplySidePlatform ssp : ssps) {
            System.out.println(ssp.getId() + " " + ssp.getName());
        }
        System.out.println(ssps);
        client.close();
    }
}