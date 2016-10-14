package com.adfonic.presentation.campaign.browser;

import java.util.Collection;

import com.adfonic.dto.browser.BrowserDto;

public interface BrowserService {
	public BrowserDto getOperaBrowser();
	public Collection<BrowserDto> getAllBrowsers();
	public BrowserDto getBrowserById(Long id);
}
