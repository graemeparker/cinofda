package com.adfonic.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.springframework.core.io.ClassPathResource;

import com.adfonic.service.proxy.EligibilityServiceRegistry;
import com.adfonic.service.proxy.EligibleCreativeRegistryService;

@WebService(serviceName = "EligibilityServiceRegistry", portName = "EligibleCreativeRegistryServicePort", wsdlLocation = "EligibilityServiceRegistry.wsdl")
public class ECRegistryService implements IEligibleServiceRegistry {

    EligibleCreativeRegistryService parentService;

    @WebMethod(exclude = true)
    public void setParentService(String parentServiceEndpoint) {
        //Dummy change
        if (parentServiceEndpoint != null) {
            try { // TODO template code; extract 
                parentService = new EligibilityServiceRegistry(new ClassPathResource("EligibilityServiceRegistry.wsdl").getURL(), new QName("http://service.adfonic.com/",
                        "EligibilityServiceRegistry")).getEligibleCreativeRegistryServicePort();
                ((BindingProvider) parentService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, parentServiceEndpoint + "EligibilityServiceRegistry");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ConcurrentMap<Long, String> includeMap = new ConcurrentHashMap<Long, String>();
    Map<List<Long>, String> excludeMap = Collections.synchronizedMap(new HashMap<List<Long>, String>());

    @Override
    public void registerECService(boolean include, List<Long> publisherIds, String ecServiceEndpoint) {
        if (include) {
            for (Long publisherId : publisherIds) {
                includeMap.put(publisherId, ecServiceEndpoint);
            }
        } else {
            excludeMap.put(publisherIds, ecServiceEndpoint);
        }

        if (parentService != null) {
            parentService.registerECService(include, publisherIds, ecServiceEndpoint);
        }

    }

    @Override
    public String lookupECService(Long publisherId) {
        String service = includeMap.get(publisherId);
        if (service != null) {
            return service;
        }
        for (Entry<List<Long>, String> excludeMapEntry : excludeMap.entrySet()) {
            if (!excludeMapEntry.getKey().contains(publisherId)) {
                return excludeMapEntry.getValue();
            }
        }
        throw new RuntimeException("Nothing registered");
    }

}
