package com.byyd.middleware.iface.service.jpa;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.service.AbstractCachingEntityResolver;
import com.byyd.middleware.iface.service.BaseManager;

public class GenericCachingEntityResolver<T extends BusinessKey> extends AbstractCachingEntityResolver<T> {
    private final Class<T> clazz;
    private final BaseManager manager;
    
    public GenericCachingEntityResolver(Class<T> clazz, BaseManager manager, FetchStrategy ... fetchStrategy) {
        super(fetchStrategy);
        this.clazz = clazz;
        this.manager = manager;
    }

    @Override
    protected T getById(long id, FetchStrategy ... fetchStrategy) {
        return manager.getObjectById(clazz, id, fetchStrategy);
    }
}
