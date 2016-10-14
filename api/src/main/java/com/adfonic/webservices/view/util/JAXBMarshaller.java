package com.adfonic.webservices.view.util;

import java.io.PrintWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("xml")
public class JAXBMarshaller implements GenericMarshaller {

    private JAXBContext jxbCtx;
    private Object toMarshal;
   
    @Override
    public void marshal(Object object, String wrapperName, PrintWriter writer) throws Exception {
    	if(object instanceof List<?> && wrapperName!=null) {
    		List<?> list = (List<?>) object;
    		if(list.size()>0) {
	        	jxbCtx = JAXBContext.newInstance(ListWrapper.class, list.get(0).getClass());
	        	toMarshal = new JAXBElement<ListWrapper>(new QName(wrapperName), ListWrapper.class, new ListWrapper<>(list));
    		} else {
    			jxbCtx = JAXBContext.newInstance(ListWrapper.class);
	        	toMarshal = new JAXBElement<ListWrapper>(new QName(wrapperName), ListWrapper.class, new ListWrapper<>(list));
    		}
    	} else {
    		jxbCtx = JAXBContext.newInstance(object.getClass());
    		toMarshal = object;
    	}
    	
        Marshaller marshaller = jxbCtx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(toMarshal, writer);
    }        
    
}
