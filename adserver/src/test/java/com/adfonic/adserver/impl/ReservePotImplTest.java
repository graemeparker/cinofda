package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

public class ReservePotImplTest {

    ReservePotDao reservePotDao;

    ReservePotImpl testobj = new ReservePotImpl(reservePotDao);

    CyclicBarrier barrier;
    CountDownLatch endSignal;

    //@Test
    public void concurrentTest() throws InterruptedException, BrokenBarrierException {
        int numWorkers = 4;
        int numCampaigns = 10;
        barrier = new CyclicBarrier(numWorkers * numCampaigns + 1);
        endSignal = new CountDownLatch(numWorkers * numCampaigns);

        // prepare worker threads
        for (int j = 0; j < numWorkers; j++) {
            for (int c = 1; c <= numCampaigns; c++) {
                Worker w = new Worker(j, c);
                w.start();
            }
        }

        // wait until all workers are ready
        barrier.await();

        System.out.println("Main waiting for workers to complete");
        endSignal.await();
        System.out.println("all completed ");

        for (long c = 1; c <= numCampaigns; c++) {
            BigDecimal left = testobj.getPriceBoost(BigDecimal.ZERO, c, new BigDecimal(Long.MAX_VALUE));
            Assert.assertEquals(new BigDecimal(40_000), left);
        }
    }

    /**
     * The worker deposit 2 and takes 1 repeats that 100 000 times, effectively depositing 100 000$
     */
    class Worker extends Thread {

        private int num;
        private long campaignId;

        Worker(int j, long campaignId) {
            this.num = j;
            this.campaignId = campaignId;
        }

        @Override
        public void run() {

            try {
                barrier.await();

                for (int j = 0; j < 10_000; j++) {
                    testobj.deposit(campaignId, new BigDecimal(2.0));
                    testobj.getPriceBoost(BigDecimal.ZERO, campaignId, new BigDecimal(1.0));
                }

                // System.out.println("running " + num);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            } finally {
                endSignal.countDown();
            }
        }

    }

}
