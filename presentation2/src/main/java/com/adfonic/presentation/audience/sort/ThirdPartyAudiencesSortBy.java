package com.adfonic.presentation.audience.sort;


public class ThirdPartyAudiencesSortBy {

	public enum Field {
		VENDOR_NAME, AUDIENCE_NAME, AUDIENCE_DATARETAIL, AUDIENCE_POPULATION
	}
	
	private Field field;
	private boolean ascending;
	
	public ThirdPartyAudiencesSortBy(Field field) {
		this(field, true);
	}
	
	public ThirdPartyAudiencesSortBy(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public Field getField() {
		return field;
	}

	public boolean isAscending() {
		return ascending;
	}
	
}
