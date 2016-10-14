package com.adfonic.adserver.impl;

import com.adfonic.adserver.BaseAdserverTest;

//@Ignore("Tests fail")
public class TestTargetingContextImpl extends BaseAdserverTest{

//	private TargetingContext targetingContext;
//	private DomainCache domainCache; 
//	private AdserverDomainCache adserverDomainCache;
//	private DeriverManager deriverManager;
//    private PostalCodeIdManager postalCodeIdManager;
//	
//    private final long androidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long udidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long dpidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long odin1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long openudidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long hifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    private final long ifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
//    
//	@Before
//	public void init(){
//		domainCache = mock(DomainCache.class);
//		adserverDomainCache = mock(AdserverDomainCache.class);
//		deriverManager = mock(DeriverManager.class);
//        postalCodeIdManager = mock(PostalCodeIdManager.class);
//		
//		targetingContext = new TargetingContextImpl(domainCache, adserverDomainCache, deriverManager, postalCodeIdManager);
//		//Just to cover extra code, to see better covergare report :)
//		targetingContext.setAdSpace(null);
//		assertNull(targetingContext.getAdSpace());
//		//Expect same object which passed in constructor
//		assertTrue(domainCache == targetingContext.getDomainCache());
//		assertTrue(adserverDomainCache == targetingContext.getAdserverDomainCache());
//	}
//	
//    // These expectations are common to many of the unit test points in this class.
//    // By using a base class we can shrink the size of this class
//    private class CommonExpectations extends Expectations {{
//        allowing (deriverManager).getDeriver(with(any(String.class))); will(returnValue(null));
//    }};
//
//	/**
//	 * 
//	 * Populate targetingContext With an Empty request. Populating should work fine and then
//	 * check default values of few fields
//	 */
//	@Test
//	public void testTargetingContextImpl1() throws InvalidIpAddressException{
//        final String attrName = "Some";
//        
//        expect(new CommonExpectations());
//        
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,true);
//		
//		//Now try ot get some attributes and header. All should be null
//		assertNull(targetingContext.getAttribute(attrName));
//
//		assertNotNull(targetingContext.getAttributes());
//		//System.out.println(targetingContext.getAttributes());
//		assertEquals(3,targetingContext.getAttributes().size());
//
//		//Ip address will be there for emprty HttpServletRequest
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//
//		
//		assertNull(targetingContext.getAttribute("Some"));
//		assertNull(targetingContext.getCookie("SomeCookie"));
//		assertNull(targetingContext.getHeader("someHeader"));
//		//System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have two headers only and useHttpHeader as true
//	 * 
//	 */
//	@Test
//	public void testTargetingContextImpl2() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "headerName1";
//		String headerValue1 = "headerValue1";
//		String headerName2 = "headerName2";
//		String headerValue2 = "headerValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addHeader(headerName2, headerValue2);
//		boolean useHttpHeaders = true;
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		//System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(2,targetingContext.getHeaders().size());
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//		assertEquals(headerValue2,targetingContext.getHeader(headerName2));
//
//		assertEquals(headerValue1,targetingContext.getAttribute(Parameters.HTTP_HEADER_PREFIX+headerName1.toLowerCase()));
//		assertEquals(headerValue2,targetingContext.getAttribute(Parameters.HTTP_HEADER_PREFIX+headerName2.toLowerCase()));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getAttribute(headerName2));
//		//Also check they can not be get as cookies
//		assertNull(targetingContext.getCookie(headerName1));
//		assertNull(targetingContext.getCookie(headerName2));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		//System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	/**
//	 * 
//	 * Populate targetingContext With request have two headers only and useHttpHeader as false
//	 */
//	@Test
//	public void testTargetingContextImpl3() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "headerName1";
//		String headerValue1 = "headerValue1";
//		String headerName2 = "headerName2";
//		String headerValue2 = "headerValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addHeader(headerName2, headerValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		//System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		assertNull(targetingContext.getHeader(headerName1));
//		assertNull(targetingContext.getHeader(headerName2));
//
//		//3 default attributes r.ip etc and 2 added headers will not be counted
//		assertEquals(3,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getAttribute(headerName2));
//		//Also check they can not be get as cookies
//		assertNull(targetingContext.getCookie(headerName1));
//		assertNull(targetingContext.getCookie(headerName2));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters whose name starts with 
//	 * Parameters.HTTP_HEADER_PREFIX
//	 */
//	@Test
//	public void testTargetingContextImpl4() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "paramName1";
//		String headerName2 = "paramName2";
//		String paramName1 = Parameters.HTTP_HEADER_PREFIX+headerName1;
//		String paramValue1 = "paramValue1";
//		String paramName2 = Parameters.HTTP_HEADER_PREFIX+headerName2;
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(2,targetingContext.getHeaders().size());
//		assertEquals(paramValue1,targetingContext.getHeader(headerName1));
//		assertEquals(paramValue2,targetingContext.getHeader(headerName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1.toLowerCase()));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2.toLowerCase()));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getAttribute(headerName2));
//		//Also check they can not be get as cookies
//		assertNull(targetingContext.getCookie(headerName1));
//		assertNull(targetingContext.getCookie(headerName2));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters whose value is empty string "" 
//	 */
//	@Test
//	public void testTargetingContextImpl5() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//
//		assertNull(targetingContext.getAttribute(paramName1.toLowerCase()));
//
//		//3 default attributes r.ip etc and 1 added parameytre will not be counted
//		assertEquals(3,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getHeader(paramName1.toLowerCase()));
//		//Also check they can not be get as cookies
//		assertNull(targetingContext.getCookie(paramName1.toLowerCase()));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
////	/**
////	 * 
////	 * Populate targetingContext With request have parameters whose name do not starts with 
////	 * Parameters.HTTP_HEADER_PREFIX
////	 */
////	@Test
////	@Ignore("TODO:REWRITE HERO")
////	public void testTargetingContextImpl6() throws InvalidIpAddressException{
////        expect(new CommonExpectations());
////        
////		//Input data for this test case
////		String paramName1 = "paramName1";
////		String paramValue1 = "paramValue1";
////		String paramName2 = "paramName2";
////		String paramValue2 = "paramValue2";
////		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
////		mockHttpServletRequest.addParameter(paramName1, paramValue1);
////		mockHttpServletRequest.addParameter(paramName2, paramValue2);
////		boolean useHttpHeaders = false;		
////		//End input Data
////		
////		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
////		
////		System.out.println(targetingContext.getHeaders());
////		assertNotNull(targetingContext.getHeaders());
////		assertEquals(0,targetingContext.getHeaders().size());
////		assertNull(targetingContext.getHeader(paramName1));
////		assertNull(targetingContext.getHeader(paramName2));
////
////		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
////		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
////
////		//3 default attributes r.ip etc and 2 added headers
////		assertEquals(5,targetingContext.getAttributes().size());
////
////		
////
////		//Do normal test for attributes, just to test if header can interfare with attributes
////		//First check we can not get hears as attributes
////		assertNull(targetingContext.getCookie(paramName1));
////		assertNull(targetingContext.getCookie(paramName2));
////		//other generic checks
////		assertNotNull(targetingContext.getAttributes());
////		System.out.println(targetingContext.getAttributes());
////		assertNotNull(targetingContext.getAttribute(Parameters.IP));
////		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
////		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
////		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
////		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
////		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
////	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have cookies
//	 */
//	@Test
//	public void testTargetingContextImpl7() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String cookieName1 = "cookieName1";
//		String cookieValue1 = "cookieValue1";
//		String cookieName2 = "cookieName2";
//		String cookieValue2 = "cookieValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		Cookie cookie1 = new Cookie(cookieName1, cookieValue1);
//		Cookie cookie2 = new Cookie(cookieName2, cookieValue2);
//		mockHttpServletRequest.setCookies(cookie1,cookie2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		assertNull(targetingContext.getHeader(cookieName1));
//		assertNull(targetingContext.getHeader(cookieName2));
//
//		assertEquals(cookieValue1,targetingContext.getCookie(cookieName1));
//		assertEquals(cookieValue2,targetingContext.getCookie(cookieName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(cookieName1));
//		assertNull(targetingContext.getAttribute(cookieName2));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when useHttpheader is false and 
//	 * Parameters.HTTP_HEADER_PREFIX + "user-agent" is missing
//	 * and header "User-Agent" is provided
//	 */
//	@Test
//	public void testTargetingContextImpl8() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "User-Agent";
//		String headerValue1 = "Some UserAgent Value";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when useHttpheader is false and 
//	 * Parameters.HTTP_HEADER_PREFIX + "user-agent" is missing
//	 * and header "User-Agent" is provided and length is more then 512
//	 */
//	@Test
//	public void testTargetingContextImpl9() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "User-Agent";
//		String headerValue1 = randomAlphaNumericString(513);
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" si provided
//	 */
//	@Test
//	public void testTargetingContextImpl10() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String headerValue1 = "213.44.23.45";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(headerValue1,targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also pass r.ip parameter
//	 */
//	@Test
//	public void testTargetingContextImpl11() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String headerValue1 = "213.44.23.45";
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "21.55.23.45";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(headerValue1,targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals(parameterValue1,targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also pass r.ip parameter but invalid ike ipAddress1,ipAddress2
//	 */
//	@Test(expected=InvalidIpAddressException.class)
//	public void testTargetingContextImpl12() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String headerValue1 = "213.44.23.45";
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "21.55.23.45,213.44.23.45";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		//Exception will be thrown from above line
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also pass r.ip parameter but its IPV6
//	 */
//	@Test
//	public void testTargetingContextImpl13() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String headerValue1 = "213.44.23.45";
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "21:55:23:45:213:44";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		//Exception will be thrown from above line
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(headerValue1,targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals(parameterValue1,targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also pass r.ip parameter but its local IPV6
//	 */
//	@Test
//	public void testTargetingContextImpl14() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String headerValue1 = "213.44.23.45";
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "0:0:0:0:0:0:0:1%0";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//        boolean previousWorkaroundValue = TargetingContextImpl.ipv6LoopbackDevWorkaround;
//        TargetingContextImpl.ipv6LoopbackDevWorkaround = false;
//
//        try {
//            ((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//            assertNotNull(targetingContext.getHeaders());
//            //User agent will go as Header in lowercase
//            assertEquals(1,targetingContext.getHeaders().size());
//            assertNotNull(targetingContext.getHeader(headerName1));
//            assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//            //3 default attributes r.ip etc and 2 added headers
//            assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//            //Do normal test for attributes, just to test if header can interfare with attributes
//            //First check we can not get hears as attributes
//            assertNull(targetingContext.getAttribute(headerName1));
//            assertNull(targetingContext.getCookie(headerName1));
//            //other generic checks
//            assertNotNull(targetingContext.getAttributes());
//            //System.out.println(targetingContext.getAttributes());
//            assertNotNull(targetingContext.getAttribute(Parameters.IP));
//            assertEquals(headerValue1,targetingContext.getAttribute(Parameters.IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertEquals(parameterValue1,targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//            assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//        } finally {        
//            TargetingContextImpl.ipv6LoopbackDevWorkaround = previousWorkaroundValue;
//        }
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when r.ip=86.194.5.66%2F186.194.5.66 or 
//	 * r.ip=StreamUtils.java+-%3DERROR%3D- ..basically some crap
//	 */
//	@Test(expected=InvalidIpAddressException.class)
//	public void testTargetingContextImpl15() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "86.194.5.66%2F186.194.5.66";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		//Exception will be thrown from above line
//		
//		
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also its IPV6
//	 */
//	@Test
//	public void testTargetingContextImpl16_withIpv6LoopbackWorkaroundEnabled() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String ipv4IpAddress = "123.23.45.67";
//		String headerValue1 = "0:0:0:0:0:0:0:1%0," + ipv4IpAddress;
//        String expectedIpAddress = "127.0.0.1"; // When the workaround is enabled, we expect this back
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "21.55.33.44";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//
//        boolean previousWorkaroundValue = TargetingContextImpl.ipv6LoopbackDevWorkaround;
//        TargetingContextImpl.ipv6LoopbackDevWorkaround = true;
//
//        try {
//            ((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//            //Exception will be thrown from above line
//		
//            assertNotNull(targetingContext.getHeaders());
//            //User agent will go as Header in lowercase
//            assertEquals(1,targetingContext.getHeaders().size());
//            assertNotNull(targetingContext.getHeader(headerName1));
//            assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//            //3 default attributes r.ip etc and 2 added headers
//            assertEquals(4,targetingContext.getAttributes().size());
//
//            //Do normal test for attributes, just to test if header can interfare with attributes
//            //First check we can not get hears as attributes
//            assertNull(targetingContext.getAttribute(headerName1));
//            assertNull(targetingContext.getCookie(headerName1));
//            //other generic checks
//            assertNotNull(targetingContext.getAttributes());
//            //System.out.println(targetingContext.getAttributes());
//            assertNotNull(targetingContext.getAttribute(Parameters.IP));
//            assertEquals(expectedIpAddress,targetingContext.getAttribute(Parameters.IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertEquals(parameterValue1,targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//            assertFalse((Boolean)targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//        } finally {        
//            TargetingContextImpl.ipv6LoopbackDevWorkaround = previousWorkaroundValue;
//        }
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * also its IPV6
//	 */
//	@Test
//	public void testTargetingContextImpl16_withIpv6LoopbackWorkaroundDisabled() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String ipv4IpAddress = "123.23.45.67";
//		String headerValue1 = "0:0:0:0:0:0:0:1%0," + ipv4IpAddress;
//        String expectedIpAddress = ipv4IpAddress; // When the workaround is disabled, we expect the IPv4 address
//		String parameterName1 = Parameters.IP;
//		String parameterValue1 = "21.55.33.44";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		mockHttpServletRequest.addParameter(parameterName1, parameterValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//
//        boolean previousWorkaroundValue = TargetingContextImpl.ipv6LoopbackDevWorkaround;
//        TargetingContextImpl.ipv6LoopbackDevWorkaround = false;
//
//        try {
//            ((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//            //Exception will be thrown from above line
//		
//            assertNotNull(targetingContext.getHeaders());
//            //User agent will go as Header in lowercase
//            assertEquals(1,targetingContext.getHeaders().size());
//            assertNotNull(targetingContext.getHeader(headerName1));
//            assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//            //3 default attributes r.ip etc and 2 added headers
//            assertEquals(4,targetingContext.getAttributes().size());
//
//            //Do normal test for attributes, just to test if header can interfare with attributes
//            //First check we can not get hears as attributes
//            assertNull(targetingContext.getAttribute(headerName1));
//            assertNull(targetingContext.getCookie(headerName1));
//            //other generic checks
//            assertNotNull(targetingContext.getAttributes());
//            //System.out.println(targetingContext.getAttributes());
//            assertNotNull(targetingContext.getAttribute(Parameters.IP));
//            assertEquals(expectedIpAddress,targetingContext.getAttribute(Parameters.IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertEquals(parameterValue1,targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//            assertFalse((Boolean)targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//        } finally {        
//            TargetingContextImpl.ipv6LoopbackDevWorkaround = previousWorkaroundValue;
//        }
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Deriver is registered for one attribute
//	 */
//	@Test
//	public void testGetAttribute17_using_deriver_invoke_once_only_non_null() throws InvalidIpAddressException{
//        final Deriver deriver = mock(Deriver.class);
//        final Gender gender = Gender.MALE;
//        
//        expect(new Expectations() {{
//            allowing (deriverManager).getDeriver(with(TargetingContext.GENDER)); will(returnValue(deriver));
//            allowing (deriverManager).getDeriver(with(not(equalTo(TargetingContext.GENDER)))); will(returnValue(null));
//            allowing (deriver).canDeriveMoreThanOnce(with(any(String.class))); will(returnValue(false));
//            oneOf (deriver).getAttribute(TargetingContext.GENDER, targetingContext); will(returnValue(gender));
//        }});
//
//		assertEquals(gender, targetingContext.getAttribute(TargetingContext.GENDER));
//        
//        // Call it again...it shouldn't invoke the deriver again
//		assertEquals(gender, targetingContext.getAttribute(TargetingContext.GENDER));
//	}
//    
//	/**
//	 * 
//	 * Populate targetingContext, when Deriver is registered for one attribute
//	 */
//	@Test
//	public void testGetAttribute17b_using_deriver_invoke_once_only_null() throws InvalidIpAddressException{
//        final Deriver deriver = mock(Deriver.class);
//        
//        expect(new Expectations() {{
//            allowing (deriverManager).getDeriver(with(TargetingContext.GENDER)); will(returnValue(deriver));
//            allowing (deriverManager).getDeriver(with(not(equalTo(TargetingContext.GENDER)))); will(returnValue(null));
//            allowing (deriver).canDeriveMoreThanOnce(with(any(String.class))); will(returnValue(false));
//            oneOf (deriver).getAttribute(TargetingContext.GENDER, targetingContext); will(returnValue(null));
//        }});
//
//		assertNull(targetingContext.getAttribute(TargetingContext.GENDER));
//		assertNull(targetingContext.getAttribute(TargetingContext.GENDER));
//		assertNull(targetingContext.getAttribute(TargetingContext.GENDER));
//	}
//    
//	/**
//	 * 
//	 * Populate targetingContext, when Deriver is registered for one attribute
//	 */
//	@Test
//	public void testGetAttribute17c_using_deriver_invoke_multiple_times() throws InvalidIpAddressException{
//        final Deriver deriver = mock(Deriver.class);
//        final Gender gender = Gender.MALE;
//        
//        expect(new Expectations() {{
//            allowing (deriverManager).getDeriver(with(TargetingContext.GENDER)); will(returnValue(deriver));
//            allowing (deriverManager).getDeriver(with(not(equalTo(TargetingContext.GENDER)))); will(returnValue(null));
//            allowing (deriver).canDeriveMoreThanOnce(with(any(String.class))); will(returnValue(true));
//            oneOf (deriver).getAttribute(TargetingContext.GENDER, targetingContext); will(returnValue(null));
//            oneOf (deriver).getAttribute(TargetingContext.GENDER, targetingContext); will(returnValue(null));
//            oneOf (deriver).getAttribute(TargetingContext.GENDER, targetingContext); will(returnValue(gender));
//        }});
//
//		assertNull(targetingContext.getAttribute(TargetingContext.GENDER));
//		assertNull(targetingContext.getAttribute(TargetingContext.GENDER));
//		assertEquals(gender, targetingContext.getAttribute(TargetingContext.GENDER));
//	}
//    
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setAttribute on 
//	 * targetingContext
//	 */
//	@Test
//	public void testTargetingContextImpl19() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//        mockHttpServletRequest.addParameter("h.user-agent", "whatever"); // for code coverage
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(1, targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		System.out.println(targetingContext.getAttributes());
//		//3 default attributes r.ip etc, plus effective user agent, plus 3 added headers
//		assertEquals(7,targetingContext.getAttributes().size());
//
//		
//		targetingContext.setAttribute(paramName1, null);
//		assertNull(targetingContext.getAttribute(paramName1));
//		assertNull(targetingContext.getAttribute(paramName2));
//		
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters 
//	 * and check if its test mode when testMode param is 1
//	 */
//	@Test
//	public void testTargetingContextImpl20() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = Parameters.TEST_MODE;
//		String paramValue1 = "1";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertTrue(targetingContext.isTestMode());
//		
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters 
//	 * and check if its test mode, when testMode param is 0
//	 */
//	@Test
//	public void testTargetingContextImpl21() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = Parameters.TEST_MODE;
//		String paramValue1 = "0";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertFalse(targetingContext.isTestMode());
//		
//	}
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters 
//	 * and check if its test mode, when testMode param is missing
//	 */
//	@Test
//	public void testTargetingContextImpl22() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "dfsdfsd";
//		String paramValue1 = "0";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertFalse(targetingContext.isTestMode());
//		
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setIpAddress on 
//	 * targetingContext with some externalId Address
//	 */
//	@Test
//	public void testTargetingContextImpl23() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		//SOme external IP address
//		String newIpAddress = "188.99.99.99";
//		targetingContext.setIpAddress(newIpAddress);
//		
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(newIpAddress, targetingContext.getAttribute(Parameters.IP));
//	}
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setIpAddress on 
//	 * targetingContext with some internal IP Address
//	 */
//	@Test
//	public void testTargetingContextImpl24() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		//SOme internal IP address
//		String newIpAddress = "192.168.0.5";
//		targetingContext.setIpAddress(newIpAddress);
//		
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(newIpAddress, targetingContext.getAttribute(Parameters.IP));
//		assertTrue((Boolean)targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setIpAddress on 
//	 * targetingContext with some junk IP Address
//	 */
//	@Test(expected=InvalidIpAddressException.class)
//	public void testTargetingContextImpl25() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		//SOme internal IP address
//		String newIpAddress = randomAlphaNumericString(19);
//		targetingContext.setIpAddress(newIpAddress);
//		//Above line will throw exception
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setUserAgent on 
//	 * targetingContext with some user agent
//	 */
//	@Test
//	public void testTargetingContextImpl26() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		String newUserAgent = randomAlphaNumericString(19);
//		targetingContext.setUserAgent(newUserAgent);
//		
//		assertNotNull(targetingContext.getEffectiveUserAgent());
//		assertEquals(newUserAgent, targetingContext.getEffectiveUserAgent());
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setUserAgent on 
//	 * targetingContext with some user agent withlength more then 512
//	 */
//	@Test
//	public void testTargetingContextImpl27() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		String newUserAgent = randomAlphaNumericString(513);
//		targetingContext.setUserAgent(newUserAgent);
//		
//		assertNotNull(targetingContext.getEffectiveUserAgent());
//		assertEquals(newUserAgent, targetingContext.getEffectiveUserAgent());
//	}
//	/**
//	 * 
//	 * Populate targetingContext With request have parameters and then call setUserAgent on 
//	 * targetingContext with some user agent withlength more then 512 and it has repiition
//	 */
//	@Test
//	public void testTargetingContextImpl28() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String paramName1 = "paramName1";
//		String paramValue1 = "paramValue1";
//		String paramName2 = "_a.paramName1";
//		String paramValue2 = "paramValue2";
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addParameter(paramName1, paramValue1);
//		mockHttpServletRequest.addParameter(paramName2, paramValue2);
//		boolean useHttpHeaders = false;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		System.out.println(targetingContext.getHeaders());
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//		
//		assertNull(targetingContext.getHeader(paramName1));
//		assertNull(targetingContext.getHeader(paramName2));
//		assertNotNull(targetingContext.getAttribute(paramName1));
//		assertNotNull(targetingContext.getAttribute(paramName2));
//
//		assertEquals(paramValue1,targetingContext.getAttribute(paramName1));
//		assertEquals(paramValue2,targetingContext.getAttribute(paramName2));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(5,targetingContext.getAttributes().size());
//
//		String newUserAgent = "SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1";
//		String finalUserAgent = newUserAgent+",SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1";
//		targetingContext.setUserAgent(finalUserAgent);
//		
//		assertNotNull(targetingContext.getEffectiveUserAgent());
//		assertEquals(newUserAgent, targetingContext.getEffectiveUserAgent());
//	}
////	/**
////	 * 
////	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
////	 * with IPV6
////	 */
////	@Test
////	@Ignore("TODO:REWRITE HERO")
////	public void testTargetingContextImpl29() throws InvalidIpAddressException{
////        expect(new CommonExpectations());
////        		
////		//Input data for this test case
////		String headerName1 = "x-forwarded-for";
////		String correctHeaderValue = "213.44.23.45";
////		String headerValue1 = "0:0:0:0:0:0:0:1%0,"+correctHeaderValue;
////		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
////		mockHttpServletRequest.addHeader(headerName1, headerValue1);
////		boolean useHttpHeaders = true;		
////		//End input Data
////		
////        boolean previousWorkaroundValue = TargetingContextImpl.ipv6LoopbackDevWorkaround;
////        TargetingContextImpl.ipv6LoopbackDevWorkaround = false;
////
////        try {
////            ((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
////		
////            assertNotNull(targetingContext.getHeaders());
////            //User agent will go as Header in lowercase
////            assertEquals(1,targetingContext.getHeaders().size());
////            assertNotNull(targetingContext.getHeader(headerName1));
////            assertEquals(headerValue1,targetingContext.getHeader(headerName1));
////
////            //3 default attributes r.ip etc and 2 added headers
////            assertEquals(4,targetingContext.getAttributes().size());
////
////		
////
////            //Do normal test for attributes, just to test if header can interfare with attributes
////            //First check we can not get hears as attributes
////            assertNull(targetingContext.getAttribute(headerName1));
////            assertNull(targetingContext.getCookie(headerName1));
////            //other generic checks
////            assertNotNull(targetingContext.getAttributes());
////            //System.out.println(targetingContext.getAttributes());
////            assertNotNull(targetingContext.getAttribute(Parameters.IP));
////            assertEquals(correctHeaderValue,targetingContext.getAttribute(Parameters.IP));
////            assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
////            assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
////            assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
////            assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
////        } finally {        
////            TargetingContextImpl.ipv6LoopbackDevWorkaround = previousWorkaroundValue;
////        }
////	}
//	
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * with IPV6 like fe80:0:0:0:0204:61ff:254.157.241.86
//	 */
//	@Test
//	public void testTargetingContextImpl30() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String correctHeaderValue = "254.157.241.86";
//		String headerValue1 = "fe80:0:0:0:0204:61ff:"+correctHeaderValue;
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//        boolean previousWorkaroundValue = TargetingContextImpl.ipv6LoopbackDevWorkaround;
//        TargetingContextImpl.ipv6LoopbackDevWorkaround = false;
//
//        try {
//            ((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//            assertNotNull(targetingContext.getHeaders());
//            //User agent will go as Header in lowercase
//            assertEquals(1,targetingContext.getHeaders().size());
//            assertNotNull(targetingContext.getHeader(headerName1));
//            assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//            //3 default attributes r.ip etc and 2 added headers
//            assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//            //Do normal test for attributes, just to test if header can interfare with attributes
//            //First check we can not get hears as attributes
//            assertNull(targetingContext.getAttribute(headerName1));
//            assertNull(targetingContext.getCookie(headerName1));
//            //other generic checks
//            assertNotNull(targetingContext.getAttributes());
//            //System.out.println(targetingContext.getAttributes());
//            assertNotNull(targetingContext.getAttribute(Parameters.IP));
//            assertEquals(correctHeaderValue,targetingContext.getAttribute(Parameters.IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//            assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//            assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//        } finally {        
//            TargetingContextImpl.ipv6LoopbackDevWorkaround = previousWorkaroundValue;
//        }
//	}
//	/**
//	 * 
//	 * Populate targetingContext, when Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for" is provided
//	 * with some crap Ip like aildm.zahid.com/212.119.69.187
//	 */
//	@Test
//	public void testTargetingContextImpl31() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		String headerName1 = "x-forwarded-for";
//		String correctHeaderValue = "212.119.69.187";
//		String headerValue1 = "aildm.zahid.com/"+correctHeaderValue;
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.addHeader(headerName1, headerValue1);
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		//User agent will go as Header in lowercase
//		assertEquals(1,targetingContext.getHeaders().size());
//		assertNotNull(targetingContext.getHeader(headerName1));
//		assertEquals(headerValue1,targetingContext.getHeader(headerName1));
//
//		//3 default attributes r.ip etc and 2 added headers
//		assertEquals(4,targetingContext.getAttributes().size());
//
//		
//
//		//Do normal test for attributes, just to test if header can interfare with attributes
//		//First check we can not get hears as attributes
//		assertNull(targetingContext.getAttribute(headerName1));
//		assertNull(targetingContext.getCookie(headerName1));
//		//other generic checks
//		assertNotNull(targetingContext.getAttributes());
//		System.out.println(targetingContext.getAttributes());
//		assertNotNull(targetingContext.getAttribute(Parameters.IP));
//		assertEquals(correctHeaderValue,targetingContext.getAttribute(Parameters.IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertEquals("127.0.0.1",targetingContext.getAttribute(TargetingContext.PROVIDED_IP));
//		assertNotNull(targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//		assertEquals(false,targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	/**
//	 * 
//	 * Populate targetingContext, when all ip addresses are local
//	 */
//	@Test
//	public void testTargetingContextImpl32() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		mockHttpServletRequest.setRemoteAddr("192.168.1.8");
//		boolean useHttpHeaders = true;		
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//
//		//3 default attributes r.ip etc 
//		assertEquals(3,targetingContext.getAttributes().size());
//		assertTrue((Boolean)targetingContext.getAttribute(TargetingContext.IS_PRIVATE_NETWORK));
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext with impression
//	 */
//	@Test
//	public void testTargetingContextImpl33() throws InvalidIpAddressException{
//        Map<Long,String> deviceIdentifiersByType = new LinkedHashMap<Long,String>();
//        final long ditId1 = uniqueLong("ditId");
//        String deviceIdentifier1 = uniqueAlphaNumericString(10, "deviceIdentifier");
//        final long ditId2 = uniqueLong("ditId");
//        String deviceIdentifier2 = uniqueAlphaNumericString(10, "deviceIdentifier");
//        final long ditId3 = uniqueLong("ditId");
//        String deviceIdentifier3 = uniqueAlphaNumericString(10, "deviceIdentifier");
//        deviceIdentifiersByType.put(ditId1, deviceIdentifier1);
//        deviceIdentifiersByType.put(ditId2, deviceIdentifier2);
//        deviceIdentifiersByType.put(ditId3, deviceIdentifier3);
//        targetingContext.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, deviceIdentifiersByType);
//
//        final DeviceIdentifierTypeDto dit1 = mock(DeviceIdentifierTypeDto.class, "dit1");
//        final DeviceIdentifierTypeDto dit2 = mock(DeviceIdentifierTypeDto.class, "dit2");
//        final DeviceIdentifierTypeDto dit3 = mock(DeviceIdentifierTypeDto.class, "dit3");
//		
//        expect(new CommonExpectations() {{
//            allowing (domainCache).getDeviceIdentifierTypeById(ditId1); will(returnValue(dit1));
//            allowing (dit1).isSecure(); will(returnValue(true));
//            allowing (domainCache).getDeviceIdentifierTypeById(ditId2); will(returnValue(dit2));
//            allowing (dit2).isSecure(); will(returnValue(false));
//            allowing (domainCache).getDeviceIdentifierTypeById(ditId3); will(returnValue(dit3));
//            allowing (dit3).isSecure(); will(returnValue(true));
//        }});
//        
//		//Input data for this test case
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		boolean useHttpHeaders = true;
//		Impression impression = new Impression();
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//
//		//4 default attributes r.ip etc 
//		assertEquals(4,targetingContext.getAttributes().size());
//		VendorDto vendor = new VendorDto();
//        vendor.setName("Test");
//		ModelDto model = new ModelDto();
//        model.setVendor(vendor);
//        model.setName("TestModel");
//		targetingContext.setAttribute(TargetingContext.MODEL, model);
//		CountryDto country = new CountryDto();
//        country.setName("UK");
//        country.setIsoCode("UK");
//		targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//		
//		OperatorDto operator = new OperatorDto();
//        operator.setName("O2");
//        operator.setCountryIsoCode(country.getIsoCode());
//		targetingContext.setAttribute(TargetingContext.OPERATOR, operator);
//		
//		Coordinates coordinates = new Coordinates() {
//			@Override
//			public double getLongitude() {
//				return 0;
//			}
//			
//			@Override
//			public double getLatitude() {
//				return 0;
//			}
//		};
//		targetingContext.setAttribute(TargetingContext.COORDINATES, coordinates);
//		
//		
//		GeotargetDto geotarget = new GeotargetDto();
//        geotarget.setName("EU");
//        geotarget.setCountryIsoCode(country.getIsoCode());
//        geotarget.setType(Type.DMA);
//        geotarget.setDisplayLatitude(2.5);
//        geotarget.setDisplayLongitude(4.6);
//		targetingContext.setAttribute(TargetingContext.GEOTARGET, geotarget);
//		
//		IntegrationTypeDto integrationType = new IntegrationTypeDto();
//        integrationType.setName("SomeType");
//        integrationType.setSystemName("name");
//		targetingContext.setAttribute(TargetingContext.INTEGRATION_TYPE, integrationType);
//
//        String secureTrackingId = randomAlphaNumericString(10);
//        targetingContext.setAttribute(TargetingContext.SECURE_TRACKING_ID, secureTrackingId);
//
//		targetingContext.populateImpression(impression, null);
//
//        assertEquals(secureTrackingId, impression.getTrackingIdentifier());
//        assertEquals(2, impression.getDeviceIdentifiers().size());
//        assertTrue(impression.getDeviceIdentifiers().containsKey(ditId1));
//        assertFalse(impression.getDeviceIdentifiers().containsKey(ditId2));
//        assertTrue(impression.getDeviceIdentifiers().containsKey(ditId3));
//	}
//	/**
//	 * 
//	 * Populate targetingContext with impression and other params as null
//	 */
//	@Test
//	public void testTargetingContextImpl34() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		boolean useHttpHeaders = true;
//		Impression impression = new Impression();
//
//        AdvertiserDto advertiser = new AdvertiserDto();
//        advertiser.setId(382L);
//		CampaignDto campaign = new CampaignDto();
//        campaign.setId(123L);
//        campaign.setAdvertiser(advertiser);
//        SegmentDto segment = new SegmentDto();
//        segment.setId(938L);
//		CreativeDto creative = new CreativeDto();
//        creative.setId(234L);
//        creative.setCampaign(campaign);
//        creative.setSegment(segment);
//
//		@SuppressWarnings("serial")
//		ProxiedDestination pd = new ProxiedDestination() {
//			@Override public void setFormat(String format) {}
//			@Override public void setDestinationUrl(String destinationUrl) {}
//			@Override public void setDestinationType(DestinationType destinationType) {}
//			@Override public String getFormat() {return null;}
//			@Override public String getDestinationUrl() {return "TestUrl";}
//			@Override public DestinationType getDestinationType() {return null;}
//			@Override public Map<String, Map<String, String>> getComponents() {return null;}
//		};
//
//		AdSpaceDto adspace = new AdSpaceDto();
//		adspace.setId(randomLong());
//        MutableWeightedCreative mwc = new MutableWeightedCreative(adspace, creative);
//		SelectedCreative selectedCreative = new SelectedCreative(mwc, pd);
//
//		//End input Data
//		
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//
//		//3 default attributes r.ip etc 
//		assertEquals(3,targetingContext.getAttributes().size());
//		
//		
//		targetingContext.populateImpression(impression, selectedCreative);
//	}
//	
//	/**
//	 * 
//	 * Populate targetingContext and test populateAdEvent
//	 */
//	@Test
//	public void testTargetingContextImpl35() throws InvalidIpAddressException{
//        expect(new CommonExpectations());
//        
//		//Input data for this test case
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		boolean useHttpHeaders = true;
//		Impression impression = new Impression();
//		AdEvent event = new AdEvent();
//		//End input Data
//		((TargetingContextImpl)targetingContext).populateAttributes(mockHttpServletRequest,useHttpHeaders);
//		
//		assertNotNull(targetingContext.getHeaders());
//		assertEquals(0,targetingContext.getHeaders().size());
//
//		//3 default attributes r.ip etc 
//		assertEquals(3,targetingContext.getAttributes().size());
//
//        AdvertiserDto advertiser = new AdvertiserDto();
//        advertiser.setId(382L);
//		CampaignDto campaign = new CampaignDto();
//        campaign.setId(123L);
//        campaign.setAdvertiser(advertiser);
//        SegmentDto segment = new SegmentDto();
//        segment.setId(938L);
//		CreativeDto creative = new CreativeDto();
//        creative.setId(234L);
//        creative.setCampaign(campaign);
//        creative.setSegment(segment);
//
//        PublisherDto publisher = new PublisherDto();
//        publisher.setId(666L);
//        PublicationDto publication = new PublicationDto();
//        publication.setId(777L);
//        publication.setPublisher(publisher);
//        AdSpaceDto adSpace = new AdSpaceDto();
//        adSpace.setId(888L);
//        adSpace.setPublication(publication);
//        
//		targetingContext.setAdSpace(adSpace);
//		
//		targetingContext.populateAdEvent(event, impression, creative);
//	}
//
//    @Test
//    public void testDeriveIpAddress_x_forwarded_for_unknown() throws Exception {
//        expect(new CommonExpectations());
//        
//        targetingContext.setAttribute(Parameters.IP, "123.45.67.89");
//        targetingContext.setAttribute(Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for", "unknown");
//        ((TargetingContextImpl)targetingContext).deriveIpAddress(null); // the HttpServletRequest isn't used
//    }
//
//    @Test
//    public void testPopulateImpression01_no_device_identifiers() {
//        expect(new CommonExpectations());
//        
//        @SuppressWarnings("serial")
//		final Map<Long,String> derivedDeviceIdentifiers = new HashMap<Long,String>() {{
//            }};
//        
//        targetingContext.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, derivedDeviceIdentifiers);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//        assertTrue(impression.getDeviceIdentifiers().isEmpty());
//    }
//
//    @Test
//    public void testPopulateImpression02_device_identifiers_filtered() {
//        final String secureAndroid = randomHexString(40);
//        final String secureUdid = randomHexString(40);
//        final String dpid = randomHexString(40);
//        final String odin1 = randomHexString(40);
//        final String openudid = randomHexString(40);
//        final String secureIfa = randomHexString(32);
//        final String hifa = randomHexString(40);
//        @SuppressWarnings("serial")
//		final Map<Long,String> derivedDeviceIdentifiers = new HashMap<Long,String>() {{
//                put(androidDeviceIdentifierTypeId, secureAndroid);
//                put(udidDeviceIdentifierTypeId, secureUdid);
//                put(dpidDeviceIdentifierTypeId, dpid);
//                put(odin1DeviceIdentifierTypeId, odin1);
//                put(openudidDeviceIdentifierTypeId, openudid);
//                put(ifaDeviceIdentifierTypeId, secureIfa);
//                put(hifaDeviceIdentifierTypeId, hifa);
//            }};
//
//        final DeviceIdentifierTypeDto androidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "androidDeviceIdentifierType");
//        final DeviceIdentifierTypeDto udidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "udidDeviceIdentifierType");
//        final DeviceIdentifierTypeDto dpidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "dpidDeviceIdentifierType");
//        final DeviceIdentifierTypeDto odin1DeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "odin1DeviceIdentifierType");
//        final DeviceIdentifierTypeDto openudidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "openudidDeviceIdentifierType");
//        final DeviceIdentifierTypeDto hifaDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "hifaDeviceIdentifierType");
//        final DeviceIdentifierTypeDto ifaDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "ifaDeviceIdentifierType");
//        
//        expect(new CommonExpectations() {{
//            oneOf (domainCache).getDeviceIdentifierTypeById(androidDeviceIdentifierTypeId); will(returnValue(androidDeviceIdentifierType));
//            allowing (androidDeviceIdentifierType).isSecure(); will(returnValue(false));
//            oneOf (domainCache).getDeviceIdentifierTypeById(udidDeviceIdentifierTypeId); will(returnValue(udidDeviceIdentifierType));
//            allowing (udidDeviceIdentifierType).isSecure(); will(returnValue(false));
//            oneOf (domainCache).getDeviceIdentifierTypeById(dpidDeviceIdentifierTypeId); will(returnValue(dpidDeviceIdentifierType));
//            allowing (dpidDeviceIdentifierType).isSecure(); will(returnValue(true));
//            oneOf (domainCache).getDeviceIdentifierTypeById(odin1DeviceIdentifierTypeId); will(returnValue(odin1DeviceIdentifierType));
//            allowing (odin1DeviceIdentifierType).isSecure(); will(returnValue(true));
//            oneOf (domainCache).getDeviceIdentifierTypeById(openudidDeviceIdentifierTypeId); will(returnValue(openudidDeviceIdentifierType));
//            allowing (openudidDeviceIdentifierType).isSecure(); will(returnValue(true));
//            oneOf (domainCache).getDeviceIdentifierTypeById(ifaDeviceIdentifierTypeId); will(returnValue(ifaDeviceIdentifierType));
//            allowing (ifaDeviceIdentifierType).isSecure(); will(returnValue(false));
//            oneOf (domainCache).getDeviceIdentifierTypeById(hifaDeviceIdentifierTypeId); will(returnValue(hifaDeviceIdentifierType));
//            allowing (hifaDeviceIdentifierType).isSecure(); will(returnValue(true));
//        }});
//
//        targetingContext.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, derivedDeviceIdentifiers);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//
//        assertEquals(4, impression.getDeviceIdentifiers().size());
//        assertFalse(impression.getDeviceIdentifiers().containsKey(androidDeviceIdentifierTypeId));
//        assertFalse(impression.getDeviceIdentifiers().containsKey(udidDeviceIdentifierTypeId));
//        assertFalse(impression.getDeviceIdentifiers().containsKey(ifaDeviceIdentifierTypeId));
//        assertEquals(dpid, impression.getDeviceIdentifiers().get(dpidDeviceIdentifierTypeId));
//        assertEquals(odin1, impression.getDeviceIdentifiers().get(odin1DeviceIdentifierTypeId));
//        assertEquals(openudid, impression.getDeviceIdentifiers().get(openudidDeviceIdentifierTypeId));
//        assertEquals(hifa, impression.getDeviceIdentifiers().get(hifaDeviceIdentifierTypeId));
//    }
//
//    @Test
//    public void testPopulateImpression03_device_identifiers_no_filtering_required() {
//        final String openudid = randomHexString(40);
//        @SuppressWarnings("serial")
//		final Map<Long,String> derivedDeviceIdentifiers = new HashMap<Long,String>() {{
//                put(openudidDeviceIdentifierTypeId, openudid);
//            }};
//        
//        final DeviceIdentifierTypeDto openudidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "openudidDeviceIdentifierType");
//        
//        expect(new CommonExpectations() {{
//            oneOf (domainCache).getDeviceIdentifierTypeById(openudidDeviceIdentifierTypeId); will(returnValue(openudidDeviceIdentifierType));
//            allowing (openudidDeviceIdentifierType).isSecure(); will(returnValue(true));
//        }});
//
//        targetingContext.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, derivedDeviceIdentifiers);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//
//        assertEquals(1, impression.getDeviceIdentifiers().size());
//        assertEquals(openudid, impression.getDeviceIdentifiers().get(openudidDeviceIdentifierTypeId));
//    }
//
//    @Test
//    public void testPopulateImpression04_AF_1305_postal_code_not_geolocatable() {
//        final CountryDto country = mock(CountryDto.class);
//        final String postalCode = randomAlphaNumericString(10);
//
//        expect(new CommonExpectations() {{
//            allowing (country).getId(); will(returnValue(randomLong()));
//        }});
//        
//        targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//        targetingContext.setAttribute(TargetingContext.GEOLOCATABLE, Boolean.FALSE);
//        
//        // Set POSTAL_CODE, but we don't expect it to be used
//        targetingContext.setAttribute(TargetingContext.POSTAL_CODE, postalCode);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//
//        assertNull(impression.getPostalCodeId());
//    }
//
//    @Test
//    public void testPopulateImpression05_AF_1305_postal_code_geolocatable() {
//        final CountryDto country = mock(CountryDto.class);
//        final String postalCode = randomAlphaNumericString(10);
//        final String countryIsoCode = randomAlphaNumericString(10);
//        final Long postalCodeId = randomLong();
//
//        expect(new CommonExpectations() {{
//            allowing (country).getId(); will(returnValue(randomLong()));
//            oneOf (country).getIsoCode(); will(returnValue(countryIsoCode));
//            oneOf (postalCodeIdManager).getPostalCodeId(countryIsoCode, postalCode); will(returnValue(postalCodeId));
//        }});
//        
//        targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//        targetingContext.setAttribute(TargetingContext.GEOLOCATABLE, Boolean.TRUE);
//        targetingContext.setAttribute(TargetingContext.POSTAL_CODE, postalCode);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//        assertEquals(postalCodeId, impression.getPostalCodeId());
//    }
//
//    @Test
//    public void testPopulateImpressionOperatorCountryMismatch() {
//        final CountryDto country = mock(CountryDto.class);
//        final OperatorDto operator = mock(OperatorDto.class);
//        final Long countryId = randomLong();
//        final String countryIsoCode = randomAlphaNumericString(10);
//        final String operatorCountryIsoCode = randomAlphaNumericString(10);
//
//        expect(new CommonExpectations() {{
//            allowing (country).getId(); will(returnValue(countryId));
//            oneOf (country).getIsoCode(); will(returnValue(countryIsoCode));
//            allowing (operator).getCountryIsoCode(); will(returnValue(operatorCountryIsoCode));
//        }});
//        
//        targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//        targetingContext.setAttribute(TargetingContext.OPERATOR, operator);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//        assertThat(impression.getOperatorId(), nullValue());
//        assertThat(impression.getCountryId(), equalTo(countryId));
//    }
//    
//    @Test
//    public void testPopulateImpressionOperatorCountryMatch() {
//        final CountryDto country = mock(CountryDto.class);
//        final OperatorDto operator = mock(OperatorDto.class);
//        final Long countryId = randomLong();
//        final String anIsoCode = "111";
//        final String anotherIsoCode = "111";
//        final Long operatorId = randomLong();
//
//        expect(new CommonExpectations() {{
//            allowing (country).getId(); will(returnValue(countryId));
//            oneOf (country).getIsoCode(); will(returnValue(anIsoCode));
//            allowing (operator).getCountryIsoCode(); will(returnValue(anotherIsoCode));
//            oneOf (operator).getId(); will(returnValue(operatorId));
//        }});
//        
//        targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//        targetingContext.setAttribute(TargetingContext.OPERATOR, operator);
//
//        Impression impression = new Impression();
//        targetingContext.populateImpression(impression, null);
//        assertThat(impression.getOperatorId(), equalTo(operatorId));
//        assertThat(impression.getCountryId(), equalTo(countryId));
//    }
//
//	@Test
//	@SuppressWarnings("unchecked")
//    public void testPopulateImpression_all_possible_attributes() {
//        final Impression impression = mock(Impression.class);
//        final SelectedCreative selectedCreative = mock(SelectedCreative.class);
//        final CreativeDto creative = mock(CreativeDto.class, "creative");
//        final long creativeId = randomLong();
//        final ProxiedDestination pd = mock(ProxiedDestination.class, "pd");
//        final String pdDestinationUrl = randomUrl();
//        final boolean testMode = false;
//        final String secureTrackingId = randomAlphaNumericString(10);
//        final long udidTypeId = uniqueLong("ditId");
//        final DeviceIdentifierTypeDto udidType = mock(DeviceIdentifierTypeDto.class, "udidType");
//        final String udid = randomHexString(40);
//        final long dpidTypeId = uniqueLong("ditId");
//        final DeviceIdentifierTypeDto dpidType = mock(DeviceIdentifierTypeDto.class, "dpidType");
//        final String dpid = randomHexString(40);
//        final ModelDto model = mock(ModelDto.class, "model");
//        final long modelId = randomLong();
//        final CountryDto country = mock(CountryDto.class, "country");
//        final long countryId = randomLong();
//        final String countryIsoCode = randomAlphaNumericString(2);
//        final String postalCode = randomAlphaNumericString(10);
//        final long postalCodeId = randomLong();
//        final OperatorDto operator = mock(OperatorDto.class, "operator");
//        final long operatorId = randomLong();
//        final GeotargetDto geotarget = mock(GeotargetDto.class, "geotarget");
//        final long geotargetId = randomLong();
//        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class, "integrationType");
//        final long integrationTypeId = randomLong();
//        final Range<Integer> ageRange = new Range<Integer>(20, 30);
//        final Gender gender = Gender.MALE;
//        final String host = HostUtils.getHostName();
//        final TimeZone timeZone = TimeZone.getDefault();
//        final Date creationTime = new Date();
//        final String userTimeZoneId = timeZone.getID();
//        final Date dateOfBirth = org.apache.commons.lang.time.DateUtils.addYears(new Date(), -38);
//        final Coordinates coordinates = mock(Coordinates.class);
//        final double latitude = 34.12345;
//        final double longitude = -84.12345;
//        final LocationSource locationSource = LocationSource.DERIVED;
//
//        @SuppressWarnings("serial")
//		final Map<Long,String> suppliedDeviceIdentifiers = new HashMap<Long,String>() {{
//                put(udidTypeId, udid);
//                put(dpidTypeId, dpid);
//            }};
//
//        targetingContext.setAttribute(Parameters.TEST_MODE, "0");
//        targetingContext.setAttribute(TargetingContext.SECURE_TRACKING_ID, secureTrackingId);
//        targetingContext.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, suppliedDeviceIdentifiers);
//        targetingContext.setAttribute(TargetingContext.MODEL, model);
//        targetingContext.setAttribute(TargetingContext.COUNTRY, country);
//        targetingContext.setAttribute(TargetingContext.GEOLOCATABLE, Boolean.TRUE);
//        targetingContext.setAttribute(TargetingContext.POSTAL_CODE, postalCode);
//        targetingContext.setAttribute(TargetingContext.OPERATOR, operator);
//        targetingContext.setAttribute(TargetingContext.GEOTARGET, geotarget);
//        targetingContext.setAttribute(TargetingContext.INTEGRATION_TYPE, integrationType);
//        targetingContext.setAttribute(TargetingContext.AGE_RANGE, ageRange);
//        targetingContext.setAttribute(TargetingContext.GENDER, gender);
//        targetingContext.setAttribute(TargetingContext.TIME_ZONE, timeZone);
//        targetingContext.setAttribute(TargetingContext.DATE_OF_BIRTH, dateOfBirth);
//        targetingContext.setAttribute(TargetingContext.COORDINATES, coordinates);
//        targetingContext.setAttribute(TargetingContext.LOCATION_SOURCE, locationSource);
//
//        expect(new Expectations() {{
//            allowing (selectedCreative).getCreative(); will(returnValue(creative));
//            allowing (creative).getId(); will(returnValue(creativeId));
//            oneOf (impression).setCreativeId(creativeId);
//            
//            allowing (selectedCreative).getProxiedDestination(); will(returnValue(pd));
//            allowing (pd).getDestinationUrl(); will(returnValue(pdDestinationUrl));
//            oneOf (impression).setPdDestinationUrl(pdDestinationUrl);
//
//            oneOf (impression).setTestMode(testMode);
//
//            oneOf (impression).setTrackingIdentifier(secureTrackingId);
//
//            allowing (domainCache).getDeviceIdentifierTypeById(udidTypeId); will(returnValue(udidType));
//            allowing (udidType).isSecure(); will(returnValue(false));
//            allowing (domainCache).getDeviceIdentifierTypeById(dpidTypeId); will(returnValue(dpidType));
//            allowing (dpidType).isSecure(); will(returnValue(true));
//            oneOf (impression).setDeviceIdentifiers(with(any(Map.class)));
//
//            allowing (model).getId(); will(returnValue(modelId));
//            oneOf (impression).setModelId(modelId);
//            allowing (country).getId(); will(returnValue(countryId));
//            oneOf (impression).setCountryId(countryId);
//            allowing (country).getIsoCode(); will(returnValue(countryIsoCode));
//            oneOf (postalCodeIdManager).getPostalCodeId(countryIsoCode, postalCode); will(returnValue(postalCodeId));
//            oneOf (impression).setPostalCodeId(postalCodeId);
//            allowing (operator).getId(); will(returnValue(operatorId));
//            oneOf (operator).getCountryIsoCode(); will(returnValue("iso"));
//            allowing (impression).setOperatorId(operatorId);
//            allowing (geotarget).getId(); will(returnValue(geotargetId));
//            oneOf (impression).setGeotargetId(geotargetId);
//            allowing (integrationType).getId(); will(returnValue(integrationTypeId));
//            oneOf (impression).setIntegrationTypeId(integrationTypeId);
//            oneOf (impression).setAgeRange(ageRange);
//            oneOf (impression).setGender(gender);
//            oneOf (impression).setHost(host);
//            allowing (impression).getCreationTime(); will(returnValue(creationTime));
//            oneOf (impression).setUserTimeZoneId(userTimeZoneId);
//            oneOf (impression).setDateOfBirth(dateOfBirth);
//            allowing (coordinates).getLatitude(); will(returnValue(latitude));
//            oneOf (impression).setLatitude(latitude);
//            allowing (coordinates).getLongitude(); will(returnValue(longitude));
//            oneOf (impression).setLongitude(longitude);
//            oneOf (impression).setLocationSource(locationSource.name());
//        }});
//
//        targetingContext.populateImpression(impression, selectedCreative);
//    }
}
