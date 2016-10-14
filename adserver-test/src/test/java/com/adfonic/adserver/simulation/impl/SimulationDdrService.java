package com.adfonic.adserver.simulation.impl;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.ddr.DdrService;
import com.adfonic.ddr.HttpHeaderAware;

public class SimulationDdrService implements DdrService {

	@Override
	public Map<String, String> getDdrProperties(String userAgent) {
		Map<String, String> m = new HashMap<String, String>();

		m.put("isFilter", "0");
		m.put("isFeedReader", "0");
		m.put("_matched", userAgent);
		m.put("isRobot", "0");
		m.put("isChecker", "0");
		m.put("isBrowser", "1");
		m.put("mobileDevice", "1");
		m.put("isDownloader", "0");
		m.put("isSpam", "0");
		m.put("id", "iPhone5s");
		
		return m;
	}

	@Override
	public Map<String, String> getDdrProperties(HttpHeaderAware context) {
		return getDdrProperties("User Agent");
	}

}
