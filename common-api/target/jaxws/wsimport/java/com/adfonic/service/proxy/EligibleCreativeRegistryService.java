
package com.adfonic.service.proxy;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-b01-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "EligibleCreativeRegistryService", targetNamespace = "http://service.adfonic.com/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface EligibleCreativeRegistryService {


    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     */
    @WebMethod
    @RequestWrapper(localName = "registerECService", targetNamespace = "http://service.adfonic.com/", className = "com.adfonic.service.proxy.RegisterECService")
    @ResponseWrapper(localName = "registerECServiceResponse", targetNamespace = "http://service.adfonic.com/", className = "com.adfonic.service.proxy.RegisterECServiceResponse")
    public void registerECService(
        @WebParam(name = "arg0", targetNamespace = "")
        boolean arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        List<Long> arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2);

    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "lookupECService", targetNamespace = "http://service.adfonic.com/", className = "com.adfonic.service.proxy.LookupECService")
    @ResponseWrapper(localName = "lookupECServiceResponse", targetNamespace = "http://service.adfonic.com/", className = "com.adfonic.service.proxy.LookupECServiceResponse")
    public String lookupECService(
        @WebParam(name = "arg0", targetNamespace = "")
        Long arg0);

}
