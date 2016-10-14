package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AdfonicUser_;
import com.byyd.middleware.account.dao.AdfonicUserDao;
import com.byyd.middleware.account.filter.AdfonicUserFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AdfonicUserDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdfonicUser> implements AdfonicUserDao {

    @Override
    public AdfonicUser getByEmail(String emailAddress, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdfonicUser> criteriaQuery = container.getQuery();
        Root<AdfonicUser> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate creativeExpression = criteriaBuilder.equal(root.get(AdfonicUser_.email), emailAddress);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    @Override
    public AdfonicUser getByLoginName(String loginName, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdfonicUser> criteriaQuery = container.getQuery();
        Root<AdfonicUser> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate creativeExpression = criteriaBuilder.equal(root.get(AdfonicUser_.loginName), loginName);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }
    
    @Override
    public List<AdfonicUser> getAll(AdfonicUserFilter filter, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdfonicUser> criteriaQuery = container.getQuery();
        Root<AdfonicUser> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        return findAll(criteriaQuery);
    }

    private Predicate getPredicate(Root<AdfonicUser> root, AdfonicUserFilter filter) {
        Predicate campaignIdsPredicate = null;
        
        if (CollectionUtils.isNotEmpty(filter.getAdfonicUserIds())) {
            campaignIdsPredicate = root.get(AdfonicUser_.id).in(filter.getAdfonicUserIds());
        }
        
        return and(campaignIdsPredicate);
    }

}
