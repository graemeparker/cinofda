package com.byyd.middleware.auditlog.dao.jpa;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.auditlog.exception.AuditLogException;
import com.byyd.middleware.auditlog.filter.AbstractAuditLogFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

/**
 * Abstract class to extend all DAO's implementations to perform their operation into the audit 
 * database (called auditLogEntityManagerFactory in spring configuration file)
 */
public abstract class AbstracAuditLogDataSourceDaoJpaImpl<T extends BusinessKey, E extends AbstractAuditLogFilter> extends BusinessKeyDaoJpaImpl<T> {

    private static final transient Logger LOG = Logger.getLogger(AbstracAuditLogDataSourceDaoJpaImpl.class.getName());
    
    private T type;

    @Autowired(required=false)
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory entityManagerFactory;
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory == null) {
            String msg = "It seems no EntityManagerFactory with name auditLogEntityManagerFactory was defined in the context used to boot this application";
            LOG.severe(msg);
            throw new AuditLogException(msg);
        }
        return entityManagerFactory;
    }
    
    @SuppressWarnings("unchecked")
    protected Class<T> getEntityGenericType() {
        return (Class<T>) type.getClass();
    }
    
    /**
     * Method to be override for all AuditDataSourceDao subclasses
     * 
     * @param root
     * @param filter
     * @return
     */
    protected abstract Predicate getPredicate(Root<T> root, E filter);
    
    public Long countAll(E filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(getEntityGenericType());

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    public List<T> getAll(E filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    public List<T> getAll(E filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    public List<T> getAll(E filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<T> getAll(E filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<T> criteriaQuery = container.getQuery();
        Root<T> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}