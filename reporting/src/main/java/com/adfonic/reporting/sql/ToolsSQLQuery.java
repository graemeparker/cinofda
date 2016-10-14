package com.adfonic.reporting.sql;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.adfonic.reporting.Report;
import com.adfonic.reporting.sql.dto.BudgetReportCampaignDailyDto;
import com.adfonic.reporting.sql.dto.BudgetReportCampaignOverallDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByCategoryDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByDayByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByDayByCategoryDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByDayDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByCategoryDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignDetailByHourDto;
import com.adfonic.reporting.sql.dto.CampaignDetailDto;
import com.adfonic.reporting.sql.dto.CampaignInvSourceDetailByDayDto;
import com.adfonic.reporting.sql.dto.CampaignInvSourceDetailDto;
import com.adfonic.reporting.sql.dto.CampaignTotalByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignTotalByCategoryDto;
import com.adfonic.reporting.sql.dto.CampaignTotalByHourDto;
import com.adfonic.reporting.sql.dto.CampaignTotalByInvSourceDto;
import com.adfonic.reporting.sql.dto.CampaignTotalDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByCategoryDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayByCategoryDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayByInvSourceDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByDayDto;
import com.adfonic.reporting.sql.dto.CreativeDetailByInvSourceDto;
import com.adfonic.reporting.sql.dto.CreativeDetailDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByBrandDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByBrandDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByBrandDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByBrandDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByPlatformDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryByPlatformDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByCountryDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByPlatformDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByPlatformDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByBrandDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByBrandDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByPlatformDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionByPlatformDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailByRegionDto;
import com.adfonic.reporting.sql.dto.DeviceDetailDayDto;
import com.adfonic.reporting.sql.dto.DeviceDetailDto;
import com.adfonic.reporting.sql.dto.LocationDetailByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.LocationDetailByCategoryDto;
import com.adfonic.reporting.sql.dto.LocationDetailByDayByCategoryByInvSourceDto;
import com.adfonic.reporting.sql.dto.LocationDetailByDayByCategoryDto;
import com.adfonic.reporting.sql.dto.LocationDetailByDayByInvSourceDto;
import com.adfonic.reporting.sql.dto.LocationDetailByInvSourceDto;
import com.adfonic.reporting.sql.dto.LocationDetailDayDto;
import com.adfonic.reporting.sql.dto.LocationDetailDto;
import com.adfonic.reporting.sql.dto.OperatorDetailDayDto;
import com.adfonic.reporting.sql.dto.OperatorDetailDto;
import com.adfonic.reporting.sql.mapper.CampaignDetailDtoMapper;
import com.adfonic.reporting.sql.mapper.CreativeReportDetailByDayDtoMapper;
import com.adfonic.reporting.sql.mapper.CreativeReportDetailDtoMapper;
import com.adfonic.reporting.sql.procedure.BudgetReportCampaignDailyProcedure;
import com.adfonic.reporting.sql.procedure.BudgetReportCampaignOverallProcedure;
import com.adfonic.reporting.sql.procedure.CampaignInvSourceReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignInvSourceReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByDayByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByDayByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByHourByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByHourByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByHourByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailByHourStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportTotalByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportTotalByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportTotalByHourStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportTotalByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CampaignReportTotalStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByDayByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByDayByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailyByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.CreativeReportDetailyByDayByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByBrandDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByBrandStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryByBrandDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryByBrandStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryByPlatformDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryByPlatformStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByCountryStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByPlatformDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByPlatformStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionByBrandDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionByBrandStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionByPlatformDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionByPlatformStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailByRegionStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.DevicesReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailByDayByCategoryStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailByDayByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailyByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.LocationReportDetailyByDayByCategoryByInvSourceStoredProcedure;
import com.adfonic.reporting.sql.procedure.OperatorReportDetailByDayStoredProcedure;
import com.adfonic.reporting.sql.procedure.OperatorReportDetailStoredProcedure;
import com.adfonic.util.CurrencyUtils;

@SuppressWarnings("unchecked")
public class ToolsSQLQuery extends BaseSQLQuery {

    public Report getLocationReportDetail(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailStoredProcedure proc = new LocationReportDetailStoredProcedure(getDataSource(), "proc_return_adv_loc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<LocationDetailDto> rowData = (List<LocationDetailDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetail(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }

    public List<LocationDetailDto> getLocationReportDetail(Long advertiserId, String campaignIds, Date from, Date to) {
        LocationReportDetailStoredProcedure proc = new LocationReportDetailStoredProcedure(getDataSource(), "proc_return_adv_loc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        return (List<LocationDetailDto>) data.get(RESULT_OBJECT);
    }
       
    /**
     * Locataion Report Detail By Day
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseGeotargeting
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getLocationReportDetailByDay(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailByDayStoredProcedure proc = new LocationReportDetailByDayStoredProcedure(getDataSource(), "proc_return_adv_loc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<LocationDetailDayDto> rowData = (List<LocationDetailDayDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByDay(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Location Report Detail By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getLocationInvSourceReportDetail(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailByInvSourceStoredProcedure proc = new LocationReportDetailByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_loc_invsrc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByInvSourceDto> rowData = (List<LocationDetailByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateLocationInvSourceReportDetail(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Location Report Detail By Day By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getLocationReportDetailByDayByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailByDayByInvSourceStoredProcedure proc = new LocationReportDetailByDayByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_loc_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByDayByInvSourceDto> rowData = (List<LocationDetailByDayByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByDayByInvSource(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }

    /**
     * Operator Report Detail
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getOperatorReportDetail(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking) {
        OperatorReportDetailStoredProcedure proc = new OperatorReportDetailStoredProcedure(getDataSource(), "proc_return_adv_loc_op_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<OperatorDetailDto> rowData = (List<OperatorDetailDto>) data.get(RESULT_OBJECT);
        return generateOperatorReportDetail(rowData, isUseConversionTracking);
    }

    /**
     * Operator Report Detail By Day
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getOperatorReportDetailByDay(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking) {
        OperatorReportDetailByDayStoredProcedure proc = new OperatorReportDetailByDayStoredProcedure(getDataSource(), "proc_return_adv_loc_op_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<OperatorDetailDayDto> rowData = (List<OperatorDetailDayDto>) data.get(RESULT_OBJECT);
        return generateOperatorReportDetailByDay(rowData, isUseConversionTracking);
    }
    
    /**
     * Campaign Report Total By Day - this report drives the chart on the Advertiser Dashboard
     * as well as the detail table for the Campaign Report page
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportTotalByDay(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportTotalStoredProcedure proc = new CampaignReportTotalStoredProcedure(getDataSource(), "proc_return_adv_cam_report_summary_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignTotalDto> rowData = (List<CampaignTotalDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportTotalByDay(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Campaign Report Total By Hour - AD-507 not to be confused with proc_return_adv_cam_report_detail_by_hour
     *  this summary report drives the chart on the Advertiser Dashboard
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportTotalByHour(Long advertiserId, String campaignIds, Date from, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportTotalByHourStoredProcedure proc = new CampaignReportTotalByHourStoredProcedure(getDataSource(), "proc_return_adv_cam_report_summary_by_hour");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from));
        List<CampaignTotalByHourDto> rowData = (List<CampaignTotalByHourDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportSummaryByHour(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByCategory(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByCategoryStoredProcedure proc = new CampaignReportDetailByCategoryStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailByCategoryDto> rowData = (List<CampaignDetailByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByDayByCategory(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByDayByCategoryStoredProcedure proc = new CampaignReportDetailByDayByCategoryStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailByDayByCategoryDto> rowData = (List<CampaignDetailByDayByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByDayByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByHourByCategory(Long advertiserId, String campaignIds, Date from, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByHourByCategoryStoredProcedure proc = new CampaignReportDetailByHourByCategoryStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_report_detail_by_hour");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from));
        List<CampaignDetailByHourByCategoryDto> rowData = (List<CampaignDetailByHourByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByHourByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportTotalByDayByCategory(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportTotalByCategoryStoredProcedure proc = new CampaignReportTotalByCategoryStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_report_summary_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignTotalByCategoryDto> rowData = (List<CampaignTotalByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportTotalByDayByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportTotalByDayByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportTotalByCategoryByInvSourceStoredProcedure proc = new CampaignReportTotalByCategoryByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_invsrc_report_summary_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignTotalByCategoryByInvSourceDto> rowData = (List<CampaignTotalByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportTotalByDayByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByCategoryByInvSourceStoredProcedure proc = new CampaignReportDetailByCategoryByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_invsrc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailByCategoryByInvSourceDto> rowData = (List<CampaignDetailByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByHourByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByHourByCategoryByInvSourceStoredProcedure proc = new CampaignReportDetailByHourByCategoryByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_invsrc_report_detail_by_hour");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from));
        List<CampaignDetailByHourByCategoryByInvSourceDto> rowData = (List<CampaignDetailByHourByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByHourByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCampaignReportDetailByDayByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByDayByCategoryByInvSourceStoredProcedure proc = new CampaignReportDetailByDayByCategoryByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_iab_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailByDayByCategoryByInvSourceDto> rowData = (List<CampaignDetailByDayByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByDayByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCreativeReportDetailByCategory(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailByCategoryStoredProcedure proc = new CreativeReportDetailByCategoryStoredProcedure(getDataSource(),"proc_return_adv_cre_iab_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByCategoryDto> rowData = (List<CreativeDetailByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCreativeReportDetailByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }

    public Report getCreativeReportDetailByDayByCategory(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailByDayByCategoryStoredProcedure proc = new CreativeReportDetailByDayByCategoryStoredProcedure(getDataSource(),"proc_return_adv_cre_iab_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByDayByCategoryDto> rowData = (List<CreativeDetailByDayByCategoryDto>) data.get(RESULT_OBJECT);
        return generateCreativeReportDetailByDayByCategory(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCreativeReportDetailByCategoryByInvSource(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailyByCategoryByInvSourceStoredProcedure proc = new CreativeReportDetailyByCategoryByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_cre_iab_invsrc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByCategoryByInvSourceDto> rowData = (List<CreativeDetailByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCreativeReportDetailByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getCreativeReportDetailByDayByCategoryByInvSource(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailyByDayByCategoryByInvSourceStoredProcedure proc = new CreativeReportDetailyByDayByCategoryByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_cre_iab_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByDayByCategoryByInvSourceDto> rowData = (List<CreativeDetailByDayByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCreativeReportDetailByDayByCategoryByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getLocationReportDetailByCategory(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailByCategoryStoredProcedure proc = new LocationReportDetailByCategoryStoredProcedure(getDataSource(),"proc_return_adv_loc_iab_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByCategoryDto> rowData = (List<LocationDetailByCategoryDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByCategory(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getLocationReportDetailByDayByCategory(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailByDayByCategoryStoredProcedure proc = new LocationReportDetailByDayByCategoryStoredProcedure(getDataSource(),"proc_return_adv_loc_iab_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByDayByCategoryDto> rowData = (List<LocationDetailByDayByCategoryDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByDayByCategory(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getLocationReportDetailByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailyByCategoryByInvSourceStoredProcedure proc = new LocationReportDetailyByCategoryByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_loc_iab_invsrc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByCategoryByInvSourceDto> rowData = (List<LocationDetailByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByCategoryByInvSource(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    public Report getLocationReportDetailByDayByCategoryByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        LocationReportDetailyByDayByCategoryByInvSourceStoredProcedure proc = new LocationReportDetailyByDayByCategoryByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_loc_iab_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, getDateAsString(from),getDateAsString(to));
        List<LocationDetailByDayByCategoryByInvSourceDto> rowData = (List<LocationDetailByDayByCategoryByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateLocationReportDetailByDayByCategoryByInvSource(rowData, isUseGeotargeting, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Campaign Report Total By Day By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportTotalByDayByInvSource(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportTotalByInvSourceStoredProcedure proc = new CampaignReportTotalByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_invsrc_report_summary_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignTotalByInvSourceDto> rowData = (List<CampaignTotalByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportTotalByDayByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Campaign Detail Report
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportDetail(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailStoredProcedure<CampaignDetailDto> proc = new CampaignReportDetailStoredProcedure<CampaignDetailDto>(getDataSource(), new CampaignDetailDtoMapper());
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailDto> rowData = (List<CampaignDetailDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetail(rowData, isUseConversionTracking, showVideoMetrics);
    }

    /**
     * Campaign Detail Report By Day
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportDetailByDay(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByDayStoredProcedure proc = new CampaignReportDetailByDayStoredProcedure(getDataSource(), "proc_return_adv_cam_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignDetailByDayDto> rowData = (List<CampaignDetailByDayDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByDay(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Campaign Detail Report By Hour
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportDetailByHour(Long advertiserId, String campaignIds, Date from, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByHourStoredProcedure proc = new CampaignReportDetailByHourStoredProcedure(getDataSource(), "proc_return_adv_cam_report_detail_by_hour");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from));
        List<CampaignDetailByHourDto> rowData = (List<CampaignDetailByHourDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByHour(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Campaign Detail Report By Hour By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignReportDetailByHourByInvSource(Long advertiserId, String campaignIds, Date from, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignReportDetailByHourByInvSourceStoredProcedure proc = new CampaignReportDetailByHourByInvSourceStoredProcedure(getDataSource(), "proc_return_adv_cam_invsrc_report_detail_by_hour");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from));
        List<CampaignDetailByHourByInvSourceDto> rowData = (List<CampaignDetailByHourByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCampaignReportDetailByHourByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }

    /**
     * Campaign Detail Report By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignInvSourceReportDetail(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignInvSourceReportDetailStoredProcedure proc = new CampaignInvSourceReportDetailStoredProcedure(getDataSource(), "proc_return_adv_cam_invsrc_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignInvSourceDetailDto> rowData = (List<CampaignInvSourceDetailDto>) data.get(RESULT_OBJECT);
        return generateCampaignInvSourceReportDetail(rowData, isUseConversionTracking, showVideoMetrics);
    }

    /**
     * Campaign Detail Report By Inventory Source By Day
     * @param advertiserId
     * @param campaignIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @param showVideoMetrics
     * @return
     */
    public Report getCampaignInvSourceReportDetailByDay(Long advertiserId, String campaignIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CampaignInvSourceReportDetailByDayStoredProcedure proc = new CampaignInvSourceReportDetailByDayStoredProcedure(getDataSource(), "proc_return_adv_cam_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds,getDateAsString(from),getDateAsString(to));
        List<CampaignInvSourceDetailByDayDto> rowData = (List<CampaignInvSourceDetailByDayDto>) data.get(RESULT_OBJECT);
        return generateCampaignInvSourceReportDetailByDay(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Creative Detail Report Detail
     * @param advertiserId
     * @param campaignIds
     * @param creativeIds
     * @param formatIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getCreativeReportDetail(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailStoredProcedure<CreativeDetailDto> proc = new CreativeReportDetailStoredProcedure<CreativeDetailDto>(getDataSource(), new CreativeReportDetailDtoMapper());
        List<CreativeDetailDto> rowData = proc.resultInList(advertiserId, campaignIds, creativeIds, formatIds, getDateAsString(from), getDateAsString(to));
        return generateCreativeReportDetail(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Creative Detail Report Detail By Day
     * @param advertiserId
     * @param campaignIds
     * @param creativeIds
     * @param formatIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getCreativeReportDetailByDay(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailByDayStoredProcedure<CreativeDetailByDayDto> proc = new CreativeReportDetailByDayStoredProcedure<>(getDataSource(), new CreativeReportDetailByDayDtoMapper());
        List<CreativeDetailByDayDto> rowData = proc.resultInList(advertiserId, campaignIds, creativeIds, formatIds, getDateAsString(from), getDateAsString(to));
        return generateCreativeReportDetailByDay(rowData, isUseConversionTracking, showVideoMetrics);
    }

    /**
     * Creative Report Detail By Inventory Source
     * @param advertiserId
     * @param campaignIds
     * @param creativeIds
     * @param formatIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getCreativeInvSourceReportDetail(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailByInvSourceStoredProcedure proc = new CreativeReportDetailByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_cre_invsrc_report_detail");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByInvSourceDto> rowData = (List<CreativeDetailByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCreativeInvSourceReportDetail(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
    /**
     * Creative Report Detail By Day By Inventory Source 
     * @param advertiserId
     * @param campaignIds
     * @param creativeIds
     * @param formatIds
     * @param from
     * @param to
     * @param isUseConversionTracking
     * @return
     */
    public Report getCreativeReportDetailByDayByInvSource(Long advertiserId, String campaignIds, String creativeIds, String formatIds, Date from, Date to, boolean isUseConversionTracking, boolean showVideoMetrics) {
        CreativeReportDetailByDayByInvSourceStoredProcedure proc = new CreativeReportDetailByDayByInvSourceStoredProcedure(getDataSource(),"proc_return_adv_cre_invsrc_report_detail_by_day");
        Map<String, Object> data = proc.execute(advertiserId,campaignIds, creativeIds, formatIds, getDateAsString(from),getDateAsString(to));
        List<CreativeDetailByDayByInvSourceDto> rowData = (List<CreativeDetailByDayByInvSourceDto>) data.get(RESULT_OBJECT);
        return generateCreativeReportDetailByDayByInvSource(rowData, isUseConversionTracking, showVideoMetrics);
    }
    
/*
* Device report by vendor by day
*/
    public Report getDeviceReportByBrandDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByBrandDayStoredProcedure proc = new DevicesReportDetailByBrandDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));        
        List<DeviceDetailByBrandDayDto> rowData = (List<DeviceDetailByBrandDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByBrandDay( rowData, useConversionTracking, showVideoMetrics);
    }
/*
 * Device report by vendor    
 */
    public Report getDeviceReportByBrand(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByBrandStoredProcedure proc = new DevicesReportDetailByBrandStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));        
        List<DeviceDetailByBrandDto> rowData = (List<DeviceDetailByBrandDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByBrand( rowData, useConversionTracking, showVideoMetrics);
    }
/*
 *  Device report by country by platform by vendor by day
 */
    public Report getDeviceReportByCountryByBrandDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryByBrandDayStoredProcedure proc = new DevicesReportDetailByCountryByBrandDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_loc_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryByBrandDayDto> rowData = (List<DeviceDetailByCountryByBrandDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountryByBrandDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by country by vendor
 */
    public Report getDeviceReportByCountryByBrand(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryByBrandStoredProcedure proc = new DevicesReportDetailByCountryByBrandStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_loc_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryByBrandDto> rowData = (List<DeviceDetailByCountryByBrandDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountryByBrand(rowData, useConversionTracking, showVideoMetrics);        
    }    
/*
 * Device report by country by platform by day    
 */
    public Report getDeviceReportByCountryByPlatformDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryByPlatformDayStoredProcedure proc = new DevicesReportDetailByCountryByPlatformDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_loc_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryByPlatformDayDto> rowData = (List<DeviceDetailByCountryByPlatformDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountryByPlatformDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by Country by platform
 */
    public Report getDeviceReportByCountryByPlatform(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryByPlatformStoredProcedure proc = new DevicesReportDetailByCountryByPlatformStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_loc_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryByPlatformDto> rowData = (List<DeviceDetailByCountryByPlatformDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountryByPlatform(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by Country, by day
 */
    public Report getDeviceReportByCountryDay(long advertiserId, String campaignIds,
            String vendors, String models, Date from, Date to, boolean useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryDayStoredProcedure proc = new DevicesReportDetailByCountryDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_loc_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryDayDto> rowData = (List<DeviceDetailByCountryDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountryDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by Country
 */
    public Report getDeviceReportByCountry(long advertiserId, String campaignIds,
            String vendors, String models, Date from, Date to, boolean useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByCountryStoredProcedure proc = new DevicesReportDetailByCountryStoredProcedure(getDataSource(),
                "proc_return_adv_dev_loc_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByCountryDto> rowData = (List<DeviceDetailByCountryDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByCountry(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
* Device report by Platform by day
*/
    public Report getDeviceReportByPlatformDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByPlatformDayStoredProcedure proc = new DevicesReportDetailByPlatformDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByPlatformDayDto> rowData = (List<DeviceDetailByPlatformDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByPlatformDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by Platform
 */
    public Report getDeviceReportByPlatform(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByPlatformStoredProcedure proc = new DevicesReportDetailByPlatformStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByPlatformDto> rowData = (List<DeviceDetailByPlatformDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByPlatform(rowData, useConversionTracking, showVideoMetrics);        
    }    
/*
 *  Device report by Region and Brand by Day
 */
    public Report getDeviceReportByRegionByBrandDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionByBrandDayStoredProcedure proc = new DevicesReportDetailByRegionByBrandDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_rgn_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionByBrandDayDto> rowData = (List<DeviceDetailByRegionByBrandDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegionByBrandDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by Region and Brand
 */
    public Report getDeviceReportByRegionByBrand(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionByBrandStoredProcedure proc = new DevicesReportDetailByRegionByBrandStoredProcedure(getDataSource(),
                "proc_return_adv_dev_vnd_rgn_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionByBrandDto> rowData = (List<DeviceDetailByRegionByBrandDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegionByBrand(rowData, useConversionTracking, showVideoMetrics);        
    }    
/*    
* Device Reports by region and platform daily breakdown
*/
    public Report getDeviceReportByRegionByPlatformDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionByPlatformDayStoredProcedure proc = new DevicesReportDetailByRegionByPlatformDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_rgn_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionByPlatformDayDto> rowData = (List<DeviceDetailByRegionByPlatformDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegionByPlatformDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device report by region and platform
 */
    public Report getDeviceReportByRegionByPlatform(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionByPlatformStoredProcedure proc = new DevicesReportDetailByRegionByPlatformStoredProcedure(getDataSource(),
                "proc_return_adv_dev_plt_rgn_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionByPlatformDto> rowData = (List<DeviceDetailByRegionByPlatformDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegionByPlatform(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device reports no grouping regional and daily breakdown
 */
    public Report getDeviceReportByRegionDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionDayStoredProcedure proc = new DevicesReportDetailByRegionDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_rgn_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionDayDto> rowData = (List<DeviceDetailByRegionDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegionDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device reports no grouping regional breakdown
 */
    public Report getDeviceReportByRegion(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailByRegionStoredProcedure proc = new DevicesReportDetailByRegionStoredProcedure(getDataSource(),
                "proc_return_adv_dev_rgn_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailByRegionDto> rowData = (List<DeviceDetailByRegionDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByRegion(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
* Device Reports no group no location daily breakdown
*/
    public Report getDeviceReportByDay(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailDayStoredProcedure proc = new DevicesReportDetailDayStoredProcedure(getDataSource(),
                "proc_return_adv_dev_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailDayDto> rowData = (List<DeviceDetailDayDto>) data.get(RESULT_OBJECT);
        return generateDeviceReportByDay(rowData, useConversionTracking, showVideoMetrics);        
    }
/*
 * Device reports no grouping no location
 */
    public Report getDeviceReport(long advertiserId, String campaignIds, String vendors, String models, Date from, Date to, boolean 
            useConversionTracking, boolean showVideoMetrics){
        DevicesReportDetailStoredProcedure proc = new DevicesReportDetailStoredProcedure(getDataSource(),
                "proc_return_adv_dev_report_detail");
        Map<String,Object> data = proc.execute(advertiserId,campaignIds,models,vendors,getDateAsString(from),getDateAsString(to));
        List<DeviceDetailDto> rowData = (List<DeviceDetailDto>) data.get(RESULT_OBJECT);
        return generateDeviceReport(rowData, useConversionTracking, showVideoMetrics);        
    }    

    public Report getBudgetReportCampaignDaily(long advertiserId, String campaignIds, Date from, Date to){
        BudgetReportCampaignDailyProcedure proc = new BudgetReportCampaignDailyProcedure(getDataSource(), "proc_return_adv_bdg_report_detail_by_day");
        Map<String,Object> data = proc.execute(advertiserId, campaignIds, getDateAsString(from), getDateAsString(to));
        List<BudgetReportCampaignDailyDto> rowData = (List<BudgetReportCampaignDailyDto>) data.get(RESULT_OBJECT);
        return generateBudgetReportCampaignDaily(rowData);
    }
    
    public Report getBudgetReportCampaignOverall(long advertiserId, String campaignIds, Date from, Date to){
        BudgetReportCampaignOverallProcedure proc = new BudgetReportCampaignOverallProcedure(getDataSource(), "proc_return_adv_bdg_report_detail");
        Map<String,Object> data = proc.execute(advertiserId, campaignIds, getDateAsString(from), getDateAsString(to));
        List<BudgetReportCampaignOverallDto> rowData = (List<BudgetReportCampaignOverallDto>) data.get(RESULT_OBJECT);
        return generateBudgetReportCampaignOverall(rowData);
    }


    
    private Report generateBudgetReportCampaignDaily(List<BudgetReportCampaignDailyDto> rowData){
        Report report = new Report("BudgetReportCampaignDaily");
        report.addColumn(CAMPAIGN_COLUMN, java.lang.String.class, null, false);
        report.addColumn(DATE_COLUMN,java.util.Date.class,null,false);
        report.addColumn(BUDGET_COLUMN,java.lang.Double.class,CurrencyUtils.CURRENCY_FORMAT_USD,true);
        report.addColumn(BUDGET_DEPLETED_COLUMN,java.lang.Double.class,NumberFormat.getPercentInstance(locale),true);
        report.addColumn(BUDGET_REMAINING_COLUMN,java.lang.Double.class,CurrencyUtils.CURRENCY_FORMAT_USD,true);
        for(BudgetReportCampaignDailyDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCampaign());
            row.add( dateFromTimeStamp(dto.getDate()));
            row.add( (Object) dto.getBudget());
            row.add( percentage( dto.getDepleted()));
            row.add( (Object) dto.getRemaining());
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateBudgetReportCampaignOverall(List<BudgetReportCampaignOverallDto> rowData){
        Report report = new Report("BudgetReportCampaignOverall");
        report.addColumn(CAMPAIGN_COLUMN, java.lang.String.class, null, false);
        report.addColumn(BUDGET_COLUMN,java.lang.Double.class,CurrencyUtils.CURRENCY_FORMAT_USD,true);
        report.addColumn(DATE_START_COLUMN,java.lang.String.class,null,false);
        report.addColumn(DATE_END_COLUMN,java.lang.String.class,null,false);
        report.addColumn(BUDGET_DEPLETED_COLUMN,java.lang.Double.class,NumberFormat.getPercentInstance(locale),true);
        report.addColumn(BUDGET_REMAINING_COLUMN,java.lang.Double.class,CurrencyUtils.CURRENCY_FORMAT_USD,true);
        for(BudgetReportCampaignOverallDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCampaign());
            row.add( (Object) dto.getBudget());
            row.add( dto.getStartDate() != null ? dateFromTimeStamp(dto.getStartDate()) : null);
            row.add( dto.getEndDate() != null ? dateFromTimeStamp(dto.getEndDate()) : null);
            row.add( percentage( dto.getDepleted()));
            row.add( (Object) dto.getRemaining());
            report.addRow(row.toArray());
        }
        return report;
    }    

    private Object percentage(double d){
        return new Double(d/100.0d);
    }

    private Report generateLocationReportDetail(List<LocationDetailDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetail");
        report = generateColumnsForReport(report);
        for (LocationDetailDto locationDetailDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailDto.getCountry());

            if(isUseGeotargeting) { 
                row.add( (Object) locationDetailDto.getLocation()); 
            }

            row.add( (Object) locationDetailDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }

    private Report generateLocationReportDetailByDay(List<LocationDetailDayDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByDay");
        report = generateColumnsForReport(report);
        for (LocationDetailDayDto locationDetailDayDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailDayDto.getCountry());

            if(isUseGeotargeting) {
                row.add( (Object) locationDetailDayDto.getLocation());
            }

            row.add( (Object) dateFromTimeStamp(locationDetailDayDto.getDayUnixTimestamp()));
            row.add( (Object) locationDetailDayDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailDayDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationReportDetailByCategory(List<LocationDetailByCategoryDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByCategory");
        report = generateColumnsForReport(report);
        for (LocationDetailByCategoryDto locationDetailByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByCategoryDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByCategoryDto.getLocation());
            }
            row.add( (Object) locationDetailByCategoryDto.getCategory());
            row.add( (Object) locationDetailByCategoryDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationReportDetailByDayByCategory(List<LocationDetailByDayByCategoryDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByDayByCategory");
        report = generateColumnsForReport(report);
        for (LocationDetailByDayByCategoryDto locationDetailByDayByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByDayByCategoryDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByDayByCategoryDto.getLocation());
            }
            row.add( (Object) locationDetailByDayByCategoryDto.getCategory());
            row.add( (Object) dateFromTimeStamp(locationDetailByDayByCategoryDto.getDayUnixTimestamp()));
            row.add( (Object) locationDetailByDayByCategoryDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByDayByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationInvSourceReportDetail(List<LocationDetailByInvSourceDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationInvSourceReportDetail");
        report = generateColumnsForReport(report);
        for (LocationDetailByInvSourceDto locationDetailByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByInvSourceDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByInvSourceDto.getLocation());
            }
            row.add( (Object) locationDetailByInvSourceDto.getInventorySource());
            row.add( (Object) locationDetailByInvSourceDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationReportDetailByDayByInvSource(List<LocationDetailByDayByInvSourceDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByDayByInvSource");
        report = generateColumnsForReport(report);
        for (LocationDetailByDayByInvSourceDto locationDetailByDayByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByDayByInvSourceDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByDayByInvSourceDto.getLocation());
            }
            row.add( (Object) locationDetailByDayByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(locationDetailByDayByInvSourceDto.getDayUnixTimestamp()));
            row.add( (Object) locationDetailByDayByInvSourceDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByDayByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationReportDetailByCategoryByInvSource(List<LocationDetailByCategoryByInvSourceDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByCategoryByInvSource");
        report = generateColumnsForReport(report);
        for (LocationDetailByCategoryByInvSourceDto locationDetailByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByCategoryByInvSourceDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByCategoryByInvSourceDto.getLocation());
            }
            row.add( (Object) locationDetailByCategoryByInvSourceDto.getCategory());
            row.add( (Object) locationDetailByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) locationDetailByCategoryByInvSourceDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateLocationReportDetailByDayByCategoryByInvSource(List<LocationDetailByDayByCategoryByInvSourceDto> rowData, boolean isUseGeotargeting, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("LocationReportDetailByDayByCategoryByInvSource");
        report = generateColumnsForReport(report);
        for (LocationDetailByDayByCategoryByInvSourceDto locationDetailByDayByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) locationDetailByDayByCategoryByInvSourceDto.getCountry());
            
            if(isUseGeotargeting) {
                row.add( (Object) locationDetailByDayByCategoryByInvSourceDto.getLocation());
            }
            row.add( (Object) locationDetailByDayByCategoryByInvSourceDto.getCategory());
            row.add( (Object) locationDetailByDayByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(locationDetailByDayByCategoryByInvSourceDto.getDayUnixTimestamp()));
            row.add( (Object) locationDetailByDayByCategoryByInvSourceDto.getPercentTotalImpressions());
            ReportUtil.addRowDetail(row, locationDetailByDayByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }

    private Report generateOperatorReportDetail(List<OperatorDetailDto> rowData, boolean isUseConversionTracking) {
        Report report = new Report("OperatorReportDetail");
        report = generateColumnsForReport(report);
        for (OperatorDetailDto operatorDetailDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) operatorDetailDto.getCountry());
            row.add( (Object) operatorDetailDto.getOperator());
            ReportUtil.addRowDetail(row, operatorDetailDto, isUseConversionTracking, false); // showVideoMetrics is false because we don't show video stats for connections report
            report.addRow(row.toArray());
        }
        return report;
    }

    private Report generateOperatorReportDetailByDay(List<OperatorDetailDayDto> rowData, boolean isUseConversionTracking) {
        Report report = new Report("OperatorReportDetailByDay");
        report = generateColumnsForReport(report);
        for (OperatorDetailDayDto operatorDetailDayDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) operatorDetailDayDto.getCountry());
            row.add( (Object) operatorDetailDayDto.getOperator());
            row.add( (Object) dateFromTimeStamp(operatorDetailDayDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, operatorDetailDayDto, isUseConversionTracking, false); // showVideoMetrics is false because we don't show video stats for connections report
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportSummaryByHour(List<CampaignTotalByHourDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportSummaryByHour");
        // AD-312 This appears to be a summary only. The proc doesn't return a campaign column so commenting it below.
        // Adding the column to the report without populating it will cause report.getColumn(String).getIndex to return incorrect values 
        // report.addColumn("Campaign");
        report = generateColumnsForReport(report);
        for (CampaignTotalByHourDto campaignTotalByHourDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignTotalByHourDto.getDayUnixTimestamp());
            row.add( (Object) campaignTotalByHourDto.getHour());
            ReportUtil.addRowDetail(row, campaignTotalByHourDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByHour(List<CampaignDetailByHourDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByHour");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByHourDto campaignDetailByHourDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByHourDto.getCampaign());
            row.add( (Object) dateFromTimeStamp(campaignDetailByHourDto.getDayUnixTimestamp()));
            row.add( (Object) campaignDetailByHourDto.getHour());
            ReportUtil.addRowDetail(row, campaignDetailByHourDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByHourByCategory(List<CampaignDetailByHourByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByHourByCategory");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByHourByCategoryDto campaignDetailByHourByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByHourByCategoryDto.getCampaign());
            row.add( (Object) campaignDetailByHourByCategoryDto.getCategory());
            row.add( (Object) dateFromTimeStamp(campaignDetailByHourByCategoryDto.getDayUnixTimestamp()));
            row.add( (Object) campaignDetailByHourByCategoryDto.getHour());
            ReportUtil.addRowDetail(row, campaignDetailByHourByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByHourByInvSource(List<CampaignDetailByHourByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByHourByInvSource");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByHourByInvSourceDto campaignDetailByHourByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByHourByInvSourceDto.getCampaign());
            row.add( (Object) campaignDetailByHourByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignDetailByHourByInvSourceDto.getDayUnixTimestamp()));
            row.add( (Object) campaignDetailByHourByInvSourceDto.getHour());
            ReportUtil.addRowDetail(row, campaignDetailByHourByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByHourByCategoryByInvSource(List<CampaignDetailByHourByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByHourByCategoryByInvSource");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByHourByCategoryByInvSourceDto campaignDetailByHourByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByHourByCategoryByInvSourceDto.getCampaign());
            row.add( (Object) campaignDetailByHourByCategoryByInvSourceDto.getCategory());
            row.add( (Object) campaignDetailByHourByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignDetailByHourByCategoryByInvSourceDto.getDayUnixTimestamp()));
            row.add( (Object) campaignDetailByHourByCategoryByInvSourceDto.getHour());
            ReportUtil.addRowDetail(row, campaignDetailByHourByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportTotalByDay(List<CampaignTotalDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportTotalByDay");
        report = generateColumnsForReport(report);
        for (CampaignTotalDto campaignTotalDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dateFromTimeStamp(campaignTotalDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignTotalDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportTotalByDayByCategory(List<CampaignTotalByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportTotalByDayByCategory");
        report = generateColumnsForReport(report);
        for (CampaignTotalByCategoryDto campaignTotalByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignTotalByCategoryDto.getCategory());
            row.add( (Object) dateFromTimeStamp(campaignTotalByCategoryDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignTotalByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportTotalByDayByInvSource(List<CampaignTotalByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportTotalByDayByInvSource");
        report = generateColumnsForReport(report);
        for (CampaignTotalByInvSourceDto campaignTotalByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignTotalByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignTotalByInvSourceDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignTotalByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;    
    }

    private Report generateCampaignReportTotalByDayByCategoryByInvSource(List<CampaignTotalByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportTotalByDayByCategoryByInvSource");
        report = generateColumnsForReport(report);
        for (CampaignTotalByCategoryByInvSourceDto campaignTotalByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignTotalByCategoryByInvSourceDto.getCategory());
            row.add( (Object) campaignTotalByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignTotalByCategoryByInvSourceDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignTotalByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;    
    }
    
    private Report generateCampaignReportDetail(List<CampaignDetailDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetail");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailDto campaignDetailDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailDto.getCampaign());
            ReportUtil.addRowDetail(row, campaignDetailDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByCategory(List<CampaignDetailByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByCategory");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByCategoryDto campaignDetailByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByCategoryDto.getCampaign());
            row.add( (Object) campaignDetailByCategoryDto.getCategory());
            ReportUtil.addRowDetail(row, campaignDetailByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByDay(List<CampaignDetailByDayDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByDay");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByDayDto campaignDetailByDayDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByDayDto.getCampaign());
//            row.add( (Object) dateFromTimeStamp(campaignDetailByDayDto.getDay()));
            row.add( (Object) dateFromTimeStamp(campaignDetailByDayDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignDetailByDayDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByDayByCategory(List<CampaignDetailByDayByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByDayByCategory");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByDayByCategoryDto campaignDetailByDayByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByDayByCategoryDto.getCampaign());
            row.add( (Object) campaignDetailByDayByCategoryDto.getCategory());
            row.add( (Object) dateFromTimeStamp(campaignDetailByDayByCategoryDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignDetailByDayByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignInvSourceReportDetail(List<CampaignInvSourceDetailDto> rowData,    boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignInvSourceReportDetail");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignInvSourceDetailDto campaignInvSourceDetailDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignInvSourceDetailDto.getCampaign());
            row.add( (Object) campaignInvSourceDetailDto.getInventorySource());
            ReportUtil.addRowDetail(row, campaignInvSourceDetailDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignInvSourceReportDetailByDay(List<CampaignInvSourceDetailByDayDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignInvSourceReportDetailByDay");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignInvSourceDetailByDayDto campaignInvSourceDetailByDayDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignInvSourceDetailByDayDto.getCampaign());
            row.add( (Object) campaignInvSourceDetailByDayDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignInvSourceDetailByDayDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignInvSourceDetailByDayDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }

    private Report generateCampaignReportDetailByCategoryByInvSource(List<CampaignDetailByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByCategoryByInvSource");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByCategoryByInvSourceDto campaignDetailByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByCategoryByInvSourceDto.getCampaign());
            row.add( (Object) campaignDetailByCategoryByInvSourceDto.getCategory());
            row.add( (Object) campaignDetailByCategoryByInvSourceDto.getInventorySource());
            ReportUtil.addRowDetail(row, campaignDetailByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCampaignReportDetailByDayByCategoryByInvSource(List<CampaignDetailByDayByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CampaignReportDetailByDayByCategoryByInvSource");
        report.addColumn(CAMPAIGN_COLUMN);
        report = generateColumnsForReport(report);
        for (CampaignDetailByDayByCategoryByInvSourceDto campaignDetailByDayByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) campaignDetailByDayByCategoryByInvSourceDto.getCampaign());
            row.add( (Object) campaignDetailByDayByCategoryByInvSourceDto.getCategory());
            row.add( (Object) campaignDetailByDayByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(campaignDetailByDayByCategoryByInvSourceDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, campaignDetailByDayByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }

    
    private Report generateCreativeReportDetail(List<CreativeDetailDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetail");
        report = generateColumnsForReport(report);
        for (CreativeDetailDto creativeDetailDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailDto.getCampaign());
            row.add( (Object) creativeDetailDto.getCreative());
            row.add( (Object) creativeDetailDto.getFormat());
            ReportUtil.addRowDetail(row, creativeDetailDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeReportDetailByDay(List<CreativeDetailByDayDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByDay");
        report = generateColumnsForReport(report);
        for (CreativeDetailByDayDto creativeDetailByDayDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByDayDto.getCampaign());
            row.add( (Object) creativeDetailByDayDto.getCreative());
            row.add( (Object) creativeDetailByDayDto.getFormat());
            row.add( (Object) dateFromTimeStamp(creativeDetailByDayDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, creativeDetailByDayDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }

    private Report generateCreativeReportDetailByCategory(List<CreativeDetailByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByCategory");
        report = generateColumnsForReport(report);
        for (CreativeDetailByCategoryDto creativeDetailByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByCategoryDto.getCampaign());
            row.add( (Object) creativeDetailByCategoryDto.getCreative());
            row.add( (Object) creativeDetailByCategoryDto.getFormat());
            row.add( (Object) creativeDetailByCategoryDto.getCategory());
            ReportUtil.addRowDetail(row, creativeDetailByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeReportDetailByDayByCategory(List<CreativeDetailByDayByCategoryDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByDayByCategory");
        report = generateColumnsForReport(report);
        for (CreativeDetailByDayByCategoryDto creativeDetailByDayByCategoryDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByDayByCategoryDto.getCampaign());
            row.add( (Object) creativeDetailByDayByCategoryDto.getCreative());
            row.add( (Object) creativeDetailByDayByCategoryDto.getFormat());
            row.add( (Object) creativeDetailByDayByCategoryDto.getCategory());
            row.add( (Object) dateFromTimeStamp(creativeDetailByDayByCategoryDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, creativeDetailByDayByCategoryDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeInvSourceReportDetail(List<CreativeDetailByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeInvSourceReportDetail");
        report = generateColumnsForReport(report);
        for (CreativeDetailByInvSourceDto creativeDetailByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByInvSourceDto.getCampaign());
            row.add( (Object) creativeDetailByInvSourceDto.getCreative());
            row.add( (Object) creativeDetailByInvSourceDto.getFormat());
            row.add( (Object) creativeDetailByInvSourceDto.getInventorySource());
            ReportUtil.addRowDetail(row, creativeDetailByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeReportDetailByDayByInvSource(List<CreativeDetailByDayByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByDayByInvSource");
        report = generateColumnsForReport(report);
        for (CreativeDetailByDayByInvSourceDto creativeDetailByDayByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByDayByInvSourceDto.getCampaign());
            row.add( (Object) creativeDetailByDayByInvSourceDto.getCreative());
            row.add( (Object) creativeDetailByDayByInvSourceDto.getFormat());
            row.add( (Object) creativeDetailByDayByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(creativeDetailByDayByInvSourceDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, creativeDetailByDayByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeReportDetailByCategoryByInvSource(List<CreativeDetailByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByCategoryByInvSource");
        report = generateColumnsForReport(report);
        for (CreativeDetailByCategoryByInvSourceDto creativeDetailByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByCategoryByInvSourceDto.getCampaign());
            row.add( (Object) creativeDetailByCategoryByInvSourceDto.getCreative());
            row.add( (Object) creativeDetailByCategoryByInvSourceDto.getFormat());
            row.add( (Object) creativeDetailByCategoryByInvSourceDto.getCategory());
            row.add( (Object) creativeDetailByCategoryByInvSourceDto.getInventorySource());
            ReportUtil.addRowDetail(row, creativeDetailByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    private Report generateCreativeReportDetailByDayByCategoryByInvSource(List<CreativeDetailByDayByCategoryByInvSourceDto> rowData, boolean isUseConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("CreativeReportDetailByDayByCategoryByInvSource");
        report = generateColumnsForReport(report);
        for (CreativeDetailByDayByCategoryByInvSourceDto creativeDetailByDayByCategoryByInvSourceDto : rowData) {
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) creativeDetailByDayByCategoryByInvSourceDto.getCampaign());
            row.add( (Object) creativeDetailByDayByCategoryByInvSourceDto.getCreative());
            row.add( (Object) creativeDetailByDayByCategoryByInvSourceDto.getFormat());
            row.add( (Object) creativeDetailByDayByCategoryByInvSourceDto.getCategory());
            row.add( (Object) creativeDetailByDayByCategoryByInvSourceDto.getInventorySource());
            row.add( (Object) dateFromTimeStamp(creativeDetailByDayByCategoryByInvSourceDto.getDayUnixTimestamp()));
            ReportUtil.addRowDetail(row, creativeDetailByDayByCategoryByInvSourceDto, isUseConversionTracking, showVideoMetrics);
            report.addRow(row.toArray());
        }
        return report;
    }
    
    
    private Report generateDeviceReportByBrandDay( List<DeviceDetailByBrandDayDto> rowData, boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByBrandDay");
        generateColumnsForReport(report);
        for( DeviceDetailByBrandDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getBrand());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByBrand( List<DeviceDetailByBrandDto> rowData, boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByBrand");
        generateColumnsForReport(report);
        for( DeviceDetailByBrandDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getBrand());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }


    private Report generateDeviceReportByCountryByBrandDay( List<DeviceDetailByCountryByBrandDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountryByBrandDay");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryByBrandDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getBrand());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByCountryByBrand( List<DeviceDetailByCountryByBrandDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountryByBrand");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryByBrandDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getBrand());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByCountryByPlatformDay( List<DeviceDetailByCountryByPlatformDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountryByPlatformDay");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryByPlatformDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getPlatform());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByCountryByPlatform( List<DeviceDetailByCountryByPlatformDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountryByPlatform");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryByPlatformDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getPlatform());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByCountryDay( List<DeviceDetailByCountryDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountryDay");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getDevice());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByCountry( List<DeviceDetailByCountryDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByCountry");
        generateColumnsForReport(report);
        for( DeviceDetailByCountryDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getCountry());
            row.add( (Object) dto.getDevice());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByPlatformDay( List<DeviceDetailByPlatformDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByPlatformDay");
        generateColumnsForReport(report);
        for( DeviceDetailByPlatformDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getPlatform());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByPlatform( List<DeviceDetailByPlatformDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByPlatform");
        generateColumnsForReport(report);
        for( DeviceDetailByPlatformDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getPlatform());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByRegionByBrandDay( List<DeviceDetailByRegionByBrandDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegionByBrandDay");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionByBrandDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getBrand());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByRegionByBrand( List<DeviceDetailByRegionByBrandDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegionByBrand");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionByBrandDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getBrand());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }            
    
    private Report generateDeviceReportByRegionByPlatformDay( List<DeviceDetailByRegionByPlatformDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegionByPlatformDay");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionByPlatformDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getPlatform());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByRegionByPlatform( List<DeviceDetailByRegionByPlatformDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegionByPlatform");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionByPlatformDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getPlatform());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }    

    private Report generateDeviceReportByRegionDay( List<DeviceDetailByRegionDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegionDay");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getDevice());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReportByRegion( List<DeviceDetailByRegionDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByRegion");
        generateColumnsForReport(report);
        for( DeviceDetailByRegionDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getRegion());
            row.add( (Object) dto.getDevice());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }
    
    private Report generateDeviceReportByDay( List<DeviceDetailDayDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReportByDay");
        generateColumnsForReport(report);
        for( DeviceDetailDayDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getDevice());
            row.add( (Object) dateFromTimeStamp(dto.getDay()));
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

    private Report generateDeviceReport( List<DeviceDetailDto> rowData,
            boolean useConversionTracking, boolean showVideoMetrics) {
        Report report = new Report("DeviceReport");
        generateColumnsForReport(report);
        for( DeviceDetailDto dto : rowData){
            List<Object> row = new ArrayList<Object>();
            row.add( (Object) dto.getDevice());
            ReportUtil.addRowDetail(row, dto, useConversionTracking, showVideoMetrics);
            report.addRow( row.toArray());
        }
        return report;
    }

}
