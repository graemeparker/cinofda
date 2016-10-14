package com.adfonic.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * A custom view handler that attempts to preempt:
 * {@link javax.faces.application.ViewExpiredException}.
 *
 * We see a large number of errors thrown when a jsf view cannot be
 * restored. The most common and easiest to reproduce case is
 * setting session timeout to a small value, loading a jsf form, and
 * submitting it after the session has timed out.
 *
 * There are many examples of dealing with this in in JSF plumbing in
 * the wild but none have made it past testing with our stack.
 *
 * Additionally, we've used web.xml to do:
 *     <error-page>
 *         <exception-type>javax.faces.application.ViewExpiredException</exception-type>
 *         <location>/index.jsf</location>
 *     </error-page>
 *
 * The above has sometimes worked to prevent the client from seeing a
 * 500 but the exception is still thrown and logged.
 *
 * This needs to be setup in the application section of faces-config.xml
 *    <view-handler>
 *        com.adfonic.util.ViewExpiredViewHandlerWrapper
 *    </view-handler>
 *
 */
public class ViewExpiredViewHandlerWrapper extends ViewHandlerWrapper {
    private Logger logger = Logger.getLogger(getClass().getName());
    private ViewHandler parent;

    public ViewExpiredViewHandlerWrapper(ViewHandler parent) {
        super();
        this.parent = parent;
    }

    @Override
    public ViewHandler getWrapped() {
        return parent;
    }

    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId) {
        UIViewRoot root = parent.restoreView(facesContext, viewId);
        if (root != null) {
            return root;
        }
        else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("restoreView failure for viewId: " + viewId);
            }

            // shove a request attribute to drive UI messaging
            HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
            request.setAttribute("sessionExpired", Boolean.TRUE);

            // alternatively, reload the current page if you really
            // want to confuse users
            // return createView(facesContext, viewId);
            return createView(facesContext, "/index.jsf");
        }
    }
}
