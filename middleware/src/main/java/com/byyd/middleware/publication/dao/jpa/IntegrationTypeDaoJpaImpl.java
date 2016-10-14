package com.byyd.middleware.publication.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.IntegrationType_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.IntegrationTypeDao;

@Repository
public class IntegrationTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<IntegrationType> implements IntegrationTypeDao {
    @Override
    public IntegrationType getBySystemName(String systemName, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<IntegrationType> criteriaQuery = container.getQuery();
        Root<IntegrationType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get(IntegrationType_.systemName), systemName);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<IntegrationType> select = criteriaQuery.select(root);

        return find(select);
    }
}
