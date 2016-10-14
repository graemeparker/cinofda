package com.adfonic.adserver.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.ortb.nativead.NativeAdRequest;
import com.adfonic.ortb.nativead.NativeAdRequest.NativeAdRequestWrapper;
import com.adfonic.ortb.nativead.NativeAdRequestAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.DataAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.DataAssetType;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.ImageAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.ImageAssetType;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.TitleAsset;
import com.adfonic.ortb.nativead.NativeAdResponse;
import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdLink;
import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdResponseWrapper;
import com.adfonic.ortb.nativead.NativeAdResponseAsset;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * OpenRTB 2.3 native request/response 
 */
public class OrtbNativeAdWorker {

    private static final OrtbNativeAdWorker instance = new OrtbNativeAdWorker();

    public static OrtbNativeAdWorker instance() {
        return instance;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectReader requestReader;
    private final ObjectWriter requestWriter;
    private final ObjectReader responseReader;
    private final ObjectWriter responseWriter;

    private OrtbNativeAdWorker() {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL); // Do NOT write null
        // create readers/writers AFTER configuring ObjectMapper
        requestReader = objectMapper.readerFor(NativeAdRequestWrapper.class);
        requestWriter = objectMapper.writerFor(NativeAdRequestWrapper.class);
        responseReader = objectMapper.readerFor(NativeAdResponseWrapper.class);
        responseWriter = objectMapper.writerFor(NativeAdResponseWrapper.class);
    }

    public String toString(NativeAdResponseWrapper response) throws IOException {
        StringWriter stringWriter = new StringWriter();
        responseWriter.writeValue(stringWriter, response);
        return stringWriter.toString();
    }

    public String toString(NativeAdRequestWrapper request) throws IOException {
        StringWriter stringWriter = new StringWriter();
        requestWriter.writeValue(stringWriter, request);
        return stringWriter.toString();
    }

    public NativeAdRequestWrapper readRequest(String string) throws IOException {
        return requestReader.readValue(string);
    }

    public NativeAdRequestWrapper readResponse(String string) throws IOException {
        return responseReader.readValue(string);
    }

    public NativeAdResponseWrapper buildResponse(NativeAdRequest nativeAdRequest, CreativeDto creative, AdComponents adComponents, TargetingContext context, boolean renderBeacons) {
        Map<String, Map<String, String>> components = adComponents.getComponents();

        Map<String, String> extendedData = creative.getExtendedData();
        List<NativeAdResponseAsset> responseAssets = new ArrayList<NativeAdResponseAsset>();
        List<NativeAdRequestAsset> requestAssets = nativeAdRequest.getAssets();
        for (NativeAdRequestAsset requestAsset : requestAssets) {
            // Build response native assets from byyd components and extended data entries and preserve original request native asset ids
            TitleAsset titleAsset = requestAsset.getTitle();
            ImageAsset imageAsset = requestAsset.getImg();
            DataAsset dataAsset = requestAsset.getData();
            // VideoAsset videoAsset = requestAsset.getVideo(); // Do not even try as we have no video in native supported at all...
            if (titleAsset != null) {
                NativeAdResponseAsset.TitleAsset responseAsset = new NativeAdResponseAsset.TitleAsset(extendedData.get("title"));
                responseAssets.add(new NativeAdResponseAsset(requestAsset.getId(), responseAsset));
            } else if (imageAsset != null) {
                // Map image asset type to byyd component  
                ImageAssetType type = ImageAssetType.valueOf(imageAsset.getType());
                String componentName = null;
                if (type == ImageAssetType.Icon) {
                    componentName = SystemName.COMPONENT_APP_ICON;
                } else if (type == ImageAssetType.Main) {
                    componentName = SystemName.COMPONENT_IMAGE;
                }
                if (componentName != null) {
                    Map<String, String> mainImage = components.get(componentName);
                    String url = mainImage.get("url");
                    Integer w = Integer.parseInt(mainImage.get("width"));
                    Integer h = Integer.parseInt(mainImage.get("height"));
                    NativeAdResponseAsset.ImageAsset responseAsset = new NativeAdResponseAsset.ImageAsset(url, w, h);
                    responseAssets.add(new NativeAdResponseAsset(requestAsset.getId(), responseAsset));
                }
            } else if (dataAsset != null) {
                // Map image asset type to byyd extended data entry  
                DataAssetType type = DataAssetType.valueOf(dataAsset.getType());
                String extDataName = null;
                if (type == DataAssetType.ctatext) {
                    extDataName = "click_to_action";
                } else if (type == DataAssetType.desc) {
                    extDataName = "description";
                }
                if (extDataName != null) {
                    String value = extendedData.get(extDataName);
                    responseAssets.add(new NativeAdResponseAsset(requestAsset.getId(), new NativeAdResponseAsset.DataAsset(value)));
                } else {

                }
            }
        }
        // Native ads have special impression trackers field - respect it!
        List<String> imptrackers = context.getAttribute(TargetingContext.IMP_TRACK_LIST);
        NativeAdLink link = new NativeAdLink(adComponents.getDestinationUrl());
        return new NativeAdResponseWrapper(new NativeAdResponse(link, responseAssets, imptrackers));
    }
}
