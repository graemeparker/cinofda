package com.adfonic.tools.converter.user;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.user.UserConverter", forClass = com.adfonic.dto.user.UserDTO.class)
public class UserConverter extends GenericConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConverter.class);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        LOGGER.debug("value: " + value);

        if (StringUtils.isBlank(value)) {
            LOGGER.debug("empty value, cannot convert");
            return null;
        } else {
            Long lvalue = null;
            try {
                lvalue = Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                // not a long
                LOGGER.debug("value is not a long, can't convert");
                return null;
            }

            if (lvalue != null && lvalue > 0) {
                UserService service = getSpringService(context, com.adfonic.presentation.user.UserService.class);
                Object obj = service.getUserById(lvalue);
                if (((UserDTO) obj).getId() == null) {
                    LOGGER.debug("lookup of id: " + lvalue + " failed.");
                    return null;
                }
                return obj;
            } else {
                LOGGER.debug("value is null or zero, cannot lookup");
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            UserDTO dto = (UserDTO) value;
            return dto.getId().toString();
        } else {
            return null;
        }
    }

}
