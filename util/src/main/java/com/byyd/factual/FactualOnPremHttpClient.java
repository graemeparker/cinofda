package com.byyd.factual;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.adfonic.http.ApiClient;
import com.adfonic.http.DefaultHttpErrorCallback;
import com.adfonic.http.JacksonHttpExecutionCallback;
import com.adfonic.util.stats.FreqLogr;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Client for Factual Geopulse On-Prem: HTTP Version
 * http://developer.factual.com/geopulse-on-prem-http/
 * 
 * @author mvanek
 *
 */
public class FactualOnPremHttpClient implements Closeable {

    private final ObjectMapper jackson = new ObjectMapper();
    {
        this.jackson.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Do NOT write null fields
        this.jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    // Create Jackson readers/writers AFTER Jackson's ObjectMapper was configured

    private final ApiClient apiClient;
    private final FactualErrorCallback errorCallback = new FactualErrorCallback();
    private final JacksonHttpExecutionCallback<List<MatchResponse>, FactualApiException> queryCallback;

    public FactualOnPremHttpClient(ApiClient apiClient) {
        Objects.requireNonNull(apiClient);
        this.apiClient = apiClient;
        ObjectReader matchResponseReader = jackson.readerFor(new TypeReference<List<MatchResponse>>() {
        });
        this.queryCallback = new JacksonHttpExecutionCallback<List<MatchResponse>, FactualApiException>(errorCallback, ApiClient.APPLICATION_JSON, HttpURLConnection.HTTP_OK,
                matchResponseReader);
    }

    public FactualOnPremHttpClient(String connectionString, int connectTimeout, int readTimeout, int poolTargetSize, int poolTtlSeconds, int failThreshold, int failLockdownMillis) {
        this(new ApiClient("factual", connectionString, connectTimeout, readTimeout, poolTargetSize, poolTtlSeconds, failThreshold, failLockdownMillis));

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

    /**
     * HTTP Test a Device ID against active Audience Sets: http://[server]/geopulse/audience/sets?user-id=[user-id]
     */
    public List<MatchResponse> audience(String userId) {
        HttpUriRequest httpGet = new HttpGet("/geopulse/audience/sets?user-id=" + userId);
        return apiClient.execute(httpGet, queryCallback, errorCallback);
    }

    /**
     * HTTP Test a coordinate against active Proximity Sets: http://[server]/geopulse/proximity/indices?latitude=[latitude]&longitude=[longitude]
     */
    public List<MatchResponse> proximity(double latitude, double longitude) {
        HttpGet httpGet = new HttpGet("/geopulse/proximity/indices?latitude=" + latitude + "&longitude=" + longitude);
        return apiClient.execute(httpGet, queryCallback, errorCallback);
    }

    /**
     * Not really well implemented and tested
     * 
     * HTTP Test a Device ID for membership in an active Audience Set: http://[server]/geopulse/audience/sets/:set-id?user-id=[user-id]
     
    public boolean member(String userId, String setId) {
        final HttpGet httpGet = new HttpGet("/geopulse/audience/sets/" + setId + "?user-id=" + userId);
        return execute(new TargetResource<HttpHost, Boolean>() {
            @Override
            public Boolean call(HttpHost target) {
                return doHttpRequest(target, httpGet, APPLICATION_JSON, matchResponseReader);
            }

            @Override
            public String toString() {
                return httpGet.toString();
            }
        });
    }
    */
    /**
     * Not really well implemented and tested
    
    public void submit(double latitude, double longitude, String userId, Date expiryDate, boolean query) {
        if (userId == null) {
            throw new IllegalArgumentException("Null userId");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Null expiryDate");
        }
        StringBuilder sb = new StringBuilder("/geopulse/example");
        if (query) {
            sb.append("?audience=true&proximity=true");
        }
        HttpPost httpPost = new HttpPost(sb.toString());
        List<NameValuePair> httpPostParams = new ArrayList<NameValuePair>();
        httpPostParams.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
        httpPostParams.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
        httpPostParams.add(new BasicNameValuePair("timestamp", dateFormat.format(expiryDate)));
        httpPostParams.add(new BasicNameValuePair("user-id", userId));
        httpPost.setEntity(new UrlEncodedFormEntity(httpPostParams, UTF_8));
        execute(new TargetResource<HttpHost, Boolean>() {
            @Override
            public Boolean call(HttpHost target) {
                return doHttpRequest(target, httpPost, APPLICATION_JSON, matchResponseReader);
            }
        });
    }
    */

    static class FactualErrorCallback extends DefaultHttpErrorCallback<FactualApiException> {

        @Override
        public FactualApiException newException(String message) {
            return new FactualApiException(message);
        }

        @Override
        public FactualApiException newException(String message, Exception x) {
            return new FactualApiException(message + " " + x);
        }

    }

    public static void main(String[] args) {
        try {
            FactualOnPremHttpClient client = new FactualOnPremHttpClient("shrd1factual01.qa.adf.local:8989,shrd1factual02.qa.adf.local:8980", 1000, 1000, 2, 10_000, 10, 30_000);
            int i = 0;
            while (true) {
                try {
                    //List<MatchResponse> audiences = client.proximity(51.4794496, -0.0097148);
                    List<MatchResponse> audiences = client.audience("b1fac7f3e64d659c2fb0c112d56735aac459ca5b");
                    System.out.println("round " + i + " " + (audiences.size() > 0));
                } catch (Exception x) {
                    FreqLogr.report(x);
                    //System.out.println("round " + i + " " + x);
                    //x.printStackTrace();
                }
                ++i;
                if (false)
                    break;
            }

            //List<MatchResponse> audiences = client.proximity(52.515, 13.4095);
            // client.submit(40.642875, -73.782991, "179da7a8-1b77-45f4-aa70-d99474e93638", new Date(31, 11, 2015), true);

            client.close();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
