package com.adfonic.adserver.web.jsf.managed.beans;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;

public class PlatformConvertor implements Converter{

	DomainCacheManager domainCacheManager;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String submittedValue) {
		if (submittedValue.trim().equals("")) {  
            return null;  
        } else {  
            try {  
            	
                long platformId = Long.parseLong(submittedValue);
                PlatformDto platform = domainCacheManager.getCache().getPlatformById(platformId);
                return platform;
            } catch(NumberFormatException exception) {  
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid player"));  
            }  
        }  
  
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object platform) {
		 if (platform == null || "".equals(platform)) {
	            return "";
	        }
		return String.valueOf(((PlatformDto)platform).getId());
	}

	public DomainCacheManager getDomainCacheManager() {
		return domainCacheManager;
	}

	public void setDomainCacheManager(DomainCacheManager domainCacheManager) {
		this.domainCacheManager = domainCacheManager;
	}

}
