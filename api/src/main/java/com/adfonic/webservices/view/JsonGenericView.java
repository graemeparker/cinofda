package com.adfonic.webservices.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.webservices.view.util.GenericMarshaller;

@Component
public class JsonGenericView extends AbstractGenericView {

    @Autowired
    @Qualifier("json")
    GenericMarshaller marshaller;

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    protected GenericMarshaller getMarshaller() {
        return marshaller;
    }
    
}
