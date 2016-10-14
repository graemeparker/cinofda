package com.byyd.middleware.campaign.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.GeotargetList;
import com.adfonic.domain.GeotargetList_;
import com.byyd.middleware.campaign.dao.GeotargetListDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class GeotargetListDaoJpaImpl extends BusinessKeyDaoJpaImpl<GeotargetList> implements GeotargetListDao {

    @Override
    public List<GeotargetList> getGeotargetListByGeotargetBy(long id,FetchStrategy fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<GeotargetList> criteriaQuery = container.getQuery();
        Root<GeotargetList> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(GeotargetList_.geotargetById), id);
        criteriaQuery = criteriaQuery.where(predicate);
        criteriaQuery = criteriaQuery.select(root);
        return findAll(criteriaQuery);
    }

}
