package com.adfonic.adserver.impl;

import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.deriver.impl.LocationAudienceRedisDeriver;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto.AudienceType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.retargeting.redis.GeoAudienceRedisReader;
import com.adfonic.util.stats.CounterManager;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class GeoRedisTargetingTest {

    @Mock
    DataCacheProperties dcProperties;

    DeriverManager deriverManager = new DeriverManager();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore
    @Test
    public void checkGeoRedisLatLon() {

        //Given 
        final double lat = 1.234;
        final double lon = 5.678;
        final Long audienceId = new Random().nextLong();

        GeoAudienceRedisReader locationReader = Mockito.mock(GeoAudienceRedisReader.class);
        Mockito.when(locationReader.getAudiences(lat, lon)).thenReturn(Sets.newHashSet(audienceId));
        CounterManager counterManager = new CounterManager();
        new LocationAudienceRedisDeriver(deriverManager, locationReader, counterManager); // Deriver automatically registers itself

        DeviceLocationTargetingChecks checks = new DeviceLocationTargetingChecks(counterManager, dcProperties);

        TargetingContextImpl contextOk = contextWithCoordiantes(lat, lon);
        CreativeDto creativeOk = creativeWithAudience(audienceId);

        SegmentDto segment = new SegmentDto();

        // When
        CreativeEliminatedReason eliminated = checks.checkLatLonTargeting(contextOk, null, creativeOk, segment, null);
        // Then
        Assertions.assertThat(eliminated).isNull();
        Assertions.assertThat(counterManager.getCount("GeoRedisCallCount")).isEqualTo(1);
        Assertions.assertThat(counterManager.getCount("GeoRedisCallErrorCount")).isEqualTo(0);

        // But - with different audienceId
        CreativeDto creativeKo = creativeWithAudience(audienceId - 1);
        // When
        CreativeEliminatedReason eliminatedAudienceX = checks.checkLatLonTargeting(contextOk, null, creativeKo, segment, null);
        // Then
        Assertions.assertThat(eliminatedAudienceX).isEqualTo(CreativeEliminatedReason.NotInGeolocationArea);

        // But - with different coordinates
        TargetingContextImpl contextKo = contextWithCoordiantes(4.321, 8.765);
        // When
        CreativeEliminatedReason eliminatedCoordinatesX = checks.checkLatLonTargeting(contextKo, null, creativeOk, segment, null);
        // Then
        Assertions.assertThat(eliminatedCoordinatesX).isEqualTo(CreativeEliminatedReason.NotInGeolocationArea);
    }

    private TargetingContextImpl contextWithCoordiantes(double latitude, double longitude) {
        TargetingContextImpl context = new TargetingContextImpl(null, null, deriverManager, null);
        context.setAttribute(TargetingContext.COORDINATES, new SimpleCoordinates(latitude, longitude));
        return context;
    }

    private CreativeDto creativeWithAudience(long audienceId) {
        CampaignDto campaign = new CampaignDto();
        campaign.getLocationAudiences().add(new CampaignAudienceDto(0, audienceId, AudienceType.LOCATION, true, null, null, null, null));
        CreativeDto creative = new CreativeDto();
        creative.setCampaign(campaign);
        return creative;
    }
}
