package com.adfonic.adserver.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.plugin.PluginFillRateTracker;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto.AudienceType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.util.stats.CounterManager;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BasicTargetingEngineImplTest {

    @Mock
    private DisplayTypeUtils displayTypeUtils;
    @Mock
    private PluginManager pluginManager;
    @Mock
    private StoppageManager stoppageManager;
    @Mock
    private FrequencyCounter frequencyCounter;
    @Mock
    private PluginFillRateTracker pluginFillRateTracker;
    @Mock
    private StatusChangeManager statusChangeManager;
    @Mock
    private CounterManager counterManager;
    @Mock
    private LocalBudgetManager budgetManager;
    @Mock
    DeviceLocationTargetingChecks geoTargeting;
    @Mock
    DeviceIdentifierTargetingChecks didTargeting;
    @Mock
    private TargetingEventListener listener = Mockito.mock(TargetingEventListener.class);
    @Mock
    private TargetingContext context;

    private DateTime mockedNow = new DateTime(2014, 10, 24, 11, 52, DateTimeZone.UTC);

    private DeviceIdentifierTargetingChecks testObj;
    private Interval nullInterval = null;
    private Instant oct7 = new DateTime(2014, 10, 1, 10, 52, DateTimeZone.UTC).toInstant();
    private Instant oct17 = new DateTime(2014, 10, 17, 10, 52, DateTimeZone.UTC).toInstant();
    private Interval oct7_17 = new Interval(oct7, oct17);

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        testObj = new DeviceIdentifierTargetingChecks(new HashSet<Long>());
        DateTimeUtils.setCurrentMillisFixed(mockedNow.getMillis());
    }

    @After
    public void after() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testCheckAudienceTargetingWithoutAudience() {

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);

        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertEquals(elimination, CreativeEliminatedReason.DeviceRedisMismatch);
        Mockito.verify(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceRedisMismatch,
                "Device Id audiences [] vs " + creative.getCampaign().getDeviceIdAudiences());
    }

    @Test
    public void testNegativeTargetingWithoutAudience() {

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        creative.getCampaign().getDeviceIdAudiences().clear();
        creative.getCampaign().getDeviceIdAudiences().add(new CampaignAudienceDto(6L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));

        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);

        Set<Long> deviceAudiences = Collections.emptySet();
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);

        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertNull(elimination);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testNegativeTargetingWithNullAudience() {

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        creative.getCampaign().getDeviceIdAudiences().clear();
        creative.getCampaign().getDeviceIdAudiences().add(new CampaignAudienceDto(6L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));

        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);
        Set<Long> deviceAudiences = null;
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);

        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertNull(elimination);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testWhenEmptyOptOut() {

        testObj = new DeviceIdentifierTargetingChecks(new HashSet<>(Arrays.asList(12345l, 45678l)));

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();

        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);
        Set<Long> deviceAudiences = makeSet(1L, 2L, 3L);
        Set<OptOutType> ooSet = Collections.emptySet();
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class)).thenReturn(ooSet);

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertNull(elimination);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testWhenNoOptOut() {
        testObj = new DeviceIdentifierTargetingChecks(new HashSet<>(Arrays.asList(12345l, 45678l)));

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);

        Set<Long> deviceAudiences = makeSet(1L, 2L, 3L);
        Set<OptOutType> ooSet = new HashSet(Arrays.asList(OptOutType.noOptout));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class)).thenReturn(ooSet);

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertNull(elimination);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testWhenWeveOptOut() {
        testObj = new DeviceIdentifierTargetingChecks(new HashSet<>(Arrays.asList(12345l, 45678l)));

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();

        Set<Long> deviceAudiences = makeSet(1L, 2L, 3L);
        Set<OptOutType> ooSet = new HashSet(Arrays.asList(OptOutType.weve));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class)).thenReturn(ooSet);

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertEquals(elimination, CreativeEliminatedReason.OptedOut);
        Mockito.verify(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OptedOut, "weve optout");
    }

    @Test
    public void testWhenGlobalOptOut() {
        testObj = new DeviceIdentifierTargetingChecks(new HashSet<Long>());

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();

        Set<Long> deviceAudiences = makeSet(1L, 2L, 3L);
        Set<OptOutType> ooSet = new HashSet(Arrays.asList(OptOutType.global));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class)).thenReturn(ooSet);

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertEquals(elimination, CreativeEliminatedReason.OptedOut);
        Mockito.verify(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.OptedOut, "global optout");
    }

    @Test
    public void testWhenWeveOptOutButNotWeveCampaign() {
        testObj = new DeviceIdentifierTargetingChecks(new HashSet<>(Arrays.asList(0l))); //no weve

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);

        Set<Long> deviceAudiences = makeSet(1L, 2L, 3L);
        Set<OptOutType> ooSet = new HashSet(Arrays.asList(OptOutType.weve));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_OPT_OUT, Set.class)).thenReturn(ooSet);

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertNull(elimination);
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testCheckAudienceTargetingWithExcluded() {

        AdSpaceDto adSpace = new AdSpaceDto();
        CreativeDto creative = getCreative();
        DeviceData dd = new DeviceData();
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(dd));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA, Set.class)).thenReturn(ddSet);

        Set<Long> deviceAudiences = makeSet(1L, 2L, 666L);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_AUDIENCES, Set.class)).thenReturn(deviceAudiences);
        creative.getCampaign().getDeviceIdAudiences().add(new CampaignAudienceDto(6L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));

        // act
        CreativeEliminatedReason elimination = testObj.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
        Assert.assertEquals(elimination, CreativeEliminatedReason.DeviceRedisMismatch);
        Mockito.verify(listener).creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DeviceRedisMismatch,
                "Device Id audiences " + deviceAudiences + " vs " + creative.getCampaign().getDeviceIdAudiences());
    }

    @Test
    public void isEligibleCampaignWithoutAudience() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        Set<Long> deviceAudienceIds = makeSet(1L);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertTrue(result);
    }

    @Test
    public void isEligibleOneIncluded() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(0L, 1L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval));
        Set<Long> deviceAudienceIds = makeSet(1L, 2L, 3L);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertTrue(result);
    }

    @Test
    public void isEligibleWithRecency() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(0L, 1L, AudienceType.DEVICE_ID, true, null, 10, 1, nullInterval));
        campaignAudience.add(new CampaignAudienceDto(1L, 3L, AudienceType.DEVICE_ID, true, null, 10, 1, nullInterval));
        Set<Long> deviceAudienceIds = makeSet(1L, 2L, 3L);

        Map<Long, Instant> recencyByAudience = new HashMap<>();
        Instant sevenDaysAgo = new DateTime(2014, 10, 17, 11, 52, DateTimeZone.UTC).toInstant();
        recencyByAudience.put(3L, sevenDaysAgo);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, recencyByAudience);
        Assert.assertTrue(result);
    }

    @Test
    public void isEligibleOneExcluded() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(0L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));
        Set<Long> deviceAudienceIds = makeSet(1L, 2L, 666L);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertFalse(result);
    }

    @Test
    public void isEligibleOneExcludedAndIncluded() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(0L, 1L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval));
        campaignAudience.add(new CampaignAudienceDto(2L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));
        Set<Long> deviceAudienceIds = makeSet(1L, 2L, 666L);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertFalse(result);
    }

    @Test
    public void isEligibleIncludeExcludeHappyPath() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(0L, 1L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval));
        campaignAudience.add(new CampaignAudienceDto(2L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));
        Set<Long> deviceAudienceIds = makeSet(1L, 2L);

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertTrue(result);
    }

    @Test
    public void negativeTargettedDeviceWithNoAudiencesShouldBeEligible() {
        Set<CampaignAudienceDto> campaignAudience = new HashSet<>();
        campaignAudience.add(new CampaignAudienceDto(2L, 666L, AudienceType.DEVICE_ID, false, null, null, null, nullInterval));
        Set<Long> deviceAudienceIds = Collections.emptySet();

        boolean result = testObj.isAudienceTargeted(campaignAudience, deviceAudienceIds, Collections.<Long, Instant> emptyMap());
        Assert.assertTrue(result);
    }

    private CreativeDto getCreative() {
        CreativeDto creative = new CreativeDto();
        creative.setId(234L);
        CampaignDto campaign = new CampaignDto();
        campaign.setHasAudience(true);
        campaign.getDeviceIdAudiences().add(new CampaignAudienceDto(1L, 2L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval));
        campaign.getDeviceIdAudiences().add(new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval));

        creative.setCampaign(campaign);
        AdvertiserDto advertiser = new AdvertiserDto();
        CompanyDto company = new CompanyDto();
        company.setId(12345L);
        advertiser.setCompany(company);
        campaign.setAdvertiser(advertiser);

        return creative;
    }

    private Set<Long> makeSet(Long... ids) {
        Set<Long> set = new HashSet<>();
        for (Long id : ids) {
            set.add(id);
        }
        return set;
    }

    @Test
    public void isWithinFixedInterval() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, null, null, oct7_17);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        Instant oct10 = new DateTime(2014, 10, 10, 11, 52, DateTimeZone.UTC).toInstant();
        recencyByAudience.put(10L, oct10);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertTrue(result);
    }

    @Test
    public void isWithinSlidingWindowInterval() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, 10, 5, nullInterval);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        Instant sevenDaysAgo = new DateTime(2014, 10, 17, 11, 52, DateTimeZone.UTC).toInstant();
        recencyByAudience.put(10L, sevenDaysAgo);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertTrue(result);
    }

    @Test
    public void isNullWithinFixedInterval() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, null, null, oct7_17);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        Instant oct10 = new DateTime(2014, 10, 10, 11, 52, DateTimeZone.UTC).toInstant();
        recencyByAudience.put(10L, null);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertFalse(result);
    }

    @Test
    public void isNullWithinSlidingWindowInterval() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, 10, 5, nullInterval);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        recencyByAudience.put(10L, null);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertFalse(result);
    }

    @Test
    public void isNullWithinSlidingWindowIntervalReversedOrder() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, 5, 10, nullInterval);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        recencyByAudience.put(10L, null);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertFalse(result);
    }

    @Test
    public void isWithinRecencyIntervalWhenRecencyNotDefined() {

        DateTime mockedNow = new DateTime(2014, 10, 24, 11, 52, DateTimeZone.UTC);
        DateTimeUtils.setCurrentMillisFixed(mockedNow.getMillis());

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, null, null, nullInterval);
        Map<Long, Instant> recencyByAudience = new HashMap<>();
        Instant sevenDaysAgo = new DateTime(2014, 10, 17, 11, 52, DateTimeZone.UTC).toInstant();
        recencyByAudience.put(10L, sevenDaysAgo);

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertTrue(result);
    }

    @Test
    public void isWithinRecencyIntervalWhenRecencyUnknown() {

        CampaignAudienceDto audienceDto = new CampaignAudienceDto(1L, 10L, AudienceType.DEVICE_ID, true, null, 10, 5, nullInterval);
        Map<Long, Instant> recencyByAudience = Collections.emptyMap();

        boolean result = testObj.isWithinRecencyInterval(audienceDto, recencyByAudience);
        Assert.assertFalse(result);
    }

}
