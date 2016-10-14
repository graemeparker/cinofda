package com.adfonic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestKeyedSynchronizer extends AbstractAdfonicTest {
    @Test
    public void sameThreadRepeatedAcquireReturnsImmediately() throws InterruptedException {
        KeyedSynchronizer<String> ks = new KeyedSynchronizer<String>();
        String key = randomAlphaNumericString(10);
        long startTime = System.currentTimeMillis();
        ks.acquire(key);
        try {
            ks.acquire(key);
            ks.acquire(key);
            ks.acquire(key);
            assertTrue(System.currentTimeMillis() - startTime < 100);
        } finally {
            ks.release(key);
        }
    }

    @Test
    public void tryAcquireSameThreadReturnsTrueImmediately() throws InterruptedException {
        KeyedSynchronizer<String> ks = new KeyedSynchronizer<String>();
        String key = randomAlphaNumericString(10);
        long startTime = System.currentTimeMillis();
        ks.acquire(key);
        try {
            assertTrue(ks.tryAcquire(key));
            assertTrue(ks.tryAcquire(key));
            assertTrue(System.currentTimeMillis() - startTime < 100);
        } finally {
            ks.release(key);
        }
    }

    @Test
    public void tryAcquireDifferentThreadReturnsFalseImmediately() throws InterruptedException {
        final KeyedSynchronizer<String> ks = new KeyedSynchronizer<String>();
        final String key = randomAlphaNumericString(10);
        Thread otherThread = new Thread() {
            @Override
            public void run() {
                try {
                    ks.acquire(key);
                } catch (InterruptedException e) {
                    fail("Locking thread was interrupted...wack");
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                } finally {
                    ks.release(key);
                }
            }
        };
        otherThread.start();
        TimeUnit.MILLISECONDS.sleep(20); // "yield"
        long startTime = System.currentTimeMillis();
        try {
            assertFalse(ks.tryAcquire(key));
            assertTrue(System.currentTimeMillis() - startTime < 100);

            // Now sleep a couple of seconds and try again, and we should get it
            TimeUnit.SECONDS.sleep(2);
            assertTrue(ks.tryAcquire(key));
        } finally {
            ks.release(key);
        }
    }

    @Test
    public void releaseWhenNotHoldingLock() {
        KeyedSynchronizer<String> ks = new KeyedSynchronizer<String>();
        String key = randomAlphaNumericString(10);
        ks.release(key);
    }
}