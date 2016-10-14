
package com.adfonic.service.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-b01-
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "EligibilityService", targetNamespace = "http://service.adfonic.com/", wsdlLocation = "classpath:*")
public class EligibilityService
    extends Service
{

    private final static URL ELIGIBILITYSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(com.adfonic.service.proxy.EligibilityService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.adfonic.service.proxy.EligibilityService.class.getResource(".");
            url = new URL(baseUrl, "classpath:*");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'classpath:*', retrying as a local file");
            logger.warning(e.getMessage());
        }
        ELIGIBILITYSERVICE_WSDL_LOCATION = url;
    }

    public EligibilityService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EligibilityService() {
        super(ELIGIBILITYSERVICE_WSDL_LOCATION, new QName("http://service.adfonic.com/", "EligibilityService"));
    }

    /**
     * 
     * @return
     *     returns EligibleCreativeService
     */
    @WebEndpoint(name = "EligibleCreativeServicePort")
    public EligibleCreativeService getEligibleCreativeServicePort() {
        return super.getPort(new QName("http://service.adfonic.com/", "EligibleCreativeServicePort"), EligibleCreativeService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns EligibleCreativeService
     */
    @WebEndpoint(name = "EligibleCreativeServicePort")
    public EligibleCreativeService getEligibleCreativeServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://service.adfonic.com/", "EligibleCreativeServicePort"), EligibleCreativeService.class, features);
    }

}
