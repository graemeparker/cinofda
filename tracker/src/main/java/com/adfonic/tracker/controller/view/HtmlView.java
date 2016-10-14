package com.adfonic.tracker.controller.view;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class HtmlView {
    
    @Value("${AdTruth.redirect.harness}")
    private String adTruthRedirectHarness;
    
    @Value("${AdTruth.redirect.tracker.baseUrl}")
    private String adTruthRedirectTrackerBaseUrl;
    
    @Value("${AdTruth.redirect.tracker.assetUrl}")
    private String adTruthRedirectTrackerAssetUrl;
    
    public ModelAndView render(Map<String, Object> model) {
        model.put("tracker_asset_url", adTruthRedirectTrackerAssetUrl);
        model.put("tracker_base_url", adTruthRedirectTrackerBaseUrl);
        return new ModelAndView(adTruthRedirectHarness, model);
    }
}
