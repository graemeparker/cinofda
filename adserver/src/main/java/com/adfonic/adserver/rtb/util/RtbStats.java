package com.adfonic.adserver.rtb.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.codahale.metrics.Meter;

public class RtbStats {

    private static final RtbStats instance = new RtbStats();

    public static RtbStats i() {
        return instance;
    }

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, StatsEntry>> creatives = new ConcurrentHashMap<Long, ConcurrentHashMap<Long, StatsEntry>>();

    private StatsTargetingEventListener targetingListener;

    private long targetingTimeoutTimestamp;

    public StatsTargetingEventListener getTargetingListener() {
        if (System.currentTimeMillis() < targetingTimeoutTimestamp) {
            return targetingListener;
        } else {
            return null;
        }
    }

    public void setTargetingGathering(int durationSecs, long creativeId, Long adSpaceId) {
        targetingListener = new StatsTargetingEventListener(creativeId, adSpaceId, durationSecs);
        targetingTimeoutTimestamp = System.currentTimeMillis() + (1000l * durationSecs);
    }

    public void bid(Long creativeId, Long adspaceId, BigDecimal price) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        entry.rtbBids.mark();
    }

    public void bidError(Long creativeId, Long adspaceId, Exception x) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        entry.rtbBidErrors.mark();
    }

    public void win(Long creativeId, Long adspaceId, BigDecimal price) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        entry.rtbWins.mark();
    }

    public void impression(Long creativeId, Long adspaceId) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        entry.impressions.mark();
    }

    public void click(Long creativeId, Long adspaceId) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        entry.clicks.mark();
    }

    public void loss(Long creativeId, Long adspaceId, String reason) {
        if (!AdServerFeatureFlag.RTB_STATS.isEnabled()) {
            return;
        }
        StatsEntry entry = getEntry(creativeId, adspaceId);
        Meter counter = entry.rtbLoses.get(reason);
        if (counter == null) {
            counter = new Meter();
            entry.rtbLoses.put(reason, counter);
        }
        counter.mark();
    }

    public Map<Long, ConcurrentHashMap<Long, StatsEntry>> get() {
        return creatives;
    }

    public Map<Long, StatsEntry> get(Long creativeId) {
        return creatives.get(creativeId);
    }

    public StatsEntry get(Long creativeId, Long adspaceId) {
        ConcurrentHashMap<Long, StatsEntry> map = creatives.get(creativeId);
        if (map != null) {
            return map.get(adspaceId);
        } else {
            return null;
        }
    }

    public void reset() {
        for (ConcurrentHashMap<Long, StatsEntry> map : creatives.values()) {
            for (StatsEntry entry : map.values()) {
                entry.reset();
            }
        }
    }

    public void reset(Long creativeId) {
        Map<Long, StatsEntry> map = get(creativeId);
        if (map != null) {
            for (StatsEntry entry : map.values()) {
                entry.reset();
            }
        }
    }

    private final StatsEntry getEntry(Long creativeId, Long adspaceId) {
        ConcurrentHashMap<Long, StatsEntry> adspaces = creatives.get(creativeId);
        StatsEntry entry;
        if (adspaces == null) {
            adspaces = new ConcurrentHashMap<Long, StatsEntry>();
            creatives.put(creativeId, adspaces);
            entry = new StatsEntry();
            adspaces.put(adspaceId, entry);
        } else {
            entry = adspaces.get(adspaceId);
            if (entry == null) {
                entry = new StatsEntry();
                adspaces.put(adspaceId, entry);
            }
        }
        return entry;
    }

    public static class StatsEntry {

        private final Meter rtbBids = new Meter();
        private final Meter rtbBidErrors = new Meter();
        private final Meter rtbWins = new Meter();
        private final Meter rtbWinErrors = new Meter();
        private final Meter impressions = new Meter();
        private final Meter impressionErrors = new Meter();
        private final Meter clicks = new Meter();
        private final Meter clicksErrors = new Meter();
        private final ConcurrentHashMap<String, Meter> rtbLoses = new ConcurrentHashMap<String, Meter>();
        private final ConcurrentHashMap<CreativeEliminatedReason, Meter> eliminations = new ConcurrentHashMap<CreativeEliminatedReason, Meter>();

        public void reset() {
            //this.started = new Date();
            //rtbBids.set(0);
            //rtbWins.set(0);
            //rtbLoses.clear();
        }

        public Meter getRtbBids() {
            return rtbBids;
        }

        public Meter getRtbWins() {
            return rtbWins;
        }

        public Map<String, Meter> getRtbLoses() {
            return rtbLoses;
        }

        public Meter getImpressions() {
            return impressions;
        }

        public Meter getClicks() {
            return clicks;
        }

        public Map<CreativeEliminatedReason, Meter> getEliminations() {
            return eliminations;
        }
    }

    public class StatsTargetingEventListener implements TargetingEventListener {

        private final Long creativeId;

        private final Long adSpaceId;

        private final long startedAt;

        private final int durationSecs;

        public StatsTargetingEventListener(Long creativeId, Long adSpaceId, int durationSecs) {
            Objects.requireNonNull(creativeId);
            this.creativeId = creativeId;
            // AdSpace id can be null
            this.adSpaceId = adSpaceId;
            this.durationSecs = durationSecs;
            this.startedAt = System.currentTimeMillis();
        }

        public Long getCreativeId() {
            return creativeId;
        }

        public Long getAdSpaceId() {
            return adSpaceId;
        }

        public long getStartedAt() {
            return startedAt;
        }

        public int getDurationSecs() {
            return durationSecs;
        }

        @Override
        public void attributesDerived(AdSpaceDto adSpace, TargetingContext context) {
            // does not care
        }

        @Override
        public void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives) {
            // does not care
        }

        @Override
        public void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason) {
            if (this.creativeId.equals(creative.getId())) {
                if (this.adSpaceId == null || this.adSpaceId.equals(adSpace.getId())) {
                    StatsEntry statsEntry = RtbStats.this.getEntry(creativeId, adSpace.getId());
                    Meter meter = statsEntry.eliminations.get(reason);
                    if (meter == null) {
                        meter = new Meter();
                        statsEntry.eliminations.put(reason, meter);
                    }
                    meter.mark();
                }
            }
        }

        @Override
        public void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives) {
            // does not care
        }

        @Override
        public void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative) {
            // does not care
        }

        @Override
        public void unfilledRequest(AdSpaceDto adSpace, TargetingContext context) {
            // does not care
        }

        @Override
        public void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit) {
            // does not care
        }

    }

}
