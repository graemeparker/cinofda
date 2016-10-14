package com.byyd.middleware.account.dao.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.CompanyMessage;
import com.adfonic.domain.CompanyMessage_;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.account.dao.CompanyMessageDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CompanyMessageDaoJpaImpl extends BusinessKeyDaoJpaImpl<CompanyMessage> implements CompanyMessageDao {

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, null, null, fetchStrategy);
    }

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, null, sort, fetchStrategy);
    }

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForAdvertiser(advertiser, systemNames, page, page.getSorting(), fetchStrategy);
    }

    protected List<CompanyMessage> getCompanyMessagesWithSystemNamesForAdvertiser(Advertiser advertiser, Collection<String> systemNames, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CompanyMessage> criteriaQuery = container.getQuery();
        Root<CompanyMessage> root = container.getRoot();
        criteriaQuery = criteriaQuery.where(getCompanyMessagesWithSystemNamesForAdvertiserPredicate(advertiser, systemNames, root));
        criteriaQuery = criteriaQuery.select(root);
        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, null, null, fetchStrategy);
    }

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, null, sort, fetchStrategy);
    }

    @Override
    public List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return getCompanyMessagesWithSystemNamesForPublisher(publisher, systemNames, page, page.getSorting(), fetchStrategy);
    }

    protected List<CompanyMessage> getCompanyMessagesWithSystemNamesForPublisher(Publisher publisher, Collection<String> systemNames, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CompanyMessage> criteriaQuery = container.getQuery();
        Root<CompanyMessage> root = container.getRoot();
        criteriaQuery = criteriaQuery.where(getCompanyMessagesWithSystemNamesForPublisherPredicate(publisher, systemNames, root));
        criteriaQuery = criteriaQuery.select(root);
        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public Long countCompanyMessagesWithSystemNamesForAdvertiser(
            Advertiser advertiser, Collection<String> systemNames) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CompanyMessage> root = criteriaQuery.from(CompanyMessage.class);
        criteriaQuery = criteriaQuery.where(getCompanyMessagesWithSystemNamesForAdvertiserPredicate(advertiser, systemNames, root));
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public Long countCompanyMessagesWithSystemNamesForPublisher(
            Publisher publisher, Collection<String> systemNames) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CompanyMessage> root = criteriaQuery.from(CompanyMessage.class);

        criteriaQuery = criteriaQuery.where(getCompanyMessagesWithSystemNamesForPublisherPredicate(publisher, systemNames, root));
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));
        return executeLongAggregateFunction(criteriaQuery);
    }

    protected Predicate getCompanyMessagesWithSystemNamesForAdvertiserPredicate(Advertiser advertiser, Collection<String> systemNames, Root<CompanyMessage> root) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate advertiserPredicate = null;
        Predicate systemNamesPredicate = null;

        if(advertiser != null) {
            advertiserPredicate = criteriaBuilder.equal(root.get(CompanyMessage_.advertiser), advertiser);
        }

        if (CollectionUtils.isNotEmpty(systemNames)) {
            systemNamesPredicate = root.get(CompanyMessage_.systemName).in(systemNames);
        }

        return and(advertiserPredicate, systemNamesPredicate);
    }

    protected Predicate getCompanyMessagesWithSystemNamesForPublisherPredicate(Publisher publisher, Collection<String> systemNames, Root<CompanyMessage> root) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherPredicate = null;
        Predicate systemNamesPredicate = null;

        if(publisher != null) {
            publisherPredicate = criteriaBuilder.equal(root.get(CompanyMessage_.publisher), publisher);
        }

        if (CollectionUtils.isNotEmpty(systemNames)) {
            systemNamesPredicate = root.get(CompanyMessage_.systemName).in(systemNames);
        }
        return and(publisherPredicate, systemNamesPredicate);
    }
}
