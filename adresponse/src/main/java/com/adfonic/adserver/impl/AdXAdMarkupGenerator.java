package com.adfonic.adserver.impl;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;

@Component(AdXAdMarkupGenerator.BEAN_NAME)
public class AdXAdMarkupGenerator extends ProfileBasedMarkupGenerator {

    public static final String BEAN_NAME = "ADX_ADM";

    @Autowired
    public AdXAdMarkupGenerator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dcProperties) {
        super(velocityEngine, displayTypeUtils, dcProperties);
    }

    @Override
    protected String getProfileBasedHtmlPathSegment() {
        return "adx-rtb/html";
    }

}
