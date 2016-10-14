package com.adfonic.converters;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;

import com.adfonic.beans.AdminGeneralException;
import com.adfonic.domain.BusinessKey;
import com.adfonic.domain.HasPrimaryKeyId;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.service.BaseManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@FacesConverter(value="jpaConverter")
public class JPAConverter extends BaseConverter {
    private static final transient Logger LOG = Logger.getLogger(JPAConverter.class.getName());

    private Class<?> clazz;

    public JPAConverter(Class<?> clazz) {
        this.clazz = clazz;
    }

    public JPAConverter() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        if (StringUtils.isNotBlank(value)) {
            Class cla55 = clazz;
            if (cla55 == null) {
                // This is a total kludge. I'm sorry. It had to be done.
                try {
                    cla55 = Class.forName(value.substring(value.indexOf(']') + 1));
                } catch (Exception e) {
                    throw new AdminGeneralException("Failed to resolve Class for: " + value, e);
                }
            }

            String oidStr = value.substring(0, value.indexOf('['));
            long lvalue = 0;
            try {
                lvalue = Long.valueOf(oidStr);
            } catch (NumberFormatException nfe) {
                LOG.log(Level.SEVERE, "NumberFormatException attempting to get Long.valueOf: " + oidStr, nfe);
                return null;
            }

            if (lvalue > 0) {
                BaseManager baseManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
                return baseManager.getObjectById(cla55, lvalue);
            }
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null && value instanceof HasPrimaryKeyId) {
            // TODO: change this to Long if we ever switch to nullable ids,
            // and if we do, we'll need to null check this.  But there's no
            // need to null check it as is.  Zero-check on the other hand...
            long oid = ((BusinessKey)value).getId();
            if (oid > 0) {
                return String.valueOf(oid) + "[OID]" + value.getClass().getName();
            }
        }
        return StringUtils.EMPTY;
    }
}
