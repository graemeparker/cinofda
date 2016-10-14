package com.adfonic.adserver.simulation.model;

public class CounterOutputModel {
	private String name;

	private Long count;
	private Long below;
	private Long above;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getBelow() {
		return below;
	}

	public void setBelow(Long below) {
		this.below = below;
	}

	public Long getAbove() {
		return above;
	}

	public void setAbove(Long above) {
		this.above = above;
	}
}
