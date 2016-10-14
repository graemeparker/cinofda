package com.adfonic.reporting.service.advertiser.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.reporting.service.advertiser.dto.BaseReportDto;

public abstract class BaseReportRowMapper<T extends Object> implements RowMapper<T> {
	
    private Set<String> setAvailableColumns;
    private ResultSet rs;
    private final String prefix;
 
    public BaseReportRowMapper() {
        prefix = "";
    }
 
    public BaseReportRowMapper(String prefix) {
        this.prefix = prefix;
    }
 
    private void init(ResultSet rs) throws SQLException {
        this.rs = rs;
        setAvailableColumns = new HashSet<String>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1, n = meta.getColumnCount() + 1; i < n; i++) {
        	setAvailableColumns.add(meta.getColumnLabel(i));
        }
    }
 
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (setAvailableColumns == null)
            init(rs);
        
        return mapRowImpl(rs, rowNum);
    }
 
    public abstract T mapRowImpl(ResultSet rs, int rowNum) throws SQLException;
 
    public boolean column(String sName) {
        return (setAvailableColumns.contains(sName));
    }
 
    public Long getLong(String sName) throws SQLException {
        if (column(prefix + sName))
            return rs.getLong(prefix + sName);
        else
        	return null;
    }
 
    public Integer getInteger(String sName) throws SQLException {
        if (column(prefix + sName))
            return rs.getInt(prefix + sName);
        else
        	return null;
    }
    
    public String getString(String sName) throws SQLException {
    	if (column(prefix + sName))
    		return rs.getString(prefix + sName);
    	else
    		return null;
    }
    
    public Double getDouble(String sName) throws SQLException {
    	if(column(prefix + sName))
    		return rs.getDouble(prefix + sName);
    	else 
    		return null;
    }
    
    public Float getFloat(String sName) throws SQLException {
    	if(column(prefix + sName))
    		return rs.getFloat(prefix + sName);
    	else 
    		return null;
    }
    
    public Date getDate(String sName) throws SQLException {
		if(column(prefix + sName)) {
			long temp = rs.getLong(prefix + sName) * 1000L;
			return temp == 0 ? null : new Date(temp);
		} else {
			return null;
		}
    }
    
    public BaseReportDto mapRowCommon(BaseReportDto source, ResultSet rs, int rowNum) throws SQLException {
    	BaseReportDto row = source;
		row.setTotalImpressions(getLong("impressions"));
		row.setTotalClicks(getLong("clicks"));
		row.setCtr(getDouble("ctr"));
		row.setEcpm(getDouble("ecpm"));
		row.setEcpc(getDouble("ecpc"));
		row.setTotalConversions(getLong("conversions"));
		row.setClickConversion(getDouble("click_conversion"));
		row.setCostPerConversion(getDouble("cost_per_conversion"));
		row.setTotalCost(getFloat("cost"));
		row.setTotalViews(getLong("total_views"));
		row.setCompletedViews(getLong("completed_views"));
	    row.setAverageDuration(getLong("average_duration"));
	    row.setCostPerView(getDouble("cost_per_view"));
	    row.setQ1percent(getDouble("q1_per"));
	    row.setQ2percent(getDouble("q2_per"));
	    row.setQ3percent(getDouble("q3_per"));
	    row.setQ4percent(getDouble("q4_per"));
	    row.setEngagementScore(getDouble("engagement"));
	    return row;
    }
}