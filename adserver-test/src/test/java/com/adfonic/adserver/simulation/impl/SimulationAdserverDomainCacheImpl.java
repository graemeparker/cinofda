package com.adfonic.adserver.simulation.impl;

import com.adfonic.domain.cache.ext.AdserverDomainCacheImpl;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CategoryService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.MiscCacheService;
import com.adfonic.domain.cache.service.RtbCacheService;
import com.adfonic.domain.cache.service.WeightageServices;

@SuppressWarnings("serial")
public class SimulationAdserverDomainCacheImpl extends AdserverDomainCacheImpl {

	public AdSpaceService getAdSpaceService() {
		return adSpaceService;
	}

	public void setAdSpaceService(AdSpaceService adSpaceService) {
		this.adSpaceService = adSpaceService;
	}

	public CategoryService getCategoryService() {
		return categoryService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public CreativeService getCreativeService() {
		return creativeService;
	}

	public void setCreativeService(CreativeService creativeService) {
		this.creativeService = creativeService;
	}

	public MiscCacheService getMiscCacheService() {
		return miscCacheService;
	}

	public void setMiscCacheService(MiscCacheService miscCacheService) {
		this.miscCacheService = miscCacheService;
	}

	public WeightageServices getWeightageServices() {
		return weightageServices;
	}

	public void setWeightageServices(WeightageServices weightageServices) {
		this.weightageServices = weightageServices;
	}

	public RtbCacheService getRtbCacheService() {
		return rtbCacheService;
	}

	public void setRtbCacheService(RtbCacheService rtbCacheService) {
		this.rtbCacheService = rtbCacheService;
	}

}
