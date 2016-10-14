package com.adfonic.adserver.simulation.impl;

import java.io.File;

import javax.annotation.PostConstruct;

import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class SimulationAdserverDomainCacheManager extends AdserverDomainCacheManager {
	private AdserverDomainCache cache = new SimulationAdserverDomainCacheImpl();
	
	public SimulationAdserverDomainCacheManager(File rootDir, String label,
			boolean useMemory) {
		super(rootDir, label, useMemory);
	}

	@PostConstruct
    public void initialize() throws java.io.IOException, java.lang.ClassNotFoundException {
	}
	
	@Override
	public AdserverDomainCache getCache() {
		return cache;
	}
}
