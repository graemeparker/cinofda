package com.adfonic.tools.converter.model;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.model.ModelDto;
import com.adfonic.presentation.model.ModelService;
import com.adfonic.tools.beans.commons.DeviceModelsMBean;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.model.ModelConverter", forClass = com.adfonic.dto.model.ModelDto.class)
public class ModelConverter extends GenericConverter {

    private static final String SEPARATOR = "#";

    /**
     * The value represents the model_id and vendor_name with separator Example:
     * 234{@value #SEPARATOR}Samsung
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            ModelService service = getSpringService(context, com.adfonic.presentation.model.ModelService.class);
            try {
                Object obj = null;

                String[] modelIdAndVendorName = value.split(SEPARATOR);
                Long modelId = Long.valueOf(modelIdAndVendorName[0]);
                if (DeviceModelsMBean.isVendorModelId(modelId)) {
                    obj = DeviceModelsMBean.buildModelForVendor(modelIdAndVendorName[1]);
                } else {
                    obj = service.getModelById(modelId);
                }

                return obj;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Retrieve the model_id and vendor_name with separator Example: 234
     * {@value #SEPARATOR}Samsung
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        ModelDto dto = (ModelDto) value;
        if (dto != null) {
            return dto.getId() + SEPARATOR + dto.getVendor().getName();
        } else {
            return null;
        }
    }
}
