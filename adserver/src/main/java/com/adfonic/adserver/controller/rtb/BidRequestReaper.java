package com.adfonic.adserver.controller.rtb;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author mvanek
 *
 */
public class BidRequestReaper implements Closeable {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ConcurrentHashMap<Long, ThreadInfo> threadStates = new ConcurrentHashMap<Long, ThreadInfo>();
    private final int checkPeriodMs;
    private final int reapTimeoutMs;

    private boolean stopFlag;
    private ReaperThread reaperThread;

    public BidRequestReaper(int checkPeriodMs, int reapTimeoutMs) {
        this.checkPeriodMs = checkPeriodMs;
        this.reapTimeoutMs = reapTimeoutMs;
        start();
    }

    public void start() {
        logger.info("Starting operation");
        stopFlag = false;
        reaperThread = new ReaperThread();
        reaperThread.start();
    }

    public void stop() {
        logger.info("Stopping operation");
        stopFlag = true;
        reaperThread.interrupt();
    }

    @Override
    public void close() {
        stop();
    }

    public void execute(RtbHttpExecutor delegate, RtbHttpContext http) {
        Thread thread = Thread.currentThread();
        ThreadInfo threadInfo = threadStates.get(thread.getId());
        long currentMs = System.currentTimeMillis();
        if (threadInfo == null) {
            threadInfo = new ThreadInfo(thread);
            threadStates.putIfAbsent(thread.getId(), threadInfo);
            logger.info("New thread " + thread.getId() + " " + thread.getName());
        }
        threadInfo.enteredAt = currentMs;
        try {
            delegate.execute(http);
        } finally {
            threadInfo.enteredAt = null;
        }
    }

    class ThreadInfo {
        private final Thread thread;
        private Long enteredAt;

        public ThreadInfo(Thread thread) {
            this.thread = thread;
        }
    }

    class ReaperThread extends Thread {

        public ReaperThread() {
            setDaemon(true);
            setName("bid-reaper");
        }

        @Override
        public void run() {
            try {
                logger.info("ReaperThread enter");
                while (!stopFlag) {
                    try {
                        Thread.sleep(checkPeriodMs);
                    } catch (InterruptedException ix) {
                        if (stopFlag) {
                            logger.info("ReaperThread exit on close signal");
                            break;
                        }
                    }
                    logger.fine("ReaperThread check");
                    long currentMs = System.currentTimeMillis();
                    for (ThreadInfo value : threadStates.values()) {
                        if (value.enteredAt != null && (currentMs - value.enteredAt > reapTimeoutMs)) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.info("ReaperThread interrupting thread " + value.thread.getId() + " " + value.thread.getName());
                            }
                            value.thread.interrupt();
                        }
                    }
                }
            } finally {
                logger.info("ReaperThread exit");
            }
        }
    }
}
