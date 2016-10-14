package com.adfonic.webservices.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.webservices.view.util.GenericMarshaller;

@Component
public class XmlGenericView extends AbstractGenericView {

    @Autowired
    @Qualifier("xml")
    GenericMarshaller marshaller;
    
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    protected GenericMarshaller getMarshaller() {
        return marshaller;
    }

}
