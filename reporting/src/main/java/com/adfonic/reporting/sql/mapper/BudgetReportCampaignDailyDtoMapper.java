package com.adfonic.reporting.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.adfonic.reporting.sql.dto.BudgetReportCampaignDailyDto;

import org.springframework.jdbc.core.RowMapper;

public class BudgetReportCampaignDailyDtoMapper implements RowMapper<BudgetReportCampaignDailyDto>{

	public BudgetReportCampaignDailyDto mapRow(ResultSet rs, int rownum) throws SQLException {
		BudgetReportCampaignDailyDto row = new BudgetReportCampaignDailyDto();
		row.setCampaign(rs.getString("campaign"));
		row.setDate(rs.getString("budget_date_timestamp"));
		row.setBudget(rs.getDouble("budget"));
		row.setDepleted(rs.getDouble("depleted"));
		row.setRemaining(rs.getDouble("remaining"));
		return row;
	}
	
}
