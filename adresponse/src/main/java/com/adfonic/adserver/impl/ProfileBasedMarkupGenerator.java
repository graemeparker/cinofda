package com.adfonic.adserver.impl;

import java.awt.Dimension;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;

public abstract class ProfileBasedMarkupGenerator extends MarkupGeneratorImpl {

    public ProfileBasedMarkupGenerator(VelocityEngine velocityEngine, DisplayTypeUtils displayTypeUtils, DynamicProperties dcProperties) {
        super(velocityEngine, displayTypeUtils, dcProperties);
    }

    @Override
    protected Template getHtmlTemplate(String formatSystemName, String displayTypeSystemName, Dimension templateSize) {
        // Build the name of the Velocity template that we'll use. The naming
        // convention is: "<format.systemName>[_<templateSize>].vtl"
        StringBuilder bld = new StringBuilder();
        String profilePathSegment = getProfileBasedHtmlPathSegment();
        if (profilePathSegment != null) {
            bld.append("adm-profiles/").append(profilePathSegment).append('/');
        } else {
            bld.append("html/");
        }

        bld.append(formatSystemName);

        // TODO: only allow 320x50 override; don't generalize
        if (templateSize != null && templateSize.getWidth() == 320 && templateSize.getHeight() == 50) {
            bld.append("_320x50");
        }

        bld.append(".vtl");
        String templateName = bld.toString();

        return getAndCacheTemplate(templateName);
    }

    protected abstract String getProfileBasedHtmlPathSegment();

}
