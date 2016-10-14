package com.adfonic.reporting.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.sql.dto.BaseReportDto;

public class ReportUtil {
    
	public static void addRowDetail(List<Object> row, BaseReportDto reportDto, boolean isUseConversionTracking, boolean showVideoMetrics) {
		row.add( (Object) reportDto.getImpressions());			//11
		row.add( (Object) reportDto.getClicks());				//12
		row.add( (Object) reportDto.getCtr());						//13

		if (showVideoMetrics) {
			row.add( (Object) reportDto.getTotalViews());			//14
			row.add( (Object) reportDto.getCompletedViews());		//15
			row.add( (Object) reportDto.getAverageDuration());		//16
			row.add( (Object) reportDto.getCostPerView());			//17
			row.add( (Object) reportDto.getQ1percent());			//18
			row.add( (Object) reportDto.getQ2percent());			//19
			row.add( (Object) reportDto.getQ3percent());			//20
			row.add( (Object) reportDto.getQ4percent());			//21
			row.add( (Object) reportDto.getEngagementScore());		//22
		} else {
			row.add( (Object) reportDto.getEcpm());					//14
			row.add( (Object) reportDto.getEcpc());					//15
		}
		row.add( (Object) reportDto.getCost());				//16 OR //23
		
		if(isUseConversionTracking) {
			row.add( (Object) reportDto.getConversions());	//17 OR //24
			row.add( (Object) reportDto.getClickConversion());		//18 OR //25
			row.add( (Object) reportDto.getCostPerConversion());	//19 OR //26
		}
	}
	
	public static void addReportMetrics(BaseSQLQuery sqlQuery, boolean showVideoMetrics, boolean isUseConversionTracking)	{
		sqlQuery.addMetrics(Metric.IMPRESSIONS, Metric.CLICKS, Metric.CTR);

		if (showVideoMetrics) {
			sqlQuery.addMetrics( Metric.TOTAL_VIEWS, Metric.COMPLETED_VIEWS, Metric.AVERAGE_DURATION, Metric.COST_PER_VIEW, Metric.Q1_PERCENT,
					Metric.Q2_PERCENT, Metric.Q3_PERCENT, Metric.Q4_PERCENT, Metric.ENGAGEMENT_SCORE);
		} else {
			sqlQuery.addMetrics(Metric.ECPM_AD, Metric.ECPC_AD);
		}

		sqlQuery.addMetrics(Metric.COST);

		if (isUseConversionTracking) {
			sqlQuery.addMetrics(Metric.CONVERSIONS, Metric.CONVERSION_PERCENT, Metric.COST_PER_CONVERSION);
		}
	}
	
	public static void rowMapper(BaseReportDto row, ResultSet rs) throws SQLException {
		row.setImpressions(rs.getLong("impressions"));
		row.setClicks(rs.getLong("clicks"));
		row.setCtr(rs.getDouble("ctr"));
		row.setEcpm(rs.getDouble("ecpm"));
		row.setEcpc(rs.getDouble("ecpc"));
		row.setConversions(rs.getLong("conversions"));
		row.setClickConversion(rs.getDouble("click_conversion"));
		row.setCostPerConversion(rs.getDouble("cost_per_conversion"));
		row.setCost(rs.getFloat("cost"));
		row.setTotalViews(rs.getLong("total_views"));
		row.setCompletedViews(rs.getLong("completed_views"));
	    row.setAverageDuration(rs.getLong("average_duration"));
	    row.setCostPerView(rs.getDouble("cost_per_view"));
	    row.setQ1percent(rs.getDouble("q1_per"));
	    row.setQ2percent(rs.getDouble("q2_per"));
	    row.setQ3percent(rs.getDouble("q3_per"));
	    row.setQ4percent(rs.getDouble("q4_per"));
	    row.setEngagementScore(rs.getDouble("engagement"));
	}
	
    public static void rowMapperDevice(BaseReportDto row, ResultSet rs) throws SQLException {
        row.setImpressions(rs.getLong("impressions"));
        row.setClicks(rs.getLong("clicks"));
        row.setCtr(rs.getDouble("ctr"));
        row.setEcpm(rs.getDouble("ecpm"));
        row.setEcpc(rs.getDouble("ecpc"));
        row.setConversions(rs.getLong("conversions"));
        row.setClickConversion(rs.getDouble("click_conv"));      // difference in column name
        row.setCostPerConversion(rs.getDouble("cost_per_conv")); // difference in column name
        row.setCost(rs.getFloat("cost"));
        row.setTotalViews(rs.getLong("total_views"));
        row.setCompletedViews(rs.getLong("completed_views"));
        row.setAverageDuration(rs.getLong("average_duration"));
        row.setCostPerView(rs.getDouble("cost_per_view"));
        row.setQ1percent(rs.getDouble("q1_per"));
        row.setQ2percent(rs.getDouble("q2_per"));
        row.setQ3percent(rs.getDouble("q3_per"));
        row.setQ4percent(rs.getDouble("q4_per"));
        row.setEngagementScore(rs.getDouble("engagement"));
    }
    
	public static void rowMapperLocation(BaseReportDto row, ResultSet rs) throws SQLException {
		row.setImpressions(rs.getLong("impressions"));
		row.setClicks(rs.getLong("clicks"));
		row.setCtr(rs.getDouble("ctr"));
		row.setEcpm(rs.getDouble("ecpm"));
		row.setEcpc(rs.getDouble("ecpc"));
		row.setConversions(rs.getLong("conversions"));
		row.setClickConversion(rs.getDouble("click_conversion"));
		row.setCostPerConversion(rs.getDouble("cost_per_conversion"));
		row.setCost(rs.getFloat("cost"));
		row.setTotalViews(rs.getLong("total_views"));
		row.setCompletedViews(rs.getLong("completed_views"));
	    row.setAverageDuration(rs.getLong("average_duration"));
	    row.setCostPerView(rs.getDouble("cost_per_view"));
	    row.setQ1percent(rs.getDouble("q1_per"));
	    row.setQ2percent(rs.getDouble("q2_per"));
	    row.setQ3percent(rs.getDouble("q3_per"));
	    row.setQ4percent(rs.getDouble("q4_per"));
	    row.setEngagementScore(rs.getDouble("engagement"));
	}
	
	public static void rowMapperOperator(BaseReportDto row, ResultSet rs) throws SQLException {
		row.setImpressions(rs.getLong("impressions"));
		row.setClicks(rs.getLong("clicks"));
		row.setCtr(rs.getDouble("ctr"));
		row.setEcpm(rs.getDouble("ecpm"));
		row.setEcpc(rs.getDouble("ecpc"));
		row.setConversions(rs.getLong("conversions"));
		row.setClickConversion(rs.getDouble("click_conversion"));
		row.setCostPerConversion(rs.getDouble("cost_per_conversion"));
		row.setCost(rs.getFloat("cost"));
	}
}
