package com.adfonic.adserver.view;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.util.XmlWriter;

@Component
public class XmlAdView extends AbstractAdView {
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderAd(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, CreativeDto creative, Impression impression,
            AdComponents adComponents, boolean doMarkup, BeaconsMode beaconsMode) throws Exception {
        XmlWriter xml = new XmlWriter(response.getOutputStream(), true);
        xml.startDoc().startTag("ad").newLine().startTag("status").text("success").endTag(true);

        if (impression.getExternalID() != null) {
            xml.startTag("adId").text(impression.getExternalID()).endTag(true);
        }

        xml.startTag("format").text(adComponents.getFormat()).endTag(true);

        xml.startTag("destination").newAttr("type", adComponents.getDestinationType()).newAttr("url", adComponents.getDestinationUrl()).endTag(true);

        Map<String, Map<String, String>> componentsToInclude = null;

        if (doMarkup) {
            xml.startTag("adContent")
                    .cdata(getMarkupGenerator().generateMarkup(adComponents, context, context.getAdSpace(), creative, impression, beaconsMode == BeaconsMode.markup)).endTag(true);

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
            xml.startTag("components").newLine();
            for (Map.Entry<String, Map<String, String>> entry : componentsToInclude.entrySet()) {
                String componentName = entry.getKey();
                Map<String, String> component = entry.getValue();
                xml.startTag("component").newAttr("type", componentName).newLine();
                for (Map.Entry<String, String> attribute : component.entrySet()) {
                    xml.startTag("attribute").newAttr("name", attribute.getKey()).text(attribute.getValue()).endTag(true);
                }
                xml.endTag(true); // component
            }
            xml.endTag(true); // components
        }

        xml.endTag(true) // ad
                .endDoc();
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderError(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, String error) throws Exception {
        XmlWriter xml = new XmlWriter(response.getOutputStream(), true);
        xml.startDoc().startTag("ad").newLine().startTag("status").text("error").endTag(true).startTag("error").text(error).endTag(true).endTag(true).endDoc();
    }
}
