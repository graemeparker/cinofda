package com.adfonic.adserver.controller.dbg;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.AdServerFeatureFlag;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping("/adserver")
public class FeatureFlagController {

    @ResponseBody
    @RequestMapping(value = "/features", method = RequestMethod.GET, produces = "application/json")
    public Map<AdServerFeatureFlag, Boolean> list() {
        AdServerFeatureFlag[] values = AdServerFeatureFlag.values();
        Map<AdServerFeatureFlag, Boolean> retval = new HashMap<AdServerFeatureFlag, Boolean>(values.length);
        for (AdServerFeatureFlag AdServerFeatureFlag : values) {
            retval.put(AdServerFeatureFlag, AdServerFeatureFlag.isEnabled());
        }
        return retval;
    }

    @ResponseBody
    @RequestMapping(value = "/features/{featureFlag}", method = RequestMethod.GET, produces = "application/json")
    public Boolean get(@PathVariable("featureFlag") AdServerFeatureFlag featureFlag) {
        return featureFlag.isEnabled();
    }

    @ResponseBody
    @RequestMapping(value = "/features/{featureFlag}/{value}", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json")
    public AdServerFeatureFlag set(@PathVariable("featureFlag") AdServerFeatureFlag featureFlag, @PathVariable("value") String value) {
        if ("1".equals(value) || "true".equals(value) || "on".equals(value)) {
            featureFlag.setEnabled(true);
        } else if ("0".equals(value) || "false".equals(value) || "off".equals(value)) {
            featureFlag.setEnabled(false);
        }
        return featureFlag;
    }
}
