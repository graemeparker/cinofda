package com.byyd.middleware.publication.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationListDao;
import com.byyd.middleware.publication.filter.PublicationListFilter;

@Repository
public class PublicationListDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationList> implements PublicationListDao {

    protected Predicate getPredicate(Root<PublicationList> root, PublicationListFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        Predicate companyPredicate = null;
        Predicate advertiserPredicate = null;
        Predicate whiteListPredicate = null;
        Predicate listLevelPredicate = null;
        
        if(filter.getCompany() != null) {
            companyPredicate = criteriaBuilder.equal(root.get(PublicationList_.company), filter.getCompany());
        }
        
        if(filter.getAdvertiser() != null) {
            advertiserPredicate = criteriaBuilder.equal(root.get(PublicationList_.advertiser), filter.getAdvertiser());
        }

        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(PublicationList_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(PublicationList_.name)),
                            filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(PublicationList_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationList_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }

        if(filter.getWhiteList() != null) {
            if(filter.getWhiteList()) {
                whiteListPredicate = criteriaBuilder.isTrue(root.get(PublicationList_.whiteList));
            } else {
                whiteListPredicate = criteriaBuilder.isFalse(root.get(PublicationList_.whiteList));
            }
        }
        
        if(filter.getPublicationListLevel() != null) {
            listLevelPredicate = criteriaBuilder.equal(root.get(PublicationList_.publicationListLevel), filter.getPublicationListLevel());
        }
        
        return and(namePredicate, companyPredicate, advertiserPredicate, whiteListPredicate, listLevelPredicate);
    }
    
    @Override
    public Long countAll(PublicationListFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PublicationList> root = criteriaQuery.from(PublicationList.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<PublicationList> getAll(PublicationListFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<PublicationList> getAll(PublicationListFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<PublicationList> getAll(PublicationListFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<PublicationList> getAll(PublicationListFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublicationList> criteriaQuery = container.getQuery();
        Root<PublicationList> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }



}
