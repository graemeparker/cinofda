package com.adfonic.webservices.view.util;

import java.io.PrintWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("json")
public class JacksonMarshaller implements GenericMarshaller {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void marshal(Object object, String wrapperName, PrintWriter writer) throws Exception {
    	mapper.writeValue(writer, object);
    }    

}
