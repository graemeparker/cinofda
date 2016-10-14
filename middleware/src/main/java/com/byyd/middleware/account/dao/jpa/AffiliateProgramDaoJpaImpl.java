package com.byyd.middleware.account.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AffiliateProgram;
import com.adfonic.domain.AffiliateProgram_;
import com.byyd.middleware.account.dao.AffiliateProgramDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AffiliateProgramDaoJpaImpl extends BusinessKeyDaoJpaImpl<AffiliateProgram> implements AffiliateProgramDao {

    @Override
    public AffiliateProgram getByAffiliateId(String affiliateId, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AffiliateProgram> criteriaQuery = container.getQuery();
        Root<AffiliateProgram> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get(AffiliateProgram_.affiliateId), affiliateId);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<AffiliateProgram> select = criteriaQuery.select(root);

        return find(select);
    }

}
