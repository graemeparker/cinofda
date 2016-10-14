package com.adfonic.adserver.view;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ResponseParameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.util.HttpUtils;

@Component
public class UrlencodeAdView extends AbstractAdView {
    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderAd(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, CreativeDto creative, Impression impression,
            AdComponents adComponents, boolean doMarkup, BeaconsMode beaconsMode) throws Exception {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(ResponseParameters.STATUS, "success");
        if (impression != null && impression.getExternalID() != null) {
            params.put(ResponseParameters.AD_RESPONSE_ID, impression.getExternalID());
        }
        if (adComponents != null) {
            params.put(ResponseParameters.FORMAT, adComponents.getFormat());

            Map<String, Map<String, String>> componentsToInclude = null;

            if (doMarkup) {
                // We need to provide a singular AD_CONTENT value with full markup of the ad components
                params.put(ResponseParameters.AD_CONTENT,
                        getMarkupGenerator().generateMarkup(adComponents, context, context.getAdSpace(), creative, impression, beaconsMode == BeaconsMode.markup));

                // See if we need to supply beacons in the metadata components alongside the markup
                if (beaconsMode == BeaconsMode.metadata) {
                    Map<String, String> beacons = adComponents.getComponents().get("beacons");
                    if (beacons != null) {
                        componentsToInclude = new LinkedHashMap<String, Map<String, String>>();
                        componentsToInclude.put("beacons", beacons);
                    }
                }

                // See if there was a "bid" component...if so, we need to pass it
                // along as metadata irrespective of markup mode.
                Map<String, String> bid = adComponents.getComponents().get("bid");
                if (bid != null) {
                    if (componentsToInclude == null) {
                        componentsToInclude = new LinkedHashMap<String, Map<String, String>>();
                    }
                    componentsToInclude.put("bid", bid);
                }
            } else {
                // We need to parameterize the ad components
                if (adComponents.getDestinationType() != null) {
                    params.put(ResponseParameters.DESTINATION_TYPE, String.valueOf(adComponents.getDestinationType()));
                }
                if (adComponents.getDestinationUrl() != null) {
                    params.put(ResponseParameters.DESTINATION_URL, adComponents.getDestinationUrl());
                }

                // Include all components in the returned metadata
                componentsToInclude = adComponents.getComponents();
            }

            if (componentsToInclude != null) {
                // Provide a comma-separated list of component names
                params.put(ResponseParameters.COMPONENTS, StringUtils.join(componentsToInclude.keySet(), ','));

                for (Map.Entry<String, Map<String, String>> entry : componentsToInclude.entrySet()) {
                    String componentName = entry.getKey();
                    Map<String, String> component = entry.getValue();
                    for (Map.Entry<String, String> attribute : component.entrySet()) {
                        params.put(ResponseParameters.COMPONENT_PREFIX + "." + componentName + "." + attribute.getKey(), attribute.getValue());
                    }
                }
            }
        }
        response.getWriter().append(HttpUtils.encodeParams(params));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderError(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, String error) throws Exception {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(ResponseParameters.STATUS, "error");
        if (error == null) {
            params.put(ResponseParameters.ERROR_MESSAGE, "No ad available");
        } else {
            params.put(ResponseParameters.ERROR_MESSAGE, error);
        }
        response.getWriter().append(HttpUtils.encodeParams(params));
    }
}
