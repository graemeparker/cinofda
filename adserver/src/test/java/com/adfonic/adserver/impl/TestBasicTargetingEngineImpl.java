package com.adfonic.adserver.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.impl.BasicTargetingEngineImpl.BidDerivedData;
import com.adfonic.adserver.impl.BasicTargetingEngineImpl.NoCreativesException;
import com.adfonic.adserver.plugin.PluginFillRateTracker;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Gender;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Segment.DayOfWeek;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.BrowserDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto.Type;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.VendorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RateCardDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCacheExt;
import com.adfonic.domain.cache.ext.AdserverDomainCacheImpl;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.util.AcceptedLanguages;
import com.adfonic.util.FastLinkedList;
import com.adfonic.util.Range;
import com.adfonic.util.Subnet;
import com.adfonic.util.stats.CounterManager;
import com.google.common.collect.Sets;

public class TestBasicTargetingEngineImpl extends BaseAdserverTest {

    private BasicTargetingEngineImpl basicTargetingEngineImpl;
    private DisplayTypeUtils displayTypeUtils;
    private FrequencyCapper frequencyCapper;
    private PluginFillRateTracker pluginFillRateTracker;
    private PluginManager pluginManager;
    private StatusChangeManager statusChangeManager;
    private StoppageManager stoppageManager;
    private DomainCache domainCache;
    private AdserverDomainCache adserverDomainCache;
    private TargetingContext context;
    private AdSpaceDto adSpace;
    private PublicationDto publication;
    private FastLinkedList<MutableWeightedCreative> reusablePool;
    private final int priority = 10;
    private CounterManager counterManager;
    private LocalBudgetManager budgetManager;
    //private DataCacheProperties dcProperties;
    DeviceLocationTargetingChecks geoChecks;
    DeviceIdentifierTargetingChecks didChecks;
    AdsquareTargetingChecks adsquareChecks;

    @Before
    public void initTests() {
        displayTypeUtils = mock(DisplayTypeUtils.class);
        frequencyCapper = mock(FrequencyCapper.class);
        pluginFillRateTracker = mock(PluginFillRateTracker.class);
        pluginManager = mock(PluginManager.class);
        statusChangeManager = mock(StatusChangeManager.class);
        stoppageManager = mock(StoppageManager.class);
        counterManager = mock(CounterManager.class);
        budgetManager = mock(LocalBudgetManager.class);
        geoChecks = mock(DeviceLocationTargetingChecks.class);
        didChecks = mock(DeviceIdentifierTargetingChecks.class);
        adsquareChecks = mock(AdsquareTargetingChecks.class);
        basicTargetingEngineImpl = new BasicTargetingEngineImpl(displayTypeUtils, pluginManager, stoppageManager, frequencyCapper, pluginFillRateTracker, statusChangeManager,
                counterManager, budgetManager, geoChecks, didChecks, adsquareChecks);
        context = mock(TargetingContext.class, "context");
        domainCache = mock(DomainCache.class);
        adserverDomainCache = mock(AdserverDomainCache.class);

        adSpace = mock(AdSpaceDto.class, "adSpace");
        publication = mock(PublicationDto.class, "publication");

        reusablePool = new FastLinkedList<MutableWeightedCreative>();
    }

    @Test
    public void testBasicTargetingEngineImpl02_selectCreative() {
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");

        final Boolean isPrivateNetwork = Boolean.TRUE;

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.PRIVATE_NETWORK);
                oneOf(listener).unfilledRequest(adSpace, context);
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.PRIVATE_NETWORK);
            }
        });

        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @Test
    public void testBasicTargetingEngineImpl03_selectCreative() {
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");

        final Boolean isPrivateNetwork = Boolean.FALSE;
        final Map<String, String> deviceProps = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_DEVICE_PROPS);

                oneOf(listener).unfilledRequest(adSpace, context);
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_DEVICE_PROPS);

            }
        });
        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @Test
    public void testBasicTargetingEngineImpl04_selectCreative() {
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");

        final Boolean isPrivateNetwork = Boolean.FALSE;
        final Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("mobileDevice", "0");

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NOT_MOBILE_DEVICE);

                oneOf(listener).unfilledRequest(adSpace, context);
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NOT_MOBILE_DEVICE);

            }
        });
        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @Test
    public void testBasicTargetingEngineImpl05_selectCreative() {
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");

        final Boolean isPrivateNetwork = Boolean.FALSE;
        final Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("mobileDevice", "1");

        final ModelDto model = null;

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);

                oneOf(listener).unfilledRequest(adSpace, context);
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);

            }
        });
        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @Test
    public void testBasicTargetingEngineImpl06_selectCreative() {
        final long adSpaceId = randomLong();
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");

        final Boolean isPrivateNetwork = Boolean.FALSE;
        final Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("mobileDevice", "1");

        final ModelDto model = mock(ModelDto.class, "model");
        final VendorDto vendor = mock(VendorDto.class, "vendor");
        final CountryDto country = null;
        final OperatorDto operator = null;
        final PlatformDto platform = null;
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final Set<Long> capabilityIds = null;
        final Medium medium = Medium.APPLICATION;
        final AdspaceWeightedCreative[] eligibleCreatives = new AdspaceWeightedCreative[0];

        expect(new Expectations() {
            {
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(model).isHidden();
                will(returnValue(false));
                allowing(vendor).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(model).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(model).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                oneOf(context).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                oneOf(context).getAttribute(TargetingContext.OPERATOR);
                will(returnValue(operator));
                oneOf(context).getAttribute(TargetingContext.PLATFORM);
                will(returnValue(platform));
                oneOf(context).getAttribute(TargetingContext.GENDER);
                will(returnValue(gender));
                oneOf(context).getAttribute(TargetingContext.AGE_RANGE);
                will(returnValue(ageRange));
                oneOf(context).getAttribute(TargetingContext.CAPABILITY_IDS);
                will(returnValue(capabilityIds));
                oneOf(context).getAttribute(TargetingContext.MEDIUM);
                will(returnValue(medium));
                allowing(context).getAttribute(TargetingContext.ECPM_FLOOR);
                will(returnValue(null));
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getEligibleCreatives(with(any(Long.class)));
                will(returnValue(eligibleCreatives));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_CREATIVES);

                oneOf(listener).attributesDerived(adSpace, context);
                oneOf(listener).creativesEligible(adSpace, context, eligibleCreatives);
                oneOf(listener).unfilledRequest(adSpace, context);
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(context).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                oneOf(context).getAttribute(TargetingContext.OPERATOR);
                will(returnValue(operator));
                oneOf(context).getAttribute(TargetingContext.PLATFORM);
                will(returnValue(platform));
                oneOf(context).getAttribute(TargetingContext.GENDER);
                will(returnValue(gender));
                oneOf(context).getAttribute(TargetingContext.AGE_RANGE);
                will(returnValue(ageRange));
                oneOf(context).getAttribute(TargetingContext.CAPABILITY_IDS);
                will(returnValue(capabilityIds));
                oneOf(context).getAttribute(TargetingContext.MEDIUM);
                will(returnValue(medium));
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getEligibleCreatives(with(any(Long.class)));
                will(returnValue(eligibleCreatives));

                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_CREATIVES);

            }
        });
        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NoCreativesException.class)
    public void testBasicTargetingEngineImpl07_targetAndSelectCreative() throws NoCreativesException {
        final Long[] wcs = new Long[0];
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final ModelDto model = mock(ModelDto.class, "model");
        final CountryDto country = null;
        final OperatorDto operator = null;
        final PlatformDto platform = null;
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final Set<Long> capabilityIds = null;
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");
        final Long countryId = 0L;
        final Set<Long> countryEligibleCreativeSet = new HashSet<Long>();
        countryEligibleCreativeSet.add(randomLong());
        final double globalRevenueFloor = 0.0;

        deviceProps.put("mobileDevice", "1");

        expect(new Expectations() {
            {
                oneOf(adserverDomainCache).getEligibleCreativeIdsForCountry(countryId);
                will(returnValue(countryEligibleCreativeSet));
                oneOf(adserverDomainCache).getDefaultDoubleValue("global_revenue_floor", 0.0);
                will(returnValue(globalRevenueFloor));

                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                oneOf(context).getAttribute(TargetingContext.DEBUG_CONTEXT);
                will(returnValue(null));

                oneOf(context).getAttribute(Parameters.ADOPS_KEY, String.class);
                will(returnValue(null));

                oneOf(listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
            }
        });

        BidDerivedData data = new BidDerivedData();
        data.allowedFormatIds = allowedFormatIds;
        data.deviceProps = deviceProps;
        data.model = model;
        data.country = country;
        data.operator = operator;
        data.platform = platform;
        data.gender = gender;
        data.ageRange = ageRange;
        data.capabilityIds = capabilityIds;
        data.medium = medium;
        data.ecpmFloor = null;
        data.strictlyUseFirstDisplayType = false;

        basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, context, data, diagnosticMode, timeLimit, listener);

    }

    @Test(expected = NoCreativesException.class)
    public void testBasicTargetingEngineImpl08_targetAndSelectCreative() throws NoCreativesException {
        final Long[] wcs = new Long[0];
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final ModelDto model = mock(ModelDto.class, "model");
        final CountryDto country = null;
        final OperatorDto operator = null;
        final PlatformDto platform = null;
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final Set<Long> capabilityIds = null;
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final Long countryId = 0L;
        final Set<Long> emptyCountryEligibleCreativeSet = new HashSet<Long>();
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final ByydImp imp = mock(ByydImp.class, "imp");

        deviceProps.put("mobileDevice", "1");

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getEligibleCreativeIdsForCountry(countryId);
                will(returnValue(emptyCountryEligibleCreativeSet));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                oneOf(context).getAttribute(Parameters.ADOPS_KEY, String.class);
                will(returnValue(null));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
            }
        });

        BidDerivedData data = new BidDerivedData();
        data.allowedFormatIds = allowedFormatIds;
        data.deviceProps = deviceProps;
        data.model = model;
        data.country = country;
        data.operator = operator;
        data.platform = platform;
        data.gender = gender;
        data.ageRange = ageRange;
        data.capabilityIds = capabilityIds;
        data.medium = medium;
        data.ecpmFloor = null;
        data.strictlyUseFirstDisplayType = false;

        basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, context, data, diagnosticMode, timeLimit, nullListener);
    }

    @Test(expected = NoCreativesException.class)
    public void testBasicTargetingEngineImpl09_targetAndSelectCreative() throws NoCreativesException {
        final Long[] wcs = new Long[1];
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final ModelDto model = mock(ModelDto.class, "model");
        final OperatorDto operator = null;
        final PlatformDto platform = null;
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final Set<Long> capabilityIds = null;
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");
        final long oneCreativeId = randomLong();
        wcs[0] = oneCreativeId;
        deviceProps.put("mobileDevice", "1");
        final boolean timeLimitExpired = true;
        final CountryDto country = new CountryDto();
        final Long countryId = randomLong();
        final Set<Long> emptyCountryEligibleCreativeSet = new HashSet<Long>();
        country.setId(countryId);
        emptyCountryEligibleCreativeSet.add(oneCreativeId);
        final double globalRevenueFloor = 0.0;

        expect(new Expectations() {
            {
                oneOf(adserverDomainCache).getEligibleCreativeIdsForCountry(countryId);
                will(returnValue(emptyCountryEligibleCreativeSet));
                oneOf(adserverDomainCache).getDefaultDoubleValue("global_revenue_floor", 0.0);
                will(returnValue(globalRevenueFloor));

                oneOf(timeLimit).hasExpired();
                will(returnValue(timeLimitExpired));
                oneOf(listener).timeLimitExpired(adSpace, context, timeLimit);
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(timeLimit).getDuration();
                will(returnValue(randomLong()));
                oneOf(context).getAttribute(Parameters.ADOPS_KEY, String.class);
                will(returnValue(null));

            }
        });

        BidDerivedData data = new BidDerivedData();
        data.allowedFormatIds = allowedFormatIds;
        data.deviceProps = deviceProps;
        data.model = model;
        data.country = country;
        data.operator = operator;
        data.platform = platform;
        data.gender = gender;
        data.ageRange = ageRange;
        data.capabilityIds = capabilityIds;
        data.medium = medium;
        data.ecpmFloor = null;
        data.strictlyUseFirstDisplayType = false;

        basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, context, data, diagnosticMode, timeLimit, listener);

    }

    @Test(expected = NoCreativesException.class)
    public void testBasicTargetingEngineImpl10_targetAndSelectCreative() throws NoCreativesException {
        final Long[] wcs = new Long[1];
        final Long oneCreativeId = randomLong();
        wcs[0] = oneCreativeId;
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final ModelDto model = mock(ModelDto.class, "model");
        final OperatorDto operator = null;
        final PlatformDto platform = null;
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final Set<Long> capabilityIds = null;
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = null;
        final ByydImp imp = mock(ByydImp.class, "imp");
        deviceProps.put("mobileDevice", "1");
        final boolean timeLimitExpired = true;

        final CountryDto country = new CountryDto();
        final Long countryId = randomLong();
        final Set<Long> emptyCountryEligibleCreativeSet = new HashSet<Long>();
        country.setId(countryId);
        emptyCountryEligibleCreativeSet.add(oneCreativeId);
        final double globalRevenueFloor = 0.0;

        expect(new Expectations() {
            {
                oneOf(adserverDomainCache).getEligibleCreativeIdsForCountry(countryId);
                will(returnValue(emptyCountryEligibleCreativeSet));
                oneOf(adserverDomainCache).getDefaultDoubleValue("global_revenue_floor", 0.0);
                will(returnValue(globalRevenueFloor));
                oneOf(timeLimit).hasExpired();
                will(returnValue(timeLimitExpired));
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                allowing(timeLimit).getDuration();
                will(returnValue(randomLong()));
                oneOf(context).getAttribute(Parameters.ADOPS_KEY, String.class);
                will(returnValue(null));

            }
        });

        BidDerivedData data = new BidDerivedData();
        data.allowedFormatIds = allowedFormatIds;
        data.deviceProps = deviceProps;
        data.model = model;
        data.country = country;
        data.operator = operator;
        data.platform = platform;
        data.gender = gender;
        data.ageRange = ageRange;
        data.capabilityIds = capabilityIds;
        data.medium = medium;
        data.ecpmFloor = null;
        data.strictlyUseFirstDisplayType = false;

        basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, context, data, diagnosticMode, timeLimit, listener);

    }

    /*
     * When given operator is null 
     */
    @Test
    public void testCheckConnectionType01() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = new SegmentDto();
        final OperatorDto operator = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.OPERATOR;
        segment.setConnectionType(ct);
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ConnectionTypeMismatch,
                        "Device connection: wifi & Segment marked: " + segment.getId());
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.ConnectionTypeMismatch);
    }

    /*
     * When given operator is null 
     * and wifi is not allowed by the segment
     * and listener is not null
     */
    @Test
    public void testCheckConnectionType01_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = new SegmentDto();
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.OPERATOR;
        segment.setConnectionType(ct);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ConnectionTypeMismatch,
                        "Device connection: wifi & Segment marked: " + segment.getId());
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.ConnectionTypeMismatch);
    }

    /*
     * When given operator is NOT null 
     * and operator is no allowed by the segment
     */
    @Test
    public void testCheckConnectionType02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.WIFI;
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.ConnectionTypeMismatch);
    }

    /*
     * When given operator is NOT null 
     * and operator is no allowed by the segment
     * and listener is not null
     */
    @Test
    public void testCheckConnectionType02_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = new SegmentDto();
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.WIFI;
        segment.setConnectionType(ct);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ConnectionTypeMismatch,
                        "Device connection: operator & Segment marked: " + segment.getId());
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.ConnectionTypeMismatch);
    }

    /*
     * When given MOBILE operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is empty
     */
    @Test
    public void testCheckMOBILEConnectionType03() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final Set<Long> operatorIds = new HashSet<Long>();
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                oneOf(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
     * When given MOBILE operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is NON empty and it contains the given operator's ID
     * and segment.getOperatorListIsWhitelist is true
     */
    @Test
    public void testCheckMOBILEConnectionType04() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
    * When given MOBILE operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is true
    */
    @Test
    public void testCheckMOBILEConnectionType05() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorNotWhitelisted);
    }

    /*
    * When given MOBILE operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is true
    * and listener is not null
    */
    @Test
    public void testCheckMOBILEConnectionType05_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        final String operatorName = "Some Operator";
        final long segmentId = randomLong();

        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(segment).getId();
                will(returnValue(segmentId));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(operator).getName();
                will(returnValue(operatorName));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
                final String eMsg = "Operator not whitelisted: " + operatorName + " & Segment marked: " + segmentId;
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorNotWhitelisted, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorNotWhitelisted);
    }

    /*
    * When given MOBILE operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it contains the given operator's ID
    * and segment.getOperatorListIsWhitelist is false
    */
    @Test
    public void testCheckMOBILEConnectionType06() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorBlacklisted);

    }

    /*
     * When given MOBILE operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is NON empty and it contains the given operator's ID
     * and segment.getOperatorListIsWhitelist is false
     * and listener is not null
     */
    @Test
    public void testCheckMOBILEConnectionType06_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        final String operatorName = "Some Operator";
        final long segmentId = randomLong();
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(segment).getId();
                will(returnValue(segmentId));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(operator).getName();
                will(returnValue(operatorName));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
                final String eMsg = "Operator blacklisted: " + operatorName + " & Segment marked: " + segmentId;
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorBlacklisted, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorBlacklisted);
    }

    /*
    * When given MOBILE operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is false
    */
    @Test
    public void testCheckMOBILEConnectionType07() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean mobileOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(true));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getMobileOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getMobileOperatorListIsWhitelist();
                will(returnValue(mobileOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
     * When given ISP operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is empty
     */
    @Test
    public void testCheckISPConnectionType03() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final Set<Long> operatorIds = new HashSet<Long>();
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                oneOf(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
     * When given ISP operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is NON empty and it contains the given operator's ID
     * and segment.getOperatorListIsWhitelist is true
     */
    @Test
    public void testCheckISPConnectionType04() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
    * When given ISP operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is true
    */
    @Test
    public void testCheckISPConnectionType05() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorNotWhitelisted);
    }

    /*
    * When given ISP operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is true
    * and listener is not null
    */
    @Test
    public void testCheckISPConnectionType05_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = true;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        final String operatorName = "Some Operator";
        final long segmentId = randomLong();

        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(segment).getId();
                will(returnValue(segmentId));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(operator).getName();
                will(returnValue(operatorName));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
                final String eMsg = "Operator not whitelisted: " + operatorName + " & Segment marked: " + segmentId;
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorNotWhitelisted, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorNotWhitelisted);
    }

    /*
    * When given ISP operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it contains the given operator's ID
    * and segment.getOperatorListIsWhitelist is false
    */
    @Test
    public void testCheckISPConnectionType06() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorBlacklisted);
    }

    /*
     * When given ISP operator is NOT null 
     * and operator is allowed by the segment
     * and segment operator ids set is NON empty and it contains the given operator's ID
     * and segment.getOperatorListIsWhitelist is false
     * and listener is not null
     */
    @Test
    public void testCheckISPConnectionType06_02() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        operatorIds.add(operatorId);
        final String operatorName = "Some Operator";
        final long segmentId = randomLong();
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(segment).getId();
                will(returnValue(segmentId));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(operator).getName();
                will(returnValue(operatorName));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
                final String eMsg = "Operator blacklisted: " + operatorName + " & Segment marked: " + segmentId;
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OperatorBlacklisted, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, CreativeEliminatedReason.OperatorBlacklisted);
    }

    /*
    * When given ISP operator is NOT null
    * and operator is allowed by the segment
    * and segment operator ids set is NON empty and it DO NOT contains given operator's ID
    * and segment.getOperatorListIsWhitelist is false
    */
    @Test
    public void testCheckISPConnectionType07() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = mock(OperatorDto.class, "operatorDto");
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.BOTH;
        final boolean ispOperatorListIsWhitelist = false;
        final Set<Long> operatorIds = new HashSet<Long>();
        final long operatorId = randomLong();
        final long otherOperatorId = operatorId + 1;
        operatorIds.add(otherOperatorId);
        expect(new Expectations() {
            {
                oneOf(operator).isMobileOperator();
                will(returnValue(false));
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
                allowing(segment).getIspOperatorIds();
                will(returnValue(operatorIds));
                allowing(operator).getId();
                will(returnValue(operatorId));
                allowing(segment).getIspOperatorListIsWhitelist();
                will(returnValue(ispOperatorListIsWhitelist));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, operator, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment has empty vendor and device set
    * i.e. no particular vendor or device targetted
    */
    @Test
    public void testCheckVendorsAndDevice01() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);

    }

    /*
    * test when segment has empty vendor  but device set is not empty
    * and given device is null
    * i.e. no particular vendor is targetted, but some devices are targetted
    */
    @Test
    public void testCheckVendorsAndDevice02() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceModelMismatch);
    }

    /*
     * test when segment has empty vendor  but device set is not empty
     * and given device id DO exists in segment device targetted list
     * i.e. no particular vendor is targetted, but some devices are targetted
     */
    @Test
    public void testCheckVendorsAndDevice03() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getId();
                will(returnValue(deviceId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment has empty vendor  but device set is not empty
    * and given device id DO NOT exists in segment device targetted list
    * i.e. no particular vendor is targetted, but some devices are targetted
    */
    @Test
    public void testCheckVendorsAndDevice04() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long deviceId = randomLong();
        final long anotherDeviceId = deviceId + 1;
        modelIds.add(anotherDeviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getId();
                will(returnValue(deviceId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceModelMismatch);
    }

    /*
    * test when segment has vendor  but device set is empty
    * and given device is null
    */
    @Test
    public void testCheckVendorsAndDevice05() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorMismatch);
    }

    /*
    * test when segment has vendor  but device set is empty
    * and given device is null
    * and listener is not null
    */
    @Test
    public void testCheckVendorsAndDevice05_02() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceVendorMismatch, "vendor");
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorMismatch);
    }

    /*
     * test when segment has vendor  but device set is empty
     * and given device is NOT null, but vendor of device is null
     */
    @Test
    public void testCheckVendorsAndDevice06() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = null;
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorMismatch);

    }

    /*
     * test when segment has  vendor  but device set is empty
     * and given vendor id of device DO exists in segment vendor targetted list
     */
    @Test
    public void testCheckVendorsAndDevice07() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = mock(VendorDto.class, "vendorDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(vendor).getId();
                will(returnValue(vendorId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment has empty vendor  but device set is not empty
    * and given device id DO NOT exists in segment device targetted list
    * i.e. no particular vendor is targetted, but some devices are targetted
    */
    @Test
    public void testCheckVendorsAndDevice08() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = mock(VendorDto.class, "vendorDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        final long anotherVendorId = vendorId + 1;
        vendorIds.add(anotherVendorId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(vendor).getId();
                will(returnValue(vendorId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorMismatch);
    }

    /*
    * test when segment and device both are targetted
    * and give model is null
    */
    @Test
    public void testCheckVendorsAndDevice09() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorModelMismatch);
    }

    /*
     * test when segment and device both are targetted
     * and give model is null
     * and listener is not null
     */
    @Test
    public void testCheckVendorsAndDevice09_01() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceVendorModelMismatch, "vendor/model");
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorModelMismatch);
    }

    /*
     * test when segment and device both are targetted
     * and give model is not null but vendor is null
     * also given device do exists in segment model list
     */
    @Test
    public void testCheckVendorsAndDevice10() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = null;
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(model).getId();
                will(returnValue(deviceId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment and device both are targetted
    * and give model is not null but vendor is not null
    * also given device do exists in segment model list
    * but given vendor do not exists in segment vendor list
    */
    @Test
    public void testCheckVendorsAndDevice11() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = mock(VendorDto.class, "vendorDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        final long anotherVendorId = randomLong();
        vendorIds.add(anotherVendorId);
        final long deviceId = randomLong();
        modelIds.add(deviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(model).getId();
                will(returnValue(deviceId));
                allowing(vendor).getId();
                will(returnValue(vendorId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment and device both are targetted
    * and give model is not null but vendor is not null
    * also given device do not exists in segment model list
    * but given vendor do exists in segment vendor list
    */
    @Test
    public void testCheckVendorsAndDevice12() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = mock(VendorDto.class, "vendorDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        vendorIds.add(vendorId);
        final long deviceId = randomLong();
        final long anotherDeviceId = randomLong();
        modelIds.add(anotherDeviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(model).getId();
                will(returnValue(deviceId));
                allowing(vendor).getId();
                will(returnValue(vendorId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when segment and device both are targetted
    * and give model is not null but vendor is not null
    * also given device do not exists in segment model list
    * but given vendor do not exists in segment vendor list
    */
    @Test
    public void testCheckVendorsAndDevice13() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final VendorDto vendor = mock(VendorDto.class, "vendorDto");
        final TargetingEventListener listener = null;
        final Set<Long> vendorIds = new HashSet<Long>();
        final Set<Long> modelIds = new HashSet<Long>();
        final long vendorId = randomLong();
        final long anotherVendorId = vendorId + 1;
        vendorIds.add(anotherVendorId);
        final long deviceId = randomLong();
        final long anotherDeviceId = deviceId + 1;
        modelIds.add(anotherDeviceId);
        expect(new Expectations() {
            {
                allowing(segment).getVendorIds();
                will(returnValue(vendorIds));
                allowing(segment).getModelIds();
                will(returnValue(modelIds));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(model).getId();
                will(returnValue(deviceId));
                allowing(vendor).getId();
                will(returnValue(vendorId));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceVendorModelMismatch);
    }

    /*
     * Test when allowedFormat id collection is empty
     */
    @Test
    public void testCheckAllowedFormats01() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final TargetingEventListener listener = null;
        final Collection<Long> allowedFormatIds = null;
        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAllowedFormats(adSpace, context, creative, allowedFormatIds, listener));
    }

    /*
    * Test when allowedFormat id collection is empty
    */
    @Test
    public void testCheckAllowedFormats02() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final TargetingEventListener listener = null;
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAllowedFormats(adSpace, context, creative, allowedFormatIds, listener));
    }

    /*
    * Test when allowedFormat id collection is Non empty
    * and creative formatid do exists in allowedFormatids
    */
    @Test
    public void testCheckAllowedFormats03() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final TargetingEventListener listener = null;
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final long creativeFormatId = randomLong();
        allowedFormatIds.add(creativeFormatId);
        expect(new Expectations() {
            {
                allowing(creative).getFormatId();
                will(returnValue(creativeFormatId));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAllowedFormats(adSpace, context, creative, allowedFormatIds, listener));
    }

    /*
    * Test when allowedFormat id collection is Non empty
    * and creative formatid DO NOT exists in allowedFormatids
    */
    @Test
    public void testCheckAllowedFormats04() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final TargetingEventListener listener = null;
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final long creativeFormatId = randomLong();
        final long anotherCreativeFormatId = creativeFormatId + 1;
        allowedFormatIds.add(anotherCreativeFormatId);
        expect(new Expectations() {
            {
                allowing(creative).getFormatId();
                will(returnValue(creativeFormatId));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAllowedFormats(adSpace, context, creative, allowedFormatIds, listener));
    }

    /*
    * Test when allowedFormat id collection is Non empty
    * and creative formatid DO NOT exists in allowedFormatids
    * and listener is not null
    */
    @Test
    public void testCheckAllowedFormats05() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final long creativeFormatId = randomLong();
        final long anotherCreativeFormatId = creativeFormatId + 1;
        allowedFormatIds.add(anotherCreativeFormatId);
        expect(new Expectations() {
            {
                allowing(creative).getFormatId();
                will(returnValue(creativeFormatId));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.formatNotAllowed,
                        "Format " + creativeFormatId + " not in " + allowedFormatIds);
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAllowedFormats(adSpace, context, creative, allowedFormatIds, listener));
    }

    /*
     * Test when Segment Browserids are empty
     */
    @Test
    public void testCheckBrowsers01() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final TargetingEventListener listener = null;
        final Set<Long> segmentBrowserIds = new HashSet<Long>();
        final Map<Long, Boolean> segmentBrowserElegibilityIds = new HashMap<Long, Boolean>();
        expect(new Expectations() {
            {
                allowing(segment).getBrowserIds();
                will(returnValue(segmentBrowserIds));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentBrowserElegibilityIds);
        assertEquals(elimination, null);
    }

    /*
     * Test when segment contains only one browser and that matches with give request context
     */
    @Test
    public void testCheckBrowsers02() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final TargetingEventListener listener = null;
        final Set<Long> segmentBrowserIds = new HashSet<Long>();
        final long browserId = randomLong();
        segmentBrowserIds.add(browserId);
        final BrowserDto browser = mock(BrowserDto.class, "browserDto");
        final Boolean isBrowserMtached = true;
        final Map<Long, Boolean> segmentBrowserElegibilityIds = new HashMap<Long, Boolean>();
        expect(new Expectations() {
            {
                allowing(segment).getBrowserIds();
                will(returnValue(segmentBrowserIds));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getBrowserById(browserId);
                will(returnValue(browser));
                allowing(browser).isMatch(context);
                will(returnValue(isBrowserMtached));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentBrowserElegibilityIds);
        assertEquals(elimination, null);
    }

    /*
    * Test when segment contains two browser and all matches with give request context
    */
    @Test
    public void testCheckBrowsers03() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final TargetingEventListener listener = null;
        final Set<Long> segmentBrowserIds = new HashSet<Long>();
        final long browserId = randomLong();
        segmentBrowserIds.add(browserId);
        final long anotherBrowserId = randomLong();
        segmentBrowserIds.add(browserId);
        segmentBrowserIds.add(anotherBrowserId);
        final BrowserDto browser = mock(BrowserDto.class, "browserDto");
        final BrowserDto anotherBrowser = mock(BrowserDto.class, "anotherBrowserDto");
        final Boolean isBrowserMatched = true;
        final Boolean isAnotherBrowserMatched = true;
        final Map<Long, Boolean> segmentBrowserElegibilityIds = new HashMap<Long, Boolean>();
        expect(new Expectations() {
            {
                allowing(segment).getBrowserIds();
                will(returnValue(segmentBrowserIds));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getBrowserById(browserId);
                will(returnValue(browser));
                allowing(domainCache).getBrowserById(anotherBrowserId);
                will(returnValue(anotherBrowser));
                allowing(browser).isMatch(context);
                will(returnValue(isBrowserMatched));
                allowing(anotherBrowser).isMatch(context);
                will(returnValue(isAnotherBrowserMatched));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentBrowserElegibilityIds);
        assertEquals(elimination, null);
    }

    /*
     * Test when segment contains two browser and one do not match with give request context
     */
    @Test
    public void testCheckBrowsers04() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final TargetingEventListener listener = null;
        final Set<Long> segmentBrowserIds = new HashSet<Long>();
        final long browserId = randomLong();
        segmentBrowserIds.add(browserId);
        final long anotherBrowserId = randomLong();
        segmentBrowserIds.add(browserId);
        segmentBrowserIds.add(anotherBrowserId);
        final BrowserDto browser = mock(BrowserDto.class, "browserDto");
        final BrowserDto anotherBrowser = mock(BrowserDto.class, "anotherBrowserDto");
        final Boolean isBrowserMatched = true;
        final Boolean isAnotherBrowserMatched = false;
        final Map<Long, Boolean> segmentBrowserElegibilityIds = new HashMap<Long, Boolean>();
        expect(new Expectations() {
            {
                allowing(segment).getBrowserIds();
                will(returnValue(segmentBrowserIds));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getBrowserById(browserId);
                will(returnValue(browser));
                allowing(domainCache).getBrowserById(anotherBrowserId);
                will(returnValue(anotherBrowser));
                allowing(browser).isMatch(context);
                will(returnValue(isBrowserMatched));
                allowing(anotherBrowser).isMatch(context);
                will(returnValue(isAnotherBrowserMatched));
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentBrowserElegibilityIds);
        assertEquals(elimination, CreativeEliminatedReason.BrowsersMismatch);
    }

    /*
     * Test when segment contains two browser and one do not match with give request context
     * and listener is not null
     */
    @Test
    public void testCheckBrowsers04_02() {
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> segmentBrowserIds = new HashSet<Long>();
        final long browserId = randomLong();
        segmentBrowserIds.add(browserId);
        final long anotherBrowserId = randomLong();
        segmentBrowserIds.add(browserId);
        segmentBrowserIds.add(anotherBrowserId);
        final BrowserDto browser = mock(BrowserDto.class, "browserDto");
        final BrowserDto anotherBrowser = mock(BrowserDto.class, "anotherBrowserDto");
        final Boolean isBrowserMatched = true;
        final Boolean isAnotherBrowserMatched = false;
        final Map<Long, Boolean> segmentBrowserElegibilityIds = new HashMap<Long, Boolean>();
        final long segmentId = randomLong();

        final String eMsg = "Not target browser: " + segmentBrowserIds + " & Segment marked: " + segmentId;
        expect(new Expectations() {
            {
                allowing(segment).getBrowserIds();
                will(returnValue(segmentBrowserIds));
                allowing(segment).getId();
                will(returnValue(segmentId));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getBrowserById(browserId);
                will(returnValue(browser));
                allowing(domainCache).getBrowserById(anotherBrowserId);
                will(returnValue(anotherBrowser));
                allowing(browser).isMatch(context);
                will(returnValue(isBrowserMatched));
                allowing(anotherBrowser).isMatch(context);
                will(returnValue(isAnotherBrowserMatched));

                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.BrowsersMismatch, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentBrowserElegibilityIds);
        assertEquals(elimination, CreativeEliminatedReason.BrowsersMismatch);
    }

    /*
     * Test when DisplayType is null for given format
     */
    @Test
    public void testCheckDisplayTypeAndAssetBundle01() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = null;
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = null;

        creative.setFormatId(creativeFormatId);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
     * Test when DisplayType is null for given format
     * and listener is not null
     */
    @Test
    public void testCheckDisplayTypeAndAssetBundle01_02() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = null;

        creative.setFormatId(creativeFormatId);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.displayTypeNotPresent, "No DisplayType");
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
    * Test when DisplayType is NOT null for given format
    * and allDisplayType List for given Format are empty
    */
    @Test
    public void testCheckDisplayTypeAndAssetBundle02() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = null;
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = new DisplayTypeDto();
        final List<String> mimeTypes = null;
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>();
        creative.setFormatId(creativeFormatId);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));

                allowing(context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST);
                will(returnValue(mimeTypes));
                allowing(displayTypeUtils).getAllDisplayTypes(format, context);
                will(returnValue(allDisplayTypes));
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
     * Test when DisplayType is NOT null for given format
     * and allDisplayType List for given Format are non empty
     * and creative has asset 
     */
    @Test
    public void testCheckDisplayTypeAndAssetBundle03() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = null;
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = new DisplayTypeDto();
        final List<String> mimeTypes = null;
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>();
        final long displayTypeId = randomLong();
        final long componentId = randomLong();
        final AssetDto asset = new AssetDto();
        final long contentTypeId = randomLong();
        creative.setFormatId(creativeFormatId);
        displayType.setId(displayTypeId);
        allDisplayTypes.add(displayType);
        creative.setAsset(displayTypeId, componentId, asset, contentTypeId);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));

                allowing(context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST);
                will(returnValue(mimeTypes));
                allowing(displayTypeUtils).getAllDisplayTypes(format, context);
                will(returnValue(allDisplayTypes));
                allowing(displayTypeUtils).setDisplayType(format, context, displayType);
            }
        });

        assertTrue(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
    * Test when DisplayType is NOT null for given format
    * and allDisplayType List for given Format are non empty
    * and creative DO NOT have asset
    */
    @Test
    public void testCheckDisplayTypeAndAssetBundle04() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = null;
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = new DisplayTypeDto();
        final List<String> mimeTypes = null;
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>();
        final long displayTypeId = randomLong();
        creative.setFormatId(creativeFormatId);
        displayType.setId(displayTypeId);
        allDisplayTypes.add(displayType);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));

                allowing(context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST);
                will(returnValue(mimeTypes));
                allowing(displayTypeUtils).getAllDisplayTypes(format, context);
                will(returnValue(allDisplayTypes));
                allowing(displayTypeUtils).setDisplayType(format, context, displayType);
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
    * Test when DisplayType is NOT null for given format
    * and allDisplayType List for given Format are non empty
    * and creative DO NOT have asset
    * and listener is not null
    */
    @Test
    public void testCheckDisplayTypeAndAssetBundle04_02() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = new DisplayTypeDto();
        final List<String> mimeTypes = null;
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>();
        final long displayTypeId = randomLong();
        creative.setFormatId(creativeFormatId);
        displayType.setId(displayTypeId);
        allDisplayTypes.add(displayType);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));

                allowing(context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST);
                will(returnValue(mimeTypes));
                allowing(displayTypeUtils).getAllDisplayTypes(format, context);
                will(returnValue(allDisplayTypes));
                allowing(displayTypeUtils).setDisplayType(format, context, displayType);

                String message = "No Asset for Format: " + format.getId() + "-" + format.getSystemName() + " DisplayType: " + displayType.getId() + "-"
                        + displayType.getSystemName() + " MimeTypes: " + mimeTypes;
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoAssetBundle, message);
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
    * Test when DisplayType is NOT null for given format
    * and allDisplayType List for given Format are non empty
    * and creative DO NOT have asset
    * and strictlyUseFirstDisplayType = true
    */
    @Test
    public void testCheckDisplayTypeAndAssetBundle05() {
        final CreativeDto creative = new CreativeDto();
        final long creativeFormatId = randomLong();
        final FormatDto format = new FormatDto();
        final TargetingEventListener listener = null;
        final boolean strictlyUseFirstDisplayType = false;
        final DisplayTypeDto displayType = new DisplayTypeDto();
        final List<String> mimeTypes = null;
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>();
        final long displayTypeId = randomLong();
        creative.setFormatId(creativeFormatId);
        displayType.setId(displayTypeId);
        allDisplayTypes.add(displayType);

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getFormatById(creativeFormatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, context);
                will(returnValue(displayType));

                allowing(context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST);
                will(returnValue(mimeTypes));
                allowing(displayTypeUtils).getAllDisplayTypes(format, context);
                will(returnValue(allDisplayTypes));
                allowing(displayTypeUtils).setDisplayType(format, context, displayType);
            }
        });

        assertFalse(basicTargetingEngineImpl.checkDisplayTypeAndAssetBundle(adSpace, context, creative, strictlyUseFirstDisplayType, listener));
    }

    /*
    * test when destination type is NOT SMS
    */
    @Test
    public void testCheckSmsSupport01() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.UNKNOWN);

        expect(new Expectations() {
            {
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
     * test when destination type is SMS
     * and SMS Ok is true in conext
     */
    @Test
    public void testCheckSmsSupport02() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = true;
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
    * test when destination type is SMS
    * and SMS Ok is false in conext
    */
    @Test
    public void testCheckSmsSupport03() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = false;
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
     * test when destination type is SMS
     * and SMS Ok is null in conext and IntegrationType is null
     */
    @Test
    public void testCheckSmsSupport04() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = null;
        final IntegrationTypeDto integrationType = null;
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
                allowing(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                allowing(context).setAttribute(DeviceFeaturesTargetingChecks.SMS_OK, false);
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
    * test when destination type is NOT SMS
    * and SMS Ok is null in conext and IntegrationType is NOT null
    * and integrationType do not support SMS feature
    */
    @Test
    public void testCheckSmsSupport05() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = null;
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
                allowing(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                allowing(context).setAttribute(DeviceFeaturesTargetingChecks.SMS_OK, false);
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
    * test when destination type is NOT SMS
    * and SMS Ok is null in conext and IntegrationType is NOT null
    * and integrationType do not support SMS feature
    * and listener is not null
    */
    @Test
    public void testCheckSmsSupport05_02() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = null;
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
                allowing(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                allowing(context).setAttribute(DeviceFeaturesTargetingChecks.SMS_OK, false);
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.smsNotPresent, "!sms");
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
    * test when destination type is NOT SMS
    * and SMS Ok is null in conext and IntegrationType is NOT null
    * and integrationType do support SMS feature
    */
    @Test
    public void testCheckSmsSupport06() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.SMS);
        final Boolean smsOk = null;
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        integrationType.getSupportedFeatures().add(Feature.SMS);
        expect(new Expectations() {
            {
                allowing(context).getAttribute(DeviceFeaturesTargetingChecks.SMS_OK, Boolean.class);
                will(returnValue(smsOk));
                allowing(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                allowing(context).setAttribute(DeviceFeaturesTargetingChecks.SMS_OK, true);
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener));
    }

    /*
     * Test when Creative is not plugin based
     */
    @Test
    public void testCheckPluginBased01() {
        final CreativeDto creative = new CreativeDto();
        final ModelDto model = new ModelDto();
        final TargetingEventListener listener = null;
        final boolean isPluginBased = false;
        creative.setPluginBased(isPluginBased);
        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkPluginBased(adSpace, context, creative, model, listener));
    }

    /*
     * Test when Creative is plugin based
     * and model is not null
     */
    @Test
    public void testCheckPluginBased02() {
        final CreativeDto creative = new CreativeDto();
        final ModelDto model = new ModelDto();
        final TargetingEventListener listener = null;
        final boolean isPluginBased = true;
        creative.setPluginBased(isPluginBased);
        expect(new Expectations() {
            {
                allowing(context).getAttribute(TargetingContext.BLOCK_PLUGINS);
                will(returnValue(null));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkPluginBased(adSpace, context, creative, model, listener));
    }

    /*
     * Test when Creative is plugin based
     * and model is null
     */
    @Test
    public void testCheckPluginBased03() {
        final CreativeDto creative = new CreativeDto();
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        final boolean isPluginBased = true;
        creative.setPluginBased(isPluginBased);
        expect(new Expectations() {
            {
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkPluginBased(adSpace, context, creative, model, listener));
    }

    /*
     * Test when Creative is plugin based
     * and model is null
     * and listener is not null
     */
    @Test
    public void testCheckPluginBased03_02() {
        final CreativeDto creative = new CreativeDto();
        final ModelDto model = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final boolean isPluginBased = true;
        creative.setPluginBased(isPluginBased);
        expect(new Expectations() {
            {
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.modelNotPresentForBackfill, "backfill needs model");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkPluginBased(adSpace, context, creative, model, listener));
    }

    /*
     * Test when destinationType is NOT DestinationType.CALL
     */
    @Test
    public void testCheckClickToCallSupport01() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.UNKNOWN);
        final TargetingEventListener listener = null;
        Map<String, String> deviceProps = null;
        expect(new Expectations() {
            {
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, deviceProps, listener));
    }

    /*
     * Test when destinationType is DestinationType.CALL
     * and deviceProps do NOT have property uriSchemeTel
     */
    @Test
    public void testCheckClickToCallSupport02() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.CALL);
        final TargetingEventListener listener = null;
        Map<String, String> deviceProps = new HashMap<String, String>();
        expect(new Expectations() {
            {
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, deviceProps, listener));
    }

    /*
    * Test when destinationType is DestinationType.CALL
    * and deviceProps do have property uriSchemeTel but value not equal to 1
    */
    @Test
    public void testCheckClickToCallSupport03() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.CALL);
        final TargetingEventListener listener = null;
        Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("uriSchemeTel", "12");
        expect(new Expectations() {
            {
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, deviceProps, listener));
    }

    /*
    * Test when destinationType is DestinationType.CALL
    * and deviceProps do have property uriSchemeTel but value not equal to 1
    * and listener is not null
    */
    @Test
    public void testCheckClickToCallSupport03_02() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.CALL);
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("uriSchemeTel", "12");

        final String eMsg = "Device can't do click-to-call";
        expect(new Expectations() {
            {
                allowing(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notClickToCallDevice, eMsg);
            }
        });

        assertFalse(DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, deviceProps, listener));
    }

    /*
    * Test when destinationType is DestinationType.CALL
    * and deviceProps do have property uriSchemeTel and value equal to 1
    */
    @Test
    public void testCheckClickToCallSupport04() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.CALL);
        final TargetingEventListener listener = null;
        Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("uriSchemeTel", "1");
        expect(new Expectations() {
            {
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, deviceProps, listener));
    }

    /*
    * Test when Blocked category ID set is null
    */
    @Test
    public void testCheckBlockedCategories01() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        creative.setDestination(destination);
        destination.setDestinationType(DestinationType.CALL);
        final TargetingEventListener listener = null;
        final Set<Long> blockedCategoryIds = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS);
                will(returnValue(blockedCategoryIds));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkBlockedCategories(adSpace, context, creative, listener));
    }

    /*
    * test when creative extendedCreativeTypeId is null
    */
    @Test
    public void testCheckExtendedCapabilities01() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = null;
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * test when creative extendedCreativeTypeId is null
    * and derived integrationType is NOT null
    */
    @Test
    public void testCheckExtendedCapabilities02() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final IntegrationTypeDto integrationType = null;
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
     * test when creative extendedCreativeTypeId is NOT null
     * and derived integrationType is null
     * and listener is not null
     */
    @Test
    public void testCheckExtendedCapabilities02_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final IntegrationTypeDto integrationType = null;
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoIntegrationType, "ExtendedCreativeType but not IntegrationType");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * test when creative extendedCreativeTypeId is NOT null
    * and derived integrationType is NOT null
    * and markupAvaiable is false
    */
    @Test
    public void testCheckExtendedCapabilities03() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final ExtendedCreativeTypeDto extendedCreativeType = new ExtendedCreativeTypeDto();
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        final boolean markupAvailable = false;
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        extendedCreativeType.setMediaType(MediaType.HTML);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getExtendedCreativeTypeById(extendedCreativeTypeId);
                will(returnValue(extendedCreativeType));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(context).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(markupAvailable));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * test when creative extendedCreativeTypeId is NOT null
    * and derived integrationType is NOT null
    * and markupAvaiable is false
    * and listener is not null
    */
    @Test
    public void testCheckExtendedCapabilities03_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final ExtendedCreativeTypeDto extendedCreativeType = new ExtendedCreativeTypeDto();
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        final boolean markupAvailable = false;
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        extendedCreativeType.setMediaType(MediaType.HTML);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getExtendedCreativeTypeById(extendedCreativeTypeId);
                will(returnValue(extendedCreativeType));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(context).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(markupAvailable));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notAvailableMediaType,
                        "MediaType." + extendedCreativeType.getMediaType() + " requires markup");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
     * test when creative extendedCreativeTypeId is NOT null
     * and derived integrationType is NOT null
     * and markupAvaiable is true
     * restrictToContentForms is Empty
     */
    @Test
    public void testCheckExtendedCapabilities04() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final ExtendedCreativeTypeDto extendedCreativeType = new ExtendedCreativeTypeDto();
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        final boolean markupAvailable = true;
        final Set<ContentForm> restrictToContentForms = new HashSet<ContentForm>();
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        extendedCreativeType.setMediaType(MediaType.HTML);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getExtendedCreativeTypeById(extendedCreativeTypeId);
                will(returnValue(extendedCreativeType));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(context).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(markupAvailable));
                oneOf(context).getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET);
                will(returnValue(restrictToContentForms));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * test when creative extendedCreativeTypeId is NOT null
    * and derived integrationType is NOT null
    * and markupAvaiable is true
    * restrictToContentForms is non empty
    */
    @Test
    public void testCheckExtendedCapabilities05() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final ExtendedCreativeTypeDto extendedCreativeType = new ExtendedCreativeTypeDto();
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        final boolean markupAvailable = true;
        final Set<ContentForm> restrictToContentForms = new HashSet<ContentForm>();
        restrictToContentForms.add(ContentForm.ADFONIC_MACRO);
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        extendedCreativeType.setMediaType(MediaType.HTML);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getExtendedCreativeTypeById(extendedCreativeTypeId);
                will(returnValue(extendedCreativeType));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(context).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(markupAvailable));
                oneOf(context).getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET);
                will(returnValue(restrictToContentForms));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
     * test when creative extendedCreativeTypeId is NOT null
     * and derived integrationType is NOT null
     * and markupAvaiable is true
     * restrictToContentForms is non empty
     * and listener is not null
     */
    @Test
    public void testCheckExtendedCapabilities05_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final Long extendedCreativeTypeId = randomLong();
        final ExtendedCreativeTypeDto extendedCreativeType = new ExtendedCreativeTypeDto();
        final IntegrationTypeDto integrationType = new IntegrationTypeDto();
        final boolean markupAvailable = true;
        final Set<ContentForm> restrictToContentForms = new HashSet<ContentForm>();
        restrictToContentForms.add(ContentForm.ADFONIC_MACRO);
        creative.setExtendedCreativeTypeId(extendedCreativeTypeId);
        extendedCreativeType.setMediaType(MediaType.HTML);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(null));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getExtendedCreativeTypeById(extendedCreativeTypeId);
                will(returnValue(extendedCreativeType));
                oneOf(context).getAttribute(TargetingContext.INTEGRATION_TYPE);
                will(returnValue(integrationType));
                oneOf(context).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(markupAvailable));
                oneOf(context).getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET);
                will(returnValue(restrictToContentForms));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.missingContentForm,
                        "!static ExtendedCreativeType: null/HTML, Templates: [] vs IntegrationType: null, BidContentForms: [ADFONIC_MACRO]");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * creative.extendedCreativeTypeId is not null (it is an extended creative)
    * BLOCK_EXTENDED_CREATIVES is true
    * no listener
    */
    @Test
    public void testCheckExtendedCapabilities06() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        creative.setExtendedCreativeTypeId(randomLong());
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(Boolean.TRUE));
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * creative.extendedCreativeTypeId is not null (it is an extended creative)
    * BLOCK_EXTENDED_CREATIVES is true
    * with listener
    */
    @Test
    public void testCheckExtendedCapabilities07() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        creative.setExtendedCreativeTypeId(randomLong());
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES);
                will(returnValue(Boolean.TRUE));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ExtendedCreativeBlocked, "Block any ExtendedCreativeType");
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkExtendedCapabilities(adSpace, context, creative, listener));
    }

    /*
    * Test when BLOCKED_DESTINATION_TYPES is null
    * and BLOCKED_BID_TYPES is null
    */

    @Test
    public void testCheckBlockedDestinationAttributes01() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = null;
        final Set<BidType> bidTypes = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
     * Test when BLOCKED_DESTINATION_TYPES is empty
     * and BLOCKED_BID_TYPES is empty 
     */

    @Test
    public void testCheckBlockedDestinationAttributes02() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
    * Test when BLOCKED_DESTINATION_TYPES is Non empty and Creative destination type is present in this set.
    * i.e ecreative's destination type is blocked
    * and BLOCKED_BID_TYPES is empty
    */

    @Test
    public void testCheckBlockedDestinationAttributes03() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.AUDIO);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
    * Test when BLOCKED_DESTINATION_TYPES is Non empty and Creative destination type is present in this set.
    * i.e ecreative's destination type is blocked
    * and BLOCKED_BID_TYPES is empty
    * and listener is not null
    */

    @Test
    public void testCheckBlockedDestinationAttributes03_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.AUDIO);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedDestinationType,
                        "destinationTypes.contains(creative.getDestination().getDestinationType()");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
     * Test when BLOCKED_DESTINATION_TYPES is Non empty and Creative destination type is NOT present in this set. 
     * i.e creative's destination type is NOT blocked
     * and BLOCKED_BID_TYPES is NON EMPTY and Creative BID_TYPE is present in  BlockedBidType set
     * i.e. Creative's BidType is blocked
     */

    @Test
    public void testCheckBlockedDestinationAttributes04() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.AUDIO);
        bidTypes.add(BidType.CPA);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        campaignBid.setBidType(BidType.CPA);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
    * Test when BLOCKED_DESTINATION_TYPES is Non empty and Creative destination type is NOT present in this set.
    * i.e creative's destination type is NOT blocked
    * and BLOCKED_BID_TYPES is NON EMPTY and Creative BID_TYPE is present in  BlockedBidType set
    * i.e. Creative's BidType is blocked
    * and listener is not null
    */

    @Test
    public void testCheckBlockedDestinationAttributes04_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.AUDIO);
        bidTypes.add(BidType.CPA);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        campaignBid.setBidType(BidType.CPA);

        final String eMsg = "Bid block BidTypes: " + bidTypes + " vs Campaign BidType: " + campaignBid.getBidType();
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedBidType, eMsg);
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
    * Test when BLOCKED_DESTINATION_TYPES is Non empty and Creative destination type is NOT present in this set.
    * i.e creative's destination type is NOT blocked
    * and BLOCKED_BID_TYPES is NON EMPTY and Creative BID_TYPE is NOT present in  BlockedBidType set
    * i.e. Creative's BidType is NOT blocked
    */

    @Test
    public void testCheckBlockedDestinationAttributes05() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final Set<DestinationType> destinationTypes = new HashSet<DestinationType>();
        final Set<BidType> bidTypes = new HashSet<BidType>();
        final DestinationDto destination = new DestinationDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        destination.setDestinationType(DestinationType.ANDROID);
        destinationTypes.add(DestinationType.AUDIO);
        bidTypes.add(BidType.CPA);
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        creative.setDestination(destination);
        campaignBid.setBidType(BidType.CPI);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
                will(returnValue(destinationTypes));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);
                will(returnValue(bidTypes));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkBlockedDestinationAttributes(adSpace, context, creative, listener));

    }

    /*
    * test when targetted Medium (segment medium) is null
    */
    @Test
    public void testCheckTargettedMedium01() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Medium medium = null;
        segment.setMedium(medium);
        expect(new Expectations() {
            {
            }
        });

        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkMedium(adSpace, context, creative, segment, medium, listener);
        assertEquals(elimination, null);
    }

    /*
    * test when targetted Medium (segment medium) is not null
    * and not equal to request medium
    */
    @Test
    public void testCheckTargettedMedium02() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Medium targettedMedium = Medium.APPLICATION;
        final Medium requestedMedium = Medium.SITE;
        segment.setMedium(targettedMedium);
        expect(new Expectations() {
            {
            }
        });

        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkMedium(adSpace, context, creative, segment, requestedMedium, listener);
        assertEquals(elimination, CreativeEliminatedReason.SiteAppMismatch);
    }

    /*
     * test when targetted Medium (segment medium) is not null
     * and not equal to request medium  
     * and listener is not null
     */
    @Test
    public void testCheckTargettedMedium02_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Medium targettedMedium = Medium.APPLICATION;
        final Medium requestedMedium = Medium.SITE;
        segment.setMedium(targettedMedium);
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.SiteAppMismatch, "medium!=segment.getMedium()");
            }
        });

        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkMedium(adSpace, context, creative, segment, requestedMedium, listener);
        assertEquals(elimination, CreativeEliminatedReason.SiteAppMismatch);
    }

    /*
    * test when targetted Medium (segment medium) is not null
    * and equal to request medium
    */
    @Test
    public void testCheckTargettedMedium03() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Medium targettedMedium = Medium.APPLICATION;
        final Medium requestedMedium = Medium.APPLICATION;
        segment.setMedium(targettedMedium);
        expect(new Expectations() {
            {
            }
        });

        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkMedium(adSpace, context, creative, segment, requestedMedium, listener);
        assertEquals(elimination, null);
    }

    /*
    * Test when subnets are null for given segment
    */
    @Test
    public void testCheckIpAddress01() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Set<Subnet> subnets = null;
        final long segmentId = randomLong();
        segment.setId(segmentId);
        segment.setIpAddressesListWhitelist(true);

        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getSubnetsBySegmentId(segmentId);
                will(returnValue(subnets));

            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
        assertEquals(elimination, null);
    }

    /*
    * Test when subnets are not null for given segment but empty
    */
    @Test
    public void testCheckIpAddress02() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Set<Subnet> subnets = new HashSet<Subnet>();
        final long segmentId = randomLong();
        segment.setId(segmentId);
        segment.setIpAddressesListWhitelist(true);

        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getSubnetsBySegmentId(segmentId);
                will(returnValue(subnets));

            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
        assertEquals(elimination, null);
    }

    /*
    * Test when subnets are not null, non empty for given segment
    * and IP address from request found in subnet
    */
    @Test
    public void testCheckIpAddress03() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Set<Subnet> subnets = new HashSet<Subnet>();
        final long segmentId = randomLong();
        final Subnet subnet = new Subnet("92.134.56.67");
        segment.setId(segmentId);
        segment.setIpAddressesListWhitelist(true);
        subnets.add(subnet);
        final long ipAddressValue = Subnet.getIpAddressValue("92.134.56.67");

        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getSubnetsBySegmentId(segmentId);
                will(returnValue(subnets));
                oneOf(context).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
                will(returnValue(ipAddressValue));

            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
        assertEquals(elimination, null);
    }

    /*
    * Test when subnets are not null, non empty for given segment
    * and IP address from request NOT found in subnet
    */
    @Test
    public void testCheckIpAddress04() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Set<Subnet> subnets = new HashSet<Subnet>();
        final long segmentId = randomLong();
        final Subnet subnet = new Subnet("92.134.56.67");
        segment.setId(segmentId);
        segment.setIpAddressesListWhitelist(true);
        subnets.add(subnet);
        final long ipAddressValue = Subnet.getIpAddressValue("92.134.56.68");

        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getSubnetsBySegmentId(segmentId);
                will(returnValue(subnets));
                oneOf(context).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
                will(returnValue(ipAddressValue));

            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
        assertEquals(elimination, CreativeEliminatedReason.IpNotWhitelisted);
    }

    /*
     * Test when subnets are not null, non empty for given segment
     * and IP address from request NOT found in subnet
     * and listener is not null
     */
    @Test
    public void testCheckIpAddress04_02() {
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final Set<Subnet> subnets = new HashSet<Subnet>();
        final long segmentId = randomLong();
        final Subnet subnet = new Subnet("92.134.56.67");
        segment.setId(segmentId);
        segment.setIpAddressesListWhitelist(true);
        subnets.add(subnet);
        final long ipAddressValue = Subnet.getIpAddressValue("92.134.56.68");

        expect(new Expectations() {
            {
                oneOf(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getSubnetsBySegmentId(segmentId);
                will(returnValue(subnets));
                oneOf(context).getAttribute(TargetingContext.IP_ADDRESS_VALUE);
                will(returnValue(ipAddressValue));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.IpNotWhitelisted, "IP not allowed " + ipAddressValue);

            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
        assertEquals(elimination, CreativeEliminatedReason.IpNotWhitelisted);
    }

    /*
     * Test when segment is not time targetted, creative can be serveed all daya all hours
     */
    @Test
    public void testCheckIsSegmentTimeTargetted01() {
        final TargetingEventListener listener = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final OperatorDto operator = null;
        final CountryDto country = null;
        final Date now = null;
        segment.addDayHour(DayOfWeek.Sunday, 16777215);
        segment.addDayHour(DayOfWeek.Monday, 16777215);
        segment.addDayHour(DayOfWeek.Tuesday, 16777215);
        segment.addDayHour(DayOfWeek.Wednesday, 16777215);
        segment.addDayHour(DayOfWeek.Thursday, 16777215);
        segment.addDayHour(DayOfWeek.Friday, 16777215);
        segment.addDayHour(DayOfWeek.Saturday, 16777215);

        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkIsSegmentTimeTargetted(adSpace, context, creative, segment, operator, country, now, listener);
        assertEquals(elimination, null);
    }

    /*
     * Test when segment is time targetted
     * operator is not null
     * now is not null
     */
    /*
    @Test
    public void testCheckIsSegmentTimeTargetted02(){
    	final TargetingEventListener listener = null;
    	final CreativeDto creative = new CreativeDto();
    	final SegmentDto segment = new SegmentDto();
    	final OperatorDto operator = new OperatorDto();
    	final CountryDto country = null;
    	final Date now = new Date();
    	segment.setDaysOfWeek(Segment.ALL_DAYS);
    	segment.setHoursOfDay(10);
    	segment.setHoursOfDayWeekend(12);
    	
    	expect(new Expectations() {{
    		
    	}});

    	assertTrue(BasicTargetingEngineImpl.checkIsSegmentTimeTargetted(adSpace, context, creative, segment, operator, country, now, listener));
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl11_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] wcs = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
        final CreativeDto oneWeightedCreativeWithNotActiveStatus = mock(CreativeDto.class,"oneCreativeWithNotActiveStatus");
        //wcs.add(oneWeightedCreativeWithNotActiveStatus);
        final Long oneCreativeId = randomLong();
        wcs[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	
        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneWeightedCreativeWithNotActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneWeightedCreativeWithNotActiveStatus);
    			will(returnValue(Creative.Status.NEW));
    			
    		oneOf (listener).creativeEliminated(adSpace, context, oneWeightedCreativeWithNotActiveStatus, "creative.statusChange");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											wcs, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl12_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneWeightedCreativeWithNotActiveStatusCreative = mock(CreativeDto.class,"oneWeightedCreativeWithNotActiveStatusCreative");
    	
        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneWeightedCreativeWithNotActiveStatusCreative));
    		oneOf (statusChangeManager).getStatus(oneWeightedCreativeWithNotActiveStatusCreative);
    			will(returnValue(Creative.Status.NEW));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl13_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneWeightedCreativeWithNotActiveStatusCreative = mock(CreativeDto.class,"oneWeightedCreativeWithNotActiveStatusCreative");
    	final CampaignDto campaignWithNotActiveStatus = mock(CampaignDto.class,"campaignWithNotActiveStatus");

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneWeightedCreativeWithNotActiveStatusCreative));
    		allowing (oneWeightedCreativeWithNotActiveStatusCreative).getCampaign();
    			will(returnValue(campaignWithNotActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneWeightedCreativeWithNotActiveStatusCreative);
    			will(returnValue(Creative.Status.ACTIVE));
    		oneOf (statusChangeManager).getStatus(campaignWithNotActiveStatus);
    			will(returnValue(Campaign.Status.NEW));
    			
    		oneOf (listener).creativeEliminated(adSpace, context, oneWeightedCreativeWithNotActiveStatusCreative, "campaign.statusChange");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl14_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneWeightedCreativeWithNotActiveStatusCreative = mock(CreativeDto.class,"oneWeightedCreativeWithNotActiveStatusCreative");
    	final CampaignDto campaignWithNotActiveStatus = mock(CampaignDto.class,"campaignWithNotActiveStatus");

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneWeightedCreativeWithNotActiveStatusCreative));
            allowing (oneWeightedCreativeWithNotActiveStatusCreative).getCampaign();
    			will(returnValue(campaignWithNotActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneWeightedCreativeWithNotActiveStatusCreative);
    			will(returnValue(Creative.Status.ACTIVE));
    		oneOf (statusChangeManager).getStatus(campaignWithNotActiveStatus);
    			will(returnValue(Campaign.Status.NEW));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl15_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatusCreative = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = true;
    	
        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatusCreative));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatusCreative); will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatusCreative).getCampaign(); will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus); will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatusCreative).getSegment(); will(returnValue(null));
            oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatusCreative); will(returnValue(isCreativeStopped));
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatusCreative, "stoppage");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl16_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneWeightedCreativeWithActiveStatusCreative = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = true;
    	
        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneWeightedCreativeWithActiveStatusCreative));
    		oneOf (statusChangeManager).getStatus(oneWeightedCreativeWithActiveStatusCreative);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneWeightedCreativeWithActiveStatusCreative).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneWeightedCreativeWithActiveStatusCreative).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneWeightedCreativeWithActiveStatusCreative);
    			will(returnValue(isCreativeStopped));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl17_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, -1);
    	final Date creativeEndDate = cal.getTime();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));

    		
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "creative.endDate");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl18_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, -1);
    	final Date creativeEndDate = cal.getTime();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));

    		
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl19_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));

    		
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "format !allowed");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl20_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	final Date creativeEndDate = null;
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));

    		
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl21_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
                oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(false));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));

    		
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "!beacons");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl22_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(false));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));

    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl23_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);
    	final DestinationType destinationType = DestinationType.SMS;
    	final IntegrationTypeDto integrationType = null;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(true));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.SMS_OK, Boolean.class); will(returnValue(null));
    		oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE);
    			will(returnValue(integrationType));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.SMS_OK, false);
    		
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "!sms");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl24_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);
    	final DestinationType destinationType = DestinationType.SMS;
    	final IntegrationTypeDto integrationType = null;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(true));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.SMS_OK, Boolean.class); will(returnValue(null));
    		oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE);
    			will(returnValue(integrationType));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.SMS_OK, false);
    		
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl25_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = null;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);
    	final DestinationType destinationType = DestinationType.ANDROID;//Not SMS
    	final Boolean isPluginBased = true;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(true));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		//oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE);
    			//will(returnValue(integrationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));

    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "backfill needs model");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl26_targetAndSelectCreative() throws NoCreativesException {
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = null;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final String destrinationBeaconUrl = randomAlphaNumericString(10);
    	final DestinationType destinationType = DestinationType.ANDROID;//Not SMS
    	final Boolean isPluginBased = true;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class); will(returnValue(true));
    		allowing (destination).getBeaconUrl();
    			will(returnValue(destrinationBeaconUrl));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));

    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl27_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.ANDROID;//Not SMS
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = false;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		allowing (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));

    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "!campaign.isCurrentlyActive");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl28_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.ANDROID;//Not SMS
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = false;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		allowing (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));

    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl29_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.CALL;//Not SMS
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		allowing (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));

    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "click-to-call");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl30_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.APPLICATION;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.CALL;//Not SMS
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		allowing (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));

    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl31_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = "market:"+randomAlphaNumericString(20);

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));

    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "market:");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl32_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = "market:"+randomAlphaNumericString(20);

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));

    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl33_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	blockedCategoriesInCache.add(oneBlockedCategory);
        final long campaignCategoryId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		oneOf (domainCache).getExpandedCategoryIds(campaignCategoryId);
    			will(returnValue(blockedCategoriesInCache));

    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "campaign.category blocked");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));

            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl34_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	blockedCategoriesInCache.add(oneBlockedCategory);
        final long campaignCategoryId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		oneOf (domainCache).getExpandedCategoryIds(campaignCategoryId);
    			will(returnValue(blockedCategoriesInCache));

            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl35_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	final Set<PlatformDto> platforms = new HashSet<PlatformDto>();
    	final PlatformDto onePlatform = mock(PlatformDto.class,"platform");
    	platforms.add(onePlatform);
        final double osVersion = 2.1;
    	deviceProps.put("osVersion", String.valueOf(osVersion));
        final boolean animated = true;
        final long campaignCategoryId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));
    		allowing (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		oneOf (domainCache).getExpandedCategoryIds(campaignCategoryId);
    			will(returnValue(blockedCategoriesInCache));
            oneOf (oneCreativeWithActiveStatus).isAnimated(); will(returnValue(animated));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class); will(returnValue(null));
    		oneOf (model).getPlatforms();
    			will(returnValue(platforms));
    		oneOf (domainCache).getPlatformBySystemName("android");
    			will(returnValue(onePlatform));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, true);

            oneOf (context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION); will(returnValue(null));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, osVersion);
            
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "android^animated");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl36_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = mock(TargetingEventListener.class,"listener");
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	final Set<PlatformDto> platforms = new HashSet<PlatformDto>();
    	final PlatformDto onePlatform = mock(PlatformDto.class,"platform");
    	platforms.add(onePlatform);
    	deviceProps.put("osVersion","");
        final double osVersion = 2.1;
    	final String effectiveUserAgent = "Android " + osVersion;
        final boolean animated = true;
        final long campaignCategoryId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));
    		allowing (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		oneOf (domainCache).getExpandedCategoryIds(campaignCategoryId);
    			will(returnValue(blockedCategoriesInCache));
            oneOf (oneCreativeWithActiveStatus).isAnimated(); will(returnValue(animated));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class); will(returnValue(null));
    		oneOf (model).getPlatforms();
    			will(returnValue(platforms));
    		oneOf (domainCache).getPlatformBySystemName("android");
    			will(returnValue(onePlatform));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, true);
    		oneOf (context).getEffectiveUserAgent();
    			will(returnValue(effectiveUserAgent));

            oneOf (context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION); will(returnValue(null));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, osVersion);
    			
    		oneOf (listener).creativeEliminated(adSpace, context, oneCreativeWithActiveStatus, "android^animated");
    		oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
    			
            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl37_targetAndSelectCreative() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final Medium medium = Medium.SITE;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final FormatDto oneAllowedFormat = mock(FormatDto.class,"oneAllowedFormat");
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	final Set<PlatformDto> platforms = new HashSet<PlatformDto>();
    	final PlatformDto onePlatform = mock(PlatformDto.class,"platform");
    	platforms.add(onePlatform);
    	deviceProps.put("osVersion","");
        final double osVersion = 2.1;
    	final String effectiveUserAgent = "Android " + osVersion;
    	final boolean animated = true;
        final long campaignCategoryId = randomLong();

        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
    		oneOf (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus);
    			will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate();
    			will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination();
    			will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType();
    			will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased();
    			will(returnValue(isPluginBased));
    		oneOf (campaignWithActiveStatus).isCurrentlyActive();
    			will(returnValue(isCampaignActive));
    		allowing (destination).getData();
    			will(returnValue(destinationData));
    		allowing (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		oneOf (domainCache).getExpandedCategoryIds(campaignCategoryId);
    			will(returnValue(blockedCategoriesInCache));
            oneOf (oneCreativeWithActiveStatus).isAnimated(); will(returnValue(animated));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class); will(returnValue(null));
    		oneOf (model).getPlatforms();
    			will(returnValue(platforms));
    		oneOf (domainCache).getPlatformBySystemName("android");
    			will(returnValue(onePlatform));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, true);
    		oneOf (context).getEffectiveUserAgent();
    			will(returnValue(effectiveUserAgent));

            oneOf (context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION); will(returnValue(null));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, osVersion);
    			
            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    
    @Test(expected=NoCreativesException.class)
    public void testBasicTargetingEngineImpl39_AdX_non_SITE_medium_hasRelevantDeviceIdentifier() throws NoCreativesException {
    	final Long[] creativeIds = new Long[1];
        final Long oneCreativeId = randomLong();
        creativeIds[0] = oneCreativeId;
    	final Collection<Long> allowedFormatIds = new HashSet<Long>();
    	final Map<String,String> deviceProps = new HashMap<String, String>();
    	final ModelDto model = mock(ModelDto.class,"model");;
    	final CountryDto country = null;
    	final OperatorDto operator = null;
    	final PlatformDto platform = null;
    	final Gender gender = null;
    	final Range<Integer> ageRange = null;
    	final Set<Long> capabilityIds = null;
    	final boolean diagnosticMode = false;
    	final TimeLimit timeLimit = null;
    	final TargetingEventListener listener = null;
    	final Set<Long> blockedCategoryIds = new HashSet<Long>();
        final Set<String> blockedAdvertiserDomains = new HashSet<String>();
    	deviceProps.put("mobileDevice", "1");
    	final CreativeDto oneCreativeWithActiveStatus = mock(CreativeDto.class,"oneWeightedCreativeWithActiveStatusCreative");
        final long oneWeightedCreativeWithActiveStatusCreativeId = randomLong();
    	final CampaignDto campaignWithActiveStatus = mock(CampaignDto.class,"campaignWithActiveStatus");
    	final Boolean isCreativeStopped = false;
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, 1);//future date
    	final Date creativeEndDate = cal.getTime();
    	final FormatDto oneAllowedFormat = mock(FormatDto.class,"oneAllowedFormat");
    	final long oneAllowedFormatId = 123L;
        allowedFormatIds.add(oneAllowedFormatId);
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
    	final long creativeFormatId = 456L;
        allowedFormatIds.add(creativeFormatId);
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.AUDIO;//Not Call
    	final Boolean isPluginBased = true;
    	final Boolean isCampaignActive = true;
    	final String destinationData = randomAlphaNumericString(20);
    	final Long oneBlockedCategory = 100L;
    	blockedCategoryIds.add(oneBlockedCategory);
    	final Set<Long> blockedCategoriesInCache = new HashSet<Long>();
    	final Set<PlatformDto> platforms = new HashSet<PlatformDto>();
    	final PlatformDto onePlatform = mock(PlatformDto.class,"platform");
    	platforms.add(onePlatform);
    	deviceProps.put("osVersion","");
        final double osVersion = 2.1;
    	final String effectiveUserAgent = "Android " + osVersion;
    	final boolean animated = true;
        final long campaignCategoryId = randomLong();

    	final Medium medium = Medium.APPLICATION;
        final long dpidTypeId = uniqueLong("DeviceIdentifierType.id");
        final long udidTypeId = uniqueLong("DeviceIdentifierType.id");
        final long odin1TypeId = uniqueLong("DeviceIdentifierType.id");
        final long openudidTypeId = uniqueLong("DeviceIdentifierType.id");
        final Map<Long,String> deviceIdentifiersMap = new HashMap<Long,String>() {{
                put(dpidTypeId, randomAlphaNumericString(10));
            }};
        
        expect(new Expectations() {{
    		oneOf (context).getAdserverDomainCache();
    			will(returnValue(adserverDomainCache));
    		oneOf (adserverDomainCache).getCreativeById(oneCreativeId);
    			will(returnValue(oneCreativeWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(oneCreativeWithActiveStatus);
    			will(returnValue(Creative.Status.ACTIVE));
    		allowing (oneCreativeWithActiveStatus).getCampaign();
    			will(returnValue(campaignWithActiveStatus));
    		oneOf (statusChangeManager).getStatus(campaignWithActiveStatus);
    			will(returnValue(Campaign.Status.ACTIVE));
            allowing (oneCreativeWithActiveStatus).getSegment(); will(returnValue(null));
            
            allowing (context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS); will(returnValue(deviceIdentifiersMap));
            allowing (domainCache).getDeviceIdentifierTypeIdBySystemName("dpid"); will(returnValue(dpidTypeId));
            allowing (domainCache).getDeviceIdentifierTypeIdBySystemName("udid"); will(returnValue(udidTypeId));
            allowing (domainCache).getDeviceIdentifierTypeIdBySystemName("odin-1"); will(returnValue(odin1TypeId));
            allowing (domainCache).getDeviceIdentifierTypeIdBySystemName("openudid"); will(returnValue(openudidTypeId));
            
    		allowing (stoppageManager).isCreativeStopped(oneCreativeWithActiveStatus); will(returnValue(isCreativeStopped));
    		allowing (oneCreativeWithActiveStatus).getEndDate(); will(returnValue(creativeEndDate));
    		allowing (oneCreativeWithActiveStatus).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
    		allowing (oneCreativeWithActiveStatus).getDestination(); will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
    		allowing (destination).getDestinationType(); will(returnValue(destinationType));
    		allowing (oneCreativeWithActiveStatus).isPluginBased(); will(returnValue(isPluginBased));
            allowing (campaignWithActiveStatus).isCurrentlyActive(); will(returnValue(isCampaignActive));
    		allowing (destination).getData(); will(returnValue(destinationData));
    		allowing (context).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
            allowing (oneCreativeWithActiveStatus).getId(); will(returnValue(oneWeightedCreativeWithActiveStatusCreativeId));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(blockedCategoryIds));
    		allowing (campaignWithActiveStatus).getCategoryId(); will(returnValue(campaignCategoryId));
    		allowing (domainCache).getExpandedCategoryIds(campaignCategoryId); will(returnValue(blockedCategoriesInCache));
            allowing (oneCreativeWithActiveStatus).isAnimated(); will(returnValue(animated));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class); will(returnValue(null));
    		oneOf (model).getPlatforms(); will(returnValue(platforms));
    		oneOf (domainCache).getPlatformBySystemName("android"); will(returnValue(onePlatform));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, true);
    		allowing (context).getEffectiveUserAgent(); will(returnValue(effectiveUserAgent));
            oneOf (context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION); will(returnValue(null));
            oneOf (context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, osVersion);
    	}});
        basicTargetingEngineImpl.targetAndSelectCreative(
        											priority, 
        											creativeIds, 
        											reusablePool, 
        											adSpace, 
        											allowedFormatIds, 
        											context, 
        											deviceProps, 
        											model, 
        											country, 
        											operator, 
        											platform, 
        											gender, 
        											ageRange, 
        											capabilityIds, 
        											medium, 
        											diagnosticMode, 
                                                    false, 
        											null, 
        											timeLimit, listener);
    }
    */
    @SuppressWarnings("unchecked")
    public void testGetUniqueIdForFrequencyCounter01_nothingAvailable() throws Exception {
        final Map<Long, String> deviceIdentifiersMap = Collections.EMPTY_MAP;
        final String trackingId = null;

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiersMap));
                oneOf(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(trackingId));
            }
        });

        // Should return null
        String uniqueId = FrequencyCapper.getUniqueIdForFrequencyCounter(context);
        assertNull(uniqueId);
    }

    @SuppressWarnings("unchecked")
    public void testGetUniqueIdForFrequencyCounter02_trackingIdBlank() throws Exception {
        final Map<Long, String> deviceIdentifiersMap = Collections.EMPTY_MAP;
        final String trackingId = "";

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiersMap));
                oneOf(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(trackingId));
            }
        });

        // Should return null
        String uniqueId = FrequencyCapper.getUniqueIdForFrequencyCounter(context);
        assertNull(uniqueId);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetUniqueIdForFrequencyCounter03_trackingIdNonBlank() throws Exception {
        final Map<Long, String> deviceIdentifiersMap = Collections.EMPTY_MAP;
        final String trackingId = randomAlphaNumericString(10);
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.PUBLISHER_GENERATED;

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiersMap));
                oneOf(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(trackingId));
                oneOf(context).getAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE);
                will(returnValue(trackingIdentifierType));
            }
        });

        assertEquals(trackingIdentifierType.name() + "." + trackingId, FrequencyCapper.getUniqueIdForFrequencyCounter(context));
    }

    @Test
    public void testGetUniqueIdForFrequencyCounter04_deviceIdentifiersAvailable() throws Exception {
        final Map<Long, String> deviceIdentifiersMap = new LinkedHashMap<Long, String>();
        final long ditId1 = uniqueLong("ditId");
        final String di1 = uniqueAlphaNumericString(10, "di");
        deviceIdentifiersMap.put(ditId1, di1);
        // Put some more values into the map
        for (int k = 0; k < 4; ++k) {
            deviceIdentifiersMap.put(uniqueLong("ditId"), uniqueAlphaNumericString(10, "di"));
        }

        expect(new Expectations() {
            {
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiersMap));
            }
        });

        assertEquals(ditId1 + "." + di1, FrequencyCapper.getUniqueIdForFrequencyCounter(context));
    }

    /*
    @Test(expected=NoCreativesException.class)
    public void testTargetAndSelectCreative_automaticNegativeRetargeting01_installed_already() throws NoCreativesException {
        final long creativeId = randomLong();
        final Long[] wcs = new Long[] { creativeId };
        final Set<Long> allowedFormatIds = null;
        final Map<String,String> deviceProps = Collections.emptyMap();
        final ModelDto model = mock(ModelDto.class, "model");
        final CountryDto country = mock(CountryDto.class, "country");
        final OperatorDto operator = mock(OperatorDto.class, "operator");
        final PlatformDto platform = mock(PlatformDto.class, "platform");
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final long capabilityId = randomLong();
        final Set<Long> capabilityIds = Collections.singleton(capabilityId);
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final SegmentDto segment = null;
        final DestinationDto destination = mock(DestinationDto.class, "destination");
        final DestinationType destinationType = DestinationType.URL;
        final String destinationData = randomAlphaNumericString(10);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class, "format");
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final long displayTypeId = randomLong();
        final long ditId = randomLong();
        final String deviceIdentifier = randomAlphaNumericString(10);
        final Map<Long,String> publisherSuppliedDeviceIdentifiers = new HashMap<Long,String>() {{
                put(ditId, deviceIdentifier);
            }};
        final Set<Long> campaignDeviceIdentifierTypeIds = Collections.singleton(ditId);
        final RetargetingData retargetingData = mock(RetargetingData.class);
        final Map<Long,RetargetingData> retargetingDataByDeviceIdentifierType = new HashMap<Long,RetargetingData>() {{
                put(ditId, retargetingData);
            }};
        final String applicationId = randomAlphaNumericString(10);
        
        expect(new Expectations() {{
            allowing (creative).getId(); will(returnValue(creativeId));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(null));
            allowing (context).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
            oneOf (adserverDomainCache).getCreativeById(creativeId); will(returnValue(creative));
            oneOf (statusChangeManager).getStatus(creative); will(returnValue(Creative.Status.ACTIVE));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            oneOf (statusChangeManager).getStatus(campaign); will(returnValue(Campaign.Status.ACTIVE));
            allowing (creative).getSegment(); will(returnValue(segment));
            oneOf (stoppageManager).isCreativeStopped(creative); will(returnValue(false));
            oneOf (creative).getEndDate(); will(returnValue(null));
            allowing (creative).getDestination(); will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
            allowing (destination).getDestinationType(); will(returnValue(destinationType));
            oneOf (creative).isPluginBased(); will(returnValue(false));
            oneOf (campaign).isCurrentlyActive(); will(returnValue(true));
            allowing (destination).getData(); will(returnValue(destinationData));
            oneOf (creative).isAnimated(); will(returnValue(false));
            oneOf (campaign).getDisableLanguageMatch(); will(returnValue(true));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            oneOf (creative).getFormatId(); will(returnValue(formatId));
            oneOf (domainCache).getFormatById(formatId); will(returnValue(format));
            oneOf (displayTypeUtils).getDisplayType(format, context); will(returnValue(displayType));
            oneOf (displayTypeUtils).getAllDisplayTypes(format, context); will(returnValue(Collections.singletonList(displayType)));
            oneOf (displayType).getId(); will(returnValue(displayTypeId));
            oneOf (creative).hasAssets(displayTypeId, null, null, domainCache); will(returnValue(true));
            oneOf (context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST); will(returnValue(null));
            oneOf (displayTypeUtils).setDisplayType(format, context, displayType);
            oneOf (creative).getExtendedCreativeTypeId(); will(returnValue(null));
            allowing (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            oneOf (context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS); will(returnValue(publisherSuppliedDeviceIdentifiers));
            allowing (campaign).getDeviceIdentifierTypeIds(); will(returnValue(campaignDeviceIdentifierTypeIds));
            oneOf (context).getAttribute(TargetingContext.RETARGETING_DATA); will(returnValue(retargetingDataByDeviceIdentifierType));
            allowing (campaign).getApplicationID(); will(returnValue(applicationId));
            oneOf (retargetingData).isInstalled(applicationId); will(returnValue(true));
            oneOf (listener).creativeEliminated(adSpace, context, creative, "already installed");
            oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
        }});
        SelectedCreative selectedCreative = basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, allowedFormatIds, context, deviceProps, model, country, operator, platform, gender, ageRange, capabilityIds, medium, diagnosticMode, false, null, timeLimit, listener);
    }

    @Test
    public void testTargetAndSelectCreative_automaticNegativeRetargeting02_no_retargeting_data() throws NoCreativesException {
        final long adSpaceId = randomLong();
        final long creativeId = randomLong();
        final Long[] wcs = new Long[] { creativeId };
        final Set<Long> allowedFormatIds = null;
        final Map<String,String> deviceProps = Collections.emptyMap();
        final ModelDto model = mock(ModelDto.class, "model");
        final CountryDto country = mock(CountryDto.class, "country");
        final OperatorDto operator = mock(OperatorDto.class, "operator");
        final PlatformDto platform = mock(PlatformDto.class, "platform");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final long capabilityId = randomLong();
        final Set<Long> capabilityIds = Collections.singleton(capabilityId);
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final SegmentDto segment = null;
        final DestinationDto destination = mock(DestinationDto.class, "destination");
        final DestinationType destinationType = DestinationType.URL;
        final String destinationData = randomAlphaNumericString(10);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class, "format");
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final long displayTypeId = randomLong();
        final long ditId = randomLong();
        final String deviceIdentifier = randomAlphaNumericString(10);
        final Map<Long,String> publisherSuppliedDeviceIdentifiers = new HashMap<Long,String>() {{
                put(ditId, deviceIdentifier);
            }};
        final Set<Long> campaignDeviceIdentifierTypeIds = Collections.singleton(ditId);
        final Map<Long,RetargetingData> retargetingDataByDeviceIdentifierType = Collections.emptyMap();
        final double campaignBoostFactor = 1.0;
        final int campaignThrottle = 100;
        final double ecpm = 1.2;
        
        expect(new Expectations() {{
            allowing (adserverDomainCache).isRtbEnabled(); will(returnValue(false));
            allowing (adserverDomainCache).getEcpm(adSpace, creative,platform, 0); will(returnValue(ecpm));
            allowing (adserverDomainCache).getEcpmWeight(adSpace, creative,platform, 0); will(returnValue(ecpm));
            allowing (adSpace).getId(); will(returnValue(adSpaceId));
            allowing (adSpace).getPublication(); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).isRtbEnabled(); will(returnValue(true));
            allowing (creative).getId(); will(returnValue(creativeId));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(null));
            oneOf (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(null));
            allowing (context).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
            oneOf (adserverDomainCache).getCreativeById(creativeId); will(returnValue(creative));
            oneOf (adserverDomainCache).getSystemVariableDoubleValue("rtb_campaign_exponent",2.0); will(returnValue(2.0));
            oneOf (adserverDomainCache).getSystemVariableDoubleValue("rtb_creative_exponent",2.0); will(returnValue(2.0));

            oneOf (statusChangeManager).getStatus(creative); will(returnValue(Creative.Status.ACTIVE));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            oneOf (statusChangeManager).getStatus(campaign); will(returnValue(Campaign.Status.ACTIVE));
            allowing (creative).getSegment(); will(returnValue(segment));
            oneOf (stoppageManager).isCreativeStopped(creative); will(returnValue(false));
            oneOf (creative).getEndDate(); will(returnValue(null));
            allowing (creative).getDestination(); will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
            allowing (destination).getDestinationType(); will(returnValue(destinationType));
            oneOf (creative).isPluginBased(); will(returnValue(false));
            oneOf (campaign).isCurrentlyActive(); will(returnValue(true));
            allowing (destination).getData(); will(returnValue(destinationData));
            oneOf (creative).isAnimated(); will(returnValue(false));
            oneOf (campaign).getDisableLanguageMatch(); will(returnValue(true));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            oneOf (creative).getFormatId(); will(returnValue(formatId));
            oneOf (domainCache).getFormatById(formatId); will(returnValue(format));
            oneOf (displayTypeUtils).getDisplayType(format, context); will(returnValue(displayType));
            oneOf (displayTypeUtils).getAllDisplayTypes(format, context); will(returnValue(Collections.singletonList(displayType)));
            oneOf (displayType).getId(); will(returnValue(displayTypeId));
            oneOf (creative).hasAssets(displayTypeId, null, null, domainCache); will(returnValue(true));
            oneOf (displayTypeUtils).setDisplayType(format, context, displayType);
            oneOf (creative).getExtendedCreativeTypeId(); will(returnValue(null));
            allowing (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            oneOf (context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS); will(returnValue(publisherSuppliedDeviceIdentifiers));
            allowing (campaign).getDeviceIdentifierTypeIds(); will(returnValue(campaignDeviceIdentifierTypeIds));
            oneOf (context).getAttribute(TargetingContext.RETARGETING_DATA); will(returnValue(retargetingDataByDeviceIdentifierType));
            oneOf (campaign).getBoostFactor(); will(returnValue(campaignBoostFactor));
            oneOf (campaign).getThrottle(); will(returnValue(campaignThrottle));
            oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
            oneOf (context).isTestMode(); will(returnValue(true)); // avoid freq cap
            oneOf (creative).isPluginBased(); will(returnValue(false));
            allowing(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);will(returnValue(null));
            allowing(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);will(returnValue(null));
            oneOf (context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST); will(returnValue(null));
        }});
        SelectedCreative selectedCreative = basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, allowedFormatIds, context, deviceProps, model, country, operator, platform, gender, ageRange, capabilityIds, medium, diagnosticMode, false, null, timeLimit, listener);
        assertNotNull(selectedCreative);
        assertEquals(creative, selectedCreative.getCreative());
    }

    @Test
    public void testTargetAndSelectCreative_automaticNegativeRetargeting03_not_installed_already() throws NoCreativesException {
        final long adSpaceId = randomLong();
        final long creativeId = randomLong();
        final Long[] wcs = new Long[] { creativeId };
        final Set<Long> allowedFormatIds = null;
        final Map<String,String> deviceProps = Collections.emptyMap();
        final ModelDto model = mock(ModelDto.class, "model");
        final CountryDto country = mock(CountryDto.class, "country");
        final OperatorDto operator = mock(OperatorDto.class, "operator");
        final PlatformDto platform = mock(PlatformDto.class, "platform");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final long capabilityId = randomLong();
        final Set<Long> capabilityIds = Collections.singleton(capabilityId);
        final Medium medium = Medium.APPLICATION;
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final SegmentDto segment = null;
        final DestinationDto destination = mock(DestinationDto.class, "destination");
        final DestinationType destinationType = DestinationType.URL;
        final String destinationData = randomAlphaNumericString(10);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class, "format");
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final long displayTypeId = randomLong();
        final long ditId = randomLong();
        final String deviceIdentifier = randomAlphaNumericString(10);
        final Map<Long,String> publisherSuppliedDeviceIdentifiers = new HashMap<Long,String>() {{
                put(ditId, deviceIdentifier);
            }};
        final Set<Long> campaignDeviceIdentifierTypeIds = Collections.singleton(ditId);
        final RetargetingData retargetingData = mock(RetargetingData.class);
        final Map<Long,RetargetingData> retargetingDataByDeviceIdentifierType = new HashMap<Long,RetargetingData>() {{
                put(ditId, retargetingData);
            }};
        final String applicationId = randomAlphaNumericString(10);
        final double campaignBoostFactor = 1.0;
        final int campaignThrottle = 100;
        final double ecpm = 1.2;
        
        expect(new Expectations() {{
            allowing (adserverDomainCache).isRtbEnabled(); will(returnValue(false));
            allowing (adserverDomainCache).getEcpm(adSpace, creative,platform,0); will(returnValue(ecpm));
            allowing (adserverDomainCache).getEcpmWeight(adSpace, creative,platform, 0); will(returnValue(ecpm));
            allowing (adSpace).getId(); will(returnValue(adSpaceId));
            allowing (adSpace).getPublication(); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).isRtbEnabled(); will(returnValue(false));
            allowing (creative).getId(); will(returnValue(creativeId));
            oneOf (context).getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS); will(returnValue(null));
            oneOf (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(null));
            allowing (context).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
            oneOf (adserverDomainCache).getCreativeById(creativeId); will(returnValue(creative));
            oneOf (adserverDomainCache).getSystemVariableDoubleValue("campaign_exponent",1.2); will(returnValue(2.0));
            oneOf (adserverDomainCache).getSystemVariableDoubleValue("creative_exponent",1.2); will(returnValue(2.0));
            oneOf (statusChangeManager).getStatus(creative); will(returnValue(Creative.Status.ACTIVE));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            oneOf (statusChangeManager).getStatus(campaign); will(returnValue(Campaign.Status.ACTIVE));
            allowing (creative).getSegment(); will(returnValue(segment));
            oneOf (stoppageManager).isCreativeStopped(creative); will(returnValue(false));
            oneOf (creative).getEndDate(); will(returnValue(null));
            allowing (creative).getDestination(); will(returnValue(destination));
            oneOf (destination).getBeaconUrl(); will(returnValue(null));
            allowing (destination).getDestinationType(); will(returnValue(destinationType));
            oneOf (creative).isPluginBased(); will(returnValue(false));
            oneOf (campaign).isCurrentlyActive(); will(returnValue(true));
            allowing (destination).getData(); will(returnValue(destinationData));
            oneOf (creative).isAnimated(); will(returnValue(false));
            oneOf (campaign).getDisableLanguageMatch(); will(returnValue(true));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            oneOf (creative).getFormatId(); will(returnValue(formatId));
            oneOf (domainCache).getFormatById(formatId); will(returnValue(format));
            oneOf (displayTypeUtils).getDisplayType(format, context); will(returnValue(displayType));
            oneOf (displayTypeUtils).getAllDisplayTypes(format, context); will(returnValue(Collections.singletonList(displayType)));
            oneOf (displayType).getId(); will(returnValue(displayTypeId));
            oneOf (creative).hasAssets(displayTypeId, null, null, domainCache); will(returnValue(true));
            oneOf (context).getAttribute(TargetingContext.MIME_TYPE_WHITELIST); will(returnValue(null));
            oneOf (displayTypeUtils).setDisplayType(format, context, displayType);
            oneOf (creative).getExtendedCreativeTypeId(); will(returnValue(null));
            allowing (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            oneOf (context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS); will(returnValue(publisherSuppliedDeviceIdentifiers));
            allowing (campaign).getDeviceIdentifierTypeIds(); will(returnValue(campaignDeviceIdentifierTypeIds));
            oneOf (context).getAttribute(TargetingContext.RETARGETING_DATA); will(returnValue(retargetingDataByDeviceIdentifierType));
            allowing (campaign).getApplicationID(); will(returnValue(applicationId));
            oneOf (retargetingData).isInstalled(applicationId); will(returnValue(false));
            oneOf (campaign).getBoostFactor(); will(returnValue(campaignBoostFactor));
            oneOf (campaign).getThrottle(); will(returnValue(campaignThrottle));
            oneOf (listener).creativesTargeted(with(adSpace), with(context), with(priority), with(any(FastLinkedList.class)));
            oneOf (context).isTestMode(); will(returnValue(true)); // avoid freq cap
            oneOf (creative).isPluginBased(); will(returnValue(false));
            allowing(context).getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);will(returnValue(null));
            allowing(context).getAttribute(TargetingContext.BLOCKED_BID_TYPES);will(returnValue(null));
        }});
        SelectedCreative selectedCreative = basicTargetingEngineImpl.targetAndSelectCreative(priority, wcs, reusablePool, adSpace, allowedFormatIds, context, deviceProps, model, country, operator, platform, gender, ageRange, capabilityIds, medium, diagnosticMode, false, null, timeLimit, listener);
        assertNotNull(selectedCreative);
        assertEquals(creative, selectedCreative.getCreative());
    }
    */
    @Test
    public void testCheckTimeLimit01_null() {
        assertTrue(BasicTargetingEngineImpl.checkTimeLimit(null, adSpace, context, null));
    }

    @Test
    public void testCheckTimeLimit02_not_expired() {
        final TimeLimit timeLimit = mock(TimeLimit.class);
        expect(new Expectations() {
            {
                oneOf(timeLimit).hasExpired();
                will(returnValue(false));
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkTimeLimit(timeLimit, adSpace, context, null));
    }

    @Test
    public void testCheckTimeLimit03_expired() {
        final TimeLimit timeLimit = mock(TimeLimit.class);
        expect(new Expectations() {
            {
                oneOf(timeLimit).hasExpired();
                will(returnValue(true));
                allowing(timeLimit).getDuration();
                will(returnValue(randomLong()));
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkTimeLimit(timeLimit, adSpace, context, null));
    }

    @Test
    public void testCheckTimeLimit04_expired_with_listener() {
        final TimeLimit timeLimit = mock(TimeLimit.class);
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(timeLimit).hasExpired();
                will(returnValue(true));
                allowing(timeLimit).getDuration();
                will(returnValue(randomLong()));
                oneOf(listener).timeLimitExpired(adSpace, context, timeLimit);
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkTimeLimit(timeLimit, adSpace, context, listener));
    }

    @Test
    public void testCheckActive01_creative_not_active() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        expect(new Expectations() {
            {
                oneOf(statusChangeManager).getStatus(creative);
                will(returnValue(Creative.Status.PAUSED));
            }
        });
        assertFalse(basicTargetingEngineImpl.checkActive(adSpace, context, creative, null));
    }

    @Test
    public void testCheckActive02_creative_not_active_with_listener() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(statusChangeManager).getStatus(creative);
                will(returnValue(Creative.Status.PAUSED));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeStatusChanged, "creative.statusChange");
            }
        });
        assertFalse(basicTargetingEngineImpl.checkActive(adSpace, context, creative, listener));
    }

    @Test
    public void testCheckActive03_campaign_not_active() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        expect(new Expectations() {
            {
                oneOf(statusChangeManager).getStatus(creative);
                will(returnValue(Creative.Status.ACTIVE));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(statusChangeManager).getStatus(campaign);
                will(returnValue(Campaign.Status.PAUSED));
            }
        });
        assertFalse(basicTargetingEngineImpl.checkActive(adSpace, context, creative, null));
    }

    @Test
    public void testCheckActive04_campaign_not_active_with_listener() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(statusChangeManager).getStatus(creative);
                will(returnValue(Creative.Status.ACTIVE));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(statusChangeManager).getStatus(campaign);
                will(returnValue(Campaign.Status.PAUSED));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.campaignStatusChanged, "campaign.statusChange");
            }
        });
        assertFalse(basicTargetingEngineImpl.checkActive(adSpace, context, creative, listener));
    }

    @Test
    public void testCheckActive05_both_active() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        expect(new Expectations() {
            {
                oneOf(statusChangeManager).getStatus(creative);
                will(returnValue(Creative.Status.ACTIVE));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(statusChangeManager).getStatus(campaign);
                will(returnValue(Campaign.Status.ACTIVE));
            }
        });
        assertTrue(basicTargetingEngineImpl.checkActive(adSpace, context, creative, null));
    }

    /*
     * Test when segment is null
     * This method expects that segment will be not null, so throws null poionter exception
     */
    @Test(expected = NullPointerException.class)
    public void testCheckSegmentCountries01_no_segment() {
        final CountryDto country = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = null;
        creative.setSegment(segment);
        expect(new Expectations() {
            {
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries02_no_countries_targeted() {
        final CountryDto country = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        creative.setSegment(segment);
        expect(new Expectations() {
            {
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries03_whitelist_no_country() {
        final CountryDto country = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = true;
        final long countryId = randomLong();
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        expect(new Expectations() {
            {
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries04_whitelist_no_country_with_listener() {
        final CountryDto country = null;
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = true;
        final long countryId = randomLong();
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryNotPresent, "!country");
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, listener));
    }

    /*
     * Test when country is derived from request and segment countryWhiteList is true
     * and derviced country is not in white list of countries
     */
    @Test
    public void testCheckSegmentCountries05_whitelist_country_not_targeted() {
        final CountryDto country = new CountryDto();
        final long countryId = randomLong();
        final long whiteListedCountryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = true;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(whiteListedCountryId);
        country.setId(countryId);
        expect(new Expectations() {
            {
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries06_whitelist_country_not_targeted_with_listener() {
        final CountryDto country = new CountryDto();
        final long countryId = randomLong();
        final long whiteListedCountryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = true;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(whiteListedCountryId);
        country.setId(countryId);
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryNotWhiteListed, "country !whitelisted");
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, listener));
    }

    @Test
    public void testCheckSegmentCountries07_whitelist_country_targeted() {
        final CountryDto country = new CountryDto();
        final long countryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = true;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        country.setId(countryId);
        expect(new Expectations() {
            {
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries08_blacklist_country_blacklisted() {
        final CountryDto country = new CountryDto();
        final long countryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = false;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        country.setId(countryId);

        expect(new Expectations() {
            {
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries09_blacklist_country_blacklisted_with_listener() {
        final CountryDto country = new CountryDto();
        final long countryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = false;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        country.setId(countryId);
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryBlackListed, "country blacklisted");
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, listener));
    }

    @Test
    public void testCheckSegmentCountries10_blacklist_no_country() {
        final CountryDto country = null;
        final long countryId = randomLong();
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = false;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(countryId);
        expect(new Expectations() {
            {
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    @Test
    public void testCheckSegmentCountries11_blacklist_country_not_blacklisted() {
        final CountryDto country = new CountryDto();
        final long blacklistedCountryId = uniqueLong("countryId");
        final long nonBlacklistedCountryId = uniqueLong("countryId");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final boolean countryListIsWhitelist = false;
        segment.setCountryListIsWhitelist(countryListIsWhitelist);
        creative.setSegment(segment);
        segment.getCountryIds().add(blacklistedCountryId);
        country.setId(nonBlacklistedCountryId);

        expect(new Expectations() {
            {
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkSegmentCountries(adSpace, context, creative, segment, country, null));
    }

    /*
     * Test when segment is null
     * This methods expects segment to be not null, if passed null will throw null pointer exception
     */
    @Test(expected = NullPointerException.class)
    public void testCheckSegmentPlatforms01_no_segment() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = null;
        final PlatformDto platform = new PlatformDto();
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        creative.setSegment(segment);
        expect(new Expectations() {
            {
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, null);
    }

    /*
     * 
     */
    @Test
    public void testCheckSegmentPlatforms02_no_platforms_targeted() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        creative.setSegment(segment);
        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, null);
    }

    /*
     * Tes when derived platform not targetted by Segment platforms
     */
    @Test
    public void testCheckSegmentPlatforms03_platform_not_targeted() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = randomLong();
        final long targettedPlatformId = platformId + 1;
        final ModelDto model = null;
        segment.getPlatformIds().add(targettedPlatformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        final TargetingEventListener listener = null;

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DevicePlatformMismatch);
    }

    @Test
    public void testCheckSegmentPlatforms04_platform_not_targeted_with_listener() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = randomLong();
        final long targettedPlatformId = platformId + 1;
        final ModelDto model = null;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        segment.getPlatformIds().add(targettedPlatformId);
        creative.setSegment(segment);
        platform.setId(platformId);

        final String eMsg = "Bid Platform " + platform.getId() + " vs " + segment.getPlatformIds();
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DevicePlatformMismatch, eMsg);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DevicePlatformMismatch);
    }

    @Test
    public void testCheckSegmentPlatforms05_platform_targeted_model_null() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = randomLong();
        final ModelDto model = null;
        final TargetingEventListener listener = null;
        segment.getPlatformIds().add(platformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, null);
    }

    @Test
    public void testCheckSegmentPlatforms06_platform_targeted_no_models_excluded() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = randomLong();
        final ModelDto model = new ModelDto();
        final TargetingEventListener listener = null;
        segment.getPlatformIds().add(platformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, null);
    }

    @Test
    public void testCheckSegmentPlatforms07_platform_targeted_model_excluded() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = uniqueLong("platformId");
        final ModelDto model = new ModelDto();
        final long modelId = uniqueLong("modelId");
        final TargetingEventListener listener = null;
        segment.getPlatformIds().add(platformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        segment.getExcludedModelIds().add(modelId);
        model.setId(modelId);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceModelExcluded);
    }

    @Test
    public void testCheckSegmentPlatforms08_platform_targeted_model_excluded_with_listener() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = uniqueLong("platformId");
        final ModelDto model = new ModelDto();
        final long modelId = uniqueLong("modelId");

        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        segment.getPlatformIds().add(platformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        segment.getExcludedModelIds().add(modelId);
        model.setId(modelId);

        final String eMsg = "Device model " + model.getId() + " blacklisted: " + Sets.newHashSet(model.getId());
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceModelExcluded, eMsg);
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, CreativeEliminatedReason.DeviceModelExcluded);
    }

    @Test
    public void testCheckSegmentPlatforms09_platform_targeted_model_not_excluded() {
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final PlatformDto platform = new PlatformDto();
        final long platformId = uniqueLong("platformId");
        final ModelDto model = new ModelDto();
        final long modelId = uniqueLong("modelId");
        final long excludedModelId = uniqueLong("modelId");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        segment.getPlatformIds().add(platformId);
        creative.setSegment(segment);
        platform.setId(platformId);
        segment.getExcludedModelIds().add(excludedModelId);
        model.setId(modelId);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, platform, model, listener);
        assertEquals(elimination, null);
    }

    @Test
    public void testCheckStoppages01_stopped() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaing = mock(CampaignDto.class, "campaing");
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaing));
                oneOf(stoppageManager).isCampaignStopped(campaing);
                will(returnValue(true));
            }
        });
        assertFalse(basicTargetingEngineImpl.checkStoppages(adSpace, context, creative, null));
    }

    @Test
    public void testCheckStoppages02_stopped_with_listener() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(stoppageManager).isCampaignStopped(campaign);
                will(returnValue(true));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeOrAdvertiserStopped, "Campaign stoppage");
            }
        });
        assertFalse(basicTargetingEngineImpl.checkStoppages(adSpace, context, creative, listener));
    }

    @Test
    public void testCheckStoppages03_not_stopped() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final AdvertiserDto advertiser = mock(AdvertiserDto.class, "advertiser");
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiser();
                will(returnValue(advertiser));
                oneOf(stoppageManager).isCampaignStopped(campaign);
                will(returnValue(false));
                oneOf(stoppageManager).isAdvertiserStopped(advertiser);
                will(returnValue(false));
            }
        });
        assertTrue(basicTargetingEngineImpl.checkStoppages(adSpace, context, creative, null));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains02_no_advertiserDomain() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(null));
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, null));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains01_no_blocked_domains() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final String advertiserDomain = randomDomainName();
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(advertiserDomain));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
                will(returnValue(null));
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, null));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains03_equals() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final String advertiserDomain = randomDomainName();
        @SuppressWarnings("serial")
        final Set<String> blockedDomains = new HashSet<String>() {
            {
                add(advertiserDomain);
            }
        };
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(advertiserDomain));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
                will(returnValue(blockedDomains));
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, null));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains04_equals_with_listener() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final String advertiserDomain = randomDomainName();
        @SuppressWarnings("serial")
        final Set<String> blockedDomains = new HashSet<String>() {
            {
                add(advertiserDomain);
            }
        };
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(advertiserDomain));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
                will(returnValue(blockedDomains));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DomainBlocked, "domain blocked");
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, listener));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains05_endsWith() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final String blockedDomain = randomDomainName();
        final String advertiserDomain = "prefix." + blockedDomain; // endsWith the blocked domain
        @SuppressWarnings("serial")
        final Set<String> blockedDomains = new HashSet<String>() {
            {
                add(blockedDomain);
            }
        };
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(advertiserDomain));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
                will(returnValue(blockedDomains));
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, null));
    }

    @Test
    public void testCheckBlockedAdvertiserDomains06_no_match() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final String advertiserDomain = randomDomainName();
        @SuppressWarnings("serial")
        final Set<String> blockedDomains = new HashSet<String>() {
            {
                add(randomDomainName()); // a different domain
            }
        };
        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiserDomain();
                will(returnValue(advertiserDomain));
                oneOf(context).getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
                will(returnValue(blockedDomains));
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkBlockedAdvertiserDomains(adSpace, context, creative, null));
    }

    /*
      * Test when there is no publication or publisher Target ECPM rate card exists
      * and ecpm floor is null
      */
    @Test
    public void test01_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final PublisherDto publisher = new PublisherDto();
        final CreativeDto creative = new CreativeDto();
        final AdserverDomainCache cache = new AdserverDomainCacheImpl();
        final RateCardDto publisherRateCardDto = null;
        final RateCardDto publicationRateCardDto = null;
        final long countryId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal ecpmFloor = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(1.0);
        ecpmInfo.setWeight(1.0);
        ecpmInfo.setExpectedRevenue(2.0);
        final double globalRevenueFloor = 0.0;
        publisher.setEcpmTargetRateCard(publisherRateCardDto);

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));
            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, ecpmFloor,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when ecpm is zero
      */
    @Test
    public void test01_02_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {
        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final long countryId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal ecpmFloor = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(0.0);
        ecpmInfo.setWeight(0.0);
        ecpmInfo.setExpectedRevenue(0.0);
        final double globalRevenueFloor = -1.0;

        expect(new Expectations() {
            {
            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, ecpmFloor,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when ecpm is zero
      * and listener is not null
      */
    @Test
    public void test01_03_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {
        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final long countryId = randomLong();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final BigDecimal ecpmFloor = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(0.0);
        ecpmInfo.setWeight(0.0);
        ecpmInfo.setExpectedRevenue(0.0);
        final double globalRevenueFloor = -1.0;

        String message = "Creative ecmp weight: " + ecpmInfo.getWeight() + " <= 0 Bid Floor: " + ecpmFloor + " vs Bid Price: " + ecpmInfo.getBidPrice();
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeWeightZero, message);
            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, ecpmFloor,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when there is no publication or publisher Target ECPM rate card exists
      * and ecpm floor is NOT null
      */
    @Test
    public void test01_04_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final RateCardDto publisherRateCardDto = null;
        final RateCardDto publicationRateCardDto = null;
        final long countryId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal ecpmFloor = new BigDecimal("1.2");
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.3);
        ecpmInfo.setWeight(2.3);
        ecpmInfo.setExpectedRevenue(2.3);
        final double globalRevenueFloor = 0.0;

        expect(new Expectations() {
            {
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getEcpmTargetRateCard();
                will(returnValue(publisherRateCardDto));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));
            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, ecpmFloor,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when ECPM value for adspaceXcreative is MORE then the targetEcpmRateCard default minimum value of Publication
      */
    @Test
    public void test02_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final BigDecimal defaultMinimumValueForrateCard = new BigDecimal(2.5);
        final long countryId = randomLong();
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final TargetingEventListener listener = null;
        final double globalRevenueFloor = 0.0;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.8);
        ecpmInfo.setWeight(2.8);
        ecpmInfo.setExpectedRevenue(2.8);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(defaultMinimumValueForrateCard));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));
            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when ECPM value for adspaceXcreative is LESS then the targetEcpmRateCard default minimum value of Publication
      */
    @Test
    public void test03_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final double globalRevenueFloor = 0.0;
        final BigDecimal defaultMinimumValueForrateCard = new BigDecimal(2.5);
        final long countryId = randomLong();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.3);
        ecpmInfo.setWeight(2.3);
        ecpmInfo.setExpectedRevenue(2.3);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                //oneOf (cache).getEcpm(adSpace,creative,null,countryId); will(returnValue(ecpmValue));
                //oneOf (publicationRateCardDto).getDefaultMinimum(); will(returnValue(defaultMinimumValueForrateCard));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(defaultMinimumValueForrateCard));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ecpmLessThenMinimumDefault,
                        "ECPM is less then minimum Default ECPM rate card for the Publication/Publisher");
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));
            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      *
      * Test when
      * Campaign Segment targeting few countries
      * ECPM value for adspaceXcreative is LESS then the targetEcpmRateCard country minimum value of Publication
      */
    @Test
    public void test04_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final Long countryId = randomLong();
        final BigDecimal countryMinimumValueForRateCard = new BigDecimal("2.4");
        final double globalRevenueFloor = 0.0;
        final TargetingEventListener listener = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.3);
        ecpmInfo.setWeight(2.3);
        ecpmInfo.setExpectedRevenue(2.3);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(countryMinimumValueForRateCard));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));

            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      *
      * Test when
      * Campaign Segment targeting few countries
      * When specific country rate card has value as nulll
      */
    @Test
    public void test04_02_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final Long countryId = randomLong();
        final BigDecimal countryMinimumValueForRateCard = null;
        final double globalRevenueFloor = 0.0;
        final TargetingEventListener listener = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.3);
        ecpmInfo.setWeight(2.3);
        ecpmInfo.setExpectedRevenue(2.3);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(countryMinimumValueForRateCard));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));

            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      *
      * Test when
      * Campaign Segment targeting few countries
      * ECPM value for adspaceXcreative is MORE then the targetEcpmRateCard country minimum value of Publication
      */
    @Test
    public void test05_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final Long countryId = randomLong();
        final BigDecimal countryMinimumValueForRateCard = new BigDecimal("2.4");
        final double globalRevenueFloor = 0.0;
        final TargetingEventListener listener = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.8);
        ecpmInfo.setWeight(2.8);
        ecpmInfo.setExpectedRevenue(2.8);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(countryMinimumValueForRateCard));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));

            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      *
      * Test when
      * Campaign Segment targeting few countries
      * ECPM value for adspaceXcreative is MORE then the targetEcpmRateCard defaul minimum value of Publication
      * country level minimum ECPM value is not specified
      */
    @Test
    public void test06_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final Long countryId = randomLong();
        final BigDecimal countryMinimumValueForRateCard = new BigDecimal(2.5);
        final double globalRevenueFloor = 0.0;
        //final BigDecimal defaultMinimumValueForrateCard = new BigDecimal(2.5);
        final TargetingEventListener listener = null;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.8);
        ecpmInfo.setWeight(2.8);
        ecpmInfo.setExpectedRevenue(2.8);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                //oneOf (publicationRateCardDto).getDefaultMinimum(); will(returnValue(defaultMinimumValueForrateCard));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(countryMinimumValueForRateCard));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));

            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener, null,
                ecpmInfo, globalRevenueFloor));
    }

    /*
      *
      * Test when
      * Campaign Segment targeting few countries
      * ECPM value for adspaceXcreative is MORE then the targetEcpmRateCard country minimum value of Publication
      */
    @Test
    public void test07_isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "Creative");
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        //final RateCardDto publisherRateCardDto = null;
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfig");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final Long countryId = randomLong();
        //final BigDecimal countryMinimumValueForRateCard = new BigDecimal("2.4");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final BigDecimal ecpmFloorValue = new BigDecimal("4.2");
        final double globalRevenueFloor = 0.0;
        final EcpmInfo ecpmInfo = new EcpmInfo();
        ecpmInfo.setBidPrice(2.8);
        ecpmInfo.setWeight(2.8);
        ecpmInfo.setExpectedRevenue(2.8);

        expect(new Expectations() {
            {

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEcpmTargetRateCard();
                will(returnValue(publicationRateCardDto));
                //oneOf (publicationRateCardDto).getMinimumBid(countryId); will(returnValue(countryMinimumValueForRateCard));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.bidPriceLessThenFloorValue,
                        "Bid Price(" + ecpmInfo.getBidPrice() + ") is less than the floor value(" + ecpmFloorValue.doubleValue() + ") specified in request context");
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(context).getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
                will(returnValue(false));

            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, cache, context, listener,
                ecpmFloorValue, ecpmInfo, globalRevenueFloor));
    }

    /*
      * Test when there is no publication or publisher Target CPC/CPM payout rate card exists
      */
    @Test
    public void test01_isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher() {

        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final CampaignBidDto currentBid = mock(CampaignBidDto.class, "currentBid");
        final BidType bidType = BidType.CPM;
        final RateCardDto publicationRateCardDto = null;
        final long countryId = randomLong();
        final TargetingEventListener listener = null;

        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getCurrentBid();
                will(returnValue(currentBid));
                oneOf(currentBid).getBidType();
                will(returnValue(bidType));
                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEffectiveRateCard(bidType);
                will(returnValue(publicationRateCardDto));
            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener));
    }

    /*
      * Test when there is a publication or publisher Target CPC/CPM payout rate card exists
      * but minimum bid was found as null
      */
    @Test
    public void test02_isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher() {
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final CampaignBidDto currentBid = mock(CampaignBidDto.class, "currentBid");
        final BidType bidType = BidType.CPM;
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final long countryId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal minimumBid = null;

        expect(new Expectations() {
            {
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getCurrentBid();
                will(returnValue(currentBid));
                oneOf(currentBid).getBidType();
                will(returnValue(bidType));
                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getEffectiveRateCard(bidType);
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(minimumBid));
            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener));
    }

    /*
      * Test when there is a publication or publisher Target CPC/CPM payout rate card exists
      * and minimum bid is less then the payout
      */
    @Test
    public void test03_isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher() {

        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final CampaignBidDto currentBid = mock(CampaignBidDto.class, "currentBid");
        final BidType bidType = BidType.CPM;
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final long countryId = randomLong();
        final long publisherId = randomLong();
        final long campaignId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal minimumBid = BigDecimal.ONE;
        final BigDecimal payout = BigDecimal.TEN;

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getCurrentBid();
                will(returnValue(currentBid));
                oneOf(currentBid).getBidType();
                will(returnValue(bidType));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                oneOf(campaign).getId();
                will(returnValue(campaignId));
                oneOf(publication).getEffectiveRateCard(bidType);
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(minimumBid));
                oneOf(context).getAdserverDomainCache();
                will(returnValue(cache));
                oneOf(cache).getPayout(publisherId, campaignId);
                will(returnValue(payout));

            }
        });
        assertTrue(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener));
    }

    /*
      * Test when there is a publication or publisher Target CPC/CPM payout rate card exists
      * and minimum bid is more then the payout
      */
    @Test
    public void test04_isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher() {

        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final CampaignBidDto currentBid = mock(CampaignBidDto.class, "currentBid");
        final BidType bidType = BidType.CPM;
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final long countryId = randomLong();
        final long publisherId = randomLong();
        final long campaignId = randomLong();
        final TargetingEventListener listener = null;
        final BigDecimal minimumBid = BigDecimal.TEN;
        final BigDecimal payout = BigDecimal.ONE;

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getCurrentBid();
                will(returnValue(currentBid));
                oneOf(currentBid).getBidType();
                will(returnValue(bidType));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                oneOf(campaign).getId();
                will(returnValue(campaignId));
                oneOf(publication).getEffectiveRateCard(bidType);
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(minimumBid));
                oneOf(context).getAdserverDomainCache();
                will(returnValue(cache));
                oneOf(cache).getPayout(publisherId, campaignId);
                will(returnValue(payout));

            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener));
    }

    /*
      * Test when there is a publication or publisher Target CPC/CPM payout rate card exists
      * and minimum bid is more then the payout
      * and TargetingEventListener is not null
      */
    @Test
    public void test05_isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher() {

        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final CampaignBidDto currentBid = mock(CampaignBidDto.class, "currentBid");
        final BidType bidType = BidType.CPM;
        final AdserverDomainCacheExt cache = mock(AdserverDomainCacheExt.class, "AdserverDomainCacheExt");
        final RateCardDto publicationRateCardDto = mock(RateCardDto.class, "publicationRateCardDto");
        final long countryId = randomLong();
        final long publisherId = randomLong();
        final long campaignId = randomLong();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final BigDecimal minimumBid = BigDecimal.TEN;
        final BigDecimal payout = BigDecimal.ONE;

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getCurrentBid();
                will(returnValue(currentBid));
                oneOf(currentBid).getBidType();
                will(returnValue(bidType));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                oneOf(campaign).getId();
                will(returnValue(campaignId));
                oneOf(publication).getEffectiveRateCard(bidType);
                will(returnValue(publicationRateCardDto));
                oneOf(publicationRateCardDto).getMinimumBid(countryId);
                will(returnValue(minimumBid));
                oneOf(context).getAdserverDomainCache();
                will(returnValue(cache));
                oneOf(cache).getPayout(publisherId, campaignId);
                will(returnValue(payout));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.payoutLessThanMinBid, "Payout < Pub minimum bid in countryId=" + countryId);

            }
        });
        assertFalse(basicTargetingEngineImpl.isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener));
    }

    /*
      * Test when creative End date is null
      */
    @Test
    public void testCheckEndDate01() {
        final CreativeDto creative = new CreativeDto();
        final TargetingEventListener listener = null;
        final Date creativeEndDate = null;

        creative.setEndDate(creativeEndDate);

        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkEndDate(adSpace, context, creative, listener));
    }

    /*
      * Test when creative End date is NOT null and its in future
      */
    @Test
    public void testCheckEndDate02() {
        final CreativeDto creative = new CreativeDto();
        final TargetingEventListener listener = null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        final Date creativeEndDate = cal.getTime();

        creative.setEndDate(creativeEndDate);

        expect(new Expectations() {
            {
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkEndDate(adSpace, context, creative, listener));
    }

    /*
      * Test when creative End date is NOT null and its in Past
      */
    @Test
    public void testCheckEndDate03() {
        final CreativeDto creative = new CreativeDto();
        final TargetingEventListener listener = null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        final Date creativeEndDate = cal.getTime();

        creative.setEndDate(creativeEndDate);

        expect(new Expectations() {
            {
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkEndDate(adSpace, context, creative, listener));
    }

    /*
      * Test when creative End date is NOT null and its in Past
      * and listener is not null
      */
    @Test
    public void testCheckEndDate04() {
        final CreativeDto creative = new CreativeDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        final Date creativeEndDate = cal.getTime();

        creative.setEndDate(creativeEndDate);

        final String eMsg = "Creative endDate " + creative.getEndDate();
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeEndDateExpired, eMsg);

            }
        });

        assertFalse(BasicTargetingEngineImpl.checkEndDate(adSpace, context, creative, listener));
    }

    /*
      * Test when Creative Destiantion BeaconUrl is empty
      */
    @Test
    public void testCheckDestinationBeaconUrl01() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        List<String> beacons = new ArrayList<String>();
        destination.setBeaconUrls(beacons);
        creative.setDestination(destination);

        expect(new Expectations() {
            {

            }
        });

        assertTrue(BasicTargetingEngineImpl.checkDestinationBeaconUrl(adSpace, context, creative, listener));
    }

    /*
      * Test when Creative Destiantion BeaconUrl is provided
      * and context say use Beacon
      */
    @Test
    public void testCheckDestinationBeaconUrl03() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        List<String> beacons = new ArrayList<String>();
        beacons.add(randomAlphaNumericString(20));
        final boolean isNative = true;
        destination.setBeaconUrls(beacons);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_NATIVE, Boolean.class);
                will(returnValue(isNative));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkDestinationBeaconUrl(adSpace, context, creative, listener));
    }

    /*
      * Test when Creative Destiantion BeaconUrl is provided
      * and context say Dont use Beacon
      */
    @Test
    public void testCheckDestinationBeaconUrl04() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        List<String> beacons = new ArrayList<String>();
        beacons.add(randomAlphaNumericString(20));
        final boolean isNative = false;
        final boolean useBeacon = false;
        destination.setBeaconUrls(beacons);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_NATIVE, Boolean.class);
                will(returnValue(isNative));
                oneOf(context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class);
                will(returnValue(useBeacon));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkDestinationBeaconUrl(adSpace, context, creative, listener));
    }

    /*
      * Test when Creative Destiantion BeaconUrl is provided
      * and context say Dont use Beacon
      * and listener is not null
      */
    @Test
    public void testCheckDestinationBeaconUrl05() {
        final CreativeDto creative = new CreativeDto();
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        List<String> beacons = new ArrayList<String>();
        beacons.add(randomAlphaNumericString(20));
        final boolean isNative = false;
        final boolean useBeacon = false;
        destination.setBeaconUrls(beacons);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_NATIVE, Boolean.class);
                will(returnValue(isNative));
                oneOf(context).getAttribute(TargetingContext.USE_BEACONS, Boolean.class);
                will(returnValue(useBeacon));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.beaconsNotPresent, "!beacons");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkDestinationBeaconUrl(adSpace, context, creative, listener));
    }

    /*
      * Test when medium is Application
      */
    @Test
    public void testCheckAndroidMarketMedium01() {
        final CreativeDto creative = new CreativeDto();
        final Medium medium = Medium.APPLICATION;
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                //oneOf (listener).creativeEliminated(adSpace, context, creative, "!beacons");
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAndroidMarketMedium(adSpace, context, creative, medium, listener));
    }

    /*
      * Test when medium is NOT Application
      * and destination data is null
      */
    @Test
    public void testCheckAndroidMarketMedium02() {
        final CreativeDto creative = new CreativeDto();
        final Medium medium = Medium.SITE;
        final String destinationData = null;
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        destination.setData(destinationData);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                //oneOf (listener).creativeEliminated(adSpace, context, creative, "!beacons");
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAndroidMarketMedium(adSpace, context, creative, medium, listener));
    }

    /*
      * Test when medium is NOT Application
      * and destination data doesnt start with "market:"
      */
    @Test
    public void testCheckAndroidMarketMedium03() {
        final CreativeDto creative = new CreativeDto();
        final Medium medium = Medium.SITE;
        final String destinationData = randomAlphaNumericString(20);
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        destination.setData(destinationData);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                //oneOf (listener).creativeEliminated(adSpace, context, creative, "!beacons");
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAndroidMarketMedium(adSpace, context, creative, medium, listener));
    }

    /*
      * Test when medium is NOT Application
      * and destination data start with "market:"
      */
    @Test
    public void testCheckAndroidMarketMedium04() {
        final CreativeDto creative = new CreativeDto();
        final Medium medium = Medium.SITE;
        final String destinationData = "market:" + randomAlphaNumericString(20);
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = null;
        destination.setData(destinationData);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAndroidMarketMedium(adSpace, context, creative, medium, listener));
    }

    /*
      * Test when medium is NOT Application
      * and destination data start with "market:"
      * and listener is not null
      */
    @Test
    public void testCheckAndroidMarketMedium05() {
        final CreativeDto creative = new CreativeDto();
        final Medium medium = Medium.SITE;
        final String destinationData = "market:" + randomAlphaNumericString(20);
        final DestinationDto destination = new DestinationDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        destination.setData(destinationData);
        creative.setDestination(destination);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.marketUrlForNotApplication, "market:");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAndroidMarketMedium(adSpace, context, creative, medium, listener));
    }

    /*
        @Test
        public void testGetAndroidOsVersion01_SC_38() {
            @SuppressWarnings("serial")
            final Map<String, String> deviceProps = new HashMap<String, String>() {
                {
                    put("osVersion", "4.0.1");
                }
            };
            final double expectedVersion = 4.0;
            expect(new Expectations() {
                {
                    oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                    will(returnValue(null));
                    oneOf(context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, expectedVersion);
                }
            });
            assertEquals(expectedVersion, BasicTargetingEngineImpl.getAndroidOsVersion(deviceProps, context), 0.0);
        }

        @Test
        public void testGetAndroidOsVersion02_SC_38() {
            @SuppressWarnings("serial")
            final Map<String, String> deviceProps = new HashMap<String, String>() {
                {
                    put("osVersion", "4.0");
                }
            };
            final double expectedVersion = 4.0;
            expect(new Expectations() {
                {
                    oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                    will(returnValue(null));
                    oneOf(context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, expectedVersion);
                }
            });
            assertEquals(expectedVersion, BasicTargetingEngineImpl.getAndroidOsVersion(deviceProps, context), 0.0);
        }

        @Test
        public void testGetAndroidOsVersion03_SC_41() {
            final Map<String, String> deviceProps = new HashMap<String, String>();
            final double expectedVersion = -1.0;
            expect(new Expectations() {
                {
                    oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                    will(returnValue(null));
                    oneOf(context).getEffectiveUserAgent();
                    will(returnValue(""));
                    oneOf(context).setAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION, expectedVersion);
                }
            });
            assertEquals(expectedVersion, BasicTargetingEngineImpl.getAndroidOsVersion(deviceProps, context), 0.0);
        }
    */
    /*
    * When creative is not animated creative.isAnimate return false
    
    @Test
    public void testCheckAnimated01() {
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final boolean animated = false;
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;

        expect(new Expectations() {
            {
                oneOf(creative).isAnimated();
                will(returnValue(animated));
            }
        });
    
        assertTrue(BasicTargetingEngineImpl.checkAnimated(adSpace, context, creative, model, deviceProps, listener));
    }
    */
    /*
    * When creative is animated creative.isAnimated return true
    * and current request coming from non android device
    
    @Test
    public void testCheckAnimated02() {
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final boolean animated = true;
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Boolean isAndroid = false;
        expect(new Expectations() {
            {
                oneOf(creative).isAnimated();
                will(returnValue(animated));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(isAndroid));
            }
        });

        assertTrue(DeviceFeaturesTargetingChecks.checkAnimated(adSpace, context, creative, model, deviceProps, listener));
    }
    */
    /*
    * When creative is animated creative.isAnimated return true
    * and current request coming from android device with version later then 2.2
    
    @Test
    public void testCheckAnimated03() {
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final boolean animated = true;
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Boolean isAndroid = true;
        final Double androidOsVersion = 2.3;
        expect(new Expectations() {
            {
                oneOf(creative).isAnimated();
                will(returnValue(animated));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(isAndroid));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                will(returnValue(androidOsVersion));
            }
        });

        assertTrue(BasicTargetingEngineImpl.checkAnimated(adSpace, context, creative, model, deviceProps, listener));
    }
    */
    /*
    * When creative is animated creative.isAnimated return true
    * and current request coming from android device with version earlier then 2.2
    
    @Test
    public void testCheckAnimated04() {
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final boolean animated = true;
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = null;
        final Boolean isAndroid = true;
        final Double androidOsVersion = 2.1;
        expect(new Expectations() {
            {
                oneOf(creative).isAnimated();
                will(returnValue(animated));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(isAndroid));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                will(returnValue(androidOsVersion));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAnimated(adSpace, context, creative, model, deviceProps, listener));
    }
    */
    /*
    * When creative is animated creative.isAnimated return true
    * and current request coming from android device with version earlier then 2.2
    * and listener is not null
    
    @Test
    public void testCheckAnimated04_02() {
        final Map<String, String> deviceProps = new HashMap<String, String>();
        final boolean animated = true;
        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final ModelDto model = mock(ModelDto.class, "modelDto");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Boolean isAndroid = true;
        final Double androidOsVersion = 2.1;
        expect(new Expectations() {
            {
                oneOf(creative).isAnimated();
                will(returnValue(animated));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(isAndroid));
                oneOf(context).getAttribute(BasicTargetingEngineImpl.ANDROID_OS_VERSION);
                will(returnValue(androidOsVersion));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.animatedCreativeOnOldAndroid, "android^animated");
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkAnimated(adSpace, context, creative, model, deviceProps, listener));
    }
    */
    /*
    * When given operator is null
    * and wifi is not allowed by the segment
    
    @Test
    public void testCheckConnectionType01() {

        final CreativeDto creative = mock(CreativeDto.class, "creativeDto");
        final SegmentDto segment = mock(SegmentDto.class, "segmentDto");
        final OperatorDto operator = null;
        final TargetingEventListener listener = null;
        final ConnectionType ct = ConnectionType.OPERATOR;
        expect(new Expectations() {
            {
                oneOf(segment).getConnectionType();
                will(returnValue(ct));
            }
        });

        assertFalse(BasicTargetingEngineImpl.checkConnectionType(adSpace, context, creative, segment, operator, listener));
    }
    */
    /*
      * Test when context has isAndorid as true
      
    @Test
    public void testIsAndroid01() {
        final ModelDto model = new ModelDto();
        final Boolean yesNo = true;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(yesNo));

            }
        });

        assertTrue(BasicTargetingEngineImpl.isAndroid(model, context));
    }
    */
    /*
      * Test when context has isAndorid as false
     
    @Test
    public void testIsAndroid02() {
        final ModelDto model = new ModelDto();
        final Boolean yesNo = false;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(yesNo));

            }
        });

        assertFalse(BasicTargetingEngineImpl.isAndroid(model, context));
    }
    */
    /*
      * Test when context has isAndorid as null
      * and model platforms do contain android platform
      
    @Test
    public void testIsAndroid03() {
        final ModelDto model = new ModelDto();
        final Boolean yesNo = null;
        final long platformId = randomLong();
        final PlatformDto platform = new PlatformDto();
        platform.setId(platformId);

        model.getPlatforms().add(platform);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(yesNo));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getPlatformBySystemName("android");
                will(returnValue(platform));
                oneOf(context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, true);

            }
        });

        assertTrue(BasicTargetingEngineImpl.isAndroid(model, context));
    }
    */
    /*
      * Test when context has isAndorid as null
      * and model platforms do NOT contain android platform
      
    @Test
    public void testIsAndroid04() {
        final ModelDto model = new ModelDto();
        final Boolean yesNo = null;
        final long platformId = randomLong();
        final PlatformDto platform = new PlatformDto();
        final long anotherPlatformId = randomLong();
        final PlatformDto anotherPlatform = new PlatformDto();
        platform.setId(platformId);
        anotherPlatform.setId(anotherPlatformId);

        model.getPlatforms().add(platform);
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(BasicTargetingEngineImpl.IS_ANDROID, Boolean.class);
                will(returnValue(yesNo));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getPlatformBySystemName("android");
                will(returnValue(anotherPlatform));
                oneOf(context).setAttribute(BasicTargetingEngineImpl.IS_ANDROID, false);

            }
        });

        assertFalse(BasicTargetingEngineImpl.isAndroid(model, context));
    }
    */
    /*
      * Test when Ecpm weight is more then 0
      * and cache is RtbEnabled
      */
    @Test
    public void testAddTargetedCreative02() {
        final CreativeDto creative = new CreativeDto();
        final FastLinkedList<MutableWeightedCreative> mwcs = new FastLinkedList<MutableWeightedCreative>();
        final double ecpmWeight = 1.2;
        final long adSpaceId = randomLong();

        expect(new Expectations() {
            {
                oneOf(adSpace).getId();
                will(returnValue(adSpaceId));
            }
        });
        //This call should have add one item to mwcs
        basicTargetingEngineImpl.addTargetedCreative(adSpace, creative, mwcs, reusablePool, context, ecpmWeight);
        assertFalse(mwcs.isEmpty());
        assertEquals(mwcs.size(), 1);
        MutableWeightedCreative mwc = mwcs.remove(0);
        assertEquals(mwc.getAdSpaceId(), adSpaceId);
        assertEquals(mwc.getCreative(), creative);
        assertEquals(mwc.getEcpmWeight(), ecpmWeight, .01);
        assertEquals(mwc.getWeight(), ecpmWeight, .01);
    }

    /*
      * Test when Ecpm weight is more then 0
      * and cache is NOT RtbEnabled
      * and resuable pool is empty
      */
    @Test
    public void testAddTargetedCreative03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final FastLinkedList<MutableWeightedCreative> mwcs = new FastLinkedList<MutableWeightedCreative>();
        final double ecpmWeight = 1.2;
        final double boostFactor = 2.1;
        final long adSpaceId = randomLong();
        creative.setCampaign(campaign);
        campaign.setBoostFactor(boostFactor);

        expect(new Expectations() {
            {
                oneOf(adSpace).getId();
                will(returnValue(adSpaceId));
            }
        });
        //This call should have add one item to mwcs
        basicTargetingEngineImpl.addTargetedCreative(adSpace, creative, mwcs, reusablePool, context, ecpmWeight);
        assertFalse(mwcs.isEmpty());
        assertEquals(mwcs.size(), 1);
        MutableWeightedCreative mwc = mwcs.remove(0);
        assertEquals(mwc.getAdSpaceId(), adSpaceId);
        assertEquals(mwc.getCreative(), creative);
        assertEquals(mwc.getEcpmWeight(), ecpmWeight, .01);
        assertEquals(mwc.getWeight(), ecpmWeight, .01);
    }

    /*
      * Test when Ecpm weight is more then 0
      * and cache is NOT RtbEnabled
      * and resuable pool is NOT empty
      */
    @Test
    public void testAddTargetedCreative04() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final FastLinkedList<MutableWeightedCreative> mwcs = new FastLinkedList<MutableWeightedCreative>();
        final double ecpmWeight = 1.2;
        final double boostFactor = 2.1;
        final long adSpaceId = randomLong();
        final MutableWeightedCreative reusableMwc = new MutableWeightedCreative();
        creative.setCampaign(campaign);
        campaign.setBoostFactor(boostFactor);
        reusablePool.add(reusableMwc);

        expect(new Expectations() {
            {
                oneOf(adSpace).getId();
                will(returnValue(adSpaceId));
            }
        });
        //This call should have add one item to mwcs
        basicTargetingEngineImpl.addTargetedCreative(adSpace, creative, mwcs, reusablePool, context, ecpmWeight);
        assertFalse(mwcs.isEmpty());
        assertEquals(mwcs.size(), 1);
        MutableWeightedCreative mwc = mwcs.remove(0);
        assertEquals(mwc.getAdSpaceId(), adSpaceId);
        assertEquals(mwc.getCreative(), creative);
        assertEquals(mwc.getEcpmWeight(), ecpmWeight, .01);
        assertEquals(mwc.getWeight(), ecpmWeight, .01);
        assertTrue(reusableMwc == mwc);
        assertTrue(reusablePool.isEmpty());
    }

    /*
      * Test whene Gender and ageRange is null
      */
    @Test
    public void testCheckGenderAndAge01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final Range<Integer> ageRange = null;
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is not null and segment.getGenderMix is null
      * ageRange is null
      */
    @Test
    public void testCheckGenderAndAge02() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = Gender.FEMALE;
        final Range<Integer> ageRange = null;
        final BigDecimal genderMix = null;
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setGenderMix(genderMix);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is not null and segment.getGenderMix is not null
      * and gender.genderMix is not null and its not completely mismatch, i.e. Math.abs(gender.getMixValue() - genderMix) != 1
      *
      * ageRange is null
      */
    @Test
    public void testCheckGenderAndAge03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = Gender.FEMALE;
        final Range<Integer> ageRange = null;
        final BigDecimal genderMix = new BigDecimal("0.7");
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setGenderMix(genderMix);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is not null and segment.getGenderMix is not null
      * and gender.genderMix is not null and its completely mismatch, i.e. Math.abs(gender.getMixValue() - genderMix) == 1
      *
      * ageRange is null
      */
    @Test
    public void testCheckGenderAndAge04() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = Gender.FEMALE;
        final Range<Integer> ageRange = null;
        final BigDecimal genderMix = new BigDecimal(Gender.MALE.getMixValue());
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setGenderMix(genderMix);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, CreativeEliminatedReason.GenderMismatch);
    }

    /*
      * Test whene Gender is not null and segment.getGenderMix is not null
      * and gender.genderMix is not null and its completely mismatch, i.e. Math.abs(gender.getMixValue() - genderMix) == 1
      * and listener is not null
      * ageRange is null
      */
    @Test
    public void testCheckGenderAndAge05() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = Gender.FEMALE;
        final Range<Integer> ageRange = null;
        final BigDecimal genderMix = new BigDecimal(Gender.MALE.getMixValue());
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setGenderMix(genderMix);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.GenderMismatch, "genderDelta==1.0");
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, CreativeEliminatedReason.GenderMismatch);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is more then ageRange start
      * and segment.minAge is more then ageRange End
      * i.e no overlapping
      */
    @Test
    public void testCheckGenderAndAge06() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 37;
        final int segmentMaxAge = 45;
        //---18----------36-----37---------45
        //     Age Range         Targetted Age        

        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, CreativeEliminatedReason.AgeRangeMismatch);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is less then ageRange start
      * and segment.minAge is Less then ageRange start
      * i.e no overlapping
      * and listener is not null
      */
    @Test
    public void testCheckGenderAndAge07() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 8;
        final int segmentMaxAge = 10;
        //---8---10--------18---------36
        //  Targetted Age    Age Range        
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AgeRangeMismatch, "ageRange");
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, CreativeEliminatedReason.AgeRangeMismatch);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is more then ageRange end
      * and segment.minAge is Less then ageRange End
      * i.e partial overlapping
      */
    @Test
    public void testCheckGenderAndAge08() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 20;
        final int segmentMaxAge = 40;
        //--------18----20-----36----40
        //  Targetted Age(20,40)    Age Range(18,36)        
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is less then ageRange end
      * and segment.minAge is more then ageRange start
      * i.e partial overlapping
      */
    @Test
    public void testCheckGenderAndAge09() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 12;
        final int segmentMaxAge = 34;
        //--------12----18-----34----36
        //  Targetted Age(12,34)    Age Range(18,36)        
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is more then ageRange end
      * and segment.minAge is Less then ageRange start
      * i.e partial overlapping
      */
    @Test
    public void testCheckGenderAndAge10() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 10;
        final int segmentMaxAge = 40;
        //--------10----18---------36----40
        //  Targetted Age(10,40)    Age Range(18,36)        
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test whene Gender is null
      * ageRange is not null
      * and segment.maxAge is more then ageRange end
      * and segment.minAge is Less then ageRange start
      * i.e partial overlapping
      */
    @Test
    public void testCheckGenderAndAge11() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final Gender gender = null;
        final int ageRangeStart = 18;
        final int ageRangeEnd = 36;
        final Range<Integer> ageRange = new Range<Integer>(ageRangeStart, ageRangeEnd);
        final int segmentMinAge = 20;
        final int segmentMaxAge = 32;
        //--------18----20------32-------36
        //  Targetted Age(20,32)    Age Range(18,36)        
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.setMinAge(segmentMinAge);
        segment.setMaxAge(segmentMaxAge);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = BasicTargetingEngineImpl.checkGenderAndAge(adSpace, context, creative, segment, gender, ageRange, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when segment capabilities ID map is empty
      */
    @Test
    public void testCheckCapabilities01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = null;
        final Set<Long> capabilityIds = null;
        creative.setCampaign(campaign);
        creative.setSegment(segment);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment matches with requested capabilities
      */
    @Test
    public void testCheckCapabilities02() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = null;
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, true);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {

            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment matches with requested capabilities
      * but segment has that id's required property as null
      */
    @Test
    public void testCheckCapabilities03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = null;
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, null);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, CreativeEliminatedReason.CapabilityNotRequired);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment matches with requested capabilities
      * but segment has that id's required property as null
      * and listener is not null
      */
    @Test
    public void testCheckCapabilities04() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, null);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityNotRequired,
                        "Device Capability required is null: " + segmentCapabilityId);
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, CreativeEliminatedReason.CapabilityNotRequired);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment matches with requested capabilities
      * but segment has that id's required property as false
      * and listener is not null
      */
    @Test
    public void testCheckCapabilities05() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, false);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityMismatch,
                        "Device capabilities " + capabilityIds + " are missing " + segmentCapabilityId);
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, CreativeEliminatedReason.CapabilityMismatch);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment DO NOT matches with requested capabilities
      * but segment has that id's required property as true
      * and listener is not null
      */
    @Test
    public void testCheckCapabilities06() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId + 1;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, true);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityMismatch,
                        "Device capabilities " + capabilityIds + " are missing " + segmentCapabilityId);
            }
        });

        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, CreativeEliminatedReason.CapabilityMismatch);
    }

    /*
      * Test when segment capabilities ID map is NOT empty
      * and all capabilities of segment DO NOT matches with requested capabilities
      * but segment has that id's required property as false
      * and listener is not null
      */
    @Test
    public void testCheckCapabilities07() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final SegmentDto segment = new SegmentDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Set<Long> capabilityIds = new HashSet<Long>();
        final long segmentCapabilityId = randomLong();
        final long requestCapabilityId = segmentCapabilityId + 1;
        creative.setCampaign(campaign);
        creative.setSegment(segment);
        segment.getCapabilityIdMap().put(segmentCapabilityId, false);
        capabilityIds.add(requestCapabilityId);

        expect(new Expectations() {
            {
            }
        });
        CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, capabilityIds, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when campaign is currently active
      * i.e.e campaign.isCurrentlyActive returns true
      */
    @Test
    public void testCheckAdvancedScheduling01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setCampaign(campaign);
        campaign.setStatus(Status.ACTIVE);

        expect(new Expectations() {
            {
            }
        });
        assertTrue(BasicTargetingEngineImpl.checkAdvancedScheduling(adSpace, context, creative, listener));
    }

    /*
      * Test when campaign is currently Not active
      * i.e.e campaign.isCurrentlyActive returns false
      */
    @Test
    public void testCheckAdvancedScheduling02() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        campaign.setStatus(Status.STOPPED);

        expect(new Expectations() {
            {
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkAdvancedScheduling(adSpace, context, creative, listener));
    }

    /*
      * Test when campaign is currently Not active
      * i.e.e campaign.isCurrentlyActive returns false
      * and listener is not null
      */
    @Test
    public void testCheckAdvancedScheduling03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setCampaign(campaign);
        campaign.setStatus(Status.STOPPED);

        final String eMsg = "Campaign not running: " + campaign.getStatus() + ", " + campaign.getSortedTimePeriods();
        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.campaignNotCurrentlyActive, eMsg);
            }
        });
        assertFalse(BasicTargetingEngineImpl.checkAdvancedScheduling(adSpace, context, creative, listener));
    }

    /*
      * Test when campaign is not install tacking enabled
      * and also campaign has no deiveIdentifier types.
      */
    @Test
    public void testCheckDeviceIdentifiers01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignInstallTrackingEnabled = false;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        creative.setCampaign(campaign);
        campaign.setInstallTrackingEnabled(camapignInstallTrackingEnabled);

        expect(new Expectations() {
            {
                //oneOf (listener).creativeEliminated(adSpace, context, creative, "!campaign.isCurrentlyActive");
            }
        });

        CreativeEliminatedReason elimination = DeviceIdentifierTargetingChecks.checkDeviceIdentifiers(adSpace, context, creative, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when campaign is install tacking enabled
      * and also campaign has no deiveIdentifier types.
      * and publisherSuppliedDeviceIdentifiers is empty
      */
    @Test
    public void testCheckDeviceIdentifiers02() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignInstallTrackingEnabled = true;
        final TargetingEventListener listener = null;
        final Map<Long, String> publisherSuppliedDeviceIdentifiers = new HashMap<Long, String>();
        creative.setCampaign(campaign);
        campaign.setInstallTrackingEnabled(camapignInstallTrackingEnabled);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(publisherSuppliedDeviceIdentifiers));
            }
        });
        CreativeEliminatedReason elimination = DeviceIdentifierTargetingChecks.checkDeviceIdentifiers(adSpace, context, creative, listener);
        assertEquals(elimination, CreativeEliminatedReason.NoDeviceIdentifier);
    }

    /*
      * Test when campaign is install tacking enabled
      * and also campaign has no deiveIdentifier types.
      * and publisherSuppliedDeviceIdentifiers is empty
      * and listener is not null
      */
    @Test
    public void testCheckDeviceIdentifiers03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignInstallTrackingEnabled = true;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final Map<Long, String> publisherSuppliedDeviceIdentifiers = new HashMap<Long, String>();
        creative.setCampaign(campaign);
        campaign.setInstallTrackingEnabled(camapignInstallTrackingEnabled);

        expect(new Expectations() {
            {
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoDeviceIdentifier, "No device identifiers in bid");
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(publisherSuppliedDeviceIdentifiers));
            }
        });

        CreativeEliminatedReason elimination = DeviceIdentifierTargetingChecks.checkDeviceIdentifiers(adSpace, context, creative, listener);
        assertEquals(elimination, CreativeEliminatedReason.NoDeviceIdentifier);
    }

    /*
      * Test when campaign is install tacking enabled
      * and also campaign has no deiveIdentifier types.
      * and publisherSuppliedDeviceIdentifiers is NOT empty
      */
    @Test
    public void testCheckDeviceIdentifiers03_01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignInstallTrackingEnabled = true;
        final TargetingEventListener listener = null;
        final Map<Long, String> publisherSuppliedDeviceIdentifiers = new HashMap<Long, String>();
        final long publisherSuppliedDeviceIdentifierTypeId = randomLong();
        creative.setCampaign(campaign);
        campaign.setInstallTrackingEnabled(camapignInstallTrackingEnabled);
        publisherSuppliedDeviceIdentifiers.put(publisherSuppliedDeviceIdentifierTypeId, "");

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(publisherSuppliedDeviceIdentifiers));
            }
        });

        CreativeEliminatedReason elimination = DeviceIdentifierTargetingChecks.checkDeviceIdentifiers(adSpace, context, creative, listener);
        assertEquals(elimination, null);
    }

    /*
      * Test when camapignDisableLanguageMatch is true
      */
    @Test
    public void testCheckLanguage01() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = true;
        final TargetingEventListener listener = null;
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(null));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        Double expectedLanguageQuality = 1.0;
        assertEquals(expectedLanguageQuality, languageQuality, .01);
    }

    /*
      * Test when camapignDisableLanguageMatch is false
      * and context Accepted language is null
      * and apspace.publication.languageId is empty
      */
    @Test
    public void testCheckLanguage02() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = false;
        final TargetingEventListener listener = null;
        final AcceptedLanguages acceptedLanguages = null;
        final Set<Long> publciationLanguageIds = new HashSet<Long>();
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
                will(returnValue(acceptedLanguages));
                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getLanguageIds();
                will(returnValue(publciationLanguageIds));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        Double expectedLanguageQuality = 1.0;
        assertEquals(expectedLanguageQuality, languageQuality, .01);
    }

    /*
      * Test when camapignDisableLanguageMatch is false
      * and context Accepted language is null
      * and apspace.publication.languageId is Not empty and it do contain creative.languageId
      */
    @Test
    public void testCheckLanguage03() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = false;
        final TargetingEventListener listener = null;
        final AcceptedLanguages acceptedLanguages = null;
        final Set<Long> publciationLanguageIds = new HashSet<Long>();
        final Long creativeLanguageId = randomLong();
        final Long publicationLanguageId = creativeLanguageId;
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);
        creative.setLanguageId(creativeLanguageId);
        publciationLanguageIds.add(publicationLanguageId);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
                will(returnValue(acceptedLanguages));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getLanguageIds();
                will(returnValue(publciationLanguageIds));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        Double expectedLanguageQuality = 1.0;
        assertEquals(expectedLanguageQuality, languageQuality, .01);
    }

    /*
      * Test when camapignDisableLanguageMatch is false
      * and context Accepted language is null
      * and apspace.publication.languageId is Not empty and it do not contain creative.languageId
      */
    @Test
    public void testCheckLanguage04() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = false;
        final TargetingEventListener listener = null;
        final AcceptedLanguages acceptedLanguages = null;
        final Set<Long> publciationLanguageIds = new HashSet<Long>();
        final Long creativeLanguageId = uniqueLong("LanguageId");
        final Long publicationLanguageId = uniqueLong("LanguageId");
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);
        creative.setLanguageId(creativeLanguageId);
        publciationLanguageIds.add(publicationLanguageId);

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
                will(returnValue(acceptedLanguages));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getLanguageIds();
                will(returnValue(publciationLanguageIds));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        assertNull(languageQuality);
    }

    /*
      * Test when camapignDisableLanguageMatch is false
      * and context Accepted language is null
      * and apspace.publication.languageId is Not empty and it do not contain creative.languageId
      * and listener is not null
      */
    @Test
    public void testCheckLanguage05() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = false;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final AcceptedLanguages acceptedLanguages = null;
        final Set<Long> publciationLanguageIds = new HashSet<Long>();
        final Long creativeLanguageId = uniqueLong("LanguageId");
        final Long publicationLanguageId = uniqueLong("LanguageId");
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);
        creative.setLanguageId(creativeLanguageId);
        publciationLanguageIds.add(publicationLanguageId);

        final String eMsg = "Languages of Publication: " + publciationLanguageIds + " vs Creative: " + creative.getLanguageId();
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(null));
                oneOf(context).getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
                will(returnValue(acceptedLanguages));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getLanguageIds();
                will(returnValue(publciationLanguageIds));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentPubblicationLanguage, eMsg);
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        assertNull(languageQuality);
    }

    /*
     * BLOCKED_LANGUAGE_ISO_CODES is not empty, but creative.language is not blocked
     * campaign does not require language matching
     */
    @Test
    public void testCheckLanguage06_some_blocked_but_creative_language_not_blocked() {
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final boolean camapignDisableLanguageMatch = true;
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final long creativeLanguageId = randomLong();
        final String creativeLanguageIsoCode = uniqueAlphaNumericString(2, "Language.isoCode");
        creative.setCampaign(campaign);
        campaign.setDisableLanguageMatch(camapignDisableLanguageMatch);
        creative.setLanguageId(creativeLanguageId);
        final LanguageDto creativeLanguage = new LanguageDto();
        creativeLanguage.setISOCode(creativeLanguageIsoCode);
        @SuppressWarnings("serial")
        final Set<String> blockedLanguageIsoCodes = new HashSet<String>() {
            {
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
            }
        };

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(blockedLanguageIsoCodes));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getLanguageById(creativeLanguageId);
                will(returnValue(creativeLanguage));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        assertEquals(1.0, languageQuality, 0.0);
    }

    /*
     * BLOCKED_LANGUAGE_ISO_CODES is not empty, and creative.language is blocked
     * with listener
     */
    @Test
    public void testCheckLanguage07_creative_language_blocked_with_listener() {
        final CreativeDto creative = new CreativeDto();
        final TargetingEventListener listener = mock(TargetingEventListener.class, "TargetingEventListener");
        final long creativeLanguageId = randomLong();
        final String creativeLanguageIsoCode = uniqueAlphaNumericString(2, "Language.isoCode");
        creative.setLanguageId(creativeLanguageId);
        final LanguageDto creativeLanguage = new LanguageDto();
        creativeLanguage.setISOCode(creativeLanguageIsoCode);
        @SuppressWarnings("serial")
        final Set<String> blockedLanguageIsoCodes = new HashSet<String>() {
            {
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
                add(creativeLanguageIsoCode);
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
            }
        };

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(blockedLanguageIsoCodes));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getLanguageById(creativeLanguageId);
                will(returnValue(creativeLanguage));
                oneOf(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.LanguageBlocked, "Bid block language: " + creativeLanguageIsoCode);
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, listener);
        assertNull(languageQuality);
    }

    /*
     * BLOCKED_LANGUAGE_ISO_CODES is not empty, and creative.language is blocked
     * no listener
     */
    @Test
    public void testCheckLanguage08_creative_language_blocked_no_listener() {
        final CreativeDto creative = new CreativeDto();
        final long creativeLanguageId = randomLong();
        final String creativeLanguageIsoCode = uniqueAlphaNumericString(2, "Language.isoCode");
        creative.setLanguageId(creativeLanguageId);
        final LanguageDto creativeLanguage = new LanguageDto();
        creativeLanguage.setISOCode(creativeLanguageIsoCode);
        @SuppressWarnings("serial")
        final Set<String> blockedLanguageIsoCodes = new HashSet<String>() {
            {
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
                add(creativeLanguageIsoCode);
                add(uniqueAlphaNumericString(2, "Language.isoCode"));
            }
        };

        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
                will(returnValue(blockedLanguageIsoCodes));
                oneOf(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getLanguageById(creativeLanguageId);
                will(returnValue(creativeLanguage));
            }
        });
        Double languageQuality = BasicTargetingEngineImpl.checkLanguage(adSpace, context, creative, null);
        assertNull(languageQuality);
    }

    @Test
    public void AO271_hidden_model() {
        final long adSpaceId = randomLong();
        final Collection<Long> allowedFormatIds = new HashSet<Long>();
        final boolean diagnosticMode = false;
        final TimeLimit timeLimit = mock(TimeLimit.class, "timeLimit");
        final TargetingEventListener listener = mock(TargetingEventListener.class, "listener");
        final ByydImp imp = mock(ByydImp.class, "imp");

        final Boolean isPrivateNetwork = Boolean.FALSE;
        final Map<String, String> deviceProps = new HashMap<String, String>();
        deviceProps.put("mobileDevice", "1");

        final ModelDto model = mock(ModelDto.class, "model");
        final VendorDto vendor = mock(VendorDto.class, "vendor");

        expect(new Expectations() {
            {
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getName();
                will(returnValue(randomAlphaNumericString(10)));
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                allowing(model).getVendor();
                will(returnValue(vendor));
                allowing(vendor).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(model).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(model).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(model).isHidden();
                will(returnValue(true));
                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);
                oneOf(listener).unfilledRequest(adSpace, context);
            }
        });
        SelectedCreative selectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, listener);
        assertNull(selectedCreative);

        //Once when listener is null
        TargetingEventListener nullListener = null;
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class);
                will(returnValue(isPrivateNetwork));
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(deviceProps));
                oneOf(context).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(context).setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);
            }
        });
        SelectedCreative anotherSelectedCreative = basicTargetingEngineImpl.selectCreative(adSpace, allowedFormatIds, context, diagnosticMode, false, timeLimit, nullListener);
        assertNull(anotherSelectedCreative);
    }

    @Test
    public void matchGeotargetsForChineseProvinces() {
        Collection<Long> geoIds = new ArrayList<>();
        geoIds.add(42L);
        geoIds.add(43L);
        geoIds.add(44L);

        final GeotargetDto geoTarget42 = new GeotargetDto();
        geoTarget42.setCountryIsoCode("CN");
        geoTarget42.setType(Type.STATE);
        geoTarget42.setName("shandong");

        final GeotargetDto geoTarget43 = new GeotargetDto();
        geoTarget43.setCountryIsoCode("CN");
        geoTarget43.setType(Type.STATE);
        geoTarget43.setName("Some other place");

        final GeotargetDto geoTarget44 = new GeotargetDto();
        geoTarget44.setCountryIsoCode("XX");
        geoTarget44.setType(Type.STATE);
        geoTarget44.setName("shandong");

        expect(new Expectations() {
            {
                oneOf(domainCache).getGeotargetById(42L);
                will(returnValue(geoTarget42));
                oneOf(domainCache).getGeotargetById(43L);
                will(returnValue(geoTarget43));
                oneOf(domainCache).getGeotargetById(44L);
                will(returnValue(geoTarget44));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(context).getAttribute(TargetingContext.CHINESE_PROVINCE);
                will(returnValue(ChineseProvince.SD));
            }
        });

        List<GeotargetDto> res = DeviceLocationTargetingChecks.matchGeotargets(context, geoIds, domainCache, false);

        assertNotNull(res);
        assertThat(res.size(), is(1));
        assertThat(res.get(0), is(geoTarget42));
    }

    @Test
    public void matchGeotargetsForAustrianProvinces() {
        Collection<Long> geoIds = new ArrayList<>();
        geoIds.add(42L);
        geoIds.add(43L);
        geoIds.add(44L);

        final GeotargetDto geoTarget42 = new GeotargetDto();
        geoTarget42.setCountryIsoCode("AT");
        geoTarget42.setType(Type.STATE);
        geoTarget42.setName("Tirol");

        final GeotargetDto geoTarget43 = new GeotargetDto();
        geoTarget43.setCountryIsoCode("AT");
        geoTarget43.setType(Type.STATE);
        geoTarget43.setName("Some other place");

        final GeotargetDto geoTarget44 = new GeotargetDto();
        geoTarget44.setCountryIsoCode("XX");
        geoTarget44.setType(Type.STATE);
        geoTarget44.setName("Tirol");

        expect(new Expectations() {
            {
                oneOf(domainCache).getGeotargetById(42L);
                will(returnValue(geoTarget42));
                oneOf(domainCache).getGeotargetById(43L);
                will(returnValue(geoTarget43));
                oneOf(domainCache).getGeotargetById(44L);
                will(returnValue(geoTarget44));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(context).getAttribute(TargetingContext.AUSTRIAN_PROVINCE);
                will(returnValue(AustrianProvince.TR));
            }
        });

        List<GeotargetDto> res = DeviceLocationTargetingChecks.matchGeotargets(context, geoIds, domainCache, false);

        assertNotNull(res);
        assertThat(res.size(), is(1));
        assertThat(res.get(0), is(geoTarget42));
    }

    @Test
    public void matchGeotargetsForSpanishProvinces() {
        Collection<Long> geoIds = new ArrayList<>();
        geoIds.add(42L);
        geoIds.add(43L);
        geoIds.add(44L);

        final GeotargetDto geoTarget42 = new GeotargetDto();
        geoTarget42.setCountryIsoCode("ES");
        geoTarget42.setType(Type.STATE);
        geoTarget42.setName("Pais Vasco");

        final GeotargetDto geoTarget43 = new GeotargetDto();
        geoTarget43.setCountryIsoCode("ES");
        geoTarget43.setType(Type.STATE);
        geoTarget43.setName("Some other place");

        final GeotargetDto geoTarget44 = new GeotargetDto();
        geoTarget44.setCountryIsoCode("XX");
        geoTarget44.setType(Type.STATE);
        geoTarget44.setName("Pais Vasco");

        expect(new Expectations() {
            {
                oneOf(domainCache).getGeotargetById(42L);
                will(returnValue(geoTarget42));
                oneOf(domainCache).getGeotargetById(43L);
                will(returnValue(geoTarget43));
                oneOf(domainCache).getGeotargetById(44L);
                will(returnValue(geoTarget44));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                allowing(context).getAttribute(TargetingContext.SPANISH_PROVINCE);
                will(returnValue("Pais Vasco"));
            }
        });

        List<GeotargetDto> res = DeviceLocationTargetingChecks.matchGeotargets(context, geoIds, domainCache, false);

        assertNotNull(res);
        assertThat(res.size(), is(1));
        assertThat(res.get(0), is(geoTarget42));
    }

}
