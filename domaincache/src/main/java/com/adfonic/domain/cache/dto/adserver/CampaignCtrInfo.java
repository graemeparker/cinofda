package com.adfonic.domain.cache.dto.adserver;

import java.io.Serializable;

public class CampaignCtrInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private double targetCtr;
	private double currentCtr;
	
	public CampaignCtrInfo(double targetCtr, double currentCtr) {
		this.targetCtr = targetCtr;
		this.currentCtr = currentCtr;
	}
	
	public CampaignCtrInfo() {
	}

	public double getTargetCtr() {
		return targetCtr;
	}
	public void setTargetCtr(double targetCtr) {
		this.targetCtr = targetCtr;
	}
	public double getCurrentCtr() {
		return currentCtr;
	}
	public void setCurrentCtr(double currentCtr) {
		this.currentCtr = currentCtr;
	}
}
