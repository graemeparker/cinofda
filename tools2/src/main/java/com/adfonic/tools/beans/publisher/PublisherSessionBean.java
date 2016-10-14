package com.adfonic.tools.beans.publisher;

import java.io.Serializable;

import org.primefaces.context.RequestContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("session")
public class PublisherSessionBean extends GenericAbstractBean implements Serializable {
    public PublisherSessionBean() {
        super();
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Boolean dontShowContentDialog = null;

    public boolean isDontShowContentDialog() {
        if (dontShowContentDialog == null) {
            String dontShow = getCookieValueFromRequest(String.class, Constants.COOKIE_DONT_SHOW_DIALOG);

            if (dontShow != null && dontShow.equals("true")) {
                dontShowContentDialog = true;
            } else {
                dontShowContentDialog = false;
            }
        }
        return dontShowContentDialog;
    }

    public void setDontShowContentDialog(boolean dontShowContentDialog) {
        this.dontShowContentDialog = dontShowContentDialog;
    }

    public String saveAndNavigate() {
        addDontShowCookie(dontShowContentDialog);
        getPublicationNavigationBean().init();
        return "pretty:publicationAdd";
    }

    public String goToAddPublication() {
        if (dontShowContentDialog) {
            getPublicationNavigationBean().init();
            return "pretty:publicationAdd";
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("createPubdialog.show()");
            return "";
        }
    }

    @Override
    protected void init() {
    }

}
