package com.adfonic.tasks.combined.truste;

import java.io.IOException;
import java.util.Date;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

@RunWith(MockitoJUnitRunner.class)
public class TrusteDateDeserializerTest {

    TrusteDateDeserializer testObj = new TrusteDateDeserializer();
    
    @Mock
    private JsonParser jp;
    @Mock
    private DeserializationContext ctxt;
    
    @Test
    public void testDeserializeJsonParserDeserializationContext() throws JsonProcessingException, IOException {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
        String strDate = "2012-03-13 14:56:32.987";
        Mockito.when(jp.getText()).thenReturn(strDate);
        
        Date result = testObj.deserialize(jp, ctxt);
        
        LocalDateTime ldt = LocalDateTime.fromDateFields(result);
        String resultParsed = dtf.print(ldt);
        Assert.assertEquals(strDate, resultParsed);
    }
    
}
