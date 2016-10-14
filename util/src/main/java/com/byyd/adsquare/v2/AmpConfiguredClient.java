package com.byyd.adsquare.v2;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;

import com.adfonic.http.ApiClient;
import com.byyd.adsquare.AdsquareApiException;
import com.byyd.breaker.CircuitException;

public class AmpConfiguredClient implements Closeable {

    private final AmpApiClient ampClient;

    private final String username;

    private final String password;

    private final String dspId;

    private volatile String authToken;

    private long tokenExpiresAt;

    public AmpConfiguredClient(AmpApiClient ampClient, String username, String password, String dspId) {
        Objects.requireNonNull(ampClient);
        this.ampClient = ampClient;
        Objects.requireNonNull(username);
        this.username = username;
        Objects.requireNonNull(password);
        this.password = password;
        Objects.requireNonNull(dspId);
        this.dspId = dspId;
    }

    @Override
    public void close() {
        ampClient.close();
    }

    public void reset() {
        ampClient.reset();
    }

    public AmpApiClient getAmpClient() {
        return ampClient;
    }

    public List<AmpCompany> companies() {
        return doAuthenticatedCall(new Function<String, List<AmpCompany>>() {
            @Override
            public List<AmpCompany> apply(String authToken) {
                return ampClient.companies(authToken, dspId);
            }
        });
    }

    public List<AmpAudience> audiences() {
        return doAuthenticatedCall(new Function<String, List<AmpAudience>>() {
            @Override
            public List<AmpAudience> apply(String authToken) {
                return ampClient.audiences(authToken, dspId);
            }
        });
    }

    public List<AmpSupplySidePlatform> ssps() {
        return doAuthenticatedCall(new Function<String, List<AmpSupplySidePlatform>>() {
            @Override
            public List<AmpSupplySidePlatform> apply(String authToken) {
                return ampClient.ssps(authToken);
            }
        });
    }

    public void trackImpression(int audienceId, String appId, Double latitude, Double longitude, String deviceId, Date impressionTime, Integer sspId) {
        doAuthenticatedCall(new Function<String, Void>() {
            @Override
            public Void apply(String authToken) {
                ampClient.trackImpression(authToken, dspId, audienceId, appId, latitude, longitude, deviceId, impressionTime, sspId);
                return null;
            }
        });
    }

    public void trackClick(int audienceId, String appId, Double latitude, Double longitude, String deviceId, Date impressionTime, Integer sspId) {
        doAuthenticatedCall(new Function<String, Void>() {
            @Override
            public Void apply(String authToken) {
                ampClient.trackClick(authToken, dspId, audienceId, appId, latitude, longitude, deviceId, impressionTime, sspId);
                return null;
            }
        });
    }

    private <R> R doAuthenticatedCall(Function<String, R> supplier) {
        String authToken = getAuthToken(false); // throws Exception...
        try {
            return supplier.apply(authToken); // try operation with existing token
        } catch (CircuitException cx) {
            Throwable x = cx.getCause();
            if (x instanceof AdsquareApiException) {
                AdsquareApiException aax = (AdsquareApiException) x;
                // XXX Hmmmmm, this is quite lame...maybe we can create and throw AdsquareApiUnauthorizedException and simply catch it here
                if (aax.getMessage().indexOf("401 Unauthorized") != -1) {
                    authToken = getAuthToken(true); // get a new token    
                    return supplier.apply(authToken); // try operation with new token // throws Exception...
                }
            }
            throw cx;
        }
    }

    /**
     * Synchronized access to prevent parallel login calls. 
     * No high volume of calls is expected to go through here so it should be ok
     */
    private synchronized String getAuthToken(boolean tokenIsBroken) {
        if (authToken == null || tokenExpiresAt < System.currentTimeMillis() || tokenIsBroken) {
            authToken = ampClient.login(username, password); // throws Exception
            String[] tokenParts = authToken.split("\\.");
            //String header = new String(Base64.decodeBase64(tokenParts[0]), ApiClient.UTF_8);
            String payload = new String(Base64.decodeBase64(tokenParts[1]), ApiClient.UTF_8);
            //String signature = new String(Base64.decodeBase64(tokenParts[2]), ApiClient.UTF_8);
            try {
                Map<String, Object> map = ampClient.jackson.readValue(payload, Map.class);
                Integer expiryAtSecs = (Integer) map.get("exp"); // seconds since 1970
                if (expiryAtSecs == null) {
                    throw new IllegalStateException("Failed to get exiry from token: " + map);
                }
                tokenExpiresAt = ((expiryAtSecs - 60) * 1000l); // Expire it 60 seconds earlier then actual  
            } catch (IOException iox) {
                throw new IllegalStateException("Cannot parse JWT token payload: " + payload, iox);
            }
        }
        return authToken;
    }

    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient("adsqrapm", "https://amp.adsquare.com", 2000, 5000, 3, 30, 5, 30_000);
        AmpApiClient ampClient = new AmpApiClient(apiClient);
        AmpConfiguredClient client = new AmpConfiguredClient(ampClient, "enrichment-demo@adsquare.com", "#enrichment-demo1", "5655c3dde4b0b70902221001");
        List<AmpAudience> ssps = client.audiences();
        System.out.println(ssps);
    }

}
