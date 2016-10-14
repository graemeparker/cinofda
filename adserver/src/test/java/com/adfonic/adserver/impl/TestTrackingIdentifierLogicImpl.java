package com.adfonic.adserver.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;

public class TestTrackingIdentifierLogicImpl extends BaseAdserverTest {

	private HttpServletResponse httpServletResponse;
	private TrackingIdentifierLogicImpl trackingIdentifierLogicImpl;
	private TargetingContext targetingContext;

    @Before
	public void initTests(){
		httpServletResponse = mock(HttpServletResponse.class,"httpServletResponse");
		targetingContext = mock(TargetingContext.class,"targetingContext");
		trackingIdentifierLogicImpl = new TrackingIdentifierLogicImpl();
		
	}
	
	@Test
    public void testWithNoAdSpace() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = null;
    	boolean cookiesAllowed = false;
		expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		}});
        // Should just log a warning and return cleanly
		trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
    
    @Test
    public void testTrackingIdentifierLogicImpl01_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = false;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.DEVICE;
        final String trackingId = null;
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
		    
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		    oneOf (targetingContext).getAttribute(Parameters.TRACKING_ID);will(returnValue(trackingId));
		}});

        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }

    /**
     * Valid, not null tracking identifier
     * @throws InvalidTrackingIdentifierException
     */
    @Test
    public void testTrackingIdentifierLogicImpl03_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = false;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.PUBLISHER_GENERATED;
        final String trackingId = "0123423aefcd4012345678909876543212345678";
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
		    
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		    allowing (targetingContext).getAttribute(Parameters.TRACKING_ID);will(returnValue(trackingId));
		    oneOf (targetingContext).setAttribute(TargetingContext.SECURE_TRACKING_ID, DigestUtils.shaHex(trackingId));
		}});
        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
    
    @Test
    public void testTrackingIdentifierLogicImpl04_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = true;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.COOKIE;
        final String trackingId = null;
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
		    
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		    oneOf (targetingContext).getCookie(TrackingIdentifierLogicImpl.TRACKING_IDENTIFIER_COOKIE);will(returnValue(trackingId));
            // The cookie should be generated
		    oneOf (httpServletResponse).addCookie(with(any(Cookie.class)));
		    oneOf (targetingContext).setAttribute(with(Parameters.TRACKING_ID), with(any(String.class)));
		    oneOf (targetingContext).setAttribute(with(TargetingContext.SECURE_TRACKING_ID), with(any(String.class)));
		}});

        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
    
    @Test
    public void testTrackingIdentifierLogicImpl05_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = true;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.COOKIE;
        final String trackingId = randomAlphaNumericString(40);
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
		    
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		    oneOf (targetingContext).getCookie(TrackingIdentifierLogicImpl.TRACKING_IDENTIFIER_COOKIE);will(returnValue(trackingId));
		    oneOf (targetingContext).setAttribute(TargetingContext.SECURE_TRACKING_ID, DigestUtils.shaHex(trackingId));
		}});

        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
    

    @Test
    public void testTrackingIdentifierLogicImpl06_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = false;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.COOKIE;
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
		    
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		}});

        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
    
    @Test
    public void testTrackingIdentifierLogicImpl07_establishTrackingIdentifier() throws InvalidTrackingIdentifierException {
    	final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
    	boolean cookiesAllowed = false;
        final PublicationDto publication = mock(PublicationDto.class,"publication");
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.NONE;
        
        expect(new Expectations() {{
		    oneOf (targetingContext).getAdSpace(); will(returnValue(adSpace));
		    oneOf (adSpace).getPublication(); will(returnValue(publication));
		    oneOf (publication).getTrackingIdentifierType(); will(returnValue(trackingIdentifierType));
            
		    oneOf (targetingContext).setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE,trackingIdentifierType);
		}});

        trackingIdentifierLogicImpl.establishTrackingIdentifier(targetingContext, httpServletResponse, cookiesAllowed);
    }
   
}
