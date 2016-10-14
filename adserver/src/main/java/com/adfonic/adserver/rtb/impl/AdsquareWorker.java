package com.adfonic.adserver.rtb.impl;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.http.TrackerNoOpClient;
import com.adfonic.util.DaemonThreadFactory;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.stats.FreqLogr;
import com.byyd.adsquare.v2.AdsqrEnrichQueryRequest;
import com.byyd.adsquare.v2.AmpApiClient;
import com.byyd.adsquare.v2.AmpApiClient.TrackType;
import com.byyd.adsquare.v2.AmpConfiguredClient;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * 
 * @author mvanek
 *
 */
@Component
public class AdsquareWorker {

    /**
     * Adsqaure SSPs as returned from amp /api/v1/enrichmentMeta/ssps endpoint
     * 
     * We need to keep mapping from our exchange publisher ids into adsquare ssp ids
     * It is configuration thing changes are NOT expected. Only when new exchange integration happends, than new mapping must be added. 
     * Possibly ADSQR_SSP_ID column could be added into RTB_CONFIG table instead of this...   
     */
    public static enum AdsquareSsp {

        Smaato(100), MoPub(101), Google_AdX(102), Nexage(103), Rubicon(104), OpenX(105), Pubmatic(106),
        //107 Improve Digital
        //108 Yahoo
        MobFox(109),
        //110 Smart AdServer
        //111 PubNative
        //112 Inneractive
        //113 Fyber
        //114 LiveRail
        //115 SpotXchange
        //116 Tremor
        //117 Facebook Exchange
        Yieldlab(118),
        //119 AOL
        //120 Microsoft Exchange
        //121 Brightroll
        AppNexus(122), //
        Omax(123);//Opera Mediaworks
        //124 Unruly
        //125 Smartclip

        private final int sspId;

        private AdsquareSsp(int sspId) {
            this.sspId = sspId;
        }

        public int getSspId() {
            return sspId;
        }
    }

    private static final Map<Long, Integer> publisher2ssp = new HashMap<Long, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            put(RtbExchange.Smaato.getPublisherId(), AdsquareSsp.Smaato.getSspId());
            put(RtbExchange.Mopub.getPublisherId(), AdsquareSsp.MoPub.getSspId());
            put(RtbExchange.AdX.getPublisherId(), AdsquareSsp.Google_AdX.getSspId());
            put(RtbExchange.Nexage.getPublisherId(), AdsquareSsp.Nexage.getSspId());
            put(RtbExchange.Rubicon.getPublisherId(), AdsquareSsp.Rubicon.getSspId());
            put(RtbExchange.OpenX.getPublisherId(), AdsquareSsp.OpenX.getSspId());
            put(RtbExchange.Pubmatic.getPublisherId(), AdsquareSsp.Pubmatic.getSspId());
            put(RtbExchange.YieldLab.getPublisherId(), AdsquareSsp.Yieldlab.getSspId());
            put(RtbExchange.Appnexus.getPublisherId(), AdsquareSsp.AppNexus.getSspId());
            put(RtbExchange.MobFox.getPublisherId(), AdsquareSsp.MobFox.getSspId());
            put(RtbExchange.Omax.getPublisherId(), AdsquareSsp.Omax.getSspId());
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CassandraTemplate template;

    private final TrackerNoOpClient trackerClient;

    private final Session session;

    private final PreparedStatement psInsert;
    private final PreparedStatement psSelect;

    private final AmpConfiguredClient ampClient;

    @Autowired
    private CounterManager counterManager;

    @Autowired
    private DynamicProperties dynaProps;

    private ThreadPoolExecutor reportingExecutor;

    @Autowired
    public AdsquareWorker(CassandraTemplate template, AmpConfiguredClient ampClient, TrackerNoOpClient trackerClient) {
        this.template = template;
        this.ampClient = ampClient;
        this.trackerClient = trackerClient;
        this.session = template.getSession();
        psInsert = session
                .prepare("INSERT INTO adsquare_open_bid (impressionId, audienceId, latitude, longitude, deviceId, sspId, appId) VALUES (?, ? , ?, ?, ?, ?, ?) USING TTL ?");
        psInsert.setConsistencyLevel(ConsistencyLevel.ANY);

        psSelect = session.prepare("SELECT * FROM adsquare_open_bid WHERE impressionId = ?");
        psSelect.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        DaemonThreadFactory threadFactory = new DaemonThreadFactory("AdsquareAmp-");
        // Existing throughput is few calls per minute so very few threads is needed 
        reportingExecutor = new ThreadPoolExecutor(1, 3, 6L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    /**
     * Only for monitoring. No funny stuff with internals!
     */
    public ThreadPoolExecutor getReportingExecutor() {
        return reportingExecutor;
    }

    @PreDestroy
    public void stop() {
        shutdownAndAwaitTermination(reportingExecutor, 6);
    }

    public static Integer getSspId(Long publisherId) {
        return publisher2ssp.get(publisherId);
    }

    public static Integer getSspIdForExchange(AdSpaceDto adspace) {
        Long publisherId = adspace.getPublication().getPublisher().getId();
        return publisher2ssp.get(publisherId);
    }

    /**
     * Do not bother to query adsqaure or cassandra if country is not whitelisted for adsquare
     */
    public boolean isCountryWhitelisted(CountryDto country) {
        String countryIso = country != null ? country.getIsoCode() : "";
        return dynaProps.getPropertyAsSet(DcProperty.AdsquareCountries, Collections.emptySet()).contains(countryIso);
    }

    public void store(String impressionId, Integer adsqrAudienceId, AdsqrEnrichQueryRequest adsqrQueryRequest, long ttlSeconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("storing impressionId: " + impressionId + ", adsqrAudienceId: " + adsqrAudienceId + ", adsqrQueryRequest: " + adsqrQueryRequest);
        }
        String deviceId = adsqrQueryRequest.getDeviceIdRaw();
        if (deviceId == null) {
            deviceId = adsqrQueryRequest.getDeviceIdSha1();
            if (deviceId == null) {
                deviceId = adsqrQueryRequest.getDeviceIdMd5();
            }
        }
        counterManager.incrementCounter(AsCounter.AdsquareCassandraInsertCall);
        try {
            BoundStatement statement = psInsert.bind(UUID.fromString(impressionId), adsqrAudienceId, adsqrQueryRequest.getLatitude(), adsqrQueryRequest.getLongitude(), deviceId,
                    adsqrQueryRequest.getSspId(), adsqrQueryRequest.getAppId(), (int) ttlSeconds);
            session.executeAsync(statement).get();
        } catch (Exception x) {
            counterManager.incrementCounter(AsCounter.AdsquareCassandraInsertError);
            FreqLogr.report(x, "Adsquare Cassandra Insert Error");
        }
    }

    public void reportImpression(String impressionId) {
        reportToAmpApi(impressionId, AmpApiClient.TrackType.IMPRESSION);
    }

    public void reportClick(String impressionId) {
        reportToAmpApi(impressionId, AmpApiClient.TrackType.CLICK);
    }

    private void reportToAmpApi(String impressionId, AmpApiClient.TrackType type) {
        if (logger.isDebugEnabled()) {
            logger.debug("reportToAmpApi: " + impressionId);
        }
        counterManager.incrementCounter(AsCounter.AdsquareCassandraSelectCall);
        Row row = null;
        try {
            ResultSetFuture resultSet = session.executeAsync(psSelect.bind(UUID.fromString(impressionId)));
            row = resultSet.get().one();
        } catch (Exception x) {
            FreqLogr.report(x, "Adsquare Cassandra Select Error");
            counterManager.incrementCounter(AsCounter.AdsquareCassandraSelectError);
            return;
        }

        if (row != null) {
            doAsyncApiCall(type, row);
        } else {
            logger.debug("Adsquare tracking record not found: " + impressionId);
        }
    }

    /**
     * Adsquare reporting endpoint is in Europe and latencies from USA shard are too big to 
     * execute reporting calls during beacon invocation synchronously. 
     * We need timeout about 1 second so we will do http api call asynchronously.
     * Existing throughput is about few calls per minute...
     */
    private void doAsyncApiCall(AmpApiClient.TrackType type, Row row) {

        reportingExecutor.submit(new Runnable() {

            @Override
            public void run() {
                Integer audienceId = row.getInt("audienceId");
                String appId = row.getString("appId");
                Double latitude = row.getDouble("latitude");
                Double longitude = row.getDouble("longitude");
                String deviceId = row.getString("deviceId");
                Date actionTime = new Date(); // simply now
                Integer sspId = row.getInt("sspId");

                if (type == TrackType.IMPRESSION) {
                    long startMs = tracker("/ir", audienceId, null, null);
                    counterManager.incrementCounter(AsCounter.AdsquareImpressionTrackCall);
                    try {
                        ampClient.trackImpression(audienceId, appId, latitude, longitude, deviceId, actionTime, sspId);
                        tracker("/ic", audienceId, startMs, null);
                    } catch (Exception x) {
                        FreqLogr.report(x);
                        counterManager.incrementCounter(AsCounter.AdsquareImpressionTrackError);
                        tracker("/if", audienceId, startMs, String.valueOf(x));
                    }
                } else if (type == TrackType.CLICK) {
                    long startMs = tracker("/cr", audienceId, null, null);
                    counterManager.incrementCounter(AsCounter.AdsquareClickTrackCall);
                    try {
                        ampClient.trackClick(audienceId, appId, latitude, longitude, deviceId, actionTime, sspId);
                        tracker("/cc", audienceId, startMs, null);
                    } catch (Exception x) {
                        FreqLogr.report(x);
                        counterManager.incrementCounter(AsCounter.AdsquareClickTrackError);
                        tracker("/cf", audienceId, startMs, String.valueOf(x));
                    }
                } else {
                    throw new IllegalStateException("Unsupported type: " + type);
                }

            }
        });
    }

    /**
     * Because we have discrepancies...
     * This is to trace all apm calls and failures into byyd tracker access log
     */
    private long tracker(String path, Integer audienceId, Long startMs, String error) {
        if ("true".equals(dynaProps.getProperty(DcProperty.TrackAdsquareApm))) {
            try {
                StringBuilder sb = new StringBuilder(path).append("?auid=").append(audienceId);
                if (startMs != null) {
                    sb.append("&tms=").append(System.currentTimeMillis() - startMs.longValue());
                }
                if (error != null) {
                    sb.append("&error=").append(URLEncoder.encode(error, "utf-8"));
                }
                trackerClient.track(sb.toString());
            } catch (Exception x) {
                FreqLogr.report(x);
                counterManager.incrementCounter(AsCounter.AdsqaureTrackerError);
            }
            return System.currentTimeMillis();
        } else {
            return 0l;
        }

    }

    void shutdownAndAwaitTermination(ExecutorService pool, int timeoutSeconds) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                logger.warn("Adsquare notification pool did not completed in " + timeoutSeconds + " seconds");
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS))
                    logger.error("Adsquare notification pool did not shutdown in " + timeoutSeconds + " seconds");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
