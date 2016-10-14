package com.adfonic.sso.beans;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware{

    private static ApplicationContext ctx = null;
    
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        ApplicationContextProvider.ctx = ctx;
    }
}
