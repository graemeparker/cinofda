package com.adfonic.adserver.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.adfonic.adserver.rtb.nativ.APIFramework;
import com.adfonic.adserver.rtb.nativ.AdType;
import com.adfonic.adserver.rtb.open.v1.RtbEnumDeserializer;
import com.byyd.ortb.CreativeAttribute;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;

/**
 * Discovered via context:component-scan from dispatcher-servlet.xml
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    public static final String PIXEL_RESOURCE = "1x1transparent.gif";

    private static final com.fasterxml.jackson.databind.ObjectMapper RTB_MAPPER = configureJackson2(new com.fasterxml.jackson.databind.ObjectMapper());

    public static com.fasterxml.jackson.databind.ObjectMapper getRtbJsonMapper() {
        return RTB_MAPPER;
    }

    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping requestHandlerMapping = super.requestMappingHandlerMapping();
        requestHandlerMapping.setAlwaysUseFullPath(true);
        return requestHandlerMapping;
    }

    /**
     * Here is Jackson2 ObjectMapper configuration used for all OpenRTB controllers. If there will be need to have more than one, because of some exchange request incompatibility,
     * we can easily create another differently configured ObjectMapper for that specific exchange.
     * 
     * It is mixup/mistake/error prone to have different JSON convertors for different Controllers in Spring MVC. 
     * For RTB Controllers we do not use Spring MVC convertors, but have rather manual approach to parse incoming exchange bid requests and serialize our bid responses.
     *  
     * 
     * http://wiki.fasterxml.com/JacksonFeaturesDeserialization
     * http://wiki.fasterxml.com/JacksonFeaturesSerialization
     */
    private static com.fasterxml.jackson.databind.ObjectMapper configureJackson2(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.MapperFeature.USE_GETTERS_AS_SETTERS, false);
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        com.fasterxml.jackson.databind.module.SimpleModule module = new com.fasterxml.jackson.databind.module.SimpleModule("EnumDeserMod", new com.fasterxml.jackson.core.Version(
                1, 0, 0, null, null, null));
        module.addDeserializer(AdType.class, new RtbEnumDeserializer<AdType>(AdType.class));
        module.addDeserializer(CreativeAttribute.class, new RtbEnumDeserializer<CreativeAttribute>(CreativeAttribute.class));
        module.addDeserializer(APIFramework.class, new RtbEnumDeserializer<APIFramework>(APIFramework.class));

        // Ufff Jackson2 does not makes this easy...
        JsonFormat.Value scalarValue = new JsonFormat.Value(null, JsonFormat.Shape.NUMBER, Locale.getDefault(), TimeZone.getDefault(), null);
        module.addSerializer(AdType.class, EnumSerializer.construct(AdType.class, objectMapper.getSerializationConfig(), null, scalarValue));
        module.addSerializer(CreativeAttribute.class, EnumSerializer.construct(CreativeAttribute.class, objectMapper.getSerializationConfig(), null, scalarValue));
        module.addSerializer(APIFramework.class, EnumSerializer.construct(APIFramework.class, objectMapper.getSerializationConfig(), null, scalarValue));

        objectMapper.registerModule(module);
        return objectMapper;
    }

    public static byte[] loadPixel() {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PIXEL_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Classpath resource not found: " + PIXEL_RESOURCE);
        }
        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException iox) {
            throw new IllegalStateException("Failed to load existing classpath resource: " + PIXEL_RESOURCE);
        }
        //LOG.info("Respource " + PIXEL_RESOURCE + " loaded. Size: " + gifContent.length + " bytes");
    }

}