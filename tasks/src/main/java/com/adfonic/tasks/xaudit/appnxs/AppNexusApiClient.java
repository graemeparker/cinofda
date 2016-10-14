package com.adfonic.tasks.xaudit.appnxs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.tasks.xaudit.ApprovalServiceManager;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusAuthResponse;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusAuthResponseWrapper;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusRequestWrapper;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusResponse;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusResponseWrapper;
import com.adfonic.tasks.xaudit.exception.ExternalSystemException;
import com.adfonic.util.AbstractPoolingClientConnectionManager;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * https://wiki.appnexus.com/display/adnexusdocumentation/Creative+Service
 * 
 * There are serious issues with this API at least on api-impbus.client-testing.adnxs.net
 * For example it blindly believes in Authentication header sent from us and just uses it as a token. But when it is sent blank, it is invalid as a token.  
 *
 */
//@Component
public class AppNexusApiClient implements ApprovalServiceManager<AppNexusCreativeRecord> {

    private static final transient Logger LOG = Logger.getLogger(AppNexusApiClient.class.getName());

    private static final String APPLICATION_JSON = "application/json";

    private final String creativeServiceUrl;
    private final String authenticateUrl;
    private final AbstractPoolingClientConnectionManager connMgr;
    private final String username;
    private final String password;

    private String authToken = null;
    private ReentrantLock authLock = new ReentrantLock();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private int memberId;
    private String apiHost;

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * AppNexusApprovalManager constructor, configuration from properties files
     */
    @Autowired
    public AppNexusApiClient(@Value("${appnxs.creative.service.url:dummynottoblockothrstf}") String creativeServiceUrl,
            @Value("${appnxs.auth.url:dummynottoblockothrstf}") String authenticateUrl, //
            @Value("${appnxs.auth.username:dummyusername}") String username, @Value("${appnxs.auth.password:dummypassword}") String password,//
            @Value("${appnxs.creative.service.memberid:2560}") int memberId, //
            @Value("${appnxs.creative.service.connTtlMs:2000}") int connTtlMs,//
            @Value("${appnxs.creative.service.maxTotalConnection:20}") int maxTotal, //
            @Value("${appnxs.creative.service.defaultMaxPerRoute:10}") int defaultMaxPerRoute,//
            @Value("${appnxs.creative.service.connect.timeout.ms:2000}") int connectTimeout, //
            @Value("${appnxs.creative.service.socket.timeout.ms:2000}") int socketTimeout) {
        this.creativeServiceUrl = creativeServiceUrl;
        this.authenticateUrl = authenticateUrl;
        this.username = username;
        this.password = password;
        this.memberId = memberId;
        URL url;
        try {
            url = new URL(creativeServiceUrl);
        } catch (MalformedURLException mux) {
            throw new IllegalArgumentException("Wrong api url " + creativeServiceUrl, mux);
        }
        int port = url.getPort();
        apiHost = url.getProtocol() + "://" + url.getHost() + ((port != -1) ? ":" + url.getPort() : "");

        LOG.info("Initialized: " + creativeServiceUrl);
        this.connMgr = new AbstractPoolingClientConnectionManager(connTtlMs, maxTotal, defaultMaxPerRoute, connectTimeout, socketTimeout) {
        };
    }

    /**
     * Without a valid auth token we cannot use the AppNexus API
     */
    @PostConstruct
    public void postConstruct() {
        LOG.info("AppNexusApprovalManager postConstruct");
        authToken = getAuthToken();
    }

    /**
     * Be aware that null can be returned 
     * 
     * https://wiki.appnexus.com/display/adnexusdocumentation/Authentication+Service
     * https://wiki.appnexus.com/display/adnexusdocumentation/API+Authentication
     * https://wiki.appnexus.com/display/adnexusdocumentation/API+Usage+Constraints#APIUsageConstraints-AuthenticationFrequency
     * 
     * After authenticating, your token remains valid for 2 hours. You do not need to re-authenticate within this time. 
     * If you do re-authenticate, please note the following limitation: The AppNexus API permits you to authenticate successfully 10 times per 5-minute period. 
     * Any subsequent authentication attempts within those 5 minutes will result in an error.
     * It is best practice to listen for the "NOAUTH" error_id in your call responses and re-authenticate only after receiving it.
     */
    String getAuthToken() {
        LOG.info("Auth API getToken");
        int attempt = 1;
        String newApiToken = null;
        while (newApiToken == null && attempt <= 2) {
            if (attempt != 1) {
                // Pause for a moment if it is retry attempt
                LOG.info("Waiting before Auth API attempt: " + attempt);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ix) {
                    // ignore wake up call
                }
            }
            ++attempt;
            try {
                HttpPost httpRequest = new HttpPost(authenticateUrl);

                String payload = "{\"auth\":{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}}";
                StringEntity httpEntity = new StringEntity(payload, "UTF-8");
                httpEntity.setContentType(APPLICATION_JSON);
                httpRequest.setEntity(httpEntity);

                LOG.log(Level.INFO, "Auth API request: " + httpRequest + " " + payload);
                HttpResponse httpResponse = connMgr.execute(httpRequest, new BasicHttpContext());

                StatusLine statusLine = httpResponse.getStatusLine();
                if (statusLine.getStatusCode() != HttpURLConnection.HTTP_OK) {
                    LOG.log(Level.WARNING, "Invalid Auth API HTTP status: " + statusLine);
                    continue;
                }

                HttpEntity entity = httpResponse.getEntity();
                Header contentType = entity.getContentType();
                if (contentType == null || !contentType.getValue().startsWith(APPLICATION_JSON)) {
                    LOG.log(Level.WARNING, "Invalid Auth API Content-Type: " + contentType);
                    continue;
                }

                AppNexusAuthResponseWrapper anxWrapper = MAPPER.readValue(entity.getContent(), AppNexusAuthResponseWrapper.class);

                AppNexusAuthResponse anxResponse = anxWrapper.getResponse();
                if (anxResponse.getError() != null || anxResponse.getError_id() != null || anxResponse == null || !anxResponse.getStatus().equals("OK")) {
                    LOG.log(Level.WARNING, "Invalid Auth API response or status: " + anxWrapper);
                    continue;
                }

                newApiToken = anxWrapper.getResponse().getToken();
                if (newApiToken == null || newApiToken.isEmpty()) {
                    // Beware that AppNexus sometimes returns blank token... (when blank Authentication header is sent in...)
                    LOG.log(Level.SEVERE, "Auth API returned empty token: " + anxWrapper);
                    return null; // No point to try again as it only makes things worse. 
                }

            } catch (Exception x) {
                LOG.log(Level.SEVERE, "Auth API call failed", x);
            }
        }
        return newApiToken;
    }

    /**
     * Get a creative using the AppNexus external id for the creative.
     */
    @Override
    public AppNexusCreativeRecord getCreative(String anxCreativeId) {

        if (anxCreativeId == null) {
            LOG.log(Level.INFO, "AppNexusApprovalManager external reference null");
            throw new IllegalArgumentException("AppNexusApprovalManager external reference null");
        }

        String url = creativeServiceUrl + "/" + anxCreativeId;
        HttpGet httpGet = new HttpGet(url);
        AppNexusCreativeRecord anxCreative = execute(httpGet, HttpURLConnection.HTTP_OK);
        if (anxCreative == null) {
            // Callers does not expect null so throw exception
            LOG.log(Level.WARNING, "Creative API returned null for HTTP GET anxId: " + anxCreativeId);
            throw new IllegalStateException("Creative API returned null for HTTP GET anxId: " + anxCreativeId);
        }
        return anxCreative;
    }

    public AppNexusCreativeRecord getCreativeByCode(String code) {
        HttpGet httpGet = new HttpGet(apiHost + "/creative?member_id=" + memberId + "&code=" + code);
        AppNexusCreativeRecord anxCreative = execute(httpGet, HttpURLConnection.HTTP_OK);
        if (anxCreative == null) {
            return null;
        }
        return anxCreative;
    }

    /**
     * Get a creative using the AppNexus external id for the creative.
     */
    @Override
    public boolean deleteCreative(String anxCreativeId) {
        LOG.info("AppNexusApprovalManager DELETING a creative from AppNexus with id : " + anxCreativeId);
        HttpDelete httpDelete = new HttpDelete(creativeServiceUrl + "/" + anxCreativeId);
        AppNexusCreativeRecord creativeRecord = execute(httpDelete, HttpURLConnection.HTTP_OK);
        return creativeRecord != null;
    }

    /**
     * 
     */
    @Override
    public String postCreative(AppNexusCreativeRecord creativeRequest) {
        HttpPost httpPost = new HttpPost(creativeServiceUrl);
        addRequestPayload(creativeRequest, httpPost);
        AppNexusCreativeRecord anxResponse = execute(httpPost, HttpURLConnection.HTTP_OK);

        if (anxResponse == null) {
            LOG.log(Level.WARNING, "Creative API returned null for HTTP POST");
            throw new IllegalStateException("Creative API returned null for HTTP POST");
        }
        Integer anxCreativeId = anxResponse.getId();
        LOG.info("Creative API returned anxCreativeId: " + anxCreativeId);

        return String.valueOf(anxCreativeId);
    }

    /**
     * Update a creative using the AppNexus external id for the creative.
     */
    @Override
    public AppNexusCreativeRecord updateCreative(String anxCreativeId, AppNexusCreativeRecord creative) {

        if (anxCreativeId == null) {
            LOG.info("AppNexusApprovalManager can't update record AppNexus with null anxId");
            return null;
        }
        HttpPut httpPut = new HttpPut(creativeServiceUrl + "/" + anxCreativeId);
        creative.setId(Integer.valueOf(anxCreativeId));
        creative.setAllow_audit(true);
        addRequestPayload(creative, httpPut);
        AppNexusCreativeRecord creativeRecord = execute(httpPut, HttpURLConnection.HTTP_OK);
        if (creativeRecord == null) {
            LOG.warning("Creative API returned null for HTTP PUT anxId: " + anxCreativeId);
        }
        return creativeRecord;
    }

    /**
     * Build the request as a String and get ready to post.
     */
    private void addRequestPayload(AppNexusCreativeRecord creative, HttpEntityEnclosingRequestBase httpRequest) {
        try {
            httpRequest.addHeader("Content-Type", "application/json");
            String request = MAPPER.writeValueAsString(new AppNexusRequestWrapper(creative));
            httpRequest.setEntity(new StringEntity(request));
            LOG.log(Level.INFO, "Creative API payload : " + request);
        } catch (Exception x) {
            LOG.log(Level.SEVERE, "AppNexusApprovalManager request RuntimeException", x);
            throw new IllegalStateException("Failed to create AppNexus API payload", x);
        }
    }

    private synchronized AppNexusCreativeRecord execute(HttpUriRequest httpRequest, int returnStatus) {
        return execute(httpRequest, returnStatus, true);
    }

    /**
     * This is where the  magic happens. Execute the request and get the  AppNexus creative from the response.
     * 
     */
    private synchronized AppNexusCreativeRecord execute(HttpUriRequest httpRequest, int returnStatus, boolean isFirstAttempt) {
        LOG.info("Creative API executing: " + httpRequest);
        try {
            authLock.lock();
            if (authToken == null) {
                // Authentication token initialization failed during startup...
                LOG.warning("AppNexus authentication token is missing. Acquiring it now.");
                String authToken = getAuthToken();
                if (authToken != null) {
                    this.authToken = authToken;
                } else {
                    throw new ExternalSystemException("Cannot acquire AppNexus authentication token");
                }
            }
        } finally {
            authLock.unlock();
        }

        String usedAuthToken = authToken;
        httpRequest.setHeader("Authorization", authToken);
        httpRequest.setHeader("Accept", "application/json");

        HttpResponse httpResponse;
        try {
            httpResponse = connMgr.execute(httpRequest, new BasicHttpContext());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "AppNexus API call failed", e);
            throw new ExternalSystemException(e);
        }

        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() != returnStatus) {
            throw new ExternalSystemException("Unexpected AppNexus API HTTP status: " + statusLine);
        }

        HttpEntity entity = httpResponse.getEntity();

        Header contentType = entity.getContentType();
        if (contentType == null || !contentType.getValue().startsWith(APPLICATION_JSON)) {
            throw new ExternalSystemException("Unexpected AppNexus API Content-Type: " + contentType);
        }

        AppNexusResponseWrapper responseRecord = null;
        try {
            responseRecord = MAPPER.readValue(entity.getContent(), AppNexusResponseWrapper.class);
        } catch (Exception x) {
            LOG.log(Level.SEVERE, "Failed to parse AppNexus API response", x);
            throw new ExternalSystemException("Failed to parse AppNexus API response", x);
        }

        AppNexusResponse anxResponse = responseRecord.getResponse();

        if ("NOAUTH".equals(anxResponse.getError_id())) {
            // Authention token is expired or not valid anymore... 
            LOG.log(Level.INFO, "AppNexus NOAUTH response. Auth token is invalid: " + anxResponse);
            if (isFirstAttempt) {
                try {
                    // We do not want to overload authentication endpoint  
                    authLock.lock();
                    // Check if another thread haven't renewed token while we waited for lock
                    if (!differs(usedAuthToken, authToken)) {
                        // Same old invalid token still there -> acquire new token
                        String authToken = getAuthToken();
                        if (authToken != null) {
                            this.authToken = authToken;
                        } else {
                            throw new ExternalSystemException("Cannot acquire AppNexus API authentication token");
                        }
                    }
                    return execute(httpRequest, returnStatus, false); //go for second attempt                    

                } finally {
                    authLock.unlock();
                }
            } else {
                throw new ExternalSystemException("Still getting NOAUTH after renewing AppNexus API authentication token");
            }
        }

        if ("NOTFOUND_CREATIVE".equals(anxResponse.getError_code())) {
            LOG.log(Level.INFO, "Creative is not known to AppNexus: " + anxResponse);
            return null; // Leave as there is nothing else to examine
        }

        // Catch any other generic errors we are not expecting.
        if (anxResponse.getError() != null || anxResponse.getError_id() != null) {
            LOG.log(Level.WARNING, "AppNexus creative API error:" + anxResponse);
            throw new ExternalSystemException("AppNexus creative API error:" + anxResponse);
        }

        return anxResponse.getCreative();
    }

    /**
     * @return true if both are null or not equal
     */
    boolean differs(String s1, String s2) {
        return s1 == null && s2 != null || s1 != null && s2 == null || s1 == null && s2 == null || !s1.equals(s2);
    }

    public void listCreatives() throws IOException {
        HttpGet httpRequest = new HttpGet(creativeServiceUrl);
        httpRequest.setHeader("Authorization", "not-yet");
        HttpResponse httpResponse = connMgr.execute(httpRequest, new BasicHttpContext());
        HttpEntity entity = httpResponse.getEntity();
        IOUtils.toString(entity.getContent());
    }

    static class CreativeListWrapper {

        private CreativeListResponse response;

    }

    static class CreativeListResponse {

        private String error;
        private String error_id;

        private String status;
        private Integer count;

        private Integer start_element;

        private Integer num_elements;

        private List<AppNexusCreativeRecord> creatives;
    }
}
