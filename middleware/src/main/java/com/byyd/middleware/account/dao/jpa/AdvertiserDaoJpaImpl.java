package com.byyd.middleware.account.dao.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company;
import com.byyd.middleware.account.dao.AdvertiserDao;
import com.byyd.middleware.account.filter.AdvertiserFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class AdvertiserDaoJpaImpl extends BusinessKeyDaoJpaImpl<Advertiser> implements AdvertiserDao {

    @Override
    public Long countAllForCompany(Company company) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Advertiser> root = criteriaQuery.from(Advertiser.class);

        Predicate companyExpression = criteriaBuilder.equal(root.get(Advertiser_.company), company);
        criteriaQuery = criteriaQuery.where(companyExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Advertiser> findAllByCompany(Company company, FetchStrategy... fetchStrategy) {
        return findAllByCompany(company, null, null, fetchStrategy);
    }

    @Override
    public List<Advertiser> findAllByCompany(Company company, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAllByCompany(company, null, sort, fetchStrategy);
    }

    @Override
    public List<Advertiser> findAllByCompany(Company company, Pagination page, FetchStrategy... fetchStrategy) {
        return findAllByCompany(company, page, page.getSorting(), fetchStrategy);
    }

    protected List<Advertiser> findAllByCompany(Company company, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Advertiser> criteriaQuery = container.getQuery();
        Root<Advertiser> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate companyExpression = criteriaBuilder.equal(root.get(Advertiser_.company), company);
        criteriaQuery = criteriaQuery.where(companyExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //----------------------------------------------------------------------------------------------------------------------------

    protected Predicate getPredicate(Root<Advertiser> root, AdvertiserFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate advertiserIdsPredicate = null;
        Predicate namePredicate = null;
        Predicate companyPredicate = null;
        Predicate statusesPredicate = null;
        Predicate userPredicate = null;

        if (CollectionUtils.isNotEmpty(filter.getAdvertiserIds())) {
            advertiserIdsPredicate = root.get(Advertiser_.id).in(filter.getAdvertiserIds());
        }

        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(Advertiser_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Advertiser_.name)), filter.getName().toLowerCase());
            }
        }

        if (filter.getContainsName() != null) {
            Predicate containsNamePredicate = null;
            Predicate nameWithPreviousSpacePredicate = null;
            LikeSpec likeSpec = filter.getContainsNameLikeSpec();
            if(likeSpec == null) {
                likeSpec = LikeSpec.CONTAINS;
            }
            if (filter.isNameCaseSensitive()) {
                containsNamePredicate = criteriaBuilder.like(root.get(Advertiser_.name), likeSpec.getPattern(filter.getContainsName()));
            } else {
                containsNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Advertiser_.name)), likeSpec.getPattern(filter.getContainsName().toLowerCase()));
            }
            if(filter.isNameWithPreviousSpace()){
                if (filter.isNameCaseSensitive()) {
                    nameWithPreviousSpacePredicate = criteriaBuilder.like(root.get(Advertiser_.name), "% " + filter.getContainsName()+"%");
                } else {
                    nameWithPreviousSpacePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Advertiser_.name)), "% " +  filter.getContainsName().toLowerCase()+"%");
                }
                namePredicate = or(containsNamePredicate,nameWithPreviousSpacePredicate);
            }else{
                namePredicate = containsNamePredicate;
            }
        }        

        if (filter.getCompany() != null) {
            companyPredicate = criteriaBuilder.equal(root.get(Advertiser_.company), filter.getCompany());
        }

        if (CollectionUtils.isNotEmpty(filter.getStatuses())) {
            statusesPredicate = root.get(Advertiser_.status).in(filter.getStatuses());
        }

        if (filter.getUser() != null) {
            userPredicate = criteriaBuilder.isMember(filter.getUser(), root.get(Advertiser_.users));

        }

        return and(advertiserIdsPredicate, namePredicate, companyPredicate, statusesPredicate, userPredicate);
    }


    @Override
    public Long countAll(AdvertiserFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Advertiser> root = criteriaQuery.from(Advertiser.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Advertiser> findAll(AdvertiserFilter filter, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Advertiser> findAll(AdvertiserFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Advertiser> findAll(AdvertiserFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return findAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<Advertiser> findAll(AdvertiserFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Advertiser> criteriaQuery = container.getQuery();
        Root<Advertiser> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //----------------------------------------------------------------------------------------------------------------------------

    @Override
    public Advertiser getByName(String name, Company company, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Advertiser> criteriaQuery = container.getQuery();
        Root<Advertiser> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = criteriaBuilder.equal(root.get(Advertiser_.name), name);
        Predicate companyPredicate = criteriaBuilder.equal(root.get(Advertiser_.company), company);
        criteriaQuery = criteriaQuery.where(and(companyPredicate, namePredicate));
        CriteriaQuery<Advertiser> select = criteriaQuery.select(root);

        return find(select);
    }

    //----------------------------------------------------------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Map<Long, BigDecimal>> getSpendAndBalanceForAdvertisersAndDateIds(List<Advertiser> advertisers, int startDateId, int endDateId) {
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        StringBuilder bld = new StringBuilder()
            .append("SELECT ADVERTISER.ID, SUM(ADVERTISER_DAILY_SPEND.AMOUNT), ACCOUNT.BALANCE")
            .append(" FROM ADVERTISER")
            .append(" INNER JOIN ADVERTISER_DAILY_SPEND ON ADVERTISER_DAILY_SPEND.ADVERTISER_ID=ADVERTISER.ID")
            .append(" INNER JOIN ACCOUNT ON ACCOUNT.ID=ADVERTISER.ACCOUNT_ID")
            .append(" WHERE ADVERTISER.ID IN (");
        for (int k = 0; k < advertisers.size(); ++k) {
            if (k > 0) {
                bld.append(',');
            }
            bld.append("?");
            params.add(new QueryParameter(advertisers.get(k).getId()));
        }
        bld.append(") AND ADVERTISER_DAILY_SPEND.DATE_ID BETWEEN ? AND ? GROUP BY ADVERTISER.ID");
        params.add(new QueryParameter(startDateId));
        params.add(new QueryParameter(endDateId));

        Map<Long,BigDecimal> spendByAdvertiserId = new HashMap<Long,BigDecimal>();
        Map<Long,BigDecimal> balanceByAdvertiserId = new HashMap<Long,BigDecimal>();
        List<Object[]> rows = this.findByNativeQueryPositionalParameters(bld.toString(), params);
        for(Object[] row : rows) {
            Number advertiserId = (Number)row[0];
            Number spendAmount = (Number)row[1];
            Number balance = (Number)row[2];

            spendByAdvertiserId.put(advertiserId.longValue(), new BigDecimal(spendAmount.doubleValue()));
            balanceByAdvertiserId.put(advertiserId.longValue(), new BigDecimal(balance.doubleValue()));
        }

        Map<String, Map<Long, BigDecimal>> map = new HashMap<String, Map<Long, BigDecimal>>();
        map.put(SPEND_BY_ADVERTISER_ID, spendByAdvertiserId);
        map.put(BALANCE_BY_ADVERTISER_ID, balanceByAdvertiserId);
        return map;
    }
 }
