package com.adfonic.adserver.simulation.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.adfonic.iws.InternalWebServicesClient;

public class SimulationIWSClient extends InternalWebServicesClient {

	public SimulationIWSClient() {
		super(null, 100, 1000, 100);
	}
	
	public SimulationIWSClient(String baseUrl, int connTtlMs, int maxTotal,
			int defaultMaxPerRoute) {
		super(baseUrl, connTtlMs, maxTotal, defaultMaxPerRoute);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getStoppages() throws IOException {
		return new HashMap();
	}

}
