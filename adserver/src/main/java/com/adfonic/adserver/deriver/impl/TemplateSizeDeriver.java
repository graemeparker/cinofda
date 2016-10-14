package com.adfonic.adserver.deriver.impl;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

/**
 * Derive a possibly publisher-supplied template size override
 */
@Component
public class TemplateSizeDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(TemplateSizeDeriver.class.getName());

    @Autowired
    public TemplateSizeDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.TEMPLATE_SIZE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.TEMPLATE_SIZE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String widthStr = context.getAttribute(Parameters.TEMPLATE_WIDTH);
        if (StringUtils.isBlank(widthStr)) {
            return null; // all or nothing
        }

        String heightStr = context.getAttribute(Parameters.TEMPLATE_HEIGHT);
        if (StringUtils.isBlank(heightStr)) {
            return null; // all or nothing
        }

        int width;
        try {
            width = Integer.parseInt(widthStr);
        } catch (NumberFormatException e) {
            LOG.warning("Invalid value for " + Parameters.TEMPLATE_WIDTH + ": " + widthStr);
            return null; // all or nothing
        }

        int height;
        try {
            height = Integer.parseInt(heightStr);
        } catch (NumberFormatException e) {
            LOG.warning("Invalid value for " + Parameters.TEMPLATE_HEIGHT + ": " + heightStr);
            return null; // all or nothing
        }

        return new Dimension(width, height);
    }
}
