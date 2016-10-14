package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.adfonic.reporting.sql.dto.BudgetReportCampaignOverallDto;

import org.springframework.jdbc.core.RowMapper;

public class BudgetReportCampaignOverallDtoMapper implements RowMapper<BudgetReportCampaignOverallDto>{

	public BudgetReportCampaignOverallDto mapRow(ResultSet rs, int rownum) throws SQLException {
		BudgetReportCampaignOverallDto row = new BudgetReportCampaignOverallDto();
		row.setCampaign(rs.getString("campaign"));
		row.setBudget(rs.getDouble("budget"));
		row.setStartDate(rs.getString("start_date_timestamp"));
		row.setEndDate(rs.getString("end_date_timestamp"));
		row.setDepleted(rs.getDouble("depleted"));
		row.setRemaining(rs.getDouble("remaining"));
		return row;
	}
	
}