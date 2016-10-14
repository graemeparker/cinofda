package com.adfonic.adserver.deriver.impl;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.Deriver;
import com.adfonic.adserver.deriver.DeriverManager;

public abstract class BaseDeriver implements Deriver {

    protected BaseDeriver(DeriverManager deriverManager, String... attributes) {
        // Register this deriver for each of its attributes with the manager
        for (String attribute : attributes) {
            deriverManager.registerDeriver(attribute, this);
        }
    }

    @Override
    public abstract Object getAttribute(String attribute, TargetingContext context);

    /** Subclasses may override this method to change the behavior for
        certain attributes.  A default implementation is provided that
        does NOT allow repetitive derivation of the same attribute.
    */
    @Override
    public boolean canDeriveMoreThanOnce(String attribute) {
        // By default, we don't allow an attribute to be derived more than once.
        return false;
    }
}
