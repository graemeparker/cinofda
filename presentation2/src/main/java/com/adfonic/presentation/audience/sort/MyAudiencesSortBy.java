package com.adfonic.presentation.audience.sort;

public class MyAudiencesSortBy {
	
	public enum Field {
		AUDIENCE_STATUS, AUDIENCE_NAME, AUDIENCE_TYPE, AUDIENCE_POPULATION
	}

	private Field field;
	private boolean ascending;
	
	public MyAudiencesSortBy(Field field) {
		this(field, true);
	}
	
	public MyAudiencesSortBy(Field field, boolean ascending) {
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
