package com.adfonic.data.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.domain.BidType;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;

@RunWith(MockitoJUnitRunner.class)
public class AdserverDataCacheImplTest {

	private static final double tolerance = 1e-20;
	@Mock
	private EcpmDataRepository ecpmDataRepository;
	@Mock
	private SystemVariable systemVariable;
	@Mock
	private PublisherDto publisher;
	@Mock
	private RtbConfigDto rtbConfigDto;
	
	private AdserverDataCacheImpl testObj;
	
	@Before
	public void before() {
		testObj = new AdserverDataCacheImpl(ecpmDataRepository);
		Mockito.when(publisher.getRtbConfig()).thenReturn(rtbConfigDto);
                Mockito.when(ecpmDataRepository.getSystemVariableByName("adfonic_ctr_dsp_buffer")).thenReturn(systemVariable);
                Mockito.when(ecpmDataRepository.getSystemVariableByName("adfonic_cpx_dsp_buffer")).thenReturn(systemVariable);
                Mockito.when(systemVariable.getDoubleValue()).thenReturn(.95);
                             
	}
	

	@Test(expected=RuntimeException.class)
	public void getAllEligibleCreatives() {
		
		testObj.getAllEligibleCreatives();
	}
}
