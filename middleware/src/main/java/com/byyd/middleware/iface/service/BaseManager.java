package com.byyd.middleware.iface.service;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import com.adfonic.domain.BusinessKey;
import com.adfonic.domain.HasPrimaryKeyId;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface BaseManager {

    boolean isPersisted(HasPrimaryKeyId entity);
    <T extends BusinessKey> T getObjectById(Class<T> clazz, Long id, FetchStrategy... fetchStrategy);
    <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, FetchStrategy... fetchStrategy);
    <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy);
    <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy);
    
    EntityManager getTransactionalEntityManager();
    <T extends BusinessKey> T attach(T t);
}
