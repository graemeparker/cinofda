package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestDeviceIdentifiersDeriver extends BaseAdserverTest {
    private final long androidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long udidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long dpidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long odin1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long openudidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long hifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long ifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long atidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");

    @SuppressWarnings("serial")
    private final Map<String, Long> deviceIdentifierTypeIdsBySystemName = new HashMap<String, Long>() {
        {
            put("android", androidDeviceIdentifierTypeId);
            put("udid", udidDeviceIdentifierTypeId);
            put("dpid", dpidDeviceIdentifierTypeId);
            put("odin-1", odin1DeviceIdentifierTypeId);
            put("openudid", openudidDeviceIdentifierTypeId);
            put("hifa", hifaDeviceIdentifierTypeId);
            put("ifa", ifaDeviceIdentifierTypeId);
            put("atid", atidDeviceIdentifierTypeId);
        }
    };

    private DeriverManager deriverManager;
    private DeviceIdentifiersDeriver deviceIdentifiersDeriver;
    private TargetingContext context;

    @Before
    public void initTests() {
        deriverManager = new DeriverManager();
        deviceIdentifiersDeriver = new DeviceIdentifiersDeriver(deriverManager);
        context = mock(TargetingContext.class);
    }

    @Test
    public void testMakeParameterName() {
        final DeviceIdentifierTypeDto dit = mock(DeviceIdentifierTypeDto.class, "dit");
        final String systemName = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(dit).getSystemName();
                will(returnValue(systemName));
            }
        });

        String expected = Parameters.DEVICE_PREFIX + systemName;
        assertEquals(expected, DeviceIdentifiersDeriver.makeParameterName(dit));
    }

    @Test
    public void testGetAttribute01_noneProvided() {
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");

        final DeviceIdentifierTypeDto dit1 = mock(DeviceIdentifierTypeDto.class, "dit1");
        final String dit1SystemName = randomAlphaNumericString(10);
        final String param1Name = Parameters.DEVICE_PREFIX + dit1SystemName;

        final DeviceIdentifierTypeDto dit2 = mock(DeviceIdentifierTypeDto.class, "dit2");
        final String dit2SystemName = randomAlphaNumericString(10);
        final String param2Name = Parameters.DEVICE_PREFIX + dit2SystemName;

        final TreeSet<DeviceIdentifierTypeDto> dits = new TreeSet<DeviceIdentifierTypeDto>();

        expect(new Expectations() {
            {
                // These allow us to put them in a SortedSet
                allowing(dit1).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(-1));
                allowing(dit2).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(1));

                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getAllDeviceIdentifierTypes();
                will(returnValue(dits));

                allowing(dit1).getSystemName();
                will(returnValue(dit1SystemName));
                allowing(context).getAttribute(param1Name);
                will(returnValue(null));

                allowing(dit2).getSystemName();
                will(returnValue(dit2SystemName));
                allowing(context).getAttribute(param2Name);
                will(returnValue(null));

                allowing(context).getAttribute(Parameters.TRACKING_ID);
                will(returnValue(null));
            }
        });

        dits.add(dit1);
        dits.add(dit2);

        Object value = deviceIdentifiersDeriver.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        assertNotNull(value);
        assertTrue(value instanceof Map);
        Map map = (Map) value;
        assertTrue(map.isEmpty());
    }

    @Test
    public void testGetAttribute02_noneProvided_rId_provided_no_adSpace() {
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final String trackingId = randomHexString(40).toUpperCase();
        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getAllDeviceIdentifierTypes();
                will(returnValue(new TreeSet()));
                oneOf(context).getAttribute(Parameters.TRACKING_ID);
                will(returnValue(trackingId));
                oneOf(context).getAdSpace();
                will(returnValue(null));
            }
        });

        Map map = (Map) deviceIdentifiersDeriver.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        assertNotNull(map);
        //System.out.println("map contents: " + map);
    }

    @Ignore("mad-2473 udid is to be removed")
    @Test
    public void testGetAttribute03_noneProvided_rId_provided_found() {
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final String trackingId = randomHexString(40).toUpperCase();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class);
        final DeviceIdentifierTypeDto dit = mock(DeviceIdentifierTypeDto.class);
        final long ditId = randomLong();
        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getAllDeviceIdentifierTypes();
                will(returnValue(new TreeSet()));
                oneOf(context).getAttribute(Parameters.TRACKING_ID);
                will(returnValue(trackingId));
                oneOf(context).getAdSpace();
                will(returnValue(adSpace));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(SystemName.IPHONE_APP));
                oneOf(domainCache).getDeviceIdentifierTypeBySystemName("udid");
                will(returnValue(dit));
                oneOf(dit).getValidationPattern();
                will(returnValue(null));
                allowing(dit).getSystemName();
                will(returnValue("udid"));
                allowing(dit).getId();
                will(returnValue(ditId));
                allowing(dit).isSecure();
                will(returnValue(false));
                oneOf(domainCache).getDeviceIdentifierTypeIdsBySystemName();
                will(returnValue(deviceIdentifierTypeIdsBySystemName));
                allowing(domainCache).isDeviceIdentifierBlacklisted(with(any(Long.class)), with(any(String.class)));
                will(returnValue(false));
            }
        });

        Map map = (Map) deviceIdentifiersDeriver.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        assertNotNull(map);
        assertEquals(DigestUtils.shaHex(trackingId.toLowerCase()), map.get(ditId));
    }

    @Test
    public void testGetAttribute03_variousProvided() {
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");

        final DeviceIdentifierTypeDto dit1 = mock(DeviceIdentifierTypeDto.class, "dit1");
        final String dit1SystemName = randomAlphaNumericString(10);
        final String param1Name = Parameters.DEVICE_PREFIX + dit1SystemName;

        final DeviceIdentifierTypeDto dit2 = mock(DeviceIdentifierTypeDto.class, "dit2");
        final String dit2SystemName = randomAlphaNumericString(10);
        final String param2Name = Parameters.DEVICE_PREFIX + dit2SystemName;
        final String param2Value = randomAlphaNumericString(10);
        final long dit2Id = randomLong();

        final DeviceIdentifierTypeDto dit3 = mock(DeviceIdentifierTypeDto.class, "dit3");
        final String dit3SystemName = randomAlphaNumericString(10);
        final String param3Name = Parameters.DEVICE_PREFIX + dit3SystemName;
        final String param3Value = randomAlphaNumericString(10);
        // Build a pattern that won't match...11 long instead of 10
        final Pattern pattern3 = Pattern.compile("^[A-Za-z0-9]{11}$");

        final DeviceIdentifierTypeDto dit4 = mock(DeviceIdentifierTypeDto.class, "dit4");
        final String dit4SystemName = randomAlphaNumericString(10);
        final String param4Name = Parameters.DEVICE_PREFIX + dit4SystemName;
        final String param4Value = randomAlphaNumericString(10);
        // Build a pattern that will match
        final Pattern pattern4 = Pattern.compile("^[A-Za-z0-9]{10}$");
        final long dit4Id = randomLong();

        final TreeSet<DeviceIdentifierTypeDto> dits = new TreeSet<DeviceIdentifierTypeDto>();

        expect(new Expectations() {
            {
                // These allow us to put them in a SortedSet
                allowing(dit1).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(-1));
                allowing(dit2).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(-1));
                allowing(dit3).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(-1));
                allowing(dit4).compareTo(with(any(DeviceIdentifierTypeDto.class)));
                will(returnValue(-1));

                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getAllDeviceIdentifierTypes();
                will(returnValue(dits));

                allowing(dit1).getSystemName();
                will(returnValue(dit1SystemName));
                allowing(context).getAttribute(param1Name);
                will(returnValue(null));

                allowing(dit2).getSystemName();
                will(returnValue(dit2SystemName));
                allowing(context).getAttribute(param2Name);
                will(returnValue(param2Value));
                allowing(dit2).getValidationPattern();
                will(returnValue(null));
                allowing(dit2).getId();
                will(returnValue(dit2Id));
                allowing(dit2).isSecure();
                will(returnValue(true)); // secure, store as-is

                allowing(dit3).getSystemName();
                will(returnValue(dit3SystemName));
                allowing(context).getAttribute(param3Name);
                will(returnValue(param3Value));
                allowing(dit3).getValidationPattern();
                will(returnValue(pattern3));

                allowing(dit4).getSystemName();
                will(returnValue(dit4SystemName));
                allowing(context).getAttribute(param4Name);
                will(returnValue(param4Value));
                allowing(dit4).getValidationPattern();
                will(returnValue(pattern4));
                allowing(dit4).getId();
                will(returnValue(dit4Id));
                allowing(dit4).isSecure();
                will(returnValue(false)); // not secure, store SHA1

                oneOf(domainCache).getDeviceIdentifierTypeIdsBySystemName();
                will(returnValue(deviceIdentifierTypeIdsBySystemName));
                allowing(domainCache).isDeviceIdentifierBlacklisted(with(any(Long.class)), with(any(String.class)));
                will(returnValue(false));
            }
        });

        dits.add(dit1);
        dits.add(dit2);
        dits.add(dit3);
        dits.add(dit4);

        Object value = deviceIdentifiersDeriver.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        assertNotNull(value);
        assertTrue(value instanceof Map);

        Map map = (Map) value;
        assertEquals(2, map.size());
        assertTrue(map.containsKey(dit2Id));
        assertEquals(param2Value.toLowerCase(), map.get(dit2Id));
        assertTrue(map.containsKey(dit4Id));
        assertEquals(param4Value.toLowerCase(), map.get(dit4Id));
    }

    @Test
    public void testGetAttribute04_SC_215_IFA_with_dashes() {
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");

        final String withDashes = UUID.randomUUID().toString();

        final String ditSystemName = "ifa";
        final String paramName = Parameters.DEVICE_PREFIX + ditSystemName;
        final String paramValue = withDashes;
        final DeviceIdentifierTypeDto dit = new DeviceIdentifierTypeDto();
        dit.setId(ifaDeviceIdentifierTypeId);
        dit.setSecure(false);
        dit.setSystemName(ditSystemName);

        final TreeSet<DeviceIdentifierTypeDto> dits = new TreeSet<DeviceIdentifierTypeDto>();
        expect(new Expectations() {
            {

                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getAllDeviceIdentifierTypes();
                will(returnValue(dits));

                allowing(context).getAttribute(paramName);
                will(returnValue(paramValue));

                oneOf(domainCache).getDeviceIdentifierTypeIdsBySystemName();
                will(returnValue(deviceIdentifierTypeIdsBySystemName));
                allowing(domainCache).isDeviceIdentifierBlacklisted(with(any(Long.class)), with(any(String.class)));
                will(returnValue(false));
            }
        });

        dits.add(dit);

        Object value = deviceIdentifiersDeriver.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        assertNotNull(value);
        assertTrue(value instanceof Map);

        Map map = (Map) value;
        assertEquals(2, map.size());
        assertEquals(withDashes.toUpperCase(), map.get(ifaDeviceIdentifierTypeId));
        assertEquals(DigestUtils.shaHex(withDashes.toUpperCase()), map.get(hifaDeviceIdentifierTypeId));
    }

}
