package com.adfonic.adserver.simulation.impl;

import java.util.HashMap;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheImpl;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.repository.EcpmRepositoryIncremental;
import com.adfonic.data.cache.util.Properties;
import com.adfonic.domain.cache.service.AdSpaceServiceImpl;
import com.adfonic.domain.cache.service.CategoryServiceImpl;
import com.adfonic.domain.cache.service.CreativeServiceImpl;
import com.adfonic.domain.cache.service.CurrencyServiceImpl;
import com.adfonic.domain.cache.service.WeightageServices;
import com.adfonic.domain.cache.service.WeightageServicesImpl;

public class SimulationAdserverDataCacheManagerImpl implements
		AdserverDataCacheManager {

	private EcpmDataRepository ecpmRepository = new EcpmRepositoryIncremental();
	private AdserverDataCacheImpl dataCache = new AdserverDataCacheImpl(
			ecpmRepository);
	private WeightageServicesImpl weightageService = new WeightageServicesImpl();

	@Override
	public AdserverDataCache getCache() {
		HashMap<String, String> m = new HashMap<String, String>();

		dataCache.setProperties(new Properties(m, m, m, m));
		dataCache.setAdSpaceService(new AdSpaceServiceImpl());
		dataCache.setCategoryService(new CategoryServiceImpl());
		dataCache.setCreativeService(new CreativeServiceImpl());
		dataCache.setCurrencyService(new CurrencyServiceImpl());
		
		return dataCache;
	}

	@Override
	public WeightageServices getEcpmDataCacheAsWS() {
		return weightageService;
	}

}
