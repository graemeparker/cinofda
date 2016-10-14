package com.adfonic.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Provide a non-blocking mechanism for doing name-based synchronization within
 * a JVM. This is useful if you want to synchronize a block of code, but you
 * don't need the lock to be global or class-wide. It only needs to lock for a
 * very specific "key" (i.e. name).
 *
 * The general idea is that you want to prevent "the same thing" from being
 * handled concurrently...but "different things" are allowed concurrently.
 *
 * The impetus for this class is the following use case: RTB publication
 * persistence. You want to allow several threads to handle different
 * publications concurrently, but only one thread is allowed to handle any given
 * publication at any given time.
 */
public class KeyedSynchronizer<T> {
    private static final transient Logger LOG = Logger.getLogger(KeyedSynchronizer.class.getName());

    // How long to wait in between acquire retry attempts
    private static final long RETRY_INTERVAL_MS = 100;

    private final ConcurrentMap<T, Thread> map = new ConcurrentHashMap<>();

    /**
     * Acquire a synchronization lock, waiting indefinitely
     * 
     * @param key
     *            the respective key on which to synchronize
     * @throws InterruptedException
     *             if the thread is interrupted while waiting
     */
    public void acquire(T key) throws InterruptedException {
        try {
            acquire(key, -1);
        } catch (TimeoutException e) {
            // When we pass -1 for maxWaitMs we never expect this to happen
            throw new IllegalStateException("Unexpected TimeoutException", e);
        }
    }

    /**
     * Acquire a synchronization lock, waiting up to a specified amount of time
     * 
     * @param key
     *            the respective key on which to synchronize
     * @param maxWaitMs
     *            the maximum amount of time to wait for the lock
     * @throws TimeoutException
     *             if the lock is not acquired after maxWaitMs
     * @throws InterruptedException
     *             if the thread is interrupted while waiting
     */
    public void acquire(T key, long maxWaitMs) throws InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();
        while (true) {
            Thread alreadyHandling = map.putIfAbsent(key, Thread.currentThread());
            if (alreadyHandling == null) {
                return;
            } else if (Thread.currentThread().equals(alreadyHandling)) {
                // No harm no foul, but warn...probably a bad coding pattern
                LOG.warning("Current thread already holds the lock...check your coding pattern!");
                return;
            }

            // Another thread holds the lock...wait as long as maxWaitMs
            long waited = System.currentTimeMillis() - startTime;
            if (maxWaitMs < 0 || waited < maxWaitMs) {
                // Wait a short amount of time and then try again
                TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_MS);
            } else {
                throw new TimeoutException("Failed to acquire " + key + " after " + waited + "ms");
            }
        }
    }

    /**
     * Release the lock for a given key
     * 
     * @param the
     *            key on which to synchronize
     */
    public void release(T key) {
        // Only remove it if it's the current thread -- won't hurt any
        // other thread, and won't hurt if nobody holds the lock
        if (!map.remove(key, Thread.currentThread())) {
            // No harm no foul, but warn...probably a bad coding pattern
            LOG.warning("Current thread tried to release but didn't hold the lock...check your coding pattern!");
        }
    }

    /**
     * Try to acquire the lock, returning immediately.
     * 
     * @return true if the lock was acquired, or false if another thread holds
     *         the lock
     */
    public boolean tryAcquire(T key) {
        Thread alreadyHandling;
        synchronized (map) {
            alreadyHandling = map.get(key);
            if (alreadyHandling == null) {
                map.put(key, Thread.currentThread());
                return true;
            }
        }
        return Thread.currentThread().equals(alreadyHandling);
    }
}