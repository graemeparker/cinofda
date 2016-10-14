package com.byyd.middleware.common.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentSpec_;
import com.byyd.middleware.common.dao.ContentSpecDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ContentSpecDaoJpaImpl extends BusinessKeyDaoJpaImpl<ContentSpec> implements ContentSpecDao {
    @Override
    public ContentSpec getByName(String name, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<ContentSpec> criteriaQuery = container.getQuery();
        Root<ContentSpec> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get(ContentSpec_.name), name);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<ContentSpec> select = criteriaQuery.select(root);

        return find(select);
    }
}
