package com.adfonic.adserver.rtb.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;

@Component
public class TargetingContextUtil {

    @Autowired
    private TargetingContextFactory targetingContextFactory;

    public ContextBuilder builder() {
        return new ContextBuilder(targetingContextFactory);
    }

    public static class ContextBuilder {

        TargetingContext context;


        public ContextBuilder(TargetingContextFactory targetingContextFactory) {
            context = targetingContextFactory.createTargetingContext();
        }


        public ContextBuilder set(String name, Object value) {
            context.setAttribute(name, value);
            return this;
        }


        public TargetingContext get() {
            return context;
        }
    }
}
