package com.adfonic.reporting.sql;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.Ignore;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Column;
import com.adfonic.reporting.Report.Row;
import com.adfonic.util.Range;

@Ignore
public class Campaign_ToolsSQLQueryTest {

	ToolsSQLQuery tools;
	
	@Test
	public void testGetCampaignReportDetail(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportDetail(6653L,"52035",new Date(112,8,24),new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);
	}	
	
	@Test
	public void testGetCampaignReportTotalByDay(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		tools.addParameters(dateRangeParameter);
		ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportTotalByDay(6653L,"52035",new Date(112,8,24),new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);
	}

	@Test
	public void testGetCampaignReportTotalByDayByInventory(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		tools.addParameters(new Parameter.GroupByInventory());
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		tools.addParameters(dateRangeParameter);
		ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportTotalByDayByInvSource(6653L,"52035",new Date(112,8,24),new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),26279L);
	}

	@Test
	public void testGetCampaignReportDetailByDay(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		tools.addParameters(dateRangeParameter);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportDetailByDay(6653L,"52035",new Date(112,8,24),new Date(112,8,25),false,false);
//	    printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);		
	}

	
	@Test
	public void testGetCampaignReportDetailByHour(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		Parameter.TimeByHour hourParameter = new Parameter.AdvertiserTimeByHour(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)));
		tools.addParameters(dateRangeParameter);
		tools.addParameters(hourParameter);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportDetailByHour(6653L,"52035",new Date(112,8,24),false,false);
//	    printReport(report);
		Long impressions = (Long) report.getRows().get(1).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),169955L);		
	}


    
	@Test
	public void testGetCampaignReportDetailByHourByInventory(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		tools.addParameters(new Parameter.GroupByInventory());
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		Parameter.TimeByHour hourParameter = new Parameter.AdvertiserTimeByHour(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)));
		tools.addParameters(dateRangeParameter);
		tools.addParameters(hourParameter);
		ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getCampaignReportDetailByHourByInvSource(6653L,"52035",new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),12659L);
	}	
	
	public void printReport(Report report){
		  for(Row row : report.getRows()){
		
			  int i=0;
			for(Column col : report.getColumns()){
				try {
					System.out.println(i+":"+col.getHeader()+" ="+row.getObject(i));
					i++;
				} catch (Exception e){
					System.out.println(i+" +e");
				}
			}
		  }
		}
		
	    private void setDataSource(ToolsSQLQuery tools){
	    	BasicDataSource dataSource = new BasicDataSource();
	        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	        dataSource.setUsername("adfonicr3p0rt42");
	        dataSource.setPassword("m3dus442");
	        dataSource.setUrl("jdbc:mysql://lon3reportdb01:3306/adfonic");
	        dataSource.setMaxActive(10);
	        dataSource.setMaxIdle(5);
	        dataSource.setInitialSize(5);
	        dataSource.setValidationQuery("SELECT 1");
	        tools.setDataSource(dataSource);
	    }
	
	
}
