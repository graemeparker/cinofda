package com.adfonic.adserver.deriver;

import com.adfonic.adserver.TargetingContext;

public interface Deriver {
    Object getAttribute(String attribute, TargetingContext context);

    boolean canDeriveMoreThanOnce(String attribute);
}
