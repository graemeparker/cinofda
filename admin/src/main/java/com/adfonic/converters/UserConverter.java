package com.adfonic.converters;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.User;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@FacesConverter(value="userConverter")
public class UserConverter extends BaseConverter {
    private static final transient Logger LOG = Logger.getLogger(UserConverter.class.getName());

    public UserConverter() {
    }

    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        else {
            Long lvalue = null;
            try {
                lvalue = Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                // not a long
                return null;
            }       
            
            if (lvalue != null && lvalue > 0) {
                UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
                User obj = userManager.getUserById(lvalue);
                if(obj==null){
                    LOG.log(Level.SEVERE, "lookup of id: " + lvalue + " failed.");
                    return null;
                }
                return obj;
            }
            else {
                LOG.log(Level.SEVERE, "value is null or zero, cannot lookup");
                return null;
            }
        }
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            User user = (User) value;
            return Long.toString(user.getId());
        } else {
            return StringUtils.EMPTY;
        }
    }
}
