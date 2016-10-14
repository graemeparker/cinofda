package com.adfonic.webservices.interceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.domain.PluginVendor;
import com.adfonic.domain.User;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.byyd.middleware.auditlog.listener.Partner;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

public class EntityAuditorInterceptor implements WebRequestInterceptor {
    private static final transient Logger LOG = Logger.getLogger(EntityAuditorInterceptor.class.getName());
    
    @Autowired(required=false)
    private AuditLogJpaListener auditLogJpaListener;    
    
    public void preHandle(WebRequest webRequest) throws Exception {
        HttpServletRequest request = (HttpServletRequest)webRequest.resolveReference(WebRequest.REFERENCE_REQUEST);
        EntityAuditor entityAuditor = AdfonicBeanDispatcher.getBean(EntityAuditor.class);
        User user = AuthenticationInterceptor.getAuthenticatedUserFromRequest(request);

        PluginVendor pluginVendor = PluginVendorAuthenticationInterceptor.getAuthenticatedPluginVendorFromRequest(request);
        
        if (pluginVendor!=null){
            Partner partner = new Partner(pluginVendor.getName(), pluginVendor.getApiUser());
            if (auditLogJpaListener!=null) auditLogJpaListener.setContextInfo(partner);
        } else {
            if (auditLogJpaListener!=null) auditLogJpaListener.setContextInfo(user, null);            
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Binding EntityAuditor context with User " + (user == null ? "null" : ("id=" + user.getId())));
        }
        entityAuditor.bindContext(user, null);
        
    }
    
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        auditLogJpaListener.cleanContextInfo();
    }
    
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Unbinding EntityAuditor context");
        }
        EntityAuditor entityAuditor = AdfonicBeanDispatcher.getBean(EntityAuditor.class);
        entityAuditor.unbindContext();
    }
}
