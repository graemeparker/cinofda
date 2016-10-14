package com.adfonic.reporting.service.parameter;

public enum Procedure {
	
	SUMMARY("summary"),
	ADVERTISER_CAMPAIGN("proc_return_adv_cam"),
	ADVERTISER_BUDGET("proc_return_adv_bud"),
	ADVERTISER_DEVICE("proc_return_adv_dev"),
	ADVERTISER_CREATIVE("proc_return_adv_cre"),
	ADVERTISER_OPERATOR("proc_return_adv_loc_op"),
	ADVERTISER_LOCATION("proc_return_adv_loc");
	
	private String name;
	
	Procedure(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
