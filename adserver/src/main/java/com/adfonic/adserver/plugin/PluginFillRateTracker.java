package com.adfonic.adserver.plugin;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

/**
 * Plugin fill rate tracker.  This object is intended to sit on "standby" and
 * not impact adserver function or performance in any way unless explicitly
 * enabled.  Once enabled, this object starts collecting filled/unfilled stats
 * for plugins in an internal frequency tracker, which it makes accessible to
 * anybody who wants to inquire about fill rate for one or more plugins.  It
 * can be enabled/disabled on the fly, i.e. to take a small "sample" of the
 * current behavior, which helps minimize the performance penalty for using
 * this.  The expected scenario is: at some point this object will be manually
 * enabled; it collects stats for some period of time, then is manually
 * disabled and stats are reviewed.  Stats may also be reviewed along the way.
 */
@Component
public class PluginFillRateTracker {
    private static final transient Logger LOG = Logger.getLogger(PluginFillRateTracker.class.getName());
    private final Map<String, PluginStats> pluginStatsByPluginName = new HashMap<String, PluginStats>();
    private volatile boolean enabled = false;
    private volatile Date dateStarted = null;
    private volatile Date dateStopped = null;

    /** The distinct mutually exclusive "outcomes" of a plugin request */
    public enum Outcome { FILLED, UNFILLED, TIMEOUT }

    /** Start tracking and clear all previous stats */
    public synchronized void start() {
        if (enabled) {
            LOG.warning("Already started");
        } else {
            LOG.info("Starting");
            synchronized (pluginStatsByPluginName) {
                pluginStatsByPluginName.clear();
            }
            dateStopped = null;
            dateStarted = new Date();
            enabled = true;
        }
    }

    /** Stop tracking */
    public synchronized void stop() {
        if (enabled) {
            LOG.info("Stopping");
            enabled = false;
            dateStopped = new Date();
        } else {
            LOG.warning("Already stopped");
        }
    }

    /** @return true if this object has been started and is tracking */
    public boolean isEnabled() {
        return enabled;
    }

    /** @return the time at which this tracker was last started, or null if it was never started */
    public Date getDateStarted() {
        return dateStarted;
    }

    /** @return the time at which this tracker was last stopped, or null if it was either never started or still running */
    public Date getDateStopped() {
        return dateStopped;
    }

    /**
     * Track the outcome of a particular plugin request
     * @param pluginName the name of the plugin
     * @param outcome the outcome of the request
     */
    public void trackOutcome(String pluginName, Outcome outcome) {
        if (!enabled) {
            return;
        }
        getPluginStats(pluginName).incrementAndGetCount(outcome);
    }

    /**
     * Get the total number of times a plugin has been tracked, irrespective of outcome
     * @param pluginName the name of the plugin
     * @return the total number of times the plugin has been tracked
     */
    public long getTotalCount(String pluginName) {
        return getPluginStats(pluginName).getTotalCount();
    }

    /**
     * Get the total number of times a plugin has been tracked for a given outcome
     * @param pluginName the name of the plugin
     * @param outcome the outcome
     * @return the total number of times the plugin has been tracked for the given outcome
     */
    public long getCount(String pluginName, Outcome outcome) {
        return getPluginStats(pluginName).getCount(outcome);
    }

    /**
     * Get stats for a particular plugin by name.
     * NOTE: the counters in the returned object are live and will continue to increment.
     * @param pluginName the name of the plugin
     * @return a PluginStats object for the given plugin
     */
    public PluginStats getPluginStats(String pluginName) {
        PluginStats pluginStats;
        if ((pluginStats = pluginStatsByPluginName.get(pluginName)) == null) {
            synchronized (pluginStatsByPluginName) {
                if ((pluginStats = pluginStatsByPluginName.get(pluginName)) == null) {
                    pluginStats = new PluginStats(pluginName);
                    pluginStatsByPluginName.put(pluginName, pluginStats);
                }
            }
        }
        return pluginStats;
    }

    /**
     * Get a map of plugin names to respective PluginStats objects.
     * The keys are sorted by plugin name.
     * NOTE: the map returned is a snapshot copy and will not be modified by this class.
     * @return a map of plugin names to PluginStats objects
     */
    public SortedMap<String, PluginStats> getPluginStatsByPluginName() {
        synchronized (pluginStatsByPluginName) {
            return new TreeMap<String, PluginStats>(pluginStatsByPluginName);
        }
    }

    /** Stats for a given plugin */
    public static final class PluginStats {
        private String pluginName;
        private final AtomicLong totalCount = new AtomicLong();
        private Map<Outcome, AtomicLong> countsByOutcome = new LinkedHashMap<Outcome, AtomicLong>();

        private PluginStats(String pluginName) {
            this.pluginName = pluginName;
            // Pre-populate the counter map with all zeros.  This way we
            // never have to synchronize on the map, we just get the counter
            // and increment away (since each counter is atomic).
            for (Outcome outcome : Outcome.values()) {
                countsByOutcome.put(outcome, new AtomicLong(0));
            }
        }

        public String getPluginName() {
            return pluginName;
        }

        public long getTotalCount() {
            return totalCount.get();
        }

        public long getCount(Outcome outcome) {
            return countsByOutcome.get(outcome).get();
        }

        public long incrementAndGetCount(Outcome outcome) {
            totalCount.incrementAndGet();
            return countsByOutcome.get(outcome).incrementAndGet();
        }

        public double getPercent(Outcome outcome) {
            return (double)getCount(outcome) / (double)totalCount.get();
        }

        public Map<Outcome, AtomicLong> getCountsByOutcome() {
            return Collections.unmodifiableMap(countsByOutcome);
        }
    }
}
