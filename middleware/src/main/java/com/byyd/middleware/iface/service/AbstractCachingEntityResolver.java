package com.byyd.middleware.iface.service;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.reporting.EntityResolver;
import com.byyd.middleware.iface.dao.FetchStrategy;

public abstract class AbstractCachingEntityResolver<T> implements EntityResolver<T> {
    private final FetchStrategy[] fetchStrategy;
    private final Map<Long,T> cache = new HashMap<Long,T>();

    protected AbstractCachingEntityResolver(FetchStrategy ... fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
    }

    @Override
    public T getEntityById(long id) {
        T t = cache.get(id);
        if (t == null) {
            t = getById(id, fetchStrategy);
            cache.put(id, t);
        }
        return t;
    }

    protected abstract T getById(long id, FetchStrategy ... fetchStrategy);
}
