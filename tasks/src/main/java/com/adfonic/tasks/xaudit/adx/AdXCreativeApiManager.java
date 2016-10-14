package com.adfonic.tasks.xaudit.adx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.adexchangebuyer.AdExchangeBuyer;
import com.google.api.services.adexchangebuyer.AdExchangeBuyerScopes;
import com.google.api.services.adexchangebuyer.model.Creative;

/**
 * This class is responsible for integration with the Google AdX java api for Creative Approval v1.4
 * https://developers.google.com/ad-exchange/buyer-rest/creative-guide
 * 
 * 
 * We submit creatives complete with html snippets for external approval by AdX, 
 * and we check the status of creatives that have been previously submitted. 
 *
 */
public class AdXCreativeApiManager {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(getClass());

    private final AdExchangeBuyer client;

    private final Integer apiAccountId;

    public AdXCreativeApiManager(InputStream privateKeyStream, Integer apiAccountId, String accountEmail) throws IOException, GeneralSecurityException {
        this.apiAccountId = apiAccountId;
        this.client = initClient(IOUtils.toByteArray(privateKeyStream), accountEmail);
    }

    public Integer getApiAccountId() {
        return apiAccountId;
    }

    public Creative submitAdxCreative(long creativeId, Creative adxCreative) {
        try {
            LOG.info("AdX API sumbit: " + creativeId + "/" + adxCreative.getBuyerCreativeId());
            Creative outAdxCreative = client.creatives().insert(adxCreative).execute();
            LOG.info("AdX API sumbit return: " + outAdxCreative);
            return outAdxCreative;
        } catch (GoogleJsonResponseException gjrx) {
            LOG.info("AdX API sumbit refused: " + gjrx.getDetails());
            if (403 == gjrx.getStatusCode()) {
                // AdX has quota for submission calls per time period... 
            } else if (500 == gjrx.getStatusCode()) {
                LOG.warn("AdX API sumbit internal error: " + gjrx.getDetails());
            } else {
                LOG.warn(gjrx.getDetails().getMessage());

            }
            throw new IllegalStateException("AdX API sumbit refused: " + gjrx.getDetails());
        } catch (IOException iox) {
            throw new IllegalStateException("AdX API sumbit failed: " + creativeId + "/" + adxCreative.getBuyerCreativeId(), iox);
        }
    }

    public Creative getAdxCreative(String buyerCreativeId) {
        try {
            LOG.info("AdX API get: " + buyerCreativeId);
            Creative creative = client.creatives().get(this.apiAccountId, buyerCreativeId).execute();
            LOG.debug("AdX API get return: " + creative.toPrettyString());
            return creative;
        } catch (GoogleJsonResponseException gjrx) {
            if (404 == gjrx.getStatusCode()) {
                LOG.info("AdX API get: " + buyerCreativeId + " not yet submitted: " + gjrx.getMessage());
                return null; // ok np
            } else {
                throw new IllegalStateException("Adx API get creative refused: " + buyerCreativeId, gjrx);
            }
        } catch (IOException iox) {
            throw new IllegalStateException("Adx API get creative failed: " + buyerCreativeId, iox);
        }
    }

    private AdExchangeBuyer initClient(byte[] p12KeyFileBytes, String accountEmail) throws GeneralSecurityException, IOException {
        Objects.requireNonNull(accountEmail);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(SecurityUtils.getPkcs12KeyStore(), new ByteArrayInputStream(p12KeyFileBytes), "notasecret", "privatekey",
                "notasecret");

        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Credential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setServiceAccountId(accountEmail)
                .setServiceAccountScopes(AdExchangeBuyerScopes.all()).setServiceAccountPrivateKey(privateKey).build();
        return new AdExchangeBuyer.Builder(httpTransport, jsonFactory, credential).setApplicationName("Byyd-AdX-Creative-Approval/1.0").build();
    }
}
