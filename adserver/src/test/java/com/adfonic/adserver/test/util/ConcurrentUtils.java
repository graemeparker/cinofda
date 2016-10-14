package com.adfonic.adserver.test.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConcurrentUtils {

    public static final int NOT_TIMED_TASK = -1;

    public static long runInParallel(int threadNumber, final ParallelTestRunnable test, final ParallelTestRunnable notTimedTask) {

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadNumber);

        Executor taskExecutor = Executors.newFixedThreadPool(threadNumber + 1);


        if (notTimedTask != null) {
            taskExecutor.execute(createTaskNotTimed(notTimedTask, startLatch));
        }

        for (int i = 0; i < threadNumber; i++) {
            taskExecutor.execute(createTask(test, startLatch, endLatch, i));
        }
        long start = System.nanoTime();

        startLatch.countDown();
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("end latch interruption", e);
        }

        long timeElapsed = (System.nanoTime() - start) / 1000; //microseconds



        return timeElapsed;
    }

    public static long runInParallel(int threadNumber, final ParallelTestRunnable test) {

        return runInParallel(threadNumber, test, null);
    }

    private static Runnable createTask(final ParallelTestRunnable test, final CountDownLatch startLatch, final CountDownLatch endLatch, final int taskNumber) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("start latch interruption", e);
                }

                try {
                    test.run(taskNumber);
                } finally {
                    endLatch.countDown();
                }
            }
        };
    }

    private static Runnable createTaskNotTimed(final ParallelTestRunnable test, final CountDownLatch startLatch) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("start latch interruption", e);
                }
                test.run(NOT_TIMED_TASK);
            }
        };
    }

}
