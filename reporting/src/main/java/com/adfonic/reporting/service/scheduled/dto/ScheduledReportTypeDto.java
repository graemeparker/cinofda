package com.adfonic.reporting.service.scheduled.dto;

public class ScheduledReportTypeDto {

	private int id;
	private String name;
	private String columnHeadings;
	private String columnNames;
	private int procedureId;
	private String transformColumns;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColumnHeadings() {
		return columnHeadings;
	}
	public void setColumnHeadings(String columnHeadings) {
		this.columnHeadings = columnHeadings;
	}
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
	public int getProcedureId() {
		return procedureId;
	}
	public void setProcedureId(int procedureId) {
		this.procedureId = procedureId;
	}
	public String getTransformColumns() {
		return transformColumns;
	}
	public void setTransformColumns(String transformColumns) {
		this.transformColumns = transformColumns;
	}
}
