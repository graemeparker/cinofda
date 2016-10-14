package com.adfonic.tools.beans.dashboard;

import static com.adfonic.tools.beans.util.Constants.P_DASHBOARD_ADVERTISER;
import static com.adfonic.tools.beans.util.Constants.P_DASHBOARD_AGENCY;
import static com.adfonic.tools.beans.util.Constants.P_DASHBOARD_PUBLISHER;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

@Component
@Scope("request")
@URLMapping(id = "dashboard-redirect", pattern = "/dashboard", viewId = "NONE")
public class DashboardRedirectMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2824406831866896692L;

    @URLAction(mappingId = "dashboard-redirect")
    public String redirectToDashboard() {
        if ("publisher".equals(getUser().getUserType())) {
            return P_DASHBOARD_PUBLISHER;
        } else if ("advertiser".equals(getUser().getUserType())) {
            return P_DASHBOARD_ADVERTISER;
        } else if ("agency".equals(getUser().getUserType())) {
            return P_DASHBOARD_AGENCY;
        }
        return null;
    }

    @Override
    protected void init() {

    }

}
