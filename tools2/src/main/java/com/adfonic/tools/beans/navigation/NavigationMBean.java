package com.adfonic.tools.beans.navigation;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.presentation.user.UserService;

@Component
@Scope("application")
public class NavigationMBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private UserService uService;

    @Value("{tools.hostname}")
    private String toolsHostname;

    @Value("${navigationMBean.ssoBaseUrl}")
    private String ssoBaseUrl;

    @Value("${navigationMBean.adminLink}")
    private String adminLink;

    @Value("${navigationMBean.changeEmailLogoutLink}")
    private String changeEmailLogoutLink;

    public String getToolsHostname() {
        return toolsHostname;
    }

    public void setToolsHostname(String toolsHostname) {
        this.toolsHostname = toolsHostname;
    }

    public String getSsoBaseUrl() {
        return ssoBaseUrl;
    }

    public void setSsoBaseUrl(String ssoBaseUrl) {
        this.ssoBaseUrl = ssoBaseUrl;
    }

    public String getAdminLink() {
        return adminLink;
    }

    public void setAdminLink(String adminLink) {
        this.adminLink = adminLink;
    }

    public String getChangeEmailLogoutLink() {
        return changeEmailLogoutLink;
    }

    public void setChangeEmailLogoutLink(String changeEmailLogoutLink) {
        this.changeEmailLogoutLink = changeEmailLogoutLink;
    }
}
