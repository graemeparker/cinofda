package com.adfonic.reporting.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.reporting.service.advertiser.dto.BaseReportDto;
import com.adfonic.reporting.service.advertiser.procedure.CampaignReportByHourlyProcedure;
import com.adfonic.reporting.service.advertiser.procedure.CampaignReportProcedure;
import com.adfonic.reporting.service.advertiser.procedure.CreativeReportProcedure;
import com.adfonic.reporting.service.advertiser.procedure.DeviceReportProcedure;
import com.adfonic.reporting.service.advertiser.procedure.InfiniteProc;
import com.adfonic.reporting.service.advertiser.procedure.LocationReportProcedure;
import com.adfonic.reporting.service.advertiser.procedure.OperatorReportProcedure;
import com.adfonic.reporting.sql.BaseSQLQuery;

public class ToolsQueryManager extends BaseSQLQuery {

	protected Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Campaign Report
	 * @param advertiserId
	 * @param campaignIds
	 * @param from
	 * @param to
	 * @param isUseConversionTracking
	 * @param showVideoMetrics
	 * @return
	 */
	public List<BaseReportDto> getCampaignReportDetail(String procedureName,Long advertiserId, String campaignIds, int from, int to) {
		CampaignReportProcedure proc = new CampaignReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds,from,to);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Campaign Report by hourly
	 * @param procedureName
	 * @param advertiserId
	 * @param campaignIds
	 * @param from
	 * @return
	 */
	public List<BaseReportDto> getCampaignReportDetailByHour(String procedureName,Long advertiserId, String campaignIds, int from) {
		CampaignReportByHourlyProcedure proc = new CampaignReportByHourlyProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds,from);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Creative Report
	 * @param procedureName
	 * @param advertiserId
	 * @param campaignIds
	 * @param creativeIds
	 * @param formatIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<BaseReportDto> getCreativeReportDetail(String procedureName,Long advertiserId, String campaignIds, String creativeIds, String formatIds, int from, int to) {
		CreativeReportProcedure proc = new CreativeReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds,creativeIds,formatIds,from,to);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Location Report
	 * @param procedureName
	 * @param advertiserId
	 * @param campaignIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<BaseReportDto> getLocationReportDetail(String procedureName,Long advertiserId, String campaignIds, int from, int to) {
		LocationReportProcedure proc = new LocationReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds,from,to);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Operator Report
	 * @param procedureName
	 * @param advertiserId
	 * @param campaignIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<BaseReportDto> getOperatorReportDetail(String procedureName,Long advertiserId, String campaignIds, int from, int to) {
		OperatorReportProcedure proc = new OperatorReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds,from,to);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Device Report
	 * @param procedureName
	 * @param advertiserId
	 * @param campaignIds
	 * @param modelIds
	 * @param vendorIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<BaseReportDto> getDeviceReportDetail(String procedureName, Long advertiserId, String campaignIds, String modelIds, String vendorIds, int from, int to) {
		DeviceReportProcedure proc = new DeviceReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(advertiserId,campaignIds, modelIds, vendorIds,from,to);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Publisher Report
	 * @param procedureName
	 * @param publisherId
	 * @param publicationIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<BaseReportDto> getPublisherReportDetail(String procedureName,Long publisherId, String publicationIds, int from, int to) {
		OperatorReportProcedure proc = new OperatorReportProcedure(getDataSource(), procedureName);
		Map<String, Object> data = proc.execute(publisherId,publicationIds);
		List<BaseReportDto> rowData = (List<BaseReportDto>) data.get("result");
		return rowData;
	}
	
	/**
	 * Testing purposes only
	 * TODO: delete this method
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Deprecated
	public void infinite(UUID uuid) throws SQLIntegrityConstraintViolationException {
		InfiniteProc proc = new InfiniteProc(getDataSource(),"infinite_proc");
		proc.execute(uuid, getDataSource());
	}

	
	/**
	 * Cancel a request for the given requestId
	 * @param requestId
	 */
	public boolean cancel(UUID requestId) {
		try {
			String connectionSelectStatement = "SELECT QUERY_ID FROM REPORTING_CONNECTION WHERE REQUEST_ID = ?";
			String connectionKillStatement = "KILL ? \n";
			
			PreparedStatement pstConnectionSelect = getConnection().prepareStatement(connectionSelectStatement);
			pstConnectionSelect.setString(1, requestId.toString());
			ResultSet rs = pstConnectionSelect.executeQuery();
			if(rs.next()) {
				//the request was found. Cancel the query.
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Cancelling request: " + requestId);
				}
				PreparedStatement pstConnectionKill = getConnection().prepareStatement(connectionKillStatement);
				pstConnectionKill.setInt(1, rs.getInt("QUERY_ID"));
				if(!pstConnectionKill.execute() && pstConnectionKill.getUpdateCount() == 0) {
					// connection killed successfully
					return true;
				}
			} else {
				//the request with specified UUID was not found
				logger.info("Request not found for : " + requestId);
			}
		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}
		return false;
	}
}
