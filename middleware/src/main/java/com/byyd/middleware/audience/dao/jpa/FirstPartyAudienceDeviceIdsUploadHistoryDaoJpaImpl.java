package com.byyd.middleware.audience.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory_;
import com.byyd.middleware.audience.dao.FirstPartyAudienceDeviceIdsUploadHistoryDao;
import com.byyd.middleware.audience.filter.FirstPartyAudienceDeviceIdsUploadHistoryFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class FirstPartyAudienceDeviceIdsUploadHistoryDaoJpaImpl extends BusinessKeyDaoJpaImpl<FirstPartyAudienceDeviceIdsUploadHistory> implements FirstPartyAudienceDeviceIdsUploadHistoryDao {

    protected Predicate getPredicate(Root<FirstPartyAudienceDeviceIdsUploadHistory> root, FirstPartyAudienceDeviceIdsUploadHistoryFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate namePredicate = null;
        Predicate firstPartyAudiencePredicate = null;
        
        if(filter.getFirstPartyAudience() != null) {
            firstPartyAudiencePredicate = criteriaBuilder.equal(root.get(FirstPartyAudienceDeviceIdsUploadHistory_.firstPartyAudience), filter.getFirstPartyAudience());
        }
        

        if(!StringUtils.isEmpty(filter.getFilename())) {
            if(filter.getFilenameLikeSpec() == null) {
                if (filter.isFilenameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(FirstPartyAudienceDeviceIdsUploadHistory_.filename), filter.getFilename());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(FirstPartyAudienceDeviceIdsUploadHistory_.filename)),
                            filter.getFilename().toLowerCase());
                }
            } else {
                if (filter.isFilenameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(FirstPartyAudienceDeviceIdsUploadHistory_.filename), filter.getFilenameLikeSpec().getPattern(filter.getFilename()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(FirstPartyAudienceDeviceIdsUploadHistory_.filename)), filter.getFilenameLikeSpec().getPattern(filter.getFilename()).toLowerCase());
                }
            }
        }

        return and(firstPartyAudiencePredicate, namePredicate);
    }
    
    @Override
    public Long countAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<FirstPartyAudienceDeviceIdsUploadHistory> root = criteriaQuery.from(FirstPartyAudienceDeviceIdsUploadHistory.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<FirstPartyAudienceDeviceIdsUploadHistory> getAll(FirstPartyAudienceDeviceIdsUploadHistoryFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<FirstPartyAudienceDeviceIdsUploadHistory> criteriaQuery = container.getQuery();
        Root<FirstPartyAudienceDeviceIdsUploadHistory> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
