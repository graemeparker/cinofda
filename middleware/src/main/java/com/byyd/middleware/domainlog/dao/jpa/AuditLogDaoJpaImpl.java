package com.byyd.middleware.domainlog.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AuditLog;
import com.adfonic.domain.AuditLog_;
import com.byyd.middleware.domainlog.dao.AuditLogDao;
import com.byyd.middleware.domainlog.filter.AuditLogFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AuditLogDaoJpaImpl extends BusinessKeyDaoJpaImpl<AuditLog> implements AuditLogDao {

    protected Predicate getPredicate(Root<AuditLog> root, AuditLogFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = null;
        if (filter.getSystemId() != null) {
            predicate = and(criteriaBuilder.equal(root.get(AuditLog_.systemId), filter.getSystemId()), predicate);
        }
        if (filter.getUser() != null) {
            predicate = and(criteriaBuilder.equal(root.get(AuditLog_.user), filter.getUser()), predicate);
        }
        if (filter.getAdfonicUser() != null) {
            predicate = and(criteriaBuilder.equal(root.get(AuditLog_.adfonicUser), filter.getAdfonicUser()), predicate);
        }
        if (filter.getObjectId() != null) {
            predicate = and(criteriaBuilder.equal(root.get(AuditLog_.objectId), filter.getObjectId()), predicate);
        }
        if (filter.getField() != null) {
            predicate = and(criteriaBuilder.equal(root.get(AuditLog_.field), filter.getField()), predicate);
        }
        return predicate;
    }

    @Override
    public Long countAll(AuditLogFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AuditLog> root = criteriaQuery.from(AuditLog.class);
        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AuditLog> getAll(AuditLogFilter filter, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AuditLog> criteriaQuery = container.getQuery();
        Root<AuditLog> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        return findAll(criteriaQuery);
    }

}
