package com.adfonic.tools.converter.browser;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.presentation.campaign.browser.BrowserService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.browser.BrowserConverter", forClass = com.adfonic.dto.browser.BrowserDto.class)
public class BrowserConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            BrowserService service = getSpringService(context, com.adfonic.presentation.campaign.browser.BrowserService.class);
            try {
                BrowserDto browserDto = service.getBrowserById(Long.valueOf(value));
                return browserDto;
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
            BrowserDto dto = (BrowserDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}
