package com.adfonic.tools.converter.extendedcreativetype;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.extendedcreativetype.ExtendedCreativeTypeConverter", forClass = com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto.class)
public class ExtendedCreativeTypeConverter extends GenericConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedCreativeTypeConverter.class);

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
                CreativeService service = getSpringService(context, com.adfonic.presentation.campaign.creative.CreativeService.class);
                Object obj = service.getExtendedCreativeTypeById(lvalue);
                if (((ExtendedCreativeTypeDto) obj).getId() == null) {
                    LOGGER.debug("lookup of id: " + lvalue + " failed.");
                    return new ExtendedCreativeTypeDto();
                }
                return obj;
            } else {
                LOGGER.debug("value is null or zero, cannot lookup");
                return new ExtendedCreativeTypeDto();
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        ExtendedCreativeTypeDto dto = (ExtendedCreativeTypeDto) value;
        if (value != null) {
            if (dto.getId() == null) {
                return FacesUtils.getBundleMessage("page.campaign.creative.thirdparty.selectvendor");
            }
            return dto.getId().toString();
        } else {
            return null;
        }
    }

}
