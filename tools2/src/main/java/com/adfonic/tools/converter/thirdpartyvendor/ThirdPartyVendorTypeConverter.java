package com.adfonic.tools.converter.thirdpartyvendor;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorTypeDto;
import com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorTypeService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.thirdpartyvendor.ThirdPartyVendorTypeConverter", forClass = com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorTypeDto.class)
public class ThirdPartyVendorTypeConverter extends GenericConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyVendorTypeConverter.class);

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
				ThirdPartyVendorTypeService service = getSpringService(context,
						com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorTypeService.class);
				Object obj = service.getThirdPartyVendorTypeDtoById(lvalue);
				if (((ThirdPartyVendorTypeDto) obj).getId() == null) {
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
		if (value instanceof String) {
			return null;
		}
		if (value != null) {
			ThirdPartyVendorTypeDto dto = (ThirdPartyVendorTypeDto) value;
			return dto.getId().toString();
		} else {
			return null;
		}
	}

}
