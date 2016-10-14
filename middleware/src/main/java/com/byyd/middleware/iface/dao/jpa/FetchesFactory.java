package com.byyd.middleware.iface.dao.jpa;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.byyd.middleware.iface.dao.FetchStrategyImpl;

/**
 * This class is a Factory class to obtain Fetches instances. It caches instances in a ConcurentHashMap as they get created.
 *
 * @author pierre
 *
 */
public class FetchesFactory {

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<String, Fetches> FETCHES = new ConcurrentHashMap<String, Fetches>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Fetches getFetches(Class<?> clazz, FetchStrategyImpl fetchStrategy)  throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        if(fetchStrategy.isCacheable()) {
            String key = makeKey(clazz, fetchStrategy);
            Fetches f = FETCHES.get(key);
            if(f == null) {
                Fetches nf = new Fetches(clazz, fetchStrategy);
                f = FETCHES.putIfAbsent(key, nf);
                if(f == null) {
                    f = nf;
                }
            }
            return f;
        } else {
            return new Fetches(clazz, fetchStrategy);
        }
    }

    public static String makeKey(Class<?> clazz, FetchStrategyImpl fetchStrategy) {
        return clazz.getCanonicalName() + "-" + fetchStrategy.getName();
    }
}
