package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.adfonic.adserver.ReservePot;

public class ReservePotImpl implements ReservePot {

    private static final transient Logger LOG = Logger.getLogger(ReservePotImpl.class.getName());
    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final MathContext MC = new MathContext(8, RoundingMode.CEILING);

    protected boolean extraLogging = false;
    private ReservePotDao reservePotDao;

    public ReservePotImpl(ReservePotDao reservePotDao) {
        this.reservePotDao = reservePotDao;

        cleanupThread = new TimeoutCleanupThread();
        cleanupThread.start();
    }

    private TimeoutCleanupThread cleanupThread;

    protected int synchDelayMs = 10_000;

    private Map<Long, AtomicLong> reservePot = new ConcurrentHashMap<>();
    private Map<Long, Long> wantedPriceBoost = new ConcurrentHashMap<>();

    class TimeoutCleanupThread extends Thread {
        private static final int needThreshold = 500;
        private static final long takeThreshold = 1_000L;

        @Override
        public void run() {
            try {
                Thread.sleep(synchDelayMs);

                while (true) {
                    try {
                        for (Entry<Long, AtomicLong> e : reservePot.entrySet()) {
                            synchronizeWithDb(e.getKey(), e.getValue());
                        }

                    } catch (Throwable t) {
                        LOG.warning("Unable to clean up reserved loss budget: " + t.getClass().getName() + ":" + t.getMessage());
                    }
                    Thread.sleep(synchDelayMs);
                }

            } catch (InterruptedException e) {
                LOG.warning("Unable to wait" + e.getMessage());
            } catch (Exception e) {
                LOG.warning(e.getMessage());
            }
        }

        boolean needBudget(long id, AtomicLong reserved, Long priceBoost) {

            // not serving
            if (priceBoost == null) {
                LOG.info("no need, id " + id + " priceBoost is null");
                return false;
            }

            long threshold = priceBoost * needThreshold;
            long accumulatedMargin = reserved.get();
            boolean need = accumulatedMargin < threshold;
            LOG.info("needBudget=" + need + " id " + id + " priceBoost " + priceBoost + " accumulatedMargin " + accumulatedMargin);
            return need;
        }

        boolean haveSpareFunds(long id, AtomicLong reserved, Long priceBoost) {

            // we are not bidding this camapign
            if (priceBoost == null) {
                LOG.info("haveSpareFunds id " + id + " priceBoost is null");
                return true;
            }

            long threshold = priceBoost * takeThreshold;
            long accumulatedMargin = reserved.get();
            boolean have = accumulatedMargin > threshold;
            LOG.info("haveSpareFunds=" + have + " id " + id + " threshold " + threshold + " accumulatedMargin " + accumulatedMargin);
            return have;
        }

        void synchronizeWithDb(long id, AtomicLong reserved) {
            // load needed boost from cassandra

            Long priceBoost = wantedPriceBoost.get(id);
            boolean need = needBudget(id, reserved, priceBoost);
            if (need) {

                takeAccumulatedBoostFromDb(id, priceBoost);
                return;
            }

            boolean haveSpare = haveSpareFunds(id, reserved, priceBoost);
            if (haveSpare) {
                storeAccumulatedBoostDb(id, reserved);
            }
        }

        private void takeAccumulatedBoostFromDb(long id, Long priceBoost) {
            if (priceBoost == null) {
                // should not happen
                LOG.warning(" id " + id + " priceBoost is null");
                return;
            }
            // we need more boost from cassnadra
            wantedPriceBoost.remove(id);
            long wanted = takeThreshold * priceBoost;
            long taken = reservePotDao.take(id, wanted);
            AtomicLong atomicLong = reservePot.get(id);
            long reservePotAfter = atomicLong.addAndGet(taken);
            LOG.info("taken=" + taken + " id " + id + " reservePotAfter " + reservePotAfter);
        }

        private void storeAccumulatedBoostDb(long id, AtomicLong reserved) {
            // store accumulated boost to cassandra
            long margin = reserved.getAndSet(0);
            if (margin <= 0) {
                // nothing to store
                return;
            }

            boolean success = reservePotDao.increaseReserved(id, margin);
            if (!success) {
                // give back
                reserved.addAndGet(margin);
                LOG.warning(" id " + id + " storing of " + margin + " failed ");
            } else {
                LOG.info("stored=" + margin + " id " + id + " reservePotAfter " + reserved.get());
            }
        }
    }

    @Override
    public BigDecimal getPriceBoost(BigDecimal bidPriceUSD, long id, BigDecimal maxBidThreshold) {
        AtomicLong reserve = reservePot.get(id);

        LOG.info("getPriceBoost id " + id + " bidPriceUSD " + bidPriceUSD  + " maxBidThreshold " + maxBidThreshold );
        BigDecimal maxBoost = maxBidThreshold.subtract(bidPriceUSD);
        long delta = maxBoost.multiply(MILLION).round(MC).longValue();
        wantedPriceBoost.put(id, delta);
        if (reserve == null) {
            LOG.info("id " + id + " reserve is null" );
            return BigDecimal.ZERO;
        }

        long left = reserve.addAndGet(-delta);
        if (left >= 0) {
            return maxBoost;
        }

        // left is negative here
        long canTake = delta + left;
        if (canTake <= 0) {
            reserve.addAndGet(delta);
            return BigDecimal.ZERO;
        }

        long putBack = delta - canTake;
        reserve.addAndGet(putBack);

        BigDecimal priceBoost = new BigDecimal(canTake).divide(MILLION);
        return priceBoost;
    }

    
    @Override
    public void deposit(long campaignId, BigDecimal carriedOver) {

        LOG.info("id " + campaignId + " carriedOver " + carriedOver);
        if(carriedOver.signum() <= 0) {
            LOG.warning("id " + campaignId + "must be positive, carriedOver " + carriedOver );
            return;
        }
        
        AtomicLong reserved = reservePot.get(campaignId);
        if (reserved == null) {
            reservePot.putIfAbsent(campaignId, new AtomicLong());
            reserved = reservePot.get(campaignId);
        }

        long delta = carriedOver.multiply(MILLION).round(MC).longValue();
        reserved.addAndGet(delta);
    }

}