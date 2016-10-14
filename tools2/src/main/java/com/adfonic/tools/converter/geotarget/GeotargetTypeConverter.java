package com.adfonic.tools.converter.geotarget;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.geotarget.GeotargetTypeConverter", forClass = com.adfonic.dto.geotarget.GeotargetTypeDto.class)
public class GeotargetTypeConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            LocationService service = getSpringService(context, com.adfonic.presentation.location.LocationService.class);
            // geotargetingCountry, geotargetingType
            // CampaignTargetingLocationMBean bean = Utils.findBean(context,
            // Constants.CAMPAIGN_TARGETING_LOCATION_BEAN);
            // List<GeotargetDto> geotargets =
            // (List<GeotargetDto>)service.getGeotargetsByNameAndTypeAndIsoCode(value,
            // bean.getGeotargetingIso(), bean.getGeotargetingType());
            try {
                GeotargetTypeDto obj = service.getGeotargetTypeById(Long.valueOf(value));
                return obj;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            GeotargetTypeDto dto = (GeotargetTypeDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}
