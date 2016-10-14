package com.adfonic.adserver.simulation.model;

import java.util.Set;

public class SizebasedFormatModel {
	private int width;
	private int height;
	private Set<Long> formatIds;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Set<Long> getFormatIds() {
		return formatIds;
	}

	public void setFormatIds(Set<Long> formatIds) {
		this.formatIds = formatIds;
	}
}
