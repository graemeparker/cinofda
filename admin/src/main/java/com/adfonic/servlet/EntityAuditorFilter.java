package com.adfonic.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import com.adfonic.audit.EntityAuditor;
import com.adfonic.beans.BaseBean;
import com.adfonic.domain.AdfonicUser;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

public class EntityAuditorFilter implements Filter {
	
	protected Logger LOGGER = LoggerFactory.getLogger(EntityAuditorFilter.class);
    
	@Autowired
	private EntityAuditor entityAuditor;
    
    @Autowired(required=false)
	private AuditLogJpaListener auditLogJpaListener;
    
    private static final String JTRAC_USER_KEY = "userId";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) srequest;

        /*
         * Use the logged in admin. If none present, check for a param/value
         * on the request to support auditing changes from jtrac requests
         */
        AdfonicUser adfonicUser = BaseBean.adfonicUser(request);
        if (adfonicUser == null) {
            Long adfonicUserId = ServletRequestUtils.getLongParameter(request, JTRAC_USER_KEY);
            if (adfonicUserId != null) {
                UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
                adfonicUser = userManager.getAdfonicUserById(adfonicUserId);
            }
        }

        LOGGER.debug("Setting EntityAuditor context to User={} and AdfonicUser={}", null, adfonicUser);
        try {
        	entityAuditor.bindContext(null, adfonicUser);
        	if (auditLogJpaListener!=null) auditLogJpaListener.setContextInfo(null, adfonicUser);
            chain.doFilter(srequest, sresponse);
        } finally {
        	if (auditLogJpaListener!=null) auditLogJpaListener.cleanContextInfo();
            entityAuditor.unbindContext();
        }
    }

    public void destroy() {
    }
}
