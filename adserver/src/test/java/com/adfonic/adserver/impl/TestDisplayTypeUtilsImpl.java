package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;

public class TestDisplayTypeUtilsImpl extends BaseAdserverTest {

	private DisplayTypeUtilsImpl displayTypeUtilsImpl;

    @Before
	public void initTests(){
		displayTypeUtilsImpl = new DisplayTypeUtilsImpl();
	}
	
	@Test
	public void testDisplayTypeUtilsImpl01_setDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		
		
		expect(new Expectations() {{
		    oneOf (targetingContext).setAttribute(with(any(String.class)),with(displayType));
		    oneOf (format).getSystemName();will(returnValue(systemName));
		}});
		
		displayTypeUtilsImpl.setDisplayType(format, targetingContext,displayType);
	}
	
	@Test
	public void testDisplayTypeUtilsImpl02_getDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		
		
		expect(new Expectations() {{
		    oneOf (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayType));
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext);
		assertNotNull(returnDisplayType);
		assertEquals(displayType, returnDisplayType);
	}
	
	@Test
	public void testDisplayTypeUtilsImpl03_getDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayType = DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE;
		final String systemName = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayType));
		    	
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext);
		assertNull(returnDisplayType);
	}
	
	@Test
	public void testDisplayTypeUtilsImpl04_getDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayType = null;
		final String systemName = randomAlphaNumericString(10);
		final String contentSpecManifest = null;
		final Map<String,String> deviceProps = null;
		
		expect(new Expectations() {{
		    //oneOf (targetingContext).setAttribute(with(any(String.class)),with(displayType));
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayType));
	    	oneOf (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
	    	oneOf (targetingContext).getAttribute(with(TargetingContext.DEVICE_PROPERTIES));
		    	will(returnValue(deviceProps));
	    	oneOf (targetingContext).setAttribute(with(any(String.class)),with(DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE));
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext);
		assertNull(returnDisplayType);
	}
	@Test
	public void testDisplayTypeUtilsImpl05_getDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayType = null;
		final String systemName = randomAlphaNumericString(10);
		final String contentSpecManifest = null;
		final Map<String,String> deviceProps = new HashMap<String, String>();
		final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
		
		expect(new Expectations() {{
		    //oneOf (targetingContext).setAttribute(with(any(String.class)),with(displayType));
		    allowing (targetingContext).setAttribute(with(any(String.class)),with(DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE));
		    allowing (format).getSystemName();will(returnValue(systemName));
		    allowing (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayType));
		    allowing (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
		    allowing (targetingContext).getAttribute(with(TargetingContext.DEVICE_PROPERTIES));
		    	will(returnValue(deviceProps));
	    	//oneOf (targetingContext).setAttribute(with(any(String.class)),with(DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE));
	    	allowing (format).getDisplayTypes();
	    		will(returnValue(displayTypes));
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext);
		assertNull(returnDisplayType);
	}
	
	@Test
	public void testDisplayTypeUtilsImpl06_getDisplayType(){
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = null;
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		final String contentSpecManifest = randomAlphaNumericString(10);
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		
		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
	    	oneOf (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
		    oneOf (targetingContext).getDomainCache();
		    	will(returnValue(domainCache));
		    oneOf (domainCache).getDisplayType(format, contentSpecManifest);
		    	will(returnValue(displayType));
	    	allowing (targetingContext).setAttribute(with(any(String.class)),with.is(anything()));
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext);
		assertNotNull(returnDisplayType);
		assertEquals(displayType, returnDisplayType);
	}
	
	@Test(expected=RuntimeException.class)
	public void testDisplayTypeUtilsImpl07_getDisplayTypeIndex() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = null;
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		final String contentSpecManifest = randomAlphaNumericString(10);
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
		
		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
	    	oneOf (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
		    oneOf (targetingContext).getDomainCache();
		    	will(returnValue(domainCache));
		    oneOf (domainCache).getDisplayType(format, contentSpecManifest);
		    	will(returnValue(displayType));
	    	allowing (targetingContext).setAttribute(with(any(String.class)),with.is(anything()));
	    	oneOf (format).getDisplayTypes();
    			will(returnValue(displayTypes));
		}});
		
		displayTypeUtilsImpl.getDisplayTypeIndex(format, targetingContext);
		//This will throw runtime exception
	}
	
	@Test
	public void testDisplayTypeUtilsImpl08_getDisplayTypeIndex() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = null;
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		final String displayTypeSystemName = randomAlphaNumericString(10);
		final String contentSpecManifest = randomAlphaNumericString(10);
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
		displayTypes.add(displayType);
		
		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    allowing (displayType).getSystemName();will(returnValue(displayTypeSystemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
	    	oneOf (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
		    oneOf (targetingContext).getDomainCache();
		    	will(returnValue(domainCache));
		    oneOf (domainCache).getDisplayType(format, contentSpecManifest);
		    	will(returnValue(displayType));
	    	allowing (targetingContext).setAttribute(with(any(String.class)),with.is(anything()));
	    	oneOf (format).getDisplayTypes();
    			will(returnValue(displayTypes));
		}});
		
		int displyTypeIndex = displayTypeUtilsImpl.getDisplayTypeIndex(format, targetingContext);
		assertEquals(0, displyTypeIndex);
	}
	@Test(expected=Throwable.class)
	public void testDisplayTypeUtilsImpl09_getDisplayTypeIndex() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = null;
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final DisplayTypeDto secondDisplayType = mock(DisplayTypeDto.class,"secondDisplayType");
		final String systemName = randomAlphaNumericString(10);
		final String displayTypeSystemName = randomAlphaNumericString(10);
		final String secondDisplayTypeSystemName = randomAlphaNumericString(12);
		final String contentSpecManifest = randomAlphaNumericString(10);
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
		displayTypes.add(secondDisplayType);
		
		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    allowing (displayType).getSystemName();will(returnValue(displayTypeSystemName));
		    allowing (secondDisplayType).getSystemName();will(returnValue(secondDisplayTypeSystemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
	    	oneOf (targetingContext).getAttribute(with(Parameters.CONSTRAINTS));
		    	will(returnValue(contentSpecManifest));
		    oneOf (targetingContext).getDomainCache();
		    	will(returnValue(domainCache));
		    oneOf (domainCache).getDisplayType(format, contentSpecManifest);
		    	will(returnValue(displayType));
	    	allowing (targetingContext).setAttribute(with(any(String.class)),with.is(anything()));
	    	oneOf (format).getDisplayTypes();
    			will(returnValue(displayTypes));
		}});
		
		displayTypeUtilsImpl.getDisplayTypeIndex(format, targetingContext);
		//This will throw runtime exception
	}
	
	@Test
	public void testDisplayTypeUtilsImpl10_getDisplayTypeIndex() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE;
		final String systemName = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
		}});
		
		int displyTypeIndex = displayTypeUtilsImpl.getDisplayTypeIndex(format, targetingContext);
		assertEquals(0, displyTypeIndex);
	}
	
	@Test
	public void testDisplayTypeUtilsImpl11_getDisplayType() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = DisplayTypeUtilsImpl.NULL_DISPLAY_TYPE;
		final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
		final String systemName = randomAlphaNumericString(10);
		final Boolean defaultToFirstAvailable = false;
		final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
		displayTypes.add(displayType);

		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
	    	oneOf (format).getDisplayTypes();
    			will(returnValue(displayTypes));
            allowing (displayType).getSystemName(); will(returnValue(randomAlphaNumericString(10)));
		}});
		
		DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext, defaultToFirstAvailable);
		assertEquals(displayType, returnDisplayType);
	}
	@Test
	public void testDisplayTypeUtilsImpl12_getDisplayType() throws RuntimeException{
		final FormatDto format = mock(FormatDto.class,"format");
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DisplayTypeDto displayTypeInContext = mock(DisplayTypeDto.class,"displayTypeInContext");
		final String systemName = randomAlphaNumericString(10);
		final Boolean defaultToFirstAvailable = false;

		expect(new Expectations() {{
		    allowing (format).getSystemName();will(returnValue(systemName));
		    oneOf (targetingContext).getAttribute(with(any(String.class)));
		    	will(returnValue(displayTypeInContext));
		}});
		
	DisplayTypeDto returnDisplayType = displayTypeUtilsImpl.getDisplayType(format, targetingContext, defaultToFirstAvailable);
		assertEquals(displayTypeInContext, returnDisplayType);
	}

    @Test
    public void testDisplayTypeUtilsImpl13_getDisplayType_AF_1165() throws RuntimeException{
        final FormatDto format = null;
        final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");

        assertNull(displayTypeUtilsImpl.getDisplayType(format, targetingContext, true));
        assertNull(displayTypeUtilsImpl.getDisplayType(format, targetingContext, false));
        assertNull(displayTypeUtilsImpl.getDisplayType(format, targetingContext));
    }
}
