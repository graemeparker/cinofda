package com.adfonic.util.stats;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.newrelic.api.agent.NewRelic;

/**
 * General purpose manager of simple counters. Various attributes and operations
 * are exposed to JMX in order to provide external access and manipulation of
 * named counters, i.e. for polling and graphing in Graphite or what not.
 */
@ManagedResource
@Component
public class CounterManager {
    private static final transient Logger LOG = Logger.getLogger(CounterManager.class.getName());

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<String, AtomicLong>();

    private final Set<Long> publishers;

    public CounterManager() {
        this.publishers = null;
    }

    public CounterManager(Set<Long> publishers) {
        this.publishers = publishers;
    }

    public void incrementCounter(long publisherId, String name) {
        if (publishers == null || publishers.contains(publisherId)) {
            incrementCounter(name + "." + publisherId);
        }
        incrementCounter(name);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param name
     *            the name of the counter to increment
     */
    public void incrementCounter(String name) {
        incrementCounter(name, 1);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param name
     *            the name of the counter to increment
     * @param delta
     *            the delta by which to increment the counter
     */
    public void incrementCounter(String name, long delta) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Incrementing counter: " + name);
        }
        long value = getOrCreateCounter(name).addAndGet(delta);
        if (value < 0) {
            getOrCreateCounter(name).set(0);
        }

        String newRelicName = "Custom/CounterManager/" + name;
        NewRelic.incrementCounter(newRelicName);
    }

    /**
     * Get the current value of a given counter. The counter will be created and
     * return zero if it didn't already exist. This method is exposed via JMX as
     * an operation that can be used externally to read well-known counter
     * values, i.e. for graphing over time in Graphite or what not.
     * 
     * @param name
     *            the name of the counter
     * @return the current value of the given counter
     */
    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "name", description = "Counter name") })
    public long getCount(String name) {
        return getOrCreateCounter(name).get();
    }

    /**
     * Set the current value of a given counter. The counter will be created and
     * returned with zero if it didn't already exist.
     * 
     * @param name
     *            the name of the counter
     * @param count
     *            value to set for the counter
     */
    public void setCount(String name, long count) {
        getOrCreateCounter(name).set(count);
    }

    /**
     * Reset a given counter to zero.
     * 
     * @param name
     *            the name of the counter to reset
     */
    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "name", description = "Counter name") })
    public void resetCounter(String name) {
        // You could argue that we could get away with simply doing
        // counters.remove(name).
        // That would work, but it would require that we create a new AtomicLong
        // instance
        // the next time we need to use that counter. This way we don't create
        // any
        // garbage, but the penalty paid is that once a counter is used, it
        // exists in
        // the map forever -- even if it never gets incremented after being
        // reset.
        // The whole idea behind this statistics stuff, though, is that these
        // counters
        // are being used pretty much constantly. So let's reuse the same
        // counter.
        getOrCreateCounter(name).set(0);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            increment
     */
    public <E extends Enum<?>> void incrementCounter(E e) {
        incrementCounter(e.name(), 1);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            increment
     * @param delta
     *            the delta by which to increment the counter
     */
    public <E extends Enum<?>> void incrementCounter(E e, long delta) {
        incrementCounter(e.name(), delta);
    }

    /**
     * Reset a given counter to zero.
     * 
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            reset
     */
    public <E extends Enum<?>> void resetCounter(E e) {
        resetCounter(e.name());
    }

    /**
     * Get the current value of a given counter. The counter will be created and
     * return zero if it didn't already exist.
     * 
     * @param e
     *            the enum whose name will be used to determine the counter
     * @return the current value of the given counter
     */
    public <E extends Enum<?>> long getCount(E e) {
        return getCount(e.name());
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param associatedClass
     *            the class associated with the counter, which is used as a
     *            prefix when constructing the counter name
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            increment
     */
    public <E extends Enum<?>> void incrementCounter(Class<?> associatedClass, E e) {
        incrementCounter(associatedClass, e, 1);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist. Will always update both the total AND publisher specific
     * counter
     * 
     * @param associatedClass
     * @param publisherId
     *            for allowing multi-tenancy publisher counters
     * @param e
     */
    public <E extends Enum<?>> void incrementCounter(Class<?> associatedClass, long publisherId, E e) {
        // always update the total
        incrementCounter(associatedClass, publisherId, e, true);
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist. This will only update the publisher specific counter and
     * not the total
     * 
     * @param associatedClass
     * @param publisherId
     *            for allowing multi-tenancy publisher counters
     * @param e
     * @param updateTotal
     *            only update the total values if true
     */
    public <E extends Enum<?>> void incrementCounter(Class<?> associatedClass, long publisherId, E e, boolean updateTotal) {
        if (publishers == null || (publisherId > 0 && publishers.contains(publisherId))) {
            incrementCounter(makeName(associatedClass, e) + "." + publisherId);
        }
        if (updateTotal) {
            incrementCounter(associatedClass, e, 1);
        }
    }

    /**
     * Increment a given counter by one. The counter is created if it didn't
     * already exist.
     * 
     * @param associatedClass
     *            the class associated with the counter, which is used as a
     *            prefix when constructing the counter name
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            increment
     * @param delta
     *            the delta by which to increment the counter
     */
    public <E extends Enum<?>> void incrementCounter(Class<?> associatedClass, E e, long delta) {
        incrementCounter(makeName(associatedClass, e), delta);
    }

    /**
     * Reset a given counter to zero.
     * 
     * @param associatedClass
     *            the class associated with the counter, which is used as a
     *            prefix when constructing the counter name
     * @param e
     *            the enum whose name will be used to determine the counter to
     *            reset
     */
    public <E extends Enum<?>> void resetCounter(Class<?> associatedClass, E e) {
        resetCounter(makeName(associatedClass, e));
    }

    /**
     * Get the current value of a given counter. The counter will be created and
     * return zero if it didn't already exist.
     * 
     * @param associatedClass
     *            the class associated with the counter, which is used as a
     *            prefix when constructing the counter name
     * @param e
     *            the enum whose name will be used to determine the counter
     * @return the current value of the given counter
     */
    public <E extends Enum<?>> long getCount(Class<?> associatedClass, E e) {
        return getCount(makeName(associatedClass, e));
    }

    /**
     * Get the set of counter names currently being managed
     * 
     * @returns a set of counter names
     */
    @ManagedAttribute
    public Set<String> getCounterNames() {
        return new TreeSet<String>(counters.keySet());
    }

    /**
     * Get the set of counter names currently being managed
     * 
     * @returns a set of counter names
     */
    @ManagedAttribute
    public Map<String, AtomicLong> getCounterValues() {
        return new TreeMap<String, AtomicLong>(counters);
    }

    /**
     * Construct a fully qualified counter name from a referencing class and
     * enum. i.e.
     * com.adfonic.adserver.controller.AbstractAdController.AD_SPACE_INVALID
     */
    private static <E extends Enum<?>> String makeName(Class<?> clazz, E e) {
        return clazz.getName() + "." + e.name();
    }

    /**
     * Get an existing counter by name, or create it if it doesn't exist yet.
     */
    private AtomicLong getOrCreateCounter(String name) {
        AtomicLong counter = counters.get(name);
        if (counter == null) {
            counter = new AtomicLong();
            AtomicLong gotThereFirst = ((ConcurrentHashMap<String, AtomicLong>) counters).putIfAbsent(name, counter);
            if (gotThereFirst != null) {
                return gotThereFirst;
            }
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Created counter: " + name);
            }
        }
        return counter;
    }

    /**
     * Package access only to give access to CounterJmxmanager
     * 
     * @return
     */
    Map<String, AtomicLong> getCounters() {
        return counters;
    }

}
