package com.adfonic.adserver.controller.fish;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.adserver.controller.rtb.RtbExecutionContext;

/**
 * 
 * @author mvanek
 *
 */
public class RtbFisherman {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FishingSession session;

    private boolean active;

    public void process(RtbExecutionContext<?, ?> execution) {
        if (active && session != null) {
            try {
                boolean enough = session.process(execution);
                if (enough) {
                    active = false;
                    session.setCompleted();
                }
            } catch (Exception x) {
                active = false;
                session.setCompleted();
                logger.error("Fishing session failed", x);

            }
        }
    }

    public void stopSession() {
        if (this.active) {
            this.active = false;
            if (session != null) {
                session.setCompleted();
            }
        }
    }

    public FishingSession getSession() {
        return session;
    }

    /**
     * handbrake...
     */
    public void reset() {
        if (session != null) {
            session.setCompleted();
        }
        active = false;
        session = null;

    }

    public FishingSession startSession(RtbFishnetConfig config) {
        FishingSession session = new FishingSession(config);
        setSession(session);
        return session;
    }

    public void setSession(FishingSession session) {
        Objects.requireNonNull(session);
        if (this.active) {
            throw new IllegalStateException("FishingSession is already active: " + this.session);
        }
        session.setStarted();
        this.session = session;
        this.active = true;
    }

    public static class FishingSession {

        private final Date createdAt = new Date();

        private final RtbFishnetConfig config;

        private Date startedAt;

        private Date stopedAt;

        private long timeoutAtMillis;

        private final ConcurrentLinkedQueue<RtbExecutionContext<?, ?>> catches = new ConcurrentLinkedQueue<RtbExecutionContext<?, ?>>();

        private final AtomicInteger matchCount = new AtomicInteger();
        private final AtomicInteger missCount = new AtomicInteger();

        public FishingSession(RtbFishnetConfig config) {
            Objects.requireNonNull(config);
            this.config = config;
        }

        public void setStarted() {
            this.startedAt = new Date();
            this.timeoutAtMillis = startedAt.getTime() + (config.getTimeLimit() * 1000);
        }

        public void setCompleted() {
            this.stopedAt = new Date();
        }

        public boolean process(RtbExecutionContext<?, ?> execution) {
            boolean match = config.match(execution);
            if (match) {
                catches.add(execution);
                matchCount.incrementAndGet();
            } else {
                missCount.incrementAndGet();
            }
            return matchCount.intValue() >= config.getCatchLimit() || System.currentTimeMillis() >= timeoutAtMillis;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public Date getStartedAt() {
            return startedAt;
        }

        public Date getStopedAt() {
            return stopedAt;
        }

        public RtbFishnetConfig getConfig() {
            return config;
        }

        public int getMissCount() {
            return missCount.intValue();
        }

        public int getMatchCount() {
            return matchCount.intValue();
        }

        public List<RtbExecutionContext<?, ?>> getCatches() {
            return new ArrayList<RtbExecutionContext<?, ?>>(catches);
        }

    }

}
