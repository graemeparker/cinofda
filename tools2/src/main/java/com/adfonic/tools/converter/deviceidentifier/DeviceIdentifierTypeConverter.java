package com.adfonic.tools.converter.deviceidentifier;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.deviceidentifier.DeviceIdentifierTypeConverter", forClass = com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto.class)
public class DeviceIdentifierTypeConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            DeviceIdentifierService service = getSpringService(context,
                    com.adfonic.presentation.deviceidentifier.DeviceIdentifierService.class);
            Object obj = service.getDeviceIdentifierTypeById(Long.valueOf(value));
            return obj;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            DeviceIdentifierService service = getSpringService(context,
                    com.adfonic.presentation.deviceidentifier.DeviceIdentifierService.class);
            DeviceIdentifierTypeDto dto = service.getDeviceIdentifierTypeById(((DeviceIdentifierTypeDto) value).getId());
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
    //
}
