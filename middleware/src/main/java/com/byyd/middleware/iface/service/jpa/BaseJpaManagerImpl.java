package com.byyd.middleware.iface.service.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.BusinessKey;
import com.adfonic.domain.HasPrimaryKeyId;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyFactory;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.service.BaseManagerImpl;

/**
 * Base class for all service layer level JPA manager implementations. Place
 * here all the common methods specific to the JPA persistence
 *
 * @author pierre
 *
 */
public abstract class BaseJpaManagerImpl extends BaseManagerImpl {

    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory entityManagerFactory;
    
    @Autowired
    private FetchStrategyFactory fetchStrategyFactory;

    @Override
    public boolean isPersisted(HasPrimaryKeyId entity) {
        return entity.getId() > 0;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly=true)
    public <T extends BusinessKey> T getObjectById(Class<T> clazz, Long id, FetchStrategy... fetchStrategy) {
        BusinessKeyDaoJpaImpl dao = new BusinessKeyDaoJpaImpl(clazz);
        dao.setEntityManagerFactory(entityManagerFactory);
        dao.setFetchStrategyFactory(fetchStrategyFactory);
        return (T) dao.getById(id, fetchStrategy);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly=true)
    public <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, FetchStrategy... fetchStrategy) {
        if(ids == null || ids.isEmpty()) {
            return new ArrayList<T>();
        }
        BusinessKeyDaoJpaImpl dao = new BusinessKeyDaoJpaImpl(clazz);
        dao.setEntityManagerFactory(entityManagerFactory);
        dao.setFetchStrategyFactory(fetchStrategyFactory);
        return dao.getObjectsByIds(ids, fetchStrategy);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly=true)
    public <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        if(ids == null || ids.isEmpty()) {
            return new ArrayList<T>();
        }
        BusinessKeyDaoJpaImpl dao = new BusinessKeyDaoJpaImpl(clazz);
        dao.setEntityManagerFactory(entityManagerFactory);
        dao.setFetchStrategyFactory(fetchStrategyFactory);
        return dao.getObjectsByIds(ids, sort, fetchStrategy);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly=true)
    public <T extends BusinessKey> List<T> getObjectsByIds(Class<T> clazz, Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        if(ids == null || ids.isEmpty()) {
            return new ArrayList<T>();
        }
        BusinessKeyDaoJpaImpl dao = new BusinessKeyDaoJpaImpl(clazz);
        dao.setEntityManagerFactory(entityManagerFactory);
        dao.setFetchStrategyFactory(fetchStrategyFactory);
        return dao.getObjectsByIds(ids, page, fetchStrategy);
    }

    public <T extends BusinessKey> Map<Long, T> asMap(List<T> list) {
        Map<Long, T> map = new HashMap<>();
        if(list != null && !list.isEmpty()) {
            for(T t : list) {
                map.put(t.getId(), t);
            }
        }
        return map;
    }

    @Transactional(readOnly=true)
    public <T extends BusinessKey> Map<Long, T> getObjectsByIdsAsMap(Class<T> clazz, Collection<Long> ids, FetchStrategy... fetchStrategy) {
        return asMap(getObjectsByIds(clazz, ids, fetchStrategy));
    }

    @Transactional(readOnly=true)
    public <T extends BusinessKey> Map<Long, T> getObjectsByIdsAsMap(Class<T> clazz, Collection<Long> ids, Sorting sort, FetchStrategy... fetchStrategy) {
        return asMap(getObjectsByIds(clazz, ids, sort, fetchStrategy));
    }

    @Transactional(readOnly=true)
    public <T extends BusinessKey> Map<Long, T> getObjectsByIdsAsMap(Class<T> clazz, Collection<Long> ids, Pagination page, FetchStrategy... fetchStrategy) {
        return asMap(getObjectsByIds(clazz, ids, page, fetchStrategy));
    }
    
    @Override
    public EntityManager getTransactionalEntityManager() {
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory, null);
        if (em == null) {
            throw new IllegalStateException("There is no current transaction.");
        }
        return em;
    }
    
    /**
     * This method attaches an entity toe the current transaction, so it can be manipulated.
     * @param t
     * @return
     */
    @Override
    public <T extends BusinessKey> T attach(T t) {
        T localT = t;
        if (!getTransactionalEntityManager().contains(localT)) {
            localT = getTransactionalEntityManager().merge(localT);
        }
        return localT;
    }


    protected static List<Long> toLongs(List<Number> numbers) {
        List<Long> longs = new ArrayList<Long>(numbers.size());
        for (Number number : numbers) {
            longs.add(number.longValue());
        }
        return longs;
    }
}
