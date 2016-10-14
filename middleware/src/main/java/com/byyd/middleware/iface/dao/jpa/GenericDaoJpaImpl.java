package com.byyd.middleware.iface.dao.jpa;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.jpa.QueryParameter.TemporalType;
import com.byyd.middleware.iface.exception.StaleObjectException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericDaoJpaImpl<T, K extends Serializable>  {

    private static final transient Logger LOG = Logger.getLogger(GenericDaoJpaImpl.class.getName());
    private boolean queryCacheEnabled = false;

    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory entityManagerFactory;

    private Class<T> type;

    /**
    * This is a convenience constructor, if you don't want to declare a
    * specific DAO class, that is to say the methods defined in GenericDao are
    * sufficient, then you this constructor and you'll have a type safe,
    * generic DAO implementation.
    *
    * @param type
    *            the model type you are providing DAO services for
    */
    public GenericDaoJpaImpl(final Class<T> type) {
        this.type = type;
    }

    /**
    * This is the default constructor, if you subclass this class, all you need
    * to do is define the type of the subclass and GenericDaoJpaImpl will
    * have the correct type. Here is how you would subclass
    * GenericDaoJpaImpl:
    */
    public GenericDaoJpaImpl() {
        try {
            Class<T> aType = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
            this.type = aType;
        } catch(Exception e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Inferring of the generics type failed for class " + this.getClass().getCanonicalName());
            }
        }
    }

    /**
    * @param type
    */
    public void setType(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return this.type;
    }

    /**
    * Getting transactional manager
    * @return EntityManager transaction aware entity manager
    * @throws IllegalStateException
    */
    protected EntityManager getTransactionalEntityManager() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to obtain transactional entity manager");
        }
        EntityManagerFactory emf = getEntityManagerFactory();
        // Assert.state(emf != null, "No EntityManagerFactory specified");
        if (emf == null) {
            throw new IllegalStateException(
                    "No EntityManagerFactory specified.");
        }
        EntityManager em = EntityManagerFactoryUtils
                .getTransactionalEntityManager(emf, null);
        if (em == null) {
            //em = emf.createEntityManager();
            LOG.severe("Attempting to obtain transactional entity manager failed!");
            throw new IllegalStateException("There is no current transaction.");
        }
        return em;
    }

    /**
    * @return the entityManagerFactory
    */
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
    * @param entityManagerFactory
    *            the entityManagerFactory to set
    */
    public void setEntityManagerFactory(
            EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
    * @param queryCacheEnabled
    */
    public void setQueryCacheEnabled(boolean queryCacheEnabled) {
        this.queryCacheEnabled = queryCacheEnabled;
    }

    /**
    * @return
    */
    public boolean isQueryCacheEnabled() {
        return queryCacheEnabled;
    }

    /**
    *
    */
    public void flush() {
        getTransactionalEntityManager().flush();

    }

    /**
    *
    */
    public void clear() {
        getTransactionalEntityManager().clear();
    }

    // ----------------------------------------------------------------------------
    // JPA-proper cache hints support
    // ----------------------------------------------------------------------------
    protected Query processCacheHints(Query query) {
        if(this.queryCacheEnabled) {
            query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.USE);
            query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE);
        } else {
            query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
            query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        }
        return query;
    }

    // ----------------------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------------------
    /**
    *
    * @param object
    */
    public void persist(T object) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to persist object");
        }
        getTransactionalEntityManager().persist(object);
    }

    /**
    *
    * @param object
    */
    public void remove(T object) {
        T localObject = object;
        if (!getTransactionalEntityManager().contains(localObject)) {
            // if object isn't managed by EM, load it into EM
            localObject = getTransactionalEntityManager().merge(localObject);
        }
        // object is now a managed object so it can be removed.
        getTransactionalEntityManager().remove(localObject);
    }

    /**
    *
    * @param object
    * @return
    */
    public T merge(T object) {
        try {
            return getTransactionalEntityManager().merge(object);
        } catch (org.hibernate.StaleObjectStateException exception) {
            throw new StaleObjectException(object, exception);
        }
    }

    // ----------------------------------------------------------------------------
    // Simple Methods - for backward compatibility only.
    // ----------------------------------------------------------------------------
    /**
    *
    */
    public List<T> findAll() {

        return findAll(0, 0, null);
    }
    /**
    *
    * @param orderBy
    * @return
    */
    public List<T> findAll(String orderBy) {

        return findAll(0, 0, orderBy);
    }
    /**
    *
    * @param page
    * @return
    */
    public List<T> findAll(Pagination page) {
        return findAll(page.getOffet(), page.getLimit());
    }
    /**
    *
    * @param offset
    * @param limit
    * @return
    */
    public List<T> findAll(int offset, int limit) {
        return findAll(offset, limit, null);
    }
    /**
    *
    * @param page
    * @param orderBy
    * @return
    */
    public List<T> findAll(Pagination page, String orderBy) {
        return findAll(page.getOffet(), page.getLimit(), orderBy);
    }
    /**
    *
    * @param offset
    * @param limit
    * @param orderBy
    * @return
    */
    public List<T> findAll(int offset, int limit, String orderBy) {

        StringBuilder buffer = new StringBuilder();
        buffer.append("select o from " + type.getSimpleName() + " o");
        if (orderBy != null) {
            buffer.append(" order by ").append(orderBy);
        }

        Query q = getTransactionalEntityManager().createQuery(buffer.toString());
        q = processCacheHints(q);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }
    /**
    *
    * @return
    */
    public Number countAll() {
        Query q = getTransactionalEntityManager().createQuery(
                "select count(o) from " + type.getSimpleName() + " o");
        return (Number) q.getSingleResult();
    }
    /**
    * @param query
    * @return
    */
    public List executeJpql(String query) {
        Query q = getTransactionalEntityManager().createQuery(query);
        return q.getResultList();
    }
    // ----------------------------------------------------------------------------
    // Native queries
    // ----------------------------------------------------------------------------
    /**
    * @param query
    * @param args
    * @return
    */
    private Query getAndCreateNativeQueryPositionalParameters(String query, QueryParameter... args) {

        Query q = getTransactionalEntityManager().createNativeQuery(query);
        q = processCacheHints(q);
        populateQueryPositionalParameters(q, args);

        return q;
    }

    /**
    *
    * @param query
    * @return
    */
    public Number executeUpdateNativeQuery(String query) {
        Query q = getTransactionalEntityManager().createNativeQuery(query);
        q = processCacheHints(q);
        return q.executeUpdate();
    }

    /**
    * @param query
    * @return
    */
    public Number executeAggregateFunctionByNativeQuery(String query) {
        try {
            Query q = getTransactionalEntityManager().createNativeQuery(query);
            q = processCacheHints(q);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    *
    * @param query
    * @param args
    * @return
    */
    public Number executeAggregateFunctionByNativeQueryPositionalParameters(String query, List<QueryParameter> list) {
        QueryParameter[] args = new QueryParameter[list.size()];
        for(int i = 0;i < list.size();i++) {
            args[i] = list.get(i);
        }
        return executeAggregateFunctionByNativeQueryPositionalParameters(query, args);
    }
    /**
    * @param query
    * @param args
    * @return
    */
    public Number executeAggregateFunctionByNativeQueryPositionalParameters(String query, QueryParameter... args) {
        try {
            Query q = getAndCreateNativeQueryPositionalParameters(query, args);
            q = processCacheHints(q);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    * @param query
    * @param args
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query, QueryParameter... args) {
        return findByNativeQueryPositionalParameters(query, 0, 0, args);
    }

    /**
    *
    * @param query
    * @param list
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query, List<QueryParameter> list) {
        QueryParameter[] args = new QueryParameter[list.size()];
        for(int i = 0;i < list.size();i++) {
            args[i] = list.get(i);
        }
         return findByNativeQueryPositionalParameters(query, args);
    }
    /**
    *
    * @param query
    * @param page
    * @param args
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query, Pagination page, QueryParameter... args) {
        if(page == null) {
            return findByNativeQueryPositionalParameters(query, args);
        } else {
            return findByNativeQueryPositionalParameters(query, page.getOffet(), page.getLimit(), args);
        }
    }
    /**
    * @param query
    * @param offset
    * @param limit
    * @param args
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query, int offset, int limit, QueryParameter... args) {
        Query q = getAndCreateNativeQueryPositionalParameters(query, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }

        return q.getResultList();
    }

    /**
    *
    * @param query
    * @param page
    * @param list
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query,
            Pagination page, List<QueryParameter> list) {
        if(page == null) {
            return findByNativeQueryPositionalParameters(query, list);
        } else {
            return findByNativeQueryPositionalParameters(query, page.getOffet(), page.getLimit(), list);
        }
    }
    /**
    *
    * @param query
    * @param offset
    * @param limit
    * @param list
    * @return
    */
    public List findByNativeQueryPositionalParameters(String query,
            int offset, int limit, List<QueryParameter> list) {
        QueryParameter[] args = new QueryParameter[list.size()];
        for(int i = 0;i < list.size();i++) {
            args[i] = list.get(i);
        }
        return findByNativeQueryPositionalParameters(query, offset, limit, args);
    }

    /**
     *
     * @param query
     * @param list
     */
    public void executeUpdateNativeQueryPositionalParameters(String query, List<QueryParameter> list) {
        QueryParameter[] args = new QueryParameter[list.size()];
        for(int i = 0;i < list.size();i++) {
            args[i] = list.get(i);
        }
        executeUpdateNativeQueryPositionalParameters(query, args);
    }
    /**
     *
     * @param query
     * @param list
     * @return
     */
    public void executeUpdateNativeQueryPositionalParameters(String query, QueryParameter... args) {
        Query q = getAndCreateNativeQueryPositionalParameters(query, args);
        q.executeUpdate();
    }


    // ----------------------------------------------------------------------------
    // Literal JPQL queries
    // ----------------------------------------------------------------------------
    /**
    * @param query
    * @return
    */
    protected Query getAndCreateLiteralQuery(String query) {
        Query q = getTransactionalEntityManager().createQuery(query);
        q = processCacheHints(q);
        return q;
    }

    /**
    * @param query
    * @param args
    * @return
    */
    private Query getAndCreateLiteralQueryNamedParameters(String query, NamedQueryParameter... args) {

        Query q = getTransactionalEntityManager().createQuery(query);
        q = processCacheHints(q);
        populateQueryNamedParameter(q, args);

        return q;
    }

    /**
    * @param query
    * @return
    */
    public Number executeAggregateFunctionByLiteralQuery(String query) {
        try {
            Query q = getAndCreateLiteralQuery(query);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    * @param query
    * @param list
    * @return
    */
    public Number executeAggregateFunctionByLiteralQueryNamedParameter(String query, List<NamedQueryParameter> list) {
        NamedQueryParameter[] args = list.toArray(new NamedQueryParameter[list.size()]);
        return executeAggregateFunctionByLiteralQueryNamedParameter(query,args);
    }

    /**
    * @param query
    * @param args
    * @return
    */
    public Number executeAggregateFunctionByLiteralQueryNamedParameter(String query, NamedQueryParameter... args) {
        try {
            Query q = getAndCreateLiteralQueryNamedParameters(query, args);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    * @param query
    * @return
    */
    public List<T> findByLiteralQuery(String query) {
        return findByLiteralQuery(query, 0, 0);
    }

    /**
    *
    * @param query
    * @param page
    * @return
    */
    public List<T> findByLiteralQuery(String query, Pagination page) {
        return findByLiteralQuery(query, page.getOffet(), page.getLimit());
    }
    /**
    * @param query
    * @param offset
    * @param limit
    * @return
    */
    public List<T> findByLiteralQuery(String query, int offset, int limit) {
        Query q = getAndCreateLiteralQuery(query);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    * @param query
    * @param args
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, NamedQueryParameter... args) {
        return findByLiteralQueryNamedParameter(query, 0, 0, args);
    }

    /**
    *
    * @param query
    * @param page
    * @param args
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, Pagination page, NamedQueryParameter... args) {
        return findByLiteralQueryNamedParameter(query, page.getOffet(), page.getLimit(), args);
    }
    /**
    * @param query
    * @param offset
    * @param limit
    * @param args
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, int offset, int limit, NamedQueryParameter... args) {
        Query q = getAndCreateLiteralQueryNamedParameters(query, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    * @param query
    * @param list
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, List<NamedQueryParameter> list) {
        return findByLiteralQueryNamedParameter(query, 0, 0, list);
    }

    /**
    *
    * @param query
    * @param page
    * @param list
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, Pagination page, List<NamedQueryParameter> list) {
        return findByLiteralQueryNamedParameter(query, page.getOffet(), page.getLimit(), list);
    }
    /**
    * @param query
    * @param offset
    * @param limit
    * @param list
    * @return
    */
    public List<T> findByLiteralQueryNamedParameter(String query, int offset, int limit, List<NamedQueryParameter> list) {
        NamedQueryParameter[] args = list.toArray(new NamedQueryParameter[list.size()]);
        return findByLiteralQueryNamedParameter(query, offset, limit, args);
    }

    /**
    * @param query
    * @param list
    * @return
    */
    public List<K> findPKsByLiteralQueryNamedParameter(String query, List<NamedQueryParameter> list) {
        return findPKsByLiteralQueryNamedParameter(query, 0, 0, list);
    }

    /**
    *
    * @param query
    * @param page
    * @param list
    * @return
    */
    public List<K> findPKsByLiteralQueryNamedParameter(String query, Pagination page, List<NamedQueryParameter> list) {
        return findPKsByLiteralQueryNamedParameter(query, page.getOffet(), page.getLimit(), list);
    }
    /**
    * @param query
    * @param offset
    * @param limit
    * @param list
    * @return
    */
    public List<K> findPKsByLiteralQueryNamedParameter(String query, int offset, int limit, List<NamedQueryParameter> list) {
        NamedQueryParameter[] args = list.toArray(new NamedQueryParameter[list.size()]);
        Query q = getAndCreateLiteralQueryNamedParameters(query, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    * @param query
    * @param list
    * @return
    */
    public T findInstanceByLiteralQueryNamedParameter(String query,List<NamedQueryParameter> list) {
        NamedQueryParameter[] args = list.toArray(new NamedQueryParameter[list.size()]);
        return findInstanceByLiteralQueryNamedParameter(query, args);
    }

    /**
    * @param query
    * @param args
    * @return
    */
    public T findInstanceByLiteralQueryNamedParameter(String query,NamedQueryParameter... args) {
        Query q = getAndCreateLiteralQueryNamedParameters(query, args);
        return (T) q.getSingleResult();
    }

    // ----------------------------------------------------------------------------
    // Named JPQL queries
    // ----------------------------------------------------------------------------

    /**
    * @param queryName
    */
    private Query getAndCreatenNamedQuery(String queryName) {
        Query q = getTransactionalEntityManager().createNamedQuery(queryName);
        return processCacheHints(q);
    }
    /**
    * @param queryName
    * @param args
    * @return
    */
    private Query getAndCreatenNamedQueryNamedParameters(String queryName,NamedQueryParameter... args) {
        Query q = getTransactionalEntityManager().createNamedQuery(queryName);
        q = processCacheHints(q);
        populateQueryNamedParameter(q, args);
        return q;
    }

    /**
    * @param queryName
    * @param args
    * @return
    */
    private Query getAndCreateNamedQueryPositionalParameters(String queryName,QueryParameter... args) {
        Query q = getTransactionalEntityManager().createNamedQuery(queryName);
        q = processCacheHints(q);
        populateQueryPositionalParameters(q, args);
        return q;
    }

    /**
    * @param queryName
    * @return
    */
    public Number executeAggregateFunctionByNamedQuery(String queryName) {
        try {
            Query q = getAndCreatenNamedQuery(queryName);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    * @param queryName
    * @param args
    * @return
    */
    public Number executeAggregateFunctionByNamedQueryNamedParameter(
            String queryName, NamedQueryParameter... args) {
        try {
            Query q = getAndCreatenNamedQueryNamedParameters(queryName, args);
            return (Number) q.getSingleResult();
        } catch (NoResultException e) {
            return Integer.valueOf(0);
        }
    }

    /**
    *
    * @param queryName
    * @return
    */
    public List<T> findByNamedQuery(String queryName) {
        return findByNamedQuery(queryName, 0, 0);
    }

    /**
    *
    * @param queryName
    * @param page
    * @return
    */
    public List<T> findByNamedQuery(String queryName, Pagination page) {
        return findByNamedQuery(queryName, page.getOffet(), page.getLimit());
    }
    /**
    *
    * @param queryName
    * @param offset
    * @param limit
    * @return
    */
    public List<T> findByNamedQuery(String queryName, int offset, int limit) {
        Query q = getAndCreatenNamedQuery(queryName);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value) {
        return findByNamedQueryNamedParameter(queryName, 0, 0, new NamedQueryParameter(name, value));
    }

    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @param page
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value, Pagination page) {
        return findByNamedQueryNamedParameter(queryName, name, value, page.getOffet(), page.getLimit());
    }
    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @param offset
    * @param limit
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value, int offset, int limit) {
        return findByNamedQueryNamedParameter(queryName, offset, limit, new NamedQueryParameter(name, value));
    }

    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @param type
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value, TemporalType type) {
        return findByNamedQueryNamedParameter(queryName, 0, 0, new NamedQueryParameter(name, value, type));
    }

    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @param type
    * @param page
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value, TemporalType type, Pagination page) {
        return findByNamedQueryNamedParameter(queryName, name, value, type, page.getOffet(), page.getLimit());
    }
    /**
    *
    * @param queryName
    * @param name
    * @param value
    * @param type
    * @param offset
    * @param limit
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, String name, Object value, TemporalType type, int offset, int limit) {
        return findByNamedQueryNamedParameter(queryName, offset, limit, new NamedQueryParameter(name, value, type));
    }

    /**
    *
    * @param queryName
    * @param args
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, NamedQueryParameter... args) {
        return findByNamedQueryNamedParameter(queryName, 0, 0, args);
    }

    /**
    *
    * @param queryName
    * @param page
    * @param args
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, Pagination page, NamedQueryParameter... args) {
        return findByNamedQueryNamedParameter(queryName, page.getOffet(), page.getLimit(), args);
    }
    /**
    *
    * @param queryName
    * @param offset
    * @param limit
    * @param args
    * @return
    */
    public List<T> findByNamedQueryNamedParameter(String queryName, int offset, int limit, NamedQueryParameter... args) {
        Query q = getAndCreatenNamedQueryNamedParameters(queryName, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    *
    * @param queryName
    * @param args
    * @return
    */
    public List<K> findIntegerPKsByNamedQueryNamedParameter(String queryName, NamedQueryParameter... args) {
        return findIntegerPKsByNamedQueryNamedParameter(queryName, 0, 0, args);
    }

    /**
    *
    * @param queryName
    * @param page
    * @param args
    * @return
    */
    public List<K> findIntegerPKsByNamedQueryNamedParameter(String queryName, Pagination page, NamedQueryParameter... args) {
        return findIntegerPKsByNamedQueryNamedParameter(queryName, page.getOffet(), page.getLimit(), args);
    }
    /**
    *
    * @param queryName
    * @param offset
    * @param limit
    * @param args
    * @return
    */
    public List<K> findIntegerPKsByNamedQueryNamedParameter(String queryName, int offset, int limit, NamedQueryParameter... args) {
        Query q = getAndCreatenNamedQueryNamedParameters(queryName, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    *
    * @param queryName
    * @param args
    * @return
    */
    public List<T> findByNamedQueryPositionalParameter(String queryName, QueryParameter... args) {
        return findByNamedQueryPositionalParameter(queryName, 0, 0, args);
    }

    /**
    *
    * @param queryName
    * @param page
    * @param args
    * @return
    */
    public List<T> findByNamedQueryPositionalParameter(String queryName, Pagination page, QueryParameter... args) {
        return findByNamedQueryPositionalParameter(queryName, page.getOffet(), page.getLimit(), args);
    }
    /**
    *
    * @param queryName
    * @param offset
    * @param limit
    * @param args
    * @return
    */
    public List<T> findByNamedQueryPositionalParameter(String queryName, int offset, int limit, QueryParameter... args) {
        Query q = getAndCreateNamedQueryPositionalParameters(queryName, args);
        if (offset > 0) {
            q.setFirstResult(offset);
        }
        if (limit > 0) {
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    /**
    *
    * @param queryName
    * @return
    */
    public T findInstanceByNamedQuery(String queryName) {
        Query q = getAndCreatenNamedQuery(queryName);
        return (T) q.getSingleResult();
    }

    /**
    *
    * @param queryName
    * @param args
    * @return
    */
    public T findInstanceByNamedQueryNamedParameter(String queryName, NamedQueryParameter... args) {
        Query q = getAndCreatenNamedQueryNamedParameters(queryName, args);
        return (T) q.getSingleResult();
    }

    /**
    *
    * @param queryName
    * @param args
    * @return
    */
    public T findInstanceByNamedQueryPositionalParameter(String queryName, QueryParameter... args) {
        Query q = getAndCreateNamedQueryPositionalParameters(queryName, args);
        return (T) q.getSingleResult();
    }

    // ----------------------------------------------------------------------------
    // Parameter management
    // ----------------------------------------------------------------------------
    private void populateQueryNamedParameter(Query q, NamedQueryParameter... args) {
        if (args != null) {

            for (NamedQueryParameter qp : args) {
                setQueryNamedParameter(qp, q);
            }
        }
    }

    private void setQueryNamedParameter(NamedQueryParameter qp, Query q) {
        if (qp.getType() == null || qp.getType() == TemporalType.NONE) {
            q.setParameter(qp.getParameterName(), qp.getValue());
        } else {
            switch (qp.getType()) {
            case DATE:
                setDateNamedParameter(qp, q);
                break;
            case TIME:
                setTimeNamedParameter(qp, q);
                break;
            case TIMESTAMP:
                setTimestampNamedParameter(qp, q);
                break;
            default:
                throw new java.lang.IllegalArgumentException("Invalid TemporalType provided: " + qp.getType());
            }
        }
    }

    private void populateQueryPositionalParameters(Query q, QueryParameter... args) {
        if (args != null) {
            int position = 1;

            for (QueryParameter qp : args) {
                setQueryParameter(qp, position++, q);
            }
        }
    }

    private void setQueryParameter(QueryParameter qp, int position, Query q) {
        if (qp.getType() == null || qp.getType() == TemporalType.NONE) {
            q.setParameter(position, qp.getValue());
        } else {
            switch (qp.getType()) {
            case DATE:
                setDateParameter(qp, position, q);
                break;
            case TIME:
                setTimeParameter(qp, position, q);
                break;
            case TIMESTAMP:
                setTimestampParameter(qp, position, q);
                break;
            default:
                throw new java.lang.IllegalArgumentException("Invalid TemporalType provided: " + qp.getType());
            }
        }
    }

    private void setDateParameter(QueryParameter qp, int position, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(position, (Date) qp.getValue(), javax.persistence.TemporalType.DATE);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(position, (Calendar) qp.getValue(), javax.persistence.TemporalType.DATE);
        } else {
            q.setParameter(position, qp.getValue());
        }
    }
    
    private void setDateNamedParameter(NamedQueryParameter qp, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(qp.getParameterName(), (Date) qp.getValue(), javax.persistence.TemporalType.DATE);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(qp.getParameterName(), (Calendar) qp.getValue(), javax.persistence.TemporalType.DATE);
        } else {
            q.setParameter(qp.getParameterName(), qp.getValue());
        }
    }
    
    private void setTimeParameter(QueryParameter qp, int position, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(position, (Date) qp.getValue(), javax.persistence.TemporalType.TIME);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(position, (Calendar) qp.getValue(), javax.persistence.TemporalType.TIME);
        } else {
            q.setParameter(position, qp.getValue());
        }
    }
    
    private void setTimeNamedParameter(NamedQueryParameter qp, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(qp.getParameterName(), (Date) qp.getValue(), javax.persistence.TemporalType.TIME);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(qp.getParameterName(), (Calendar) qp.getValue(), javax.persistence.TemporalType.TIME);
        } else {
            q.setParameter(qp.getParameterName(), qp.getValue());
        }
    }
    
    private void setTimestampParameter(QueryParameter qp, int position, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(position, (Date) qp.getValue(), javax.persistence.TemporalType.TIMESTAMP);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(position, (Calendar) qp.getValue(), javax.persistence.TemporalType.TIMESTAMP);
        } else {
            q.setParameter(position, qp.getValue());
        }
    }
    
    private void setTimestampNamedParameter(NamedQueryParameter qp, Query q) {
        if (qp.getValue() instanceof Date) {
            q.setParameter(qp.getParameterName(),  (Date) qp.getValue(), javax.persistence.TemporalType.TIMESTAMP);
        } else if (qp.getValue() instanceof Calendar) {
            q.setParameter(qp.getParameterName(),  (Calendar) qp.getValue(), javax.persistence.TemporalType.TIMESTAMP);
        } else {
            q.setParameter(qp.getParameterName(), qp.getValue());
        }
    }
}
