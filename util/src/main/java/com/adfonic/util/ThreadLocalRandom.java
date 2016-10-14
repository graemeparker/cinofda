package com.adfonic.util;

import java.util.Random;

/**
 * Centralization of ThreadLocal management of Random instances. This class
 * precludes the need for other classes to deal with their own ThreadLocal
 * instances of Random. Just call ThreadLocalRandom.getRandom() if you want a
 * nice thread-safe, non-synchronized way of getting a Random.
 */
public final class ThreadLocalRandom {
    // This is the most performant way to do this. Don't construct a new
    // Random every time you need it, and don't use a single static instance.
    // Synchronization ends up hurting us. With ThreadLocal, there should be
    // no thread-to-thread blocking going on.
    private static final ThreadLocal<Random> THREAD_LOCAL_RANDOM = new ThreadLocal<Random>() {
        @Override
        public Random initialValue() {
            return new Random();
        }
    };
    
    private ThreadLocalRandom(){
    }

    public static Random getRandom() {
        return THREAD_LOCAL_RANDOM.get();
    }
}
