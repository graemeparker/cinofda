package com.adfonic.adserver.view;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Component
public class JsonAdView extends AbstractAdView {
    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void renderAd(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, CreativeDto creative, Impression impression,
            AdComponents adComponents, boolean doMarkup, BeaconsMode beaconsMode) throws Exception {
        JSONObject ad = new JSONObject();
        ad.put("status", "success");

        if (impression.getExternalID() != null) {
            ad.put("adId", impression.getExternalID());
        }

        ad.put("format", adComponents.getFormat());

        JSONObject destination = new JSONObject();
        destination.put("type", adComponents.getDestinationType().name());
        destination.put("url", adComponents.getDestinationUrl());
        ad.put("destination", destination);

        Map<String, Map<String, String>> componentsToInclude = null;

        if (doMarkup) {
            ad.put("adContent", getMarkupGenerator().generateMarkup(adComponents, context, context.getAdSpace(), creative, impression, beaconsMode == BeaconsMode.markup));

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
            // Include all components in the returned metadata
            componentsToInclude = adComponents.getComponents();
        }

        if (componentsToInclude != null) {
            JSONObject components = new JSONObject();
            for (Map.Entry<String, Map<String, String>> entry : componentsToInclude.entrySet()) {
                JSONObject component = new JSONObject();
                for (Map.Entry<String, String> attribute : entry.getValue().entrySet()) {
                    component.put(attribute.getKey(), attribute.getValue());
                }
                components.put(entry.getKey(), component);
            }
            ad.put("components", components);
        }

        ad.writeJSONString(response.getWriter());
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void renderError(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, String error) throws Exception {
        JSONObject ad = new JSONObject();
        ad.put("status", "error");
        ad.put("error", error);
        ad.writeJSONString(response.getWriter());
    }
}
