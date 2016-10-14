package com.adfonic.reporting.sql;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Column;
import com.adfonic.reporting.Report.Row;
import com.adfonic.util.Range;

@Ignore
public class Locations_ToolsSQLQueryTest {

	ToolsSQLQuery tools;
	
	@Before
	public void setup(){

	}

	@Test
	public void testGetLocationReportDetail(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		tools.addParameters(new Parameter.LocationByCountries());
    	tools.addMetrics(Metric.LOCATION_PERCENT_IMPRESSIONS);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getLocationReportDetail(6653L,"52035",new Date(112,8,24),new Date(112,8,24),false,false,false);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);
	}
	
	@Test
	public void testGetLocationReportDetailByDay(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		tools.addParameters(dateRangeParameter);
		tools.addParameters(new Parameter.LocationByCountries());
    	tools.addMetrics(Metric.LOCATION_PERCENT_IMPRESSIONS);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getLocationReportDetailByDay(6653L,"52035",new Date(112,8,24),new Date(112,8,25),false,false,false);
//	    printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);		
	}
	
	
	@Ignore // Fails missing aggregation table
	@Test
	public void testGetLocationInvSourceReportDetail(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
  		tools.addParameters(new Parameter.GroupByInventory());
		tools.addParameters(new Parameter.LocationByCountries());
    	tools.addMetrics(Metric.LOCATION_PERCENT_IMPRESSIONS);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getLocationInvSourceReportDetail(6653L,"52035",new Date(112,8,24),new Date(112,8,24),false,false,false);
		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),395290L);		
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

