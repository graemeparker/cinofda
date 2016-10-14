package com.byyd.middleware.common.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Channel;
import com.adfonic.domain.Channel_;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.common.dao.ChannelDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ChannelDaoJpaImpl extends BusinessKeyDaoJpaImpl<Channel> implements ChannelDao {

    @Override
    public Long countAll(ChannelFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Channel> root = criteriaQuery.from(Channel.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Channel> getAll(ChannelFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Channel> getAll(ChannelFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Channel> getAll(ChannelFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<Channel> getAll(ChannelFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Channel> criteriaQuery = container.getQuery();
        Root<Channel> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            // Careful. NPE if nothing is set! I'd have allowed for null, myself, but...
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
     }

    protected Predicate getPredicate(Root<Channel> root, ChannelFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;

        if(filter.getExcludeUncategorized() != null && filter.getExcludeUncategorized()) {
            namePredicate = criteriaBuilder.notEqual(root.get(Channel_.name), Channel.NOT_CATEGORIZED_NAME);
        }
        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getLikeSpec() == null) {
                // Straight equals
                if(filter.isCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(root.get(Channel_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Channel_.name)), filter.getName().toLowerCase());
                }
            } else {
                if(filter.isCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(Channel_.name), filter.getLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Channel_.name)), filter.getLikeSpec().getPattern(filter.getName().toLowerCase()));
                }
            }
        }
        return namePredicate;
    }
}
