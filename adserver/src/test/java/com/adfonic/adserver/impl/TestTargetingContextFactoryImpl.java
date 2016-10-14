package com.adfonic.adserver.impl;

import org.junit.Before;

import com.adfonic.adserver.BaseAdserverTest;

public class TestTargetingContextFactoryImpl extends BaseAdserverTest {

    @Before
	public void initTests(){
	}
    
// MAD-730 - Delete ignored tests in Adserver project 	
//    @Ignore
//	@Test
//	public void testTargetingContextFactoryImpl01_TargetingContextFactoryImpl(){
//		final DomainCacheManager domainCacheManager = mock(DomainCacheManager.class,"domainCacheManager");
//		final AdserverDomainCacheManager adserverDomainCacheManager = mock(AdserverDomainCacheManager.class,"adserverDomainCacheManager");
//		final AdserverDataCacheManager adserverDataCacheManager = mock(AdserverDataCacheManagerImpl.class,"adserverDataCacheManager");
//		final DeriverManager deriverManager = mock(DeriverManager.class,"deriverManager");
//		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
//		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
//		final AdserverDataCache adserverDataCache = mock(AdserverDataCache.class,"adserverDataCache");
//        final PostalCodeIdManager postalCodeIdManager = mock(PostalCodeIdManager.class, "postalCodeIdManager");
//		
//		final TargetingContextFactoryImpl targetingContextFactoryImpl = new TargetingContextFactoryImpl(domainCacheManager,
//				adserverDataCacheManager,adserverDomainCacheManager, deriverManager, postalCodeIdManager);
//		
//        
//		expect(new Expectations() {{
//			oneOf (domainCacheManager).getCache();
//				will(returnValue(domainCache));
//			oneOf (adserverDomainCacheManager).getCache();
//				will(returnValue(adserverDomainCache));		
//			oneOf (adserverDataCacheManager).getCache();
//				will(returnValue(adserverDataCache));	
//		}});
//		
//		TargetingContext targetingContext = targetingContextFactoryImpl.createTargetingContext();
//	}
//    
//
//    @Ignore
//	@Test
//	public void testTargetingContextFactoryImpl02_TargetingContextFactoryImpl() throws InvalidIpAddressException{
//		final DomainCacheManager domainCacheManager = mock(DomainCacheManager.class,"domainCacheManager");
//		final AdserverDomainCacheManager adserverDomainCacheManager = mock(AdserverDomainCacheManager.class,"adserverDomainCacheManager");
//		final AdserverDataCacheManager adserverDataCacheManager = mock(AdserverDataCacheManagerImpl.class,"adserverDataCacheManager");
//		final AdserverDataCache adserverDataCache = mock(AdserverDataCache.class,"adserverDataCache");
//		final DeriverManager deriverManager = mock(DeriverManager.class,"deriverManager");
//		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
//		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
//        final PostalCodeIdManager postalCodeIdManager = mock(PostalCodeIdManager.class, "postalCodeIdManager");
//		
//		final TargetingContextFactoryImpl targetingContextFactoryImpl = new TargetingContextFactoryImpl(domainCacheManager, 
//				adserverDataCacheManager,  adserverDomainCacheManager, deriverManager, postalCodeIdManager);
//		
//		expect(new Expectations() {{
//			oneOf (domainCacheManager).getCache();
//				will(returnValue(domainCache));
//			oneOf (adserverDomainCacheManager).getCache();
//				will(returnValue(adserverDomainCache));
//			allowing (deriverManager).getDeriver(with(any(String.class)));
//				will(returnValue(null));
//			oneOf (adserverDataCacheManager).getCache();
//				will(returnValue(adserverDataCache));		
//		}});
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		TargetingContext targetingContext = targetingContextFactoryImpl.createTargetingContext(request, false);
//	}
}
