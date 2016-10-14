package com.adfonic.domain.cache.dto.adserver;

import java.io.Serializable;

public class CampaignCvrInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private double targetCvr;
	private double currentCvr;
	
	
	public CampaignCvrInfo(double targetCvr, double currentCvr) {
		this.targetCvr = targetCvr;
		this.currentCvr = currentCvr;
	}
	
	public CampaignCvrInfo() {
	}

	public double getTargetCvr() {
		return targetCvr;
	}
	public void setTargetCvr(double targetCvr) {
		this.targetCvr = targetCvr;
	}
	public double getCurrentCvr() {
		return currentCvr;
	}
	public void setCurrentCvr(double currentCvr) {
		this.currentCvr = currentCvr;
	}
	
	
}
