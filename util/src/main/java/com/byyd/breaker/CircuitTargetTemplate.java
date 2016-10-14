package com.byyd.breaker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mvanek
 *
 */
public class CircuitTargetTemplate<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Similar to Java 8 {@link java.util.function.Function}
     */
    public static interface TargetResource<T, R> {

        public R call(T target) throws Exception;
    }

    private final List<TargetBreaker<T>> targetBreakers;
    private final int targetCount;
    private final AtomicInteger pointer = new AtomicInteger();
    private final boolean targetSticky;
    private final boolean failFast;

    public CircuitTargetTemplate(List<T> targets, int failThreshold, int failLockdownMs) {
        this(targets, failThreshold, failLockdownMs, false, true);
    }

    public CircuitTargetTemplate(List<T> targets, int failThreshold, int failLockdownMs, boolean stickyTarget, boolean failFast) {
        this.targetSticky = stickyTarget;
        this.failFast = failFast;
        this.targetCount = targets.size();
        this.targetBreakers = new ArrayList<TargetBreaker<T>>(targets.size());
        for (int i = 0; i < targets.size(); ++i) {
            ManagedBreaker cbreaker = new CircuitBreaker.AtomicCircuitBreaker(failThreshold, failLockdownMs);
            this.targetBreakers.add(new TargetBreaker<T>(targets.get(i), cbreaker));
        }
    }

    /**
     * For monitoring. Please do not mess it up. 
     */
    public List<TargetBreaker<T>> getTargetBreakers() {
        return targetBreakers;
    }

    /**
     * For monitoring. Please do not mess it up.
     */
    public AtomicInteger getPointer() {
        return pointer;
    }

    public boolean isTargetSticky() {
        return targetSticky;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public <R> R execute(TargetResource<T, R> resource) {
        List<String> errors = new LinkedList<String>();
        pointer.compareAndSet(Integer.MAX_VALUE, 0); // Say NO to overflow into negative value

        // Use local index as other threads are incrementing "pointer" concurrently
        int index = (targetSticky ? pointer.get() : pointer.incrementAndGet());
        int attempts = 0;
        Exception exception = null;
        do {
            TargetBreaker<T> targetData = targetBreakers.get(index % targetCount);
            if (logger.isDebugEnabled()) {
                logger.debug("Calling " + targetData.target + ", " + resource);
            }
            CircuitResult<R> result = targetData.breaker.execute(new java.util.concurrent.Callable<R>() {

                @Override
                public R call() throws Exception {
                    return resource.call(targetData.target);
                }
            });
            if (result.isPrimary()) {
                return result.getValue(); // Look! Rainbow! Happy! Happy!
            } else {
                exception = result.getException();
                if (exception != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed " + targetData.target + ", " + resource, exception);
                    }
                    if (failFast) {
                        throw new CircuitTargetException(resource, targetData.target, "Failed target: " + targetData.target + ", resource: " + resource + ", exception: "
                                + exception, exception);
                    } else {
                        errors.add(String.valueOf(exception.getMessage()));
                    }
                } else {
                    // fallback (empty) value because primary is marked down
                    errors.add(targetData.target + " broken");
                }
                ++attempts;
                ++index;
            }
        } while (attempts != targetCount);
        // We tried every target and nothing...
        throw new CircuitException(resource, "No target available: " + errors, exception);

    }

    public static class TargetBreaker<T> {

        private final T target;
        private final ManagedBreaker breaker;

        public TargetBreaker(T target, ManagedBreaker breaker) {
            this.target = target;
            this.breaker = breaker;
        }

        public T getTarget() {
            return target;
        }

        public ManagedBreaker getBreaker() {
            return breaker;
        }

    }
}
