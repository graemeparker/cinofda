package com.byyd.middleware.common.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Language;
import com.adfonic.domain.Language_;
import com.byyd.middleware.common.dao.LanguageDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class LanguageDaoJpaImpl extends BusinessKeyDaoJpaImpl<Language> implements LanguageDao {

    @Override
    public Language getByIsoCode(String isoCode, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Language> criteriaQuery = container.getQuery();
        Root<Language> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(Language_.isoCode), isoCode);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

}
