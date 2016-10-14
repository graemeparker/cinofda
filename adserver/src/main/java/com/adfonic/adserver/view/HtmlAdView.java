package com.adfonic.adserver.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Component
public class HtmlAdView extends AbstractAdView {
    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderAd(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, CreativeDto creative, Impression impression,
            AdComponents adComponents, boolean doMarkupIgnored, BeaconsMode beaconsModeIgnored) throws Exception {
        // Pass true as the last arg so beacons get rendered in the HTML markup
        response.getWriter().append(getMarkupGenerator().generateMarkup(adComponents, context, context.getAdSpace(), creative, impression, true));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void renderError(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, String error) throws Exception {
        response.getWriter().append("<!-- No ad available -->");
    }
}
