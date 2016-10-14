package com.adfonic.reporting.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.adfonic.reporting.service.scheduled.dto.ScheduledReportDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportFrequencyDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportStatusDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportTimePeriodDto;
import com.adfonic.reporting.service.scheduled.dto.ScheduledReportTypeDto;
import com.adfonic.reporting.service.scheduled.mapper.ScheduledReportFrequencyMapper;
import com.adfonic.reporting.service.scheduled.mapper.ScheduledReportMapper;
import com.adfonic.reporting.service.scheduled.mapper.ScheduledReportStatusMapper;
import com.adfonic.reporting.service.scheduled.mapper.ScheduledReportTimePeriodMapper;
import com.adfonic.reporting.service.scheduled.mapper.ScheduledReportTypeMapper;
import com.adfonic.reporting.service.scheduled.procedure.ScheduledReportProcedure;

public class ScheduledReportManager extends JdbcDaoSupport {

	protected Logger logger = Logger.getLogger(getClass().getName());
	 
	public String newReport(ScheduledReportDto report) {
		//CALL proc_add_scheduled_report_adv(19,1,1,982,"22160",null,null,null,null,null,null,"anuj.saboo@adfonic.com","",0,@rowout);
		
		ScheduledReportProcedure  proc = new ScheduledReportProcedure(getDataSource());
		Map<String, Object> data = proc.execute(report.getReportType(),
												report.getFrequency(),
												report.getTimePeriod(),
												report.getAdvertiser(),
												report.getCampaigns(),
												report.getCreatives(),
												report.getFormats(),
												report.getModels(),
												report.getVendors(),
												report.getStartDay(),
												report.getEndDay(),
												report.getEmails(),
												report.getDateFormat(),
												report.getRunHour(),Collections.EMPTY_MAP);
		return data.get("out_result").toString();
	}
	
	public int deleteReport(String reportHashId) {
		//we are setting the status to DELETED (id = 7). This means a soft delete so that the reports aren't visible 
		String sql = "UPDATE scheduled_report_adv SET report_status_id = 7 WHERE scheduled_report_hash_id = ?";
		return getJdbcTemplate().update(sql, reportHashId);
	}
	
	public List<ScheduledReportDto> selectReport(String reportHashId) {
		String sql = "SELECT * FROM scheduled_report_adv WHERE scheduled_report_hash_id = ?";
		List<ScheduledReportDto> result = getJdbcTemplate().query(sql,new Object[]{reportHashId}, new ScheduledReportMapper());
		return result;
	}
	
	public List<ScheduledReportDto> selectReports(long advertiserId) {
		String sql = "SELECT * FROM scheduled_report_adv WHERE adv_id = ?";
		List<ScheduledReportDto> result = getJdbcTemplate().query(sql,new Object[]{advertiserId}, new ScheduledReportMapper());
		return result;
	}
	
	public int updateReportStatus(String reportHashId, long status) {
		String sql = "UPDATE scheduled_report_adv SET report_status_id = ? WHERE scheduled_report_hash_id = ?";
		return getJdbcTemplate().update(sql, status, reportHashId);
	}

	public int deleteReports(List<String> reportHashIdList) {
		//we are setting the status to DELETED (id = 7). This means a soft delete so that the reports aren't visible 
		String sql = "UPDATE scheduled_report_adv SET report_status_id = 7 WHERE scheduled_report_hash_id in (:reportHashIds)";
		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(getDataSource());
		return template.update(sql, Collections.singletonMap("reportHashIds", reportHashIdList));
	}

	public List<ScheduledReportDto> selectReports(List<String> reportHashIdList) {
		String sql = "SELECT * FROM scheduled_report_adv WHERE scheduled_report_hash_id in (:reportHashIds)";
		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(getDataSource());
		List<ScheduledReportDto> result = template.query(sql, Collections.singletonMap("reportHashIds", reportHashIdList), new ScheduledReportMapper());
		return result;
	}

	public List<ScheduledReportStatusDto> getReportStatuses() {
		String sql = "SELECT * FROM scheduled_report_status";
		List<ScheduledReportStatusDto> result = getJdbcTemplate().query(sql, new ScheduledReportStatusMapper());
		return result;
	}
	
	public List<ScheduledReportFrequencyDto> getReportFrequencies() {
		String sql = "SELECT * FROM scheduled_report_frequency";
		List<ScheduledReportFrequencyDto> result = getJdbcTemplate().query(sql, new ScheduledReportFrequencyMapper());
		return result;
	}
	
	public List<ScheduledReportTypeDto> getReportTypes() {
		String sql = "SELECT * FROM scheduled_report_type";
		List<ScheduledReportTypeDto> result = getJdbcTemplate().query(sql, new ScheduledReportTypeMapper());
		return result;
	}
	
	public List<ScheduledReportTimePeriodDto> getReportTimeperiods() {
		String sql = "SELECT * FROM scheduled_report_time_period";
		List<ScheduledReportTimePeriodDto> result = getJdbcTemplate().query(sql, new ScheduledReportTimePeriodMapper());
		return result;
	}
}
