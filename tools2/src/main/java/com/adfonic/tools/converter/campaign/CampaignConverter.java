package com.adfonic.tools.converter.campaign;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.campaign.CampaignConverter", forClass = com.adfonic.dto.campaign.CampaignDto.class)
public class CampaignConverter extends GenericConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignConverter.class);

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
                CampaignService service = getSpringService(context, com.adfonic.presentation.campaign.CampaignService.class);
                CampaignSearchDto dto = new CampaignSearchDto();
                dto.setId(lvalue);
                Object obj = service.getCampaignTypeAheadDtoById(dto);
                if (((CampaignTypeAheadDto) obj).getId() == null) {
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
            NameIdBusinessDto dto = (NameIdBusinessDto) value;
            return dto.getId().toString();
        } else {
            return null;
        }
    }

}
