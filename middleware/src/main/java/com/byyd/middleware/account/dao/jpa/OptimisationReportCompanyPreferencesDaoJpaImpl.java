package com.byyd.middleware.account.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Company;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.OptimisationReportCompanyPreferences_;
import com.byyd.middleware.account.dao.OptimisationReportCompanyPreferencesDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class OptimisationReportCompanyPreferencesDaoJpaImpl extends BusinessKeyDaoJpaImpl<OptimisationReportCompanyPreferences> implements OptimisationReportCompanyPreferencesDao {

    @Override
    public OptimisationReportCompanyPreferences getForCompany(Company company,
            FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<OptimisationReportCompanyPreferences> criteriaQuery = container.getQuery();
        Root<OptimisationReportCompanyPreferences> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate companyPredicate = criteriaBuilder.equal(root.get(OptimisationReportCompanyPreferences_.company), company);
        criteriaQuery = criteriaQuery.where(companyPredicate);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

}
