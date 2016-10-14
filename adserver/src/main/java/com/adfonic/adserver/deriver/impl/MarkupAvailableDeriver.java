package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ResponseFormat;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

/** Derive whether or not markup is available */
@Component
public class MarkupAvailableDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(MarkupAvailableDeriver.class.getName());

    @Autowired
    public MarkupAvailableDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.MARKUP_AVAILABLE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.MARKUP_AVAILABLE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String markupParam = context.getAttribute(Parameters.MARKUP);
        if (markupParam == null || "1".equals(markupParam) || "true".equals(markupParam)) {
            return true;
        }

        // Markup is also implicitly available for t.format=html
        String formatParam = context.getAttribute(Parameters.FORMAT);
        return ResponseFormat.html.name().equals(formatParam);
    }
}
