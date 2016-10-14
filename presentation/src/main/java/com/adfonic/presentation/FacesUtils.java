package com.adfonic.presentation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Named;

public class FacesUtils {

    private static final String MSG = "msg";

    private FacesUtils() {
    }

    public static FacesContext getFacesContext(ServletRequest request, ServletResponse response) {
        // Try to get it first
        // http://www.thoughtsabout.net/blog/archives/000033.html
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext != null) {
            return facesContext;
        }

        FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        facesContext = contextFactory.getFacesContext(request.getServletContext(), request, response, lifecycle);

        InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

        return facesContext;
    }

    // You need an inner class to be able to call
    // FacesContext.setCurrentInstance
    // since it's a protected method
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }

    /**
     * Returns a FacesMessage given a Severity, summaryKey and detailKey
     *
     * @param severity
     * @param summaryKey
     * @param detailKey
     * */
    public static FacesMessage getFacesMessageById(Severity severity, String summaryKey, String detailKey, String... params) {
        ResourceBundle bundle = getResourceBundle();
        if (bundle != null) {
            return new FacesMessage(severity, (summaryKey != null) ? bundle.getString(summaryKey) : "", (detailKey != null) ? MessageFormat.format(
                    bundle.getString(detailKey), (Object[]) params) : "");
        } else {
            return new FacesMessage(FacesMessage.SEVERITY_ERROR, summaryKey, detailKey);
        }
    }

    /**
     * Meant to replace T1's BaseBean.messageForId()
     * 
     * @param id
     * @return
     */
    public static FacesMessage getFacesMessageForId(String id, String... params) {
        return new FacesMessage(getBundleMessage(id, params));
    }

    /**
     * Returns a message for de required key
     *
     * @param key
     * */
    public static String getBundleMessage(String key, String... params) {
        ResourceBundle bundle = getResourceBundle();
        if (bundle != null) {
            return MessageFormat.format(bundle.getString(key), (Object[]) params);
        } else {
            return null;
        }
    }

    /**
     * Returns a FacesMessage given a Severity, summaryKey and detailKey
     *
     * @param severity
     * @param summaryKey
     * @param detailKey
     * */
    public static String getLocalizedMessage(String key) {
        ResourceBundle bundle = getResourceBundle();
        if (bundle != null) {
            return bundle.getString(key);
        } else {
            return null;
        }
    }
    
    /**
     * Add a FacesMessage to the FacesContext.
     *
     * @param serverity the FacesMessage.Severity of the message
     * @param formId the id of the xhtml file we want to attach the message
     * @param messagethe  Message to show
     *
     **/
    public static void addFacesMessage(Severity severity, String formId, String message) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(severity, message, message);
        fc.addMessage(formId, fm);
    }
    
    /**
     * Add a FacesMessage to the FacesContext.
     *
     * @param serverity the FacesMessage.Severity of the message
     * @param formId the id of the xhtml file we want to attach the message
     * @param summaryKey the FacesMessage summary param
     * @param detailKey the FacesMessage detail param
     *
     **/
    public static void addFacesMessage(Severity severity, String formId, String summaryKey, String detailKey, String... params) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = getFacesMessageById(severity, summaryKey, detailKey, params);
        fc.addMessage(formId, fm);
    }
    
    /**
     * Encodes the URL relative to the context. For use where the c:url tag can't be used.
     */
    public static String url(String path) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        return sc.getContextPath() + path;
    }

    public static boolean advertiserCheck() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        Advertiser advertiser = (session == null) ? null : (Advertiser) session.getAttribute("advertiser");
        return advertiser != null;
    }

    /**
     * Makes a list from persistent items (that implement Named) that can be easily used as a bean property.
     */
    public static List<SelectItem> makeSelectItems(Collection<? extends Named> list) {
        return makeSelectItems(list, false);
    }

    /**
     * Makes a list from persistent items that implement Named to be used as a bean property. Optionally sorts the list.
     */
    public static List<SelectItem> makeSelectItems(Collection<? extends Named> list, boolean sort) {
        List<SelectItem> items = new ArrayList<SelectItem>(list.size());
        for (Named n : list) {
            items.add(new SelectItem(n, n.getName()));
        }
        if (sort) {
            Collections.sort(items, SelectItemComparators.LABEL_COMPARATOR);
        }
        return items;
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Enum> List<SelectItem> makeEnumSelectItems(T[] values, boolean sort) {
        return makeEnumSelectItems(java.util.Arrays.asList(values), sort);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Enum> List<SelectItem> makeEnumSelectItems(List<T> list, boolean sort) {
        List<SelectItem> items = new ArrayList<SelectItem>(list.size());
        for (T t : list) {
            items.add(new SelectItem(t, t.name()));
        }
        if (sort) {
            Collections.sort(items, SelectItemComparators.LABEL_COMPARATOR);
        }
        return items;
    }
    
    public static Locale getLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }
    
    /** Get the resource bundle */
    private static ResourceBundle getResourceBundle() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getResourceBundle(context, MSG);
    }

}
