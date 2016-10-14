package com.adfonic.adserver.impl;

import java.awt.Dimension;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;

@Component(AppNexusMarkupGenerator.BEAN_NAME)
public final class AppNexusMarkupGenerator extends ProfileBasedMarkupGenerator {

    public static final String BEAN_NAME = "APPNXS_ADM";

    @Autowired
    public AppNexusMarkupGenerator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dcProperties) {
        super(velocityEngine, displayTypeUtils, dcProperties);
    }

    private static final String PATH_SEG = "appnxs-rtb";

    @Override
    protected String getProfileBasedHtmlPathSegment() {
        return PATH_SEG;
    }

    @Override
    protected Template getHtmlTemplate(String formatSystemName, String displayTypeSystemName, Dimension templateSize) {
        return getAndCacheTemplate("adm-profiles/" + PATH_SEG + "/beacons_generic.vtl");
    }

}
