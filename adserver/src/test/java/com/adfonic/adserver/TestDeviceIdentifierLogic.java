package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.test.AbstractAdfonicTest;

@SuppressWarnings("serial")
public class TestDeviceIdentifierLogic extends AbstractAdfonicTest {
    private final long androidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long udidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long dpidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long odin1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long openudidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long ifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long hifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long idfaMd5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long adidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long adidMd5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long adidSha1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");

    private final Map<String, Long> deviceIdentifierTypeIdsBySystemName = new HashMap<String, Long>() {
        {
            put("android", androidDeviceIdentifierTypeId);
            put("udid", udidDeviceIdentifierTypeId);
            put("dpid", dpidDeviceIdentifierTypeId);
            put("odin-1", odin1DeviceIdentifierTypeId);
            put("openudid", openudidDeviceIdentifierTypeId);
            put("ifa", ifaDeviceIdentifierTypeId);
            put("hifa", hifaDeviceIdentifierTypeId);
            put("idfa_md5", idfaMd5DeviceIdentifierTypeId);
            put("adid", adidDeviceIdentifierTypeId);
            put("adid_md5", adidMd5DeviceIdentifierTypeId);
            put("adid_sha1", adidSha1DeviceIdentifierTypeId);
        }
    };

    private DomainCache domainCache;

    @Before
    public void runBeforeEachTest() {
        domainCache = mock(DomainCache.class);
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType01_adSpaceNull() {
        String trackingId = randomAlphaNumericString(10);
        assertNull(DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, null, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType02_IPHONE_APP_invalid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = SystemName.IPHONE_APP;
        final String deviceIdentifierTypeSystemName = "ifa";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // Not 40-char hex, invalid
        String trackingId = randomAlphaNumericString(10).toLowerCase();
        assertNull(DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType03_IPHONE_APP_valid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = SystemName.IPHONE_APP;
        final String deviceIdentifierTypeSystemName = "ifa";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // 40-char hex, valid
        String trackingId = randomHexString(40).toLowerCase();
        assertEquals(deviceIdentifierType, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType04_IPAD_APP_invalid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "IPAD_APP";
        final String deviceIdentifierTypeSystemName = "ifa";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // not 40-char hex, invalid
        String trackingId = randomAlphaNumericString(10).toLowerCase();
        assertNull(DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType05_IPAD_APP_valid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "IPAD_APP";
        final String deviceIdentifierTypeSystemName = "ifa";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // 40-char hex, valid
        String trackingId = randomHexString(40).toLowerCase();
        assertEquals(deviceIdentifierType, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType06_ANDROID_APP_invalid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "ANDROID_APP";
        final String deviceIdentifierTypeSystemName = "adid";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{16}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // not 16-char hex, invalid
        String trackingId = randomAlphaNumericString(10).toLowerCase();
        assertNull(DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType07_ANDROID_APP_valid() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "ANDROID_APP";
        final String deviceIdentifierTypeSystemName = "adid";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{16}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        // 16-char hex, valid
        String trackingId = randomHexString(16).toLowerCase();
        assertEquals(deviceIdentifierType, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType08_OTHER_APP_is_UDID() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "OTHER_APP";
        final String deviceIdentifierTypeSystemName = "udid";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        String trackingId = randomHexString(40).toLowerCase();
        assertEquals(deviceIdentifierType, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType09_OTHER_APP_is_Android() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "OTHER_APP";
        final String deviceIdentifierTypeSystemName = "android";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{16}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        String trackingId = randomHexString(16).toLowerCase();
        assertEquals(deviceIdentifierType, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType10_OTHER_APP_is_16_long_but_not_Android() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "OTHER_APP";
        final String deviceIdentifierTypeSystemName = "android";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{16}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        String trackingId = randomString("!@#$%", 16);
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType11_OTHER_APP_is_40_long_but_not_UDID() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "OTHER_APP";
        final String deviceIdentifierTypeSystemName = "udid";
        final DeviceIdentifierTypeDto deviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "deviceIdentifierType");
        final Pattern validationPattern = Pattern.compile("^[a-f0-9]{40}$");

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
                allowing(domainCache).getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeSystemName);
                will(returnValue(deviceIdentifierType));
                allowing(deviceIdentifierType).getSystemName();
                will(returnValue(deviceIdentifierTypeSystemName));
                allowing(deviceIdentifierType).getValidationPattern();
                will(returnValue(validationPattern));
            }
        });

        String trackingId = randomString("!@#$%", 40);
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType12_OTHER_APP_is_nothing() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = "OTHER_APP";

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
            }
        });

        String trackingId;

        trackingId = randomHexString(15).toLowerCase();
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));

        trackingId = randomHexString(17).toLowerCase();
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));

        trackingId = randomHexString(39).toLowerCase();
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));

        trackingId = randomHexString(41).toLowerCase();
        assertEquals(null, DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testDetermineTrackingIdDeviceIdentifierType13_someOtherPubType() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationTypeId = randomLong();
        final PublicationTypeDto publicationType = mock(PublicationTypeDto.class, "publicationType");
        final String pubTypeSystemName = randomAlphaNumericString(10); // not iphone or ipad or android

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublicationTypeId();
                will(returnValue(publicationTypeId));
                allowing(domainCache).getPublicationTypeById(publicationTypeId);
                will(returnValue(publicationType));
                allowing(publicationType).getSystemName();
                will(returnValue(pubTypeSystemName));
            }
        });

        String trackingId = randomAlphaNumericString(10);
        assertNull(DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(trackingId, adSpace, domainCache));
    }

    @Test
    public void testGetSecureDeviceIdentifier() {
        final String android = randomHexString(16);
        final String secureAndroid = DigestUtils.shaHex(android);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(androidDeviceIdentifierTypeId, secureAndroid);
            }
        };

        assertEquals(secureAndroid, DeviceIdentifierLogic.getSecureDeviceIdentifier(secureDeviceIdentifiers, "android", deviceIdentifierTypeIdsBySystemName));

        assertNull(DeviceIdentifierLogic.getSecureDeviceIdentifier(secureDeviceIdentifiers, "udid", deviceIdentifierTypeIdsBySystemName));

        assertNull(DeviceIdentifierLogic.getSecureDeviceIdentifier(secureDeviceIdentifiers, "openudid", deviceIdentifierTypeIdsBySystemName));
    }

    @Test
    public void testPromoteDeviceIdentifiers01_dpid_already_supplied() {
        final String dpid = randomHexString(40);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(dpidDeviceIdentifierTypeId, dpid);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        // Make sure no promotions occurred
        assertEquals(1, secureDeviceIdentifiers.size());
        assertEquals(dpid, secureDeviceIdentifiers.get(dpidDeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteDeviceIdentifiers02_android_to_dpid() {
        final String android = randomHexString(16);
        final String secureAndroid = DigestUtils.shaHex(android);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(androidDeviceIdentifierTypeId, secureAndroid);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);
        // Make sure it got promoted to dpid
        assertEquals(secureAndroid, secureDeviceIdentifiers.get(dpidDeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteDeviceIdentifiers03_udid_to_dpid() {
        final String udid = randomHexString(40);
        final String secureUdid = DigestUtils.shaHex(udid);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(udidDeviceIdentifierTypeId, secureUdid);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);
        // Make sure it got promoted to dpid
        assertEquals(secureUdid, secureDeviceIdentifiers.get(dpidDeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteDeviceIdentifiers04_odin1_already_supplied() {
        final String odin1 = randomHexString(40);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(odin1DeviceIdentifierTypeId, odin1);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        // Make sure no promotions occurred
        assertEquals(1, secureDeviceIdentifiers.size());
        assertEquals(odin1, secureDeviceIdentifiers.get(odin1DeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteDeviceIdentifiers05_android_to_odin1() {
        final String android = randomHexString(16);
        final String secureAndroid = DigestUtils.shaHex(android);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(androidDeviceIdentifierTypeId, secureAndroid);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);
        // Make sure it got promoted to odin1
        assertEquals(secureAndroid, secureDeviceIdentifiers.get(odin1DeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteDeviceIdentifiers06_none_supplied() {
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>();

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        // No promotions should have occurred since there's nothing to promote
        assertTrue(secureDeviceIdentifiers.isEmpty());
    }

    @Test
    public void testPromoteDeviceIdentifiers07_no_promotions_applicable() {
        final String openudid = randomHexString(40);
        final Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(openudidDeviceIdentifierTypeId, openudid);
            }
        };

        DeviceIdentifierLogic.promoteDeviceIdentifiers(secureDeviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        // No promotions should have occurred
        assertEquals(1, secureDeviceIdentifiers.size());
        assertEquals(openudid, secureDeviceIdentifiers.get(openudidDeviceIdentifierTypeId));
    }

    @Test
    public void test08_enforceBlacklist() {
        final long ditId1 = uniqueLong("DeviceIdentifierType.id");
        final String did1 = randomHexString(40);
        final long ditId2 = uniqueLong("DeviceIdentifierType.id");
        final String did2 = randomHexString(40);
        final Map<Long, String> dids = new HashMap<Long, String>() {
            {
                put(ditId1, did1);
                put(ditId2, did2);
            }
        };
        expect(new Expectations() {
            {
                oneOf(domainCache).isDeviceIdentifierBlacklisted(ditId1, did1);
                will(returnValue(true));
                oneOf(domainCache).isDeviceIdentifierBlacklisted(ditId2, did2);
                will(returnValue(false));
            }
        });
        DeviceIdentifierLogic.enforceBlacklist(dids, domainCache);
        assertEquals(1, dids.size());
        assertFalse(dids.containsKey(ditId1));
        assertTrue(dids.containsKey(ditId2));
    }

    @Test
    public void testPromoteIfa() {
        Map<Long, String> deviceIdentifiers = new TreeMap<Long, String>();
        String ifa = "12345678-abcd-1234-abcd-1234567890EF";
        deviceIdentifiers.put(ifaDeviceIdentifierTypeId, ifa);

        DeviceIdentifierLogic.promoteDeviceIdentifiers(deviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        String expectedMd5 = DigestUtils.md5Hex(ifa.toUpperCase());
        Assert.assertEquals("12345678-abcd-1234-abcd-1234567890EF", deviceIdentifiers.get(ifaDeviceIdentifierTypeId));
        Assert.assertEquals("f88028c027f59afc99d0bcb2ee960dd3839ac8f7", deviceIdentifiers.get(hifaDeviceIdentifierTypeId));
        Assert.assertEquals(expectedMd5, deviceIdentifiers.get(idfaMd5DeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteIfaWhenHifaAlreadyGiven() {
        Map<Long, String> deviceIdentifiers = new TreeMap<Long, String>();
        String ifa = "12345678-ABCD-1234-ABCD-1234567890EF";
        deviceIdentifiers.put(ifaDeviceIdentifierTypeId, ifa);
        deviceIdentifiers.put(hifaDeviceIdentifierTypeId, "AAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaFFFFFF");

        DeviceIdentifierLogic.promoteDeviceIdentifiers(deviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        String expectedMd5 = DigestUtils.md5Hex(ifa.toUpperCase());
        Assert.assertEquals("12345678-ABCD-1234-ABCD-1234567890EF", deviceIdentifiers.get(ifaDeviceIdentifierTypeId));
        Assert.assertEquals("AAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaFFFFFF", deviceIdentifiers.get(hifaDeviceIdentifierTypeId));
        Assert.assertEquals(expectedMd5, deviceIdentifiers.get(idfaMd5DeviceIdentifierTypeId));

    }

    @Test
    public void testPromoteADID() {
        Map<Long, String> deviceIdentifiers = new TreeMap<Long, String>();
        String adid = "12345678-abcd-1234-abcd-1234567890EF"; // wrong letter casing ?
        deviceIdentifiers.put(adidDeviceIdentifierTypeId, adid);

        DeviceIdentifierLogic.promoteDeviceIdentifiers(deviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        String expected = DigestUtils.md5Hex(adid);
        Assert.assertEquals("12345678-abcd-1234-abcd-1234567890EF", deviceIdentifiers.get(adidDeviceIdentifierTypeId));
        Assert.assertEquals(expected, deviceIdentifiers.get(adidMd5DeviceIdentifierTypeId));
    }

    @Test
    public void testPromoteIfaWhenAdidMd5AlreadyGiven() {
        Map<Long, String> deviceIdentifiers = new TreeMap<Long, String>();
        deviceIdentifiers.put(adidDeviceIdentifierTypeId, "12345678-abcd-1234-abcd-1234567890EF");
        deviceIdentifiers.put(adidMd5DeviceIdentifierTypeId, "AAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaFFFFFF");

        DeviceIdentifierLogic.promoteDeviceIdentifiers(deviceIdentifiers, deviceIdentifierTypeIdsBySystemName);

        Assert.assertEquals("12345678-abcd-1234-abcd-1234567890EF", deviceIdentifiers.get(adidDeviceIdentifierTypeId));
        Assert.assertEquals("AAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaFFFFFF", deviceIdentifiers.get(adidMd5DeviceIdentifierTypeId));

    }
}
