package com.adfonic.tasks.xaudit.impl;

import java.util.Set;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;

@Component("default")
public class ExternalApprovalFallBackImpl extends ExternalApprovalSystem {
    
    protected ExternalApprovalFallBackImpl() {
    }

    protected ExternalApprovalFallBackImpl(Set<Long> publisherIds) {
		super(publisherIds);
	}

	private static final transient Logger LOG = Logger.getLogger(ExternalApprovalFallBackImpl.class.getName());


    @Override
    public String newCreative(Creative creative, Publisher publisher) {
        LOG.warning("External creative not really added for :" + creative.getName());
        return "DummyExtIdFor-" + creative.getId();
    }


    @Override
    public void updateCreative(String externalReference, Creative creative, Publisher publisher) {
        LOG.warning("External creative " + creative.getName() + " not really updated against ref: " + externalReference);
    }


    @Override
    public String checkForAnyCreativeIncompatibility(Creative creative) {
        LOG.warning("External creative " + creative.getName() + " - passing without checks");
        return null;
    }

    @Override
    public AppNexusCreativeRecord getAppNexusCreative(String externalReference) {
        // TODO Auto-generated method stub
        return null;
    }

}
