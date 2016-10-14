package com.adfonic.sso.utils;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;

import com.adfonic.domain.User;

public class RequestContextUtils {
    
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;
    public static final String USER_SESSION_KEY = "user";
    
    private RequestContextUtils(){
    }

    public static Object getModel(RequestContext context, String modelName) {
        return context.getFlowScope().get(modelName);
    }

    public static Object getRequestParameter(RequestContext context, String parameterName) {
        return context.getRequestScope().get(parameterName);
    }
    
    public static void addError(MessageContext messages, String source, String code) {
        messages.addMessage(new MessageBuilder().error().source(source).code(code).build());
    }
    
    public static void addError(MessageContext messages, String code) {
        messages.addMessage(new MessageBuilder().error().code(code).build());
    }
    
    public static String getURLRoot(RequestContext context, boolean includeContextPath){
        ServletExternalContext externalContext = (ServletExternalContext)context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        ServletContext sc = (ServletContext) externalContext.getNativeContext();
        
        StringBuilder out = new StringBuilder();
        String scheme = request.getScheme();
        out.append(request.getScheme())
        .append("://")
        .append(request.getServerName());
        int port = request.getServerPort();
        if (("http".equals(scheme) && port != HTTP_DEFAULT_PORT) ||
                ("https".equals(scheme) && port != HTTPS_DEFAULT_PORT)) {
            out.append(':').append(port);
        }
        if (includeContextPath) {
            out.append(sc.getContextPath());
        }
        return out.toString();
    }
    
    public static String getRequestURI(RequestContext context) {
        ServletExternalContext externalContext = (ServletExternalContext)context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        return request.getRequestURI();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String[]> getParameterMap(RequestContext context) {
        ServletExternalContext externalContext = (ServletExternalContext)context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        return request.getParameterMap();
    }
    
    public static User getUserInSession(RequestContext context) {
        ServletExternalContext externalContext = (ServletExternalContext)context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        } else {
            return (User)session.getAttribute(USER_SESSION_KEY);
        }
    }

    public static String getRemoteAddress(RequestContext context) {
        ServletExternalContext externalContext = (ServletExternalContext)context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
        return request.getRemoteAddr();
    }
}
