package com.adfonic.presentation.campaign.browser.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Browser;
import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.presentation.campaign.browser.BrowserService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

@Service("browserService")
public class BrowserServiceImpl extends GenericServiceImpl implements BrowserService {
         
	@Autowired
	private DeviceManager deviceManager;
	
	public BrowserDto getOperaBrowser() {
		Browser br = deviceManager.getOperaBrowser();
		return getObjectDto(BrowserDto.class, br);
	}
 
	public Collection<BrowserDto> getAllBrowsers() {
	    List<Browser> browsers = deviceManager.getAllBrowsers(new Sorting(SortOrder.asc("browserOrder"))); 
        Collection<BrowserDto> browserDtos = getList(BrowserDto.class, browsers);
	    return browserDtos;
	}
	
    public BrowserDto getBrowserById(Long id) {
        Browser entity = deviceManager.getBrowserById(id);
        BrowserDto dto = getObjectDto(BrowserDto.class, entity);
        return dto;
    }
}
