package com.adfonic.data.cache.util;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

public class PropertiesFactory {

	private static final Logger LOG = Logger.getLogger(PropertiesFactory.class.getName());
	
	private final DataSource dataSource;  
	
    public PropertiesFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }
	
	public Properties getProperties(){
		
		Map<String,String> global = new HashMap<String, String>();
		Map<String,String> location = new HashMap<String, String>();
		Map<String,String> shard = new HashMap<String, String>();
		Map<String,String> server = new HashMap<String, String>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {

            String sql = "select PROPERTIES.PROPERTY_NAME ,PROPERTIES.PROPERTY_VALUE ,PROPERTIES.PRIORITY_SCOPE " +
            			 " from PROPERTIES " +
            			 "	WHERE " +
            			 " (SCOPE_IDENTIFIER_NAME = '" + this.getHostname() + "' and PRIORITY_SCOPE = 1) or " +
            			 " (SCOPE_IDENTIFIER_NAME = '" + this.getShard()    + "' and PRIORITY_SCOPE = 2) or " +
            			 " (SCOPE_IDENTIFIER_NAME = '" + this.getLocation() + "' and PRIORITY_SCOPE = 3) or " +
            			 " (PRIORITY_SCOPE = 4) ";
            
            
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);

            while (rs.next()) {
            	String propertyName  = rs.getString(1);
            	String propertyValue = rs.getString(2);
                int   scopeId 	 	 = rs.getInt(3);
                
                switch (scopeId){
                	case 1:  server.put(propertyName, propertyValue);
                	break;
                	case 2:  shard.put(propertyName, propertyValue);
                	break;
                	case 3:  location.put(propertyName, propertyValue);
                	break;
                	case 4:  global.put(propertyName, propertyValue);
                	break;
                }
                	
            }


        }catch (Exception e){
       	 LOG.severe("Error Loading Properties :" + e.getMessage());
        }finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
   	return new Properties(global, location, shard, server);
   }
	
    public String getShard() throws UnknownHostException {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        String shardName = null;
        try {

            String sql = "select ADSERVER_SHARD.NAME " +
            			 " from " +
            			 " ADSERVER_STATUS, " +
            			 " ADSERVER_SHARD   " +
            			 " where ADSERVER_STATUS.ADSERVER_SHARD_ID = ADSERVER_SHARD.ID " +
            			 " and ADSERVER_STATUS.NAME = '" + this.getHostname() + "'";
            
            LOG.fine("CHECK SQL: " + sql);
            
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);

            while (rs.next()) {
            	shardName  = rs.getString(1);
                	
            }


        }catch (Exception e){
       	 LOG.severe("Error Getting Shard Name :" + e.getMessage());
        }finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
        
        return shardName;
	}
    
    public String getHostname() throws UnknownHostException {
        return java.net.InetAddress.getLocalHost().getHostName();
    }

    public String getLocation() throws UnknownHostException {
    	String fullHostName = java.net.InetAddress.getLocalHost().getHostName();
    	int indexOfDot = fullHostName.indexOf('.');
    	if(indexOfDot != -1) {
    	    return fullHostName.substring(indexOfDot + 1);
    	} else {
    	    return fullHostName;
    	}
	}
    
}
