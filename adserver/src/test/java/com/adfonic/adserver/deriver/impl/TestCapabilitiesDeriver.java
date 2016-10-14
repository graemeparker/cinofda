package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestCapabilitiesDeriver extends BaseAdserverTest {

	private CapabilitiesDeriver capabilitiesDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		capabilitiesDeriver = new CapabilitiesDeriver(new DeriverManager());
		context = mock(TargetingContext.class, "context");
	}

	@Test
	public void testGetAttribute01_invalid_attribute() {
		assertNull(capabilitiesDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testGetAttribute02_capabilities_no_device_props() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DEVICE_PROPERTIES); will(returnValue(null));
		}});
		assertNull(capabilitiesDeriver.getAttribute(TargetingContext.CAPABILITIES, context));
	}
	
	@Test
	public void testGetAttribute03_capabilities_with_device_props() {
		final DomainCache domainCache = mock(DomainCache.class);
		final List<CapabilityDto> list = new ArrayList<CapabilityDto>();
		final Map<String,String> props = new HashMap<String, String>();
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DEVICE_PROPERTIES); will(returnValue(props));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getCapabilities(); will(returnValue(list));
		}});
		assertNotNull(capabilitiesDeriver.getAttribute(TargetingContext.CAPABILITIES, context));
	}

    @Test
    public void testGetAttribute04_capability_ids_no_capabilities() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.CAPABILITIES); will(returnValue(null));
        }});
        assertNull(capabilitiesDeriver.getAttribute(TargetingContext.CAPABILITY_IDS, context));
    }

    @Test
    public void testGetAttribute05_capability_ids_with_capabilities() {
        final CapabilityDto capability = mock(CapabilityDto.class);
        final long capabilityId = randomLong();
        @SuppressWarnings("serial")
		final List<CapabilityDto> capabilities = new ArrayList<CapabilityDto>() {{
                add(capability);
            }};
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.CAPABILITIES); will(returnValue(capabilities));
            oneOf (capability).getId(); will(returnValue(capabilityId));
        }});
        Set<Long> result = (Set)capabilitiesDeriver.getAttribute(TargetingContext.CAPABILITY_IDS, context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(capabilityId));
    }
}
