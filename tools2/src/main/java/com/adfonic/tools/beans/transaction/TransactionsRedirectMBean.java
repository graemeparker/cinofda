package com.adfonic.tools.beans.transaction;

import static com.adfonic.tools.beans.util.Constants.P_TRANSACTIONS_ADVERTISER;
import static com.adfonic.tools.beans.util.Constants.P_TRANSACTIONS_PUBLISHER;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

@Component
@Scope("request")
@URLMapping(id = "transactions-redirect", pattern = "/transactions", viewId = "NONE")
public class TransactionsRedirectMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2824406831866896692L;

    @URLAction(mappingId = "transactions-redirect")
    public String redirectToDashboard() {
        if ("publisher".equals(getUser().getUserType())) {
            return P_TRANSACTIONS_PUBLISHER;
        } else if ("advertiser".equals(getUser().getUserType())) {
            return P_TRANSACTIONS_ADVERTISER;
        } else if ("agency".equals(getUser().getUserType())) {
            if (getUser().getAdvertiserDto() != null) {
                return P_TRANSACTIONS_ADVERTISER;
            } else {
                return P_TRANSACTIONS_PUBLISHER;
            }
        }
        return null;
    }

    @Override
    protected void init() {

    }

}
