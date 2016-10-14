package com.byyd.breaker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.byyd.breaker.FallbackCallback.ValueFallbackResource;

/**
 * 
 * @author martin.vanek
 *
 */
public interface CircuitBreaker {

    /**
     * Check before performing guarded operation
     */
    public boolean check();

    /**
     * callback on primary resource success
     */
    public void success();

    /**
     * callback on primary resource exception
     */
    public void error(Exception x);

    /**
     * Fallback is CircuitResult.NULL_FALLBACK returning null value
     */
    public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> callable);

    /**
     * Fallback is passed directly as value. It can be null.
     */
    public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> primary, final R fallbackValue);

    /**
     * @return
     */
    public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> primary, FallbackCallback<R> fallback);

    /**
     * Base implementation with ManagedBreaker
     */
    public static abstract class BaseCircuitBreaker implements ManagedBreaker {

        protected final Logger logger = LoggerFactory.getLogger(getClass());

        protected final long lockdownMillis;

        protected final int failureThreshold;

        protected boolean inactive = false; //allow everything

        protected boolean blockade = false; //allow nothing

        public BaseCircuitBreaker(int failureThreshold, int lockdownMillis) {
            this.failureThreshold = failureThreshold;
            this.lockdownMillis = lockdownMillis;
        }

        @Override
        public void setInactive(boolean inactive) {
            this.inactive = inactive;
        }

        @Override
        public boolean isInactive() {
            return inactive;
        }

        @Override
        public void setBlockade(boolean blockade) {
            this.blockade = blockade;
        }

        @Override
        public boolean isBlockade() {
            return blockade;
        }

        public int getFailureThreshold() {
            return failureThreshold;
        }

        @Override
        public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> resource) {
            return execute(resource, FallbackCallback.NULL_FALLBACK);
        }

        @Override
        public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> resource, final R fallback) {
            if (fallback != null) {
                return execute(resource, new ValueFallbackResource<R>(fallback));
            } else {
                return execute(resource, FallbackCallback.NULL_FALLBACK);
            }
        }

        /**
         * 
         */
        @Override
        public <R> CircuitResult<R> execute(java.util.concurrent.Callable<R> primary, FallbackCallback<R> fallback) {
            if (primary == null) {
                throw new IllegalArgumentException("Primary resource is null");
            }
            if (fallback == null) {
                throw new IllegalArgumentException("Fallback resource is null");
            }
            if (check()) {
                logger.debug("Primary execution allowed");
                try {
                    R result = primary.call();
                    logger.debug("Primary execution succeded");
                    success();
                    return new CircuitResult<R>(result, true, null);
                } catch (Exception x) {
                    error(x);
                    Logger logger = LoggerFactory.getLogger(primary.getClass()); // use own logger?
                    if (logger.isDebugEnabled()) {
                        logger.debug("Primary execution failed", x);
                    }// else if (logger.isInfoEnabled()) {
                     //   logger.info("Primary execution failed: " + x);
                     //}
                    logger.debug("Fallback failure execution return");
                    return new CircuitResult<R>(fallback.call(x), false, x);
                }
            } else {
                logger.debug("Fallback disallow execution return");
                // Do not create unecessary CircuitResult instances
                if (fallback == FallbackCallback.NULL_FALLBACK) {
                    return CircuitResult.NULL_RESULT;
                }
                R value = fallback.call();
                if (value == null) {
                    return CircuitResult.NULL_RESULT;
                } else {
                    return new CircuitResult<R>(value, false, null);
                }
            }
        }

    }

    /**
     * This breaker intentionaly lacks any synchronization
     * 
     * It may allow slight deviations of configured thresholds, but it has minimal overhead
     * 
     */
    public static class SimpleCircuitBreaker extends BaseCircuitBreaker {

        private volatile boolean broken; // unsafe

        private volatile int failuresCount; // unsafe

        private volatile long lastBreakOrProbeMillis; // unsafe

        public SimpleCircuitBreaker(int failureThreshold, int lockdownMillis) {
            super(failureThreshold, lockdownMillis);
        }

        @Override
        public boolean check() {
            if (inactive)
                return true;
            if (blockade)
                return false;

            if (broken) {
                if (System.currentTimeMillis() > lastBreakOrProbeMillis + lockdownMillis) {
                    lastBreakOrProbeMillis = System.currentTimeMillis();
                    logger.debug("canary request allowed");
                    return true; //allow canary request
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        @Override
        public void success() {
            if (broken) {
                reset();
            }
        }

        @Override
        public void error(Exception x) {
            if (lastBreakOrProbeMillis != 0 && System.currentTimeMillis() > (lastBreakOrProbeMillis + lockdownMillis)) {
                failuresCount = 0; //optimization - discard old failures 
                logger.debug("failure count expired");
            }

            lastBreakOrProbeMillis = System.currentTimeMillis();
            if (++failuresCount >= failureThreshold) {
                //lastBreakOrProbeMillis = now;
                broken = true;
                logger.debug("failure lockdown started");
            }
        }

        @Override
        public boolean isBroken() {
            return broken;
        }

        @Override
        public void reset() {
            broken = false;
            failuresCount = 0;
            lastBreakOrProbeMillis = 0;
        }

    }

    /**
     * java.util.concurrent based CircuitBreaker
     */
    public static class AtomicCircuitBreaker extends BaseCircuitBreaker {

        private final AtomicBoolean broken = new AtomicBoolean(false);

        private final AtomicInteger failuresCount = new AtomicInteger(0);

        private final AtomicLong lastBreakOrProbeMillis = new AtomicLong(0);

        public AtomicCircuitBreaker(int failureThreshold, int lockdownMillis) {
            super(failureThreshold, lockdownMillis);
        }

        @Override
        public boolean check() {
            if (inactive)
                return true;
            if (blockade)
                return false;

            if (broken.get()) {
                long lastBreakOrProbe = lastBreakOrProbeMillis.get();
                if (System.currentTimeMillis() > lastBreakOrProbe + lockdownMillis) {
                    // allow canary request = half open circuit
                    if (lastBreakOrProbeMillis.compareAndSet(lastBreakOrProbe, System.currentTimeMillis())) {
                        logger.debug("canary request allowed");
                        return true; //thread race winer
                    } else {
                        return false; //thread race loser
                    }
                } else {
                    return false; //not even probe allowed
                }
            } else {
                return true; // 
            }

        }

        @Override
        public void success() {
            if (broken.get()) {
                reset();
            }
        }

        @Override
        public void error(Exception x) {
            long lastBreakOrProbe = lastBreakOrProbeMillis.get();
            if (lastBreakOrProbe != 0 && System.currentTimeMillis() > lastBreakOrProbe + lockdownMillis) {
                failuresCount.set(0); //optimization - discard old failures
                logger.debug("failure count expired");
            }

            lastBreakOrProbeMillis.set(System.currentTimeMillis());
            if (failuresCount.incrementAndGet() >= failureThreshold) {
                //lastBreakOrProbeMillis.set(System.currentTimeMillis());
                broken.set(true);
                logger.debug("failure lockdown started");
            }

        }

        @Override
        public boolean isBroken() {
            return broken.get();
        }

        @Override
        public void reset() {
            logger.debug("reset");
            failuresCount.set(0);
            lastBreakOrProbeMillis.set(0);
            broken.set(false);
        }

    }
}
