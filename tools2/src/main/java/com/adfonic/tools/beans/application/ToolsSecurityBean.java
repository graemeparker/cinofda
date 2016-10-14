package com.adfonic.tools.beans.application;

import com.adfonic.presentation.credentials.AdfonicUserDetailsService;

public class ToolsSecurityBean {

    private AdfonicUserDetailsService userDetailsService;

    private AdfonicUserDetailsService userDetailsServiceAdmin;

    public AdfonicUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(AdfonicUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public AdfonicUserDetailsService getUserDetailsServiceAdmin() {
        return userDetailsServiceAdmin;
    }

    public void setUserDetailsServiceAdmin(AdfonicUserDetailsService userDetailsServiceAdmin) {
        this.userDetailsServiceAdmin = userDetailsServiceAdmin;
    }
}
