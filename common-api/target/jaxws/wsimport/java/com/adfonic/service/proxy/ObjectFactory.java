
package com.adfonic.service.proxy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.adfonic.service.proxy package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RegisterECServiceResponse_QNAME = new QName("http://service.adfonic.com/", "registerECServiceResponse");
    private final static QName _LookupECServiceResponse_QNAME = new QName("http://service.adfonic.com/", "lookupECServiceResponse");
    private final static QName _RegisterECService_QNAME = new QName("http://service.adfonic.com/", "registerECService");
    private final static QName _LookupECService_QNAME = new QName("http://service.adfonic.com/", "lookupECService");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.adfonic.service.proxy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RegisterECService }
     * 
     */
    public RegisterECService createRegisterECService() {
        return new RegisterECService();
    }

    /**
     * Create an instance of {@link LookupECService }
     * 
     */
    public LookupECService createLookupECService() {
        return new LookupECService();
    }

    /**
     * Create an instance of {@link RegisterECServiceResponse }
     * 
     */
    public RegisterECServiceResponse createRegisterECServiceResponse() {
        return new RegisterECServiceResponse();
    }

    /**
     * Create an instance of {@link LookupECServiceResponse }
     * 
     */
    public LookupECServiceResponse createLookupECServiceResponse() {
        return new LookupECServiceResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterECServiceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.adfonic.com/", name = "registerECServiceResponse")
    public JAXBElement<RegisterECServiceResponse> createRegisterECServiceResponse(RegisterECServiceResponse value) {
        return new JAXBElement<RegisterECServiceResponse>(_RegisterECServiceResponse_QNAME, RegisterECServiceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LookupECServiceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.adfonic.com/", name = "lookupECServiceResponse")
    public JAXBElement<LookupECServiceResponse> createLookupECServiceResponse(LookupECServiceResponse value) {
        return new JAXBElement<LookupECServiceResponse>(_LookupECServiceResponse_QNAME, LookupECServiceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterECService }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.adfonic.com/", name = "registerECService")
    public JAXBElement<RegisterECService> createRegisterECService(RegisterECService value) {
        return new JAXBElement<RegisterECService>(_RegisterECService_QNAME, RegisterECService.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LookupECService }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.adfonic.com/", name = "lookupECService")
    public JAXBElement<LookupECService> createLookupECService(LookupECService value) {
        return new JAXBElement<LookupECService>(_LookupECService_QNAME, LookupECService.class, null, value);
    }

}
