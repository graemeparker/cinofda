package com.adfonic.tools.converter.advertiser;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.advertiser.AdvertiserConverter", forClass = com.adfonic.dto.advertiser.AdvertiserDto.class)
public class AdvertiserConverter extends GenericConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdvertiserConverter.class);

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
                CompanyService service = getSpringService(context, com.adfonic.presentation.company.CompanyService.class);
                Object obj = service.getAdvertiserById(lvalue);
                if (((AdvertiserDto) obj).getId() == null) {
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
            AdvertiserDto dto = (AdvertiserDto) value;
            return dto.getId().toString();
        } else {
            return null;
        }
    }

}
