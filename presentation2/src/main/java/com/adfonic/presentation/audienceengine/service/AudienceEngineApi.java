package com.adfonic.presentation.audienceengine.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.adfonic.presentation.audienceengine.exception.AudienceEngineApiException;


@Service("AudienceEngineApi")
public class AudienceEngineApi implements AudienceEngineAPIInterface {

    private static final int STATUS_200 = 200;

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceEngineApi.class);

    // Audience Engine configs
    
    @Value("${audienceengine.protocol:http}")
    private String audienceEngineProtocol;
    @Value("${audienceengine.host:localhost}")
    private String audienceEngineHost;
    @Value("${audienceengine.port:9000}")
    private String audienceEnginePort;
    @Value("${audienceengine.api.assign:/api/audience/add/}")
    private String audienceEngineApiAssign;
    
    private HttpClient httpclient;

    public AudienceEngineApi() {
    }

    @Override
    public void notifyAssignedFiles(Long firstPartyAudienceId, Set<String> fileIds) throws AudienceEngineApiException {
        if (!fileIds.isEmpty()) {
            String errorMessage = StringUtils.EMPTY;
            httpclient = new DefaultHttpClient();
    
            // Build the get query
            StringBuilder getQuery = new StringBuilder(audienceEngineProtocol).append("://");
            getQuery.append(audienceEngineHost);
            getQuery.append(":").append(audienceEnginePort);
            getQuery.append(audienceEngineApiAssign);
            getQuery.append(String.valueOf(firstPartyAudienceId));
            getQuery.append("?").append("file=").append(StringUtils.join(fileIds, "&file="));
    
            LOGGER.info("AudienceEngine API Get call: " + getQuery.toString());
            
            HttpGet httpGet = new HttpGet(getQuery.toString());
            httpGet.addHeader("accept", "application/json");
            try {
                HttpResponse response = httpclient.execute(httpGet);
                String responseContent = getResponseContent(response);
                if (STATUS_200 == response.getStatusLine().getStatusCode()) {
                    LOGGER.info("Successful AudienceEngine response:\n" + responseContent);
                } else {
                    errorMessage = "Failed AudienceEngine response with statuscode:\n" + responseContent;
                    LOGGER.error(errorMessage);
                    throw new AudienceEngineApiException(errorMessage);
                }
            } catch (IOException ioe) {
                errorMessage = "Error during calling AudienceEngine api";
                LOGGER.error(errorMessage, ioe);
                throw new AudienceEngineApiException(errorMessage, ioe);
            } finally {
                if (httpclient != null && httpclient.getConnectionManager() != null) {
                    httpclient.getConnectionManager().shutdown();
                }
            }
        }
    }
    
    // ////////////////////
    // Private methods
    // ////////////////////

    private String getResponseContent(HttpResponse response) throws IOException {
        StringBuilder content = new StringBuilder();
        if (response != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
            br.close();
        }
        return content.toString();
    }
}
