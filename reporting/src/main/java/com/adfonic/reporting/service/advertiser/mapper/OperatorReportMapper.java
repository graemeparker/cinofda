package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.reporting.service.advertiser.dto.CampaignReportDto;
import com.adfonic.reporting.service.advertiser.dto.OperatorReportDto;

public class OperatorReportMapper extends BaseReportRowMapper<OperatorReportDto> {

	@Override
	public OperatorReportDto mapRowImpl(ResultSet rs, int rowNum) throws SQLException {
		OperatorReportDto row = new OperatorReportDto();
		
		row.setCountry(getString("country"));
		row.setCountryIso(getString("country_isocode"));
		row.setOperator(getString("operator"));
		row.setDay(getDate("advertiser_day_unix_timestamp"));

		row = (OperatorReportDto) mapRowCommon(row, rs, rowNum);
		return row;
	}
}
