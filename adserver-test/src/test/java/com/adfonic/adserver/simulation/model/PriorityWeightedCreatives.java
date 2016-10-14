package com.adfonic.adserver.simulation.model;

import java.util.List;

public class PriorityWeightedCreatives {
	private Integer priority;
	private List<Long> creatives;

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public List<Long> getCreatives() {
		return creatives;
	}

	public void setCreatives(List<Long> creatives) {
		this.creatives = creatives;
	}
}
