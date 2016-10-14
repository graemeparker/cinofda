package com.adfonic.weve.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mariadb.jdbc.MySQLDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.weve.dao.BeaconServiceDaoImpl;
import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

@Ignore("needs certs imported in keystore and System properties set")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:adfonic-weve-test-context.xml")
public class LocalTestSSLConnectivity {
	
	String url = "jdbc:mysql://10.96.16.208:3306/weve"+
			"?autoReconnect=true"+
			"&useSSL=true"+
			"&serverSslCert=classpath:ca-cert.pem";
	
	@Value("${weve.jdbc.username}")
	String user;
	@Value("${weve.jdbc.password}")
	String password;
	@Value("${weve.jdbc.ssl.url}")
	String wiredUrl;
	
	@Autowired
	BeaconServiceDaoImpl myTestDao;
	
	@BeforeClass
	public static void initSystemProperties() {
		System.setProperty("javax.net.ssl.keyStore", "path/to/keystore"); 
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("javax.net.ssl.trustStore", "path/to/truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
	}
	
	@Test
	public void shouldConnectToDBUsingSslWithWiredDataSource() {
		BasicDataSource dataSource = (BasicDataSource) myTestDao.getDataSource();
		assertNotNull(dataSource);
		
		List<WeveOperatorDto> operator = myTestDao.getIpRangesAndHeaderNameForOperator();
		assertThat(operator.size(), equalTo(3));
		
		List<DeviceIdentifierTypeDto> list = myTestDao.getDeviceIdsAndRegexValidationString();
		assertThat(list.size(), equalTo(8));
	}

	@Test
	public void shouldConnectToDBUsingSslAndOverridenDataSource() throws Exception {
		MySQLDataSource anotherDataSource = new MySQLDataSource();
		anotherDataSource.setUrl(url);
		anotherDataSource.setUser(user);
		anotherDataSource.setPassword(password);

		DataSource dataSource = myTestDao.getDataSource();
		myTestDao.setDataSource(anotherDataSource);
		myTestDao.getJdbcTemplate().execute("SELECT 1");
		List<WeveOperatorDto> operator = myTestDao.getIpRangesAndHeaderNameForOperator();
		assertThat(operator.size(), equalTo(1));
		
		myTestDao.setDataSource(dataSource);
	}
	
	@Test
	public void shouldConnectToDBUsingSslAndBasicDataSource() throws Exception {
		BasicDataSource anotherDataSource = new BasicDataSource();
		anotherDataSource.setUrl(url);
		anotherDataSource.setUsername(user);
		anotherDataSource.setPassword(password);
		myTestDao.setDataSource(anotherDataSource);
		myTestDao.getJdbcTemplate().execute("SELECT 1");		
		List<WeveOperatorDto> operator = myTestDao.getIpRangesAndHeaderNameForOperator();
		assertThat(operator.size(), equalTo(1));
	}

}
