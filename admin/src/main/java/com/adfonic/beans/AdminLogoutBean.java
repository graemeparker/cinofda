package com.adfonic.beans;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * Handles user logout by clearing user from session.
 */

@RequestScoped
@ManagedBean
public class AdminLogoutBean extends BaseBean {
    @ManagedProperty(value="#{casServerURL}")
    private String casServerURL;

    @ManagedProperty(value="#{casServerLogout}")
    private String casServerLogout;

    @ManagedProperty(value="#{baseURL}")
    private String baseURL;

    /**
     * Log the user out
     */
    public void doLogout() {
        ExternalContext ec =  FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(false);
        if (session != null) {
            session.invalidate();

            // cas logout
            try {
                ec.redirect(ec.encodeResourceURL(casServerURL + casServerLogout + "?url=" + baseURL));
            } catch (IOException ioe) {
                //
            }
        }
    }

    public void setCasServerURL(String casServerURL) {
        this.casServerURL = casServerURL;
    }
    public String getCasServerURL() {
        return this.casServerURL;
    }
    public void setCasServerLogout(String casServerLogout) {
        this.casServerLogout = casServerLogout;
    }
    public String getCasServerLogout() {
        return this.casServerLogout;
    }
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    public String getBaseURL() {
        return this.baseURL;
    }
}
