package com.adfonic.reporting.sql;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

import com.adfonic.domain.Geotarget;
import com.adfonic.domain.Model;
import com.adfonic.domain.Vendor;
import com.adfonic.reporting.Dimension;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Parameter;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Row;
import com.adfonic.reporting.Report.Column;

import com.adfonic.util.Range;

import static org.mockito.Mockito.*;

@Ignore
public class Device_ToolsSQLQueryTest {

	public ToolsSQLQuery tools;
	
	@Test
	public void testGetDeviceReportDetail(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
	    tools.addParameters(new Parameter.Device());
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getDeviceReport(6653L,"52035",null,null,new Date(112,8,24),new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),225195L);
	}
	
	@Test
	public void testGetDeviceReportDetailByDay(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
	    tools.addParameters(new Parameter.Device());
	    Parameter.TimeByDay dateRangeParameter = new Parameter.TimeByDay(TimeZone.getDefault(), new Range<Date>(new Date(112,8,24),new Date(112,8,25)),"Date");
		tools.addParameters(dateRangeParameter);
    	ReportUtil.addReportMetrics(tools, false, false);
		Report report = tools.getDeviceReportByDay(6653L,"52035",null,null,new Date(112,8,24),new Date(112,8,24),false,false);
//		printReport(report);
		Long impressions = (Long) report.getRows().get(0).getObject(report.getColumn("IMPRESSIONS").getIndex());
		assertEquals("Impressions",impressions.longValue(),225195L);
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
