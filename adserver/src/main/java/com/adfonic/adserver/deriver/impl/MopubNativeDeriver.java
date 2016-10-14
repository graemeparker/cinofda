package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

@Component
public class MopubNativeDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(MopubNativeDeriver.class.getName());

    @Autowired
    public MopubNativeDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.IS_NATIVE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.IS_NATIVE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        Boolean nativeReq = context.getAttribute(Parameters.NATIVE);
        if (nativeReq == null) {
            return Boolean.valueOf(false);
        }

        return nativeReq;
    }
}
