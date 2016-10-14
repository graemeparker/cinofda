package com.adfonic.weve.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.mariadb.jdbc.MySQLDataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.adfonic.weve.dao.BeaconServiceDaoImpl;
import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

@Ignore("needs certs imported into keystore and System properties set")
public class LocalTestSSLWithJDBC {
	
	String url = "jdbc:mysql://10.96.16.208:3306/weve"+
			"?autoReconnect=true"+
			"&useSSL=true"+
			"&serverSslCert=classpath:ca-cert.pem";
	String user = "adfonic";
	String password = "adfon1c";
	
	@Test
	public void shouldConnectToDBUsingSslAndStraightJDBC() throws Exception {
		Connection con = null;
		try {
			Properties info = new Properties();
			info.setProperty("user", "adfonic");
			info.setProperty("password", "adfon1c");
			info.setProperty("database", "weve");
			info.setProperty("useSSL", "true");
			info.setProperty("serverSslCert", "classpath:ca-cert.pem");
			Class.forName("org.mariadb.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://10.96.16.208:3306", info);
			assertNotNull(con);
			
			Statement s = con.createStatement();
			ResultSet result = s.executeQuery("call weve.proc_return_operator_ip_ranges");
			result.next();
			assertThat(result.getInt("service_user"), equalTo(2));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e){
					System.out.println("ERROR: couldn't close connection to db");
					fail();
				}
			}
		}
	}
	
	@Test
	public void shouldConnectToDBUsingSslNoSpringWithDataSource() {
		Connection con = null;
		try {
			MySQLDataSource anotherDataSource = new MySQLDataSource();
			anotherDataSource.setUrl(url);
			anotherDataSource.setUser(user);
			anotherDataSource.setPassword(password);
			con = DataSourceUtils.getConnection(anotherDataSource);
			assertTrue(con != null);
			
			Statement s = con.createStatement();
			ResultSet result = s.executeQuery("call weve.proc_return_operator_ip_ranges");
			result.next();
			assertThat(result.getInt("service_user"), equalTo(2));
			
			BeaconServiceDaoImpl dao = new BeaconServiceDaoImpl();
			dao.setDataSource(anotherDataSource);
			List<WeveOperatorDto> operator = dao.getIpRangesAndHeaderNameForOperator();
			assertThat(operator.size(), equalTo(1));
			assertThat(operator.get(0).getOperatorId(), equalTo(2));
			
			List<DeviceIdentifierTypeDto> list = dao.getDeviceIdsAndRegexValidationString();
			assertThat(list.size(), equalTo(8));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e){
					fail();
				}
			}
		}
	}

}
