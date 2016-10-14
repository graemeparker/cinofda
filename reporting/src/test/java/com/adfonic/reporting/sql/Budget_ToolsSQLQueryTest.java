package com.adfonic.reporting.sql;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.Column;
import com.adfonic.reporting.Report.Row;

@Ignore
public class Budget_ToolsSQLQueryTest {

	public ToolsSQLQuery tools;
	
	@Test
	public void testGetBudgetReportCampaignDaily(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Report report = tools.getBudgetReportCampaignDaily(6653L,"52035",new Date(112,8,24),new Date(112,8,24));
//		printReport(report);
		Double budget = (Double) report.getRows().get(0).getObject(report.getColumn("Budget").getIndex());
		assertEquals("Budget",budget.doubleValue(),1046.19,0.001);
	}
	
	@Test
	public void testGetBudgetReportCampaignOverall(){
		tools = new ToolsSQLQuery();
		tools.init(Locale.ENGLISH, TimeZone.getDefault());
		setDataSource(tools);
		Report report = tools.getBudgetReportCampaignOverall(6653L,"52035",new Date(112,8,24),new Date(112,8,24));
//			printReport(report);
		Double budget = (Double) report.getRows().get(0).getObject(report.getColumn("Budget").getIndex());
		assertEquals("Budget",budget.doubleValue(),8137.04,0.001);
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
