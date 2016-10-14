package com.adfonic.adserver.simulation.model;

import java.util.List;

public class WeightedAdspaceEligibilityModel {
	private long adspaceId;
	private List<PriorityWeightedCreatives> creativesByPriority;
	private List<String> countriesIsoCode;

	public long getAdspaceId() {
		return adspaceId;
	}

	public void setAdspaceId(long adspaceId) {
		this.adspaceId = adspaceId;
	}

	public List<String> getCountriesIsoCode() {
		return countriesIsoCode;
	}

	public void setCountriesIsoCode(List<String> countriesIsoCode) {
		this.countriesIsoCode = countriesIsoCode;
	}

	public List<PriorityWeightedCreatives> getCreativesByPriority() {
		return creativesByPriority;
	}

	public void setCreativesByPriority(
			List<PriorityWeightedCreatives> creativesByPriority) {
		this.creativesByPriority = creativesByPriority;
	}

}
