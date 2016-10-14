package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.util.Range;

public class TestIntegrationTypeDeriver  extends BaseAdserverTest {

	DeriverManager deriverManager;
	IntegrationTypeDeriver integrationTypeDeriver;
	private TargetingContext context;
    private DomainCache domainCache;
    
	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		integrationTypeDeriver = new IntegrationTypeDeriver(deriverManager);
		context = mock(TargetingContext.class);
		domainCache = mock(DomainCache.class);
	}

	@Test
	public void testIntegrationTypeDeriver01(){
		assertNull(integrationTypeDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testIntegrationTypeDeriver02(){
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final PublicationDto publication = mock(PublicationDto.class);
		final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final Long integrationTypeId = randomLong();
		final PublicationTypeDto publicationType = mock(PublicationTypeDto.class);
        final long publicationTypeId = randomLong();
		final PublisherDto publisher = mock(PublisherDto.class);
		expect(new Expectations() {{
			allowing (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(null));
			allowing (context).getDomainCache();will(returnValue(domainCache));
			allowing (adSpace).getPublication();will(returnValue(publication));
			allowing (publication).getPublisher();will(returnValue(publisher));
			allowing (publication).getPublicationTypeId();will(returnValue(publicationTypeId));
            allowing (domainCache).getPublicationTypeById(publicationTypeId); will(returnValue(publicationType));
            allowing (domainCache).getIntegrationTypeById(integrationTypeId); will(returnValue(integrationType));
            
			//1
			oneOf (context).getAdSpace();will(returnValue(null));
			//2
			allowing (context).getAdSpace();will(returnValue(adSpace));
			oneOf (publication).getDefaultIntegrationTypeId();will(returnValue(null));
			oneOf (publisher).getDefaultIntegrationTypeId(publicationTypeId);will(returnValue(null));
			oneOf (publicationType).getDefaultIntegrationTypeId();will(returnValue(integrationTypeId));
			//3
			oneOf (publication).getDefaultIntegrationTypeId();will(returnValue(integrationTypeId));
			//4
			oneOf (publication).getDefaultIntegrationTypeId();will(returnValue(null));
			oneOf (publisher).getDefaultIntegrationTypeId(publicationTypeId);will(returnValue(integrationTypeId));
			//5
			oneOf (publication).getDefaultIntegrationTypeId();will(returnValue(null));
			oneOf (publisher).getDefaultIntegrationTypeId(publicationTypeId);will(returnValue(null));
			oneOf (publicationType).getDefaultIntegrationTypeId();will(returnValue(null));
		}});

        IntegrationTypeDto integrationTypeResult;

		assertNull(integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
		
		integrationTypeResult = (IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context);
		assertNotNull(integrationTypeResult);
		
		integrationTypeResult = (IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context);
		assertNotNull(integrationTypeResult);
		
        integrationTypeResult = (IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context);
		assertNotNull(integrationTypeResult);
		
		assertNull(integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}
	
	@Test
	public void testIntegrationTypeDeriver03(){
		
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final PublicationDto publication = mock(PublicationDto.class);
		final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final String integrationTypeSystemName = "TestSystem"; // won't match the INTEGRATION_TYPE_PATTERN regex
		final PublicationTypeDto publicationType = mock(PublicationTypeDto.class);
        final long publicationTypeId = 123L;
		final PublisherDto publisher = mock(PublisherDto.class);
        final Long integrationTypeId = randomLong();
		expect(new Expectations() {{
			allowing (context).getDomainCache();will(returnValue(domainCache));
			allowing (context).getAdSpace();will(returnValue(adSpace));
			allowing (adSpace).getPublication();will(returnValue(publication));
			allowing (publication).getPublicationTypeId();will(returnValue(publicationTypeId));
			allowing (publication).getPublisher();will(returnValue(publisher));
            allowing (domainCache).getPublicationTypeById(publicationTypeId); will(returnValue(publicationType));
            
			//1
			allowing (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(integrationTypeSystemName));
			oneOf (domainCache).getIntegrationTypeBySystemName(integrationTypeSystemName);will(returnValue(null));
            
			oneOf (publication).getDefaultIntegrationTypeId();will(returnValue(null));
			oneOf (publisher).getDefaultIntegrationTypeId(publicationTypeId);will(returnValue(null));
			oneOf (publicationType).getDefaultIntegrationTypeId();will(returnValue(integrationTypeId));
            oneOf (domainCache).getIntegrationTypeById(integrationTypeId); will(returnValue(integrationType));
		}});
		
		IntegrationTypeDto integrationTypeResult = (IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context);
		assertNotNull(integrationTypeResult);
	}

	@Test
	public void testIntegrationTypeDeriver04_patternMatching_withMatch() {
		final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class, "integrationType");
        final String rClient = "Some/Thing/1.2.3";

        final IntegrationTypeDto someOtherIntegrationType = mock(IntegrationTypeDto.class, "someOtherIntegrationType");
        
        final Map<Range<Integer>,IntegrationTypeDto> rangeMap = new HashMap<Range<Integer>,IntegrationTypeDto>();
        rangeMap.put(new Range<Integer>(1000000, 1002002), someOtherIntegrationType);
        rangeMap.put(new Range<Integer>(1002003, 1002003), integrationType);
        rangeMap.put(new Range<Integer>(1002004, 9999999), someOtherIntegrationType);
        
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(rClient));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getIntegrationTypeBySystemName(rClient); will(returnValue(null));
            oneOf (domainCache).getIntegrationTypeVersionRangeMapByPrefix(with(any(String.class))); will(returnValue(rangeMap));
            allowing (integrationType).getSystemName(); will(returnValue(rClient));
		}});
		
		assertEquals(integrationType, (IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}

	@Test
	public void testIntegrationTypeDeriver05_patternMatching_noVersion() {
        final String rClient = "Some/Thing/abc"; // non-numeric version
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(rClient));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getIntegrationTypeBySystemName(rClient); will(returnValue(null));
            // It'll fall through, but we tested that scenario in previous test cases
			allowing (context).getAdSpace(); will(returnValue(null));
		}});
		assertNull(integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}

	@Test
	public void testIntegrationTypeDeriver06_patternMatching_noRangeMap() {
        final String rClient = "Some/Thing/1.2.3";
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(rClient));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getIntegrationTypeBySystemName(rClient); will(returnValue(null));
            oneOf (domainCache).getIntegrationTypeVersionRangeMapByPrefix(with(any(String.class))); will(returnValue(null));
            // It'll fall through, but we tested that scenario in previous test cases
			allowing (context).getAdSpace(); will(returnValue(null));
		}});
		assertNull(integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}

	@Test
	public void testIntegrationTypeDeriver07_patternMatching_noMatch() {
        final String rClient = "Some/Thing/1.2.3";

        final IntegrationTypeDto someOtherIntegrationType = mock(IntegrationTypeDto.class, "someOtherIntegrationType");
        
        final Map<Range<Integer>,IntegrationTypeDto> rangeMap = new HashMap<Range<Integer>,IntegrationTypeDto>();
        rangeMap.put(new Range<Integer>(1000000, 1002002), someOtherIntegrationType);
        // leave a gap at 1002003 (1.2.3)
        rangeMap.put(new Range<Integer>(1002004, 9999999), someOtherIntegrationType);
        
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(rClient));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getIntegrationTypeBySystemName(rClient); will(returnValue(null));
            oneOf (domainCache).getIntegrationTypeVersionRangeMapByPrefix(with(any(String.class))); will(returnValue(rangeMap));
            // It'll fall through, but we tested that scenario in previous test cases
			allowing (context).getAdSpace(); will(returnValue(null));
		}});
		
		assertNull((IntegrationTypeDto)integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}

	@Test
	public void testIntegrationTypeDeriver08_found_by_parameter() {
        final String rClient = randomAlphaNumericString(10);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class, "integrationType");
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.INTEGRATION_TYPE); will(returnValue(rClient));
			allowing (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getIntegrationTypeBySystemName(rClient); will(returnValue(integrationType));
		}});
		assertEquals(integrationType, integrationTypeDeriver.getAttribute(TargetingContext.INTEGRATION_TYPE, context));
	}
}
