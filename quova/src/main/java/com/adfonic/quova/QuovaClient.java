package com.adfonic.quova;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.adfonic.util.LoadBalancingHttpClient;
import com.quova.data._1.Ipinfo;

public class QuovaClient {
    private static final transient Logger LOG = Logger.getLogger(QuovaClient.class.getName());

    private final LoadBalancingHttpClient httpClient;
    private final String quovaBaseUri;
    private final JAXBContext jaxbContext;
    // It's fairly expensive to create Unmarshaller instances, so we enable
    // reuse of instances by using ThreadLocal
    private final ThreadLocal<Unmarshaller> tlUnmarshaller;
    private final ResponseHandler<Ipinfo> ipinfoResponseHandler;

    public QuovaClient(LoadBalancingHttpClient httpClient,

    String quovaBaseUri) {
        this.httpClient = httpClient;
        this.quovaBaseUri = quovaBaseUri;

        try {
            LOG.info("Initializing JAXB context for Quova data classes");
            jaxbContext = JAXBContext.newInstance("com.quova.data._1");
        } catch (javax.xml.bind.JAXBException e) {
            throw new UnsupportedOperationException("Failed to create JAXBContext", e);
        }

        tlUnmarshaller = new ThreadLocal<Unmarshaller>() {
            @Override
            public Unmarshaller initialValue() {
                try {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Creating Unmarshaller instance");
                    }
                    return jaxbContext.createUnmarshaller();
                } catch (javax.xml.bind.JAXBException e) {
                    throw new UnsupportedOperationException("Failed to create Unmarshaller", e);
                }
            }
        };

        ipinfoResponseHandler = new IpinfoResponseHandler();
    }

    /**
     * Get info for a given IP address
     * @param ip the IP address in question
     * @return the Ipinfo object representing the XML response from Quova
     * @throws java.io.IOException if the request cannot be processed by the Quova server
     */
    public Ipinfo getIpinfo(String ip) throws java.io.IOException {
        // Use the load-balanced client wrapper to issue the request
        return httpClient.execute("GET", quovaBaseUri + ip, ipinfoResponseHandler);
    }

    /**
     * This ResponseHandler implementation transforms HttpResponse content into
     * a Quova Ipinfo object by using JAXB to unmarshal the XML response.
     */
    private final class IpinfoResponseHandler implements ResponseHandler<Ipinfo> {

        @Override
        public Ipinfo handleResponse(HttpResponse httpResponse) throws java.io.IOException {
            HttpEntity httpEntity = httpResponse.getEntity();
            try {
                StatusLine statusLine = httpResponse.getStatusLine();
                switch (statusLine.getStatusCode()) {
                case HttpStatus.SC_OK: // 200 Should be a valid XML response
                    return (Ipinfo) tlUnmarshaller.get().unmarshal(httpEntity.getContent());
                case HttpStatus.SC_NOT_FOUND: // 404 IP address not found
                    return null;
                case HttpStatus.SC_SERVICE_UNAVAILABLE: // 503 service not available (i.e. during Quova startup)
                default:
                    // Throw an exception to force a failover if possible
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.toString());
                }

            } catch (javax.xml.bind.JAXBException e) {
                LOG.log(Level.SEVERE, "Failed to unmarshall Ipinfo from Quova response", e);
                return null;
            } finally {
                // Ensure that the HttpEntity's InputStream gets closed
                EntityUtils.consumeQuietly(httpEntity);
            }
        }
    }
}
