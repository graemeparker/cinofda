package com.adfonic.adserver.rtb.mapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.util.TargetingContextUtil;
import com.adfonic.adserver.rtb.util.TargetingContextUtil.ContextBuilder;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.SerializationConfig;

/**
 * BidRequsets of all sorts/exchanges are unmarshaller using spring-mvc marshallers so careful mvc configuration is crucial
 */
public class BidMapperTest {

    @Test
    public void testSpringMvcConverters() throws Exception {

        com.fasterxml.jackson.databind.ObjectMapper mapper = WebConfig.getRtbJsonMapper();

        DeserializationConfig deserConfig = mapper.getDeserializationConfig();
        Assertions.assertThat(deserConfig.isEnabled(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        Assertions.assertThat(deserConfig.isEnabled(com.fasterxml.jackson.databind.MapperFeature.USE_GETTERS_AS_SETTERS)).isFalse();

        SerializationConfig serialConfig = mapper.getSerializationConfig();
        Assertions.assertThat(serialConfig.getSerializationInclusion()).isEqualTo(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        // Check custom RtbEnumDeserializer for invalid APIFramework code : 1000
        APIFramework[] apif = mapper.readValue("[3, 1000]", APIFramework[].class);
        Assertions.assertThat(apif).hasSize(2);
        Assertions.assertThat(apif[0]).isEqualTo(APIFramework.MRAID);
        Assertions.assertThat(apif[1]).isNull();

        // Check custom enum ordinal(index) serialization
        StringWriter stringWriter = new StringWriter();
        mapper.writeValue(stringWriter, new APIFramework[] { APIFramework.MRAID, APIFramework.MRAID_2 });
        Assertions.assertThat(stringWriter.toString()).isEqualTo("[3,5]");
    }

    /**
     * This crap is injected into every Mapper so mock that sh*t 
     */
    public static TargetingContextUtil getMockedTargetingContextUtil() {

        TargetingContextUtil ctxtUtil = Mockito.mock(TargetingContextUtil.class);
        ContextBuilder contextBuilder = Mockito.mock(ContextBuilder.class);
        Mockito.when(ctxtUtil.builder()).thenReturn(contextBuilder);
        Mockito.when(contextBuilder.set(Mockito.anyString(), Mockito.anyObject())).thenReturn(contextBuilder);

        TargetingContext targetingContext = Mockito.mock(TargetingContext.class);
        Mockito.when(contextBuilder.get()).thenReturn(targetingContext);
        return ctxtUtil;
    }

    static class StringInput implements HttpInputMessage {

        private String body;

        public StringInput(String body) {
            this.body = body;
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body.getBytes(Charset.forName("utf-8")));
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }

    }
}
