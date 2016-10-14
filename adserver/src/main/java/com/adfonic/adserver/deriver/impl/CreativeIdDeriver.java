package com.adfonic.adserver.deriver.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/** Derive the viewer's age range from the request */
@Component
public class CreativeIdDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(CreativeIdDeriver.class.getName());

    @Autowired
    public CreativeIdDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.PROVIDED_CREATIVE_ID);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.PROVIDED_CREATIVE_ID.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String creativeIds = context.getAttribute(Parameters.CREATIVE_ID, String.class);
        if (creativeIds != null && !creativeIds.trim().equals("")) {
            String[] allCreativeExternalIds = creativeIds.split(",");
            List<Long> allCreativeIds = new ArrayList<Long>();
            CreativeDto creative;
            for (int i = 0; i < allCreativeExternalIds.length; i++) {
                creative = context.getAdserverDomainCache().getCreativeByExternalID(allCreativeExternalIds[i].trim());
                if (creative != null) {
                    allCreativeIds.add(creative.getId());
                } else {
                    LOG.log(Level.INFO, "Provided Creative External Id not found in cache :" + allCreativeExternalIds[i].trim());
                    //adding -1 as Creative Id to make sure we pass non existing Creatives to Targeting Loop and 
                    //then it can say No add found, if we dont add non existing creatives in the list , it will be same as 
                    // if there was no creative passed, and we can continue with normal ad request processing
                    allCreativeIds.add(-1L);
                }
            }
            return allCreativeIds.toArray(new Long[allCreativeIds.size()]);
        }
        //Creative Id was not present in request to return null
        return null;
    }
}
