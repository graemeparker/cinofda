package com.adfonic.weve.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.weve.OperatorEnum;
import com.adfonic.weve.dao.BeaconServiceDao;
import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

public class BeaconServiceTest extends AbstractAdfonicTest {
	
	BeaconServiceImpl service = new BeaconServiceImpl();
	BeaconServiceDao dao;

	@Before
	public void setUp() {
		dao = mock(BeaconServiceDao.class, "dao");
		inject(service, "dao", dao);
	}
	
	@Test
	public void shouldNotMatchLocalhostInOperatorLookup() {
		final List<WeveOperatorDto> operatorList = Arrays.asList(new WeveOperatorDto(2, 3368601601L, 3368601855L, "header", 0L, false, false));
		expect(new Expectations() {{
			oneOf (dao).getIpRangesAndHeaderNameForOperator(); will(returnValue(operatorList));
		}});
		
		service.populateOperatorInfo();
		WeveOperatorDto result = service.retrieveOperatorInfoByIpAddressLookup("127.0.0.1");
		assertThat(result.getOperatorId(), equalTo(-1));
		assertThat(result.getRequestHeaderName(), equalTo(OperatorEnum.OPERATOR_NOT_FOUND.toString()));
	}
	
	@Test
	public void shouldMatchIpAddressInRangeForOperator() {
		final int operatorId = 2;
		final WeveOperatorDto dto = new WeveOperatorDto(operatorId, 3368601601L, 3368601855L, "header", 0L, false, false);
		expect(new Expectations() {{
			oneOf (dao).getIpRangesAndHeaderNameForOperator(); will(returnValue(Arrays.asList(dto)));
		}});
		
		service.populateOperatorInfo();
		WeveOperatorDto result = service.retrieveOperatorInfoByIpAddressLookup("200.200.200.111");
		assertThat(result.getOperatorId(), equalTo(operatorId));
		assertThat(result.getRequestHeaderName(), equalTo("header"));
	}
	
	@Test
	public void shouldMatchIpAddressForOperatorWithMultipleRanges() {
		int operatorId = 3;
		final List<WeveOperatorDto> operatorList = Arrays.asList(new WeveOperatorDto(operatorId, 1605109953L, 1605109953L, "header", 0L, false, false),
				new WeveOperatorDto(operatorId, 3368601601L, 3368601855L, "header", 0L, false, false));
		
		expect(new Expectations() {{
			oneOf (dao).getIpRangesAndHeaderNameForOperator(); will(returnValue(operatorList));
		}});
		
		service.populateOperatorInfo();
		WeveOperatorDto actualDto = service.retrieveOperatorInfoByIpAddressLookup("95.172.8.193");
		assertThat(actualDto.getRequestHeaderName(), equalTo("header"));
		assertThat(actualDto.getOperatorId(), equalTo(operatorId));
	}
	
	@Test
	public void shouldRetrieveHeaderNameCorrespondingToIpWhenOperatorHasMultipleRanges() throws Exception {
		final int operatorId = 4;
		WeveOperatorDto expectedDto = new WeveOperatorDto(operatorId, 3368601601L, 3368601855L, "testHeaderName", 0L, false, false);
		final List<WeveOperatorDto> operatorList = Arrays.asList(expectedDto, new WeveOperatorDto(operatorId, 0L, 0L, "someOtherHeaderName", 0L, false, false));
		
		expect(new Expectations() {{
			oneOf (dao).getIpRangesAndHeaderNameForOperator(); will(returnValue(operatorList));
		}});
		
		service.populateOperatorInfo();
		WeveOperatorDto actualDto = service.retrieveOperatorInfoByIpAddressLookup("200.200.200.23");
		assertThat(actualDto.getRequestHeaderName(), equalTo(expectedDto.getRequestHeaderName()));		
	}
	
	@Test
	public void shouldFailGracefullyIfOperatorDoesNotExist() {
		expect(new Expectations() {{
			oneOf (dao).getIpRangesAndHeaderNameForOperator(); will(returnValue(Collections.<WeveOperatorDto> emptyList()));
		}});
		
		service.populateOperatorInfo();
		WeveOperatorDto result = service.retrieveOperatorInfoByIpAddressLookup("1.1.1.1");
		assertThat(result.getOperatorId(), equalTo(-1));
		assertThat(result.getRequestHeaderName(), equalTo(OperatorEnum.getNameById(-1)));
	}
	
	@Test
	public void shouldRetrieveValidationRegexForKnownDeviceIdType() {
		int deviceTypeId = 1;
		String regex = "^[0-9A-Fa-f]{40}$";
		final List<DeviceIdentifierTypeDto> deviceIds = Arrays.asList(new DeviceIdentifierTypeDto(deviceTypeId, "deviceIdName", regex, true));
		expect(new Expectations() {{
			oneOf (dao).getDeviceIdsAndRegexValidationString(); will(returnValue(deviceIds));
		}});
		
		service.populateDeviceIdValidationInfo();
		assertThat(service.retrieveValidationRegexForDeviceId(deviceTypeId), equalTo(regex));
		Pattern validationPattern = Pattern.compile(regex);
		assertThat(validationPattern.matcher("f3da317c-4f3d-ea60-f43d-102ec1fa433b").matches(), equalTo(false));
	}
	
	@Test
	public void shouldReturnNullForUnknownDeviceIdType() {
		expect(new Expectations() {{
			oneOf (dao).getDeviceIdsAndRegexValidationString(); will(returnValue(Collections.<DeviceIdentifierTypeDto> emptyList()));
		}});
		
		service.populateDeviceIdValidationInfo();
		assertThat(service.retrieveValidationRegexForDeviceId(1), equalTo(null));
	}
	
	@Test
	public void shouldRefreshOperatorCacheAtCorrectInterval() throws InterruptedException {
		expect(new Expectations() {{
			atLeast(2).of(dao).getIpRangesAndHeaderNameForOperator(); 
			will(onConsecutiveCalls(
					returnValue(Collections.<WeveOperatorDto> emptyList()),
					returnValue(Arrays.asList(new WeveOperatorDto(1, 1111111111L, 2222222222L, "random-header", 0L, false, false), 
											  new WeveOperatorDto(2, 3333333333L, 3355555555L, "blah", 0L, false, false)))
			));
		}});
		
		service.populateOperatorInfo();
		String testIpAddress = "66.58.53.199";
		WeveOperatorDto result = service.retrieveOperatorInfoByIpAddressLookup(testIpAddress);
		assertThat(service.getOperatorCache(), is(Collections.<WeveOperatorDto> emptyList()));
		assertThat(result.getOperatorId(), equalTo(-1));
		assertThat(result.getRequestHeaderName(), equalTo("OPERATOR_NOT_FOUND"));

		// would run on a spring configured schedule
		service.populateOperatorInfo();
		WeveOperatorDto secondAttempt = service.retrieveOperatorInfoByIpAddressLookup(testIpAddress);
		assertThat(secondAttempt.getOperatorId(), equalTo(1));
		assertThat(secondAttempt.getRequestHeaderName(), equalTo("random-header"));
	}
}
