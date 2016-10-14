package com.adfonic.weve.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

@Ignore("Please unignore to run locally. This test should run against the current weve bootstrap sql")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:adfonic-weve-test-context.xml")
public class LocalTestBeaconServiceDao {

	@Autowired
	BeaconServiceDaoImpl dao;
	
	@Autowired
	DataSource dataSource;

	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void setUp() {
		jdbcTemplate = dao.getJdbcTemplate();
		jdbcTemplate.execute("delete from unmatched_display_uid");
		jdbcTemplate.execute("delete from beacon_requests");
		jdbcTemplate.execute("delete from beacon_requests_display_uid");
	}	
	
	@Test
	public void shouldReturnOperatorIdAndIpRanges() {
		List<WeveOperatorDto> operatorInfo = dao.getIpRangesAndHeaderNameForOperator();
		assertThat(operatorInfo, notNullValue());
		assertThat(operatorInfo.size(), is(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void shouldReturnOperatorIdAndIpRangesForTelefonica() throws Exception {
		List<WeveOperatorDto> operatorInfo = dao.getIpRangesAndHeaderNameForOperator();
		for (WeveOperatorDto weveOperatorDto : operatorInfo) {
			if (weveOperatorDto.getOperatorId() == 2) {
				assertThat(weveOperatorDto.getRequestHeaderName(), equalTo("x-up-calling-line-id#2"));
			}
		}
	}
	
	@Test
	public void shouldReturnDeviceIdentifiersAndValidationRegex() {
		List<DeviceIdentifierTypeDto> list = dao.getDeviceIdsAndRegexValidationString();
		assertThat(list.get(0).getSystemName(), equalTo("dpid"));
		assertThat(list.get(5).getRegexPattern(), equalTo("^([0-9A-Fa-f]{32}|[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12})$"));
	}
	
	@Test
	public void shouldReturnInsecureForAndroidDeviceIdentifier() {
		List<DeviceIdentifierTypeDto> list = dao.getDeviceIdsAndRegexValidationString();
		assertThat(list.get(3).getSystemName(), equalTo("android"));
		assertThat(list.get(3).isSecure(), equalTo(false));
	}
	
	@Test
	public void shouldReturnSecureForHifaDeviceIdentifier() {
		List<DeviceIdentifierTypeDto> list = dao.getDeviceIdsAndRegexValidationString();
		assertThat(list.get(6).getSystemName(), equalTo("hifa"));
		assertThat(list.get(6).isSecure(), equalTo(true));
	}
	
	//  works on localhost with bootstrap version of schema 
	@Test  
	public void shouldReturnZeroForWeveUserNotFound() {
		Long weveId = dao.findWeveId(2, "55555");
		assertThat(weveId, equalTo(0L));
	}
	
	//  works on localhost with bootstrap version of schema 
	@Test 
	public void shouldReturnWeveIdWhenEndUserIdExistsRegardlessOfCase() throws Exception {
		assertThat(dao.findWeveId(2, "adfonic1"), equalTo(2437472318L));
		assertThat(dao.findWeveId(2, "ADFONIC1"), equalTo(2437472318L));
	}
	
	@Test
	public void shouldReturnErrorCodeWhenDeviceIdsInvalid() throws Exception {
		//trailing pipe character
		Integer result = dao.saveDeviceIds(1L, "1111-1111-1111-1111-1111-1111-1111-11111~1|", "dummyAdSpace", "dummyCreative");
		assertThat(result, equalTo(-3));
		
		//device id less than 40 chars
		Integer result2 = dao.saveDeviceIds(1L, "hu9fh3fuvgh0npfi~1", "dummyAdSpace", "dummyCreative");
		assertThat(result2, equalTo(-2));
		
		//missing device type id and tilda separator
		Integer result3 = dao.saveDeviceIds(1L, "1ffffeeeeggggqqqq1111ffffeeeeggggqqqq1111", "dummyAdSpace", "dummyCreative");
		assertThat(result3, equalTo(-1));
	}

	@Test
	public void shouldReturnSameNumberOfRowsInsertedForValidDeviceIds() {
		Integer rowsInserted = dao.saveDeviceIds(1L,  "0123456789abcdefa12345678aabcdaf0223421b~1", "dummyAdSpace", "dummyCreative");
		assertThat(rowsInserted, equalTo(1));
		
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests where display_service_esk = 1");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace"));
	}
	
	@Test
	public void shouldHandleBlankDeviceIds() {
		Integer rowsInserted = dao.saveDeviceIds(5L,  "", "dummyAdSpace5", "dummyCreative");
		// translated from null as we aren't inserting a record into the device processing queue. 
		assertThat(rowsInserted, equalTo(0));
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests where display_service_esk = 5");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace5"));
	}
	
	@Test
	public void shouldHandleNullCreativeId() {
		Integer rowsInserted = dao.saveDeviceIds(6L,  "1111-1111-1111-1111-1111-1111-1111-11111~1", "dummyAdSpace6", null);
		assertThat(rowsInserted, equalTo(1));
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests where display_service_esk = 6");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace6"));
	}
	
	@Test
	public void saveDeviceIdsDisplayUidShouldHandleNullCreativeId() {
		Integer rowsInserted = dao.saveDeviceIdsForUnknownUser("pretend-hash", 1,  "1111-2222-3333-4444-5555-6666-1111-11111~1", "dummyAdSpace7", null);
		assertThat(rowsInserted, equalTo(1));
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests_display_uid where display_uid = 'pretend-hash'");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace7"));
		List<Map<String,Object>> result2 = jdbcTemplate.queryForList("select * from unmatched_display_uid where display_uid = 'pretend-hash'");
		assertThat(result2.size(), equalTo(1));
		assertThat(result2.get(0).get("device_id").toString(), equalTo("1111-2222-3333-4444-5555-6666-1111-11111"));
		assertThat(result2.get(0).get("device_identifier_type").toString(), equalTo("1"));
	}
	
	@Test
	public void saveDeviceIdsDisplayUidShouldHandleBlankDeviceIds() {
		Integer rowsInserted = dao.saveDeviceIdsForUnknownUser("another-pretend-hash", 1,  "", "dummyAdSpace8", "dummyCreative");
		// translated from null as we aren't inserting a record into the device processing queue. 
		assertThat(rowsInserted, equalTo(0));
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests_display_uid where display_uid = 'another-pretend-hash'");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace8"));
		List<Map<String,Object>> result2 = jdbcTemplate.queryForList("select * from unmatched_display_uid where display_uid = 'another-pretend-hash'");
		assertThat(result2.size(), equalTo(0));
	}
	
	@Test
	public void saveDeviceIdsDisplayUidShouldReturnErrorCodeWhenDeviceIdsInvalid() throws Exception {
		//trailing pipe character
		Integer result = dao.saveDeviceIdsForUnknownUser("me", 1, "1111-1111-1111-1111-1111-1111-1111-11111~1|", "dummyAdSpace", "dummyCreative");
		assertThat(result, equalTo(-3));
		
		//device id less than 40 chars
		Integer result2 = dao.saveDeviceIdsForUnknownUser("meAgain", 2, "hu9fh3fuvgh0npfi~1", "dummyAdSpace", "dummyCreative");
		assertThat(result2, equalTo(-2));
		
		//missing device type id and tilda separator
		Integer result3 = dao.saveDeviceIdsForUnknownUser("notYouAgain", 1, "1ffffeeeeggggqqqq1111ffffeeeeggggqqqq1111", "dummyAdSpace", "dummyCreative");
		assertThat(result3, equalTo(-1));
	}

	@Test
	public void saveDeviceIdsDisplayUidShouldReturnSameNumberOfRowsInsertedForValidDeviceIds() {
		Integer rowsInserted = dao.saveDeviceIdsForUnknownUser("encodedUserId", 1,  "0123456789abcdefa12345678aabcdaf0223421b~1|8a22c84bbcba0e4452905700ca07057e69406ad8~7", "dummyAdSpace9", "dummyCreative");
		assertThat(rowsInserted, equalTo(2));
		List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from beacon_requests_display_uid where display_uid = 'encodedUserId'");
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0).get("external_adspace_id").toString(), equalTo("dummyAdSpace9"));
		List<Map<String,Object>> result2 = jdbcTemplate.queryForList("select * from unmatched_display_uid where display_uid = 'encodedUserId'");
		assertThat(result2.size(), equalTo(2));		
		assertThat(result2.get(0).get("device_id").toString(), equalTo("0123456789abcdefa12345678aabcdaf0223421b"));
		assertThat(result2.get(0).get("device_identifier_type").toString(), equalTo("1"));
		assertThat(result2.get(1).get("device_id").toString(), equalTo("8a22c84bbcba0e4452905700ca07057e69406ad8"));
		assertThat(result2.get(1).get("device_identifier_type").toString(), equalTo("7"));
	}
	
	@Test
	public void shouldReturnFineLoggingPerIpRange() {
		List<WeveOperatorDto> operatorInfo = dao.getIpRangesAndHeaderNameForOperator();
		for (WeveOperatorDto weveOperatorDto : operatorInfo) {
			if (weveOperatorDto.getIpRangeStart() == 3368601601L) {
				assertThat(weveOperatorDto.getBeaconServiceFineLoggingOn(), equalTo(false));
				assertThat(weveOperatorDto.getOptOutFineLoggingOn(), equalTo(false));
			}
		}
	}
}
