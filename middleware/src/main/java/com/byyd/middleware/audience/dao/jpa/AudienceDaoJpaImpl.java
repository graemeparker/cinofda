package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Audience;
import com.adfonic.domain.Audience_;
import com.byyd.middleware.audience.dao.AudienceDao;
import com.byyd.middleware.audience.filter.AudienceFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AudienceDaoJpaImpl extends BusinessKeyDaoJpaImpl<Audience> implements AudienceDao {

       protected Predicate getPredicate(Root<Audience> root, AudienceFilter filter) {
            CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

            Predicate namePredicate = null;
            Predicate advertiserPredicate = null;
            Predicate statusNotIncludedPredicate = null;
            Predicate statusIncludedPredicate = null;
            
            if(!StringUtils.isEmpty(filter.getName())) {
                if(filter.getNameLikeSpec() == null) {
                    if (filter.isNameCaseSensitive()) {
                        namePredicate = criteriaBuilder.equal(
                                root.get(Audience_.name), filter.getName());
                    } else {
                        namePredicate = criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get(Audience_.name)),
                                filter.getName().toLowerCase());
                    }
                } else {
                    if (filter.isNameCaseSensitive()) {
                        namePredicate = criteriaBuilder.like(root.get(Audience_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                    } else {
                        namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Audience_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                    }
                }
            }

            if(filter.getAdvertiser() != null) {
                advertiserPredicate = criteriaBuilder.equal(root.get(Audience_.advertiser), filter.getAdvertiser());
            }
            
            if(filter.getStatusesNotIncluded() != null){
                for (Audience.Status status : filter.getStatusesNotIncluded()){
                    if (statusNotIncludedPredicate==null){
                        statusNotIncludedPredicate = criteriaBuilder.notEqual(root.get(Audience_.status), status);
                    }else{
                        statusNotIncludedPredicate = and(statusNotIncludedPredicate, criteriaBuilder.notEqual(root.get(Audience_.status), status));
                    }
                }
            }
            
            if(filter.getStatusesIncluded() != null){
                for (Audience.Status status : filter.getStatusesIncluded()){
                    if (statusIncludedPredicate==null){
                        statusIncludedPredicate = criteriaBuilder.equal(root.get(Audience_.status), status);
                    }else{
                        statusIncludedPredicate = or(statusIncludedPredicate, criteriaBuilder.equal(root.get(Audience_.status), status));
                    }
                }
            }
            
            return and(namePredicate, advertiserPredicate, statusNotIncludedPredicate, statusIncludedPredicate);
        }
        
        @Override
        public Long countAll(AudienceFilter filter) {
            CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<Audience> root = criteriaQuery.from(Audience.class);

            Predicate predicate = getPredicate(root, filter);
            criteriaQuery = criteriaQuery.where(predicate);

            criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

            return executeLongAggregateFunction(criteriaQuery);
        }

        @Override
        public List<Audience> getAll(AudienceFilter filter, FetchStrategy ... fetchStrategy) {
            return getAll(filter, null, null, fetchStrategy);
        }

        @Override
        public List<Audience> getAll(AudienceFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
            return getAll(filter, page, page.getSorting(), fetchStrategy);
        }

        @Override
        public List<Audience> getAll(AudienceFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
            return getAll(filter, null, sort, fetchStrategy);
        }

        protected List<Audience> getAll(AudienceFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
            CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
            CriteriaQuery<Audience> criteriaQuery = container.getQuery();
            Root<Audience> root = container.getRoot();

            Predicate predicate = getPredicate(root, filter);
            if(predicate != null) {
                criteriaQuery = criteriaQuery.where(predicate);
            }

            criteriaQuery = criteriaQuery.select(root);

            criteriaQuery = processOrderBy(criteriaQuery, root, sort);
            return findAll(criteriaQuery, page);
        }

}
