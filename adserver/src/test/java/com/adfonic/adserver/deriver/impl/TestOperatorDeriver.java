package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.NetworkType;

public class TestOperatorDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	OperatorDeriver operatorDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		operatorDeriver = new OperatorDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testOperatorDeriver01(){
		assertNull(operatorDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testOperatorDeriver02(){
		final CountryDto country = mock(CountryDto.class);
		final Ipinfo ipinfo = mock(Ipinfo.class);
		final DomainCache domainCache = mock(DomainCache.class);
		final OperatorDto operator = mock(OperatorDto.class);
		final NetworkType network = mock(NetworkType.class);
		expect(new Expectations() {{
            allowing (country).getId(); will(returnValue(randomLong()));
            allowing (country).getIsoCode(); will(returnValue(randomAlphaString(2)));
            allowing (operator).getId(); will(returnValue(randomLong()));
            allowing (operator).getName(); will(returnValue(randomAlphaString(10)));
            allowing (operator).getCountryIsoCode(); will(returnValue(randomAlphaString(2)));
			allowing (context).getAttribute(Parameters.NETWORK_TYPE); will(returnValue("wifi"));
			//1
			oneOf (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(null));
			//2
			allowing (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
			oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//3
			allowing (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
			oneOf (ipinfo).getNetwork(); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//4
			oneOf (ipinfo).getNetwork(); will(returnValue(network));
			oneOf (network).getCarrier(); will(returnValue(""));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//5
			oneOf (ipinfo).getNetwork(); will(returnValue(network));
			allowing (network).getCarrier(); will(returnValue("Orange"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperatorByCountryAndQuovaAlias(country,"Orange"); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//6
			oneOf (ipinfo).getNetwork(); will(returnValue(network));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperatorByCountryAndQuovaAlias(country,"Orange"); will(returnValue(operator));
		}});
		
		assertNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
	}
	
	@Test
	public void testOperatorDeriver03(){
		final CountryDto country = mock(CountryDto.class);
		final DomainCache domainCache = mock(DomainCache.class);
		final OperatorDto operator = mock(OperatorDto.class);
		expect(new Expectations() {{
            allowing (operator).getId(); will(returnValue(randomLong()));
            allowing (operator).getName(); will(returnValue(randomAlphaString(10)));
            allowing (operator).getCountryIsoCode(); will(returnValue(randomAlphaString(2)));
			allowing (context).getAttribute(Parameters.NETWORK_TYPE); will(returnValue("non-wifi"));
			//1
			oneOf (context).getAttribute(Parameters.MCC_MNC); will(returnValue(null));
			allowing (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
			oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//2
			oneOf (context).getAttribute(Parameters.MCC_MNC); will(returnValue("sd"));
			allowing (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
			oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
			//3
			oneOf (context).getAttribute(Parameters.MCC_MNC); will(returnValue("something"));
			oneOf (domainCache).getOperatorByMccMnc("something"); will(returnValue(operator));
			//4
			oneOf (context).getAttribute(Parameters.MCC_MNC); will(returnValue("something"));
			allowing (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
			oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("1.1.1.189"));
			oneOf (domainCache).getOperatorByMccMnc("something"); will(returnValue(null));
			oneOf (domainCache).getOperator("1.1.1.189",country); will(returnValue(operator));
		}});
		
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
		assertNotNull(operatorDeriver.getAttribute(TargetingContext.OPERATOR, context));
	}
	
}
