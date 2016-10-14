package com.byyd.middleware.campaign.dao.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Audience;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Publisher;
import com.adfonic.util.Range;
import com.byyd.middleware.campaign.dao.CampaignDao;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class CampaignDaoJpaImpl extends BusinessKeyDaoJpaImpl<Campaign> implements CampaignDao {

    protected Predicate getPredicate(Root<Campaign> root, CampaignFilter filter) {
        Advertiser advertiser = filter.getAdvertiser();
        Range<Date> dateRangeForActive = filter.getDateRangeForActive();
        Boolean houseAds = filter.getHouseAds();

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate campaignIdsPredicate = null;
        Predicate advertiserPredicate = null;
        Predicate statusesPredicate = null;
        Predicate dateRangeForActivePredicate = null;
        Predicate houseAdsPredicate = null;
        Predicate namePredicate = null;
        Predicate excludedIdsPredicate = null;
        Predicate publicationListPredicate = null;

        if (CollectionUtils.isNotEmpty(filter.getCampaignIds())) {
            campaignIdsPredicate = root.get(Campaign_.id).in(filter.getCampaignIds());
        }
        if(advertiser != null) {
            advertiserPredicate = criteriaBuilder.equal(root.get(Campaign_.advertiser), advertiser);
        }
        if (CollectionUtils.isNotEmpty(filter.getStatuses())) {
            statusesPredicate = root.get(Campaign_.status).in(filter.getStatuses());
        }
        if(houseAds != null) {
            if(houseAds) {
                houseAdsPredicate = criteriaBuilder.isTrue(root.get(Campaign_.houseAd));
            } else {
                   houseAdsPredicate = criteriaBuilder.isFalse(root.get(Campaign_.houseAd));
            }
        }
        if(dateRangeForActive != null) {
            Predicate startDatePredicate = null;
            Predicate endDatePredicate = null;

            if (dateRangeForActive.getEnd() != null) {
                startDatePredicate = or(
                            criteriaBuilder.isNull(root.get(Campaign_.startDate)),
                            criteriaBuilder.lessThan(root.get(Campaign_.startDate), dateRangeForActive.getEnd())
                        );
            }else {
                startDatePredicate = criteriaBuilder.isNull(root.get(Campaign_.startDate));
            }

            if (dateRangeForActive.getStart() != null) {
                endDatePredicate = or(
                        criteriaBuilder.isNull(root.get(Campaign_.endDate)),
                        criteriaBuilder.greaterThan(root.get(Campaign_.endDate), dateRangeForActive.getStart())
                    );
            }else {
                endDatePredicate = criteriaBuilder.isNull(root.get(Campaign_.endDate));
            }
            dateRangeForActivePredicate = and(startDatePredicate, endDatePredicate);
        }

        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(Campaign_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Campaign_.name)), filter.getName().toLowerCase());
            }
        }
        if (filter.getContainsName() != null) {
            Predicate startsNamePredicate = null;
            Predicate containsNamePredicate = null;
            if (filter.isNameCaseSensitive()) {
                startsNamePredicate = criteriaBuilder.like(root.get(Campaign_.name), filter.getContainsName()+"%");
            } else {
                startsNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Campaign_.name)), filter.getContainsName().toLowerCase()+"%");
            }
            if(filter.isNameWithPreviousSpace()){
                if (filter.isNameCaseSensitive()) {
                    containsNamePredicate = criteriaBuilder.like(root.get(Campaign_.name), "% " + filter.getContainsName()+"%");
                } else {
                    containsNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Campaign_.name)), "% " +  filter.getContainsName().toLowerCase()+"%");
                }
                namePredicate = or(startsNamePredicate,containsNamePredicate);
            }else{
                namePredicate = startsNamePredicate;
            }
        }        

        if (CollectionUtils.isNotEmpty(filter.getExcludedIds())) {
            excludedIdsPredicate = criteriaBuilder.not(root.get(Campaign_.id).in(filter.getExcludedIds()));
        }
        
        if(filter.getPublicationList() != null) {
            publicationListPredicate = criteriaBuilder.equal(root.get(Campaign_.publicationList), filter.getPublicationList());
         }

        return and(campaignIdsPredicate, advertiserPredicate, statusesPredicate, dateRangeForActivePredicate, houseAdsPredicate, namePredicate, excludedIdsPredicate, publicationListPredicate);
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Long countAll(CampaignFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Campaign> root = criteriaQuery.from(Campaign.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Campaign> getAll(CampaignFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Campaign> getAll(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Campaign> getAll(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<Campaign> getAll(CampaignFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Campaign> criteriaQuery = container.getQuery();
        Root<Campaign> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
     }

    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, FetchStrategy... fetchStrategy) {
        return getAllUsingTwoPhaseLoad(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllUsingTwoPhaseLoad(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllUsingTwoPhaseLoad(filter, page, page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<Campaign> getAllUsingTwoPhaseLoad(CampaignFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Campaign> root = criteriaQuery.from(Campaign.class);
        criteriaQuery.where(getPredicate(root, filter));
        criteriaQuery = criteriaQuery.select((Expression)root.get("id"));
        criteriaQuery.distinct(true);
        criteriaQuery = processOrderByForObjects(criteriaQuery, root, sort);
        List<Long> ids = findAllObjects(criteriaQuery, page);

        List<Campaign> list = new ArrayList<Campaign>();
        for(Long id : ids) {
            list.add(this.getById(id, fetchStrategy));
        }
        return list;
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    /**
     * JDO version:
     *
     *  Query q = getPersistenceManager().newQuery(Campaign.class, "advertiser.company == p1 && houseAd == true");
     *  q.declareParameters("com.adfonic.domain.Company p1");
     *  q.setResult("count(this)");
     *  return (Long)q.execute(publisher.getCompany());
     *
     * Generated SQL from JDO version (using Publisher 1/Company 1):
     *
     * SELECT COUNT(`THIS`.`ID`)
     *    FROM `CAMPAIGN` `THIS`
     *    LEFT OUTER JOIN `ADVERTISER` `THIS_ADVERTISER_COMPANY` ON `THIS`.`ADVERTISER_ID` = `THIS_ADVERTISER_COMPANY`.`ID`
     *    WHERE <1> = `THIS_ADVERTISER_COMPANY`.`COMPANY_ID`
     *    AND `THIS`.`HOUSE_AD` = 1
     *
     * Generated SQL from the JPA version:
     *
     *    select
     *            count(campaign0_.ID) as col_0_0_
     *        from
     *            CAMPAIGN campaign0_
     *        left outer join
     *            ADVERTISER advertiser1_
     *                on campaign0_.ADVERTISER_ID=advertiser1_.ID
     *        where
     *            advertiser1_.COMPANY_ID=?
     *            and campaign0_.HOUSE_AD=? limit ?
     *
     * @param publisher
     * @return
     */
    @Override
    public Long getHouseAdCountForPublisher(Publisher publisher) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Campaign> root = criteriaQuery.from(Campaign.class);

        Join<Campaign, Advertiser> advertiserJoin = root.join(Campaign_.advertiser, JoinType.LEFT);

        Predicate companyPredicate = criteriaBuilder.equal(advertiserJoin.get(Advertiser_.company), publisher.getCompany());
        Predicate houseAdPredicate = criteriaBuilder.equal(root.get(Campaign_.houseAd), true);
        Predicate predicate = and(companyPredicate, houseAdPredicate);

        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------

    protected Predicate getCampaignWithNameForAdvertiserPredicate(String name, Advertiser advertiser, Campaign excludeCampaign, Root<Campaign> root) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate advertiserExpression = criteriaBuilder.equal(root.get(Campaign_.advertiser), advertiser);
        Predicate nameExpression = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Campaign_.name)), name.toLowerCase());
        Predicate campaignExpression = null;
        if(excludeCampaign != null) {
            campaignExpression =  criteriaBuilder.notEqual(root.get(Campaign_.id), excludeCampaign.getId());
        }

        Predicate predicate = and(advertiserExpression, nameExpression);
        if(campaignExpression != null) {
            predicate = and(predicate, campaignExpression);
        }
        return predicate;
    }

    @Override
    public Long countCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Campaign> root = criteriaQuery.from(Campaign.class);

        Predicate predicate = getCampaignWithNameForAdvertiserPredicate(name, advertiser, excludeCampaign, root);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, FetchStrategy... fetchStrategy) {
        return getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, null, null, fetchStrategy);
    }

    @Override
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Sorting sort, FetchStrategy... fetchStrategy) {
        return getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, null, sort, fetchStrategy);
    }

    @Override
    public List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Pagination page, FetchStrategy... fetchStrategy) {
        return getCampaignWithNameForAdvertiser(name, advertiser, excludeCampaign, page, page.getSorting(), fetchStrategy);
    }

    protected List<Campaign> getCampaignWithNameForAdvertiser(String name, Advertiser advertiser, Campaign excludeCampaign, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Campaign> criteriaQuery = container.getQuery();
        Root<Campaign> root = container.getRoot();

        Predicate predicate = getCampaignWithNameForAdvertiserPredicate(name, advertiser, excludeCampaign, root);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public BigDecimal getTotalDailySpendForCampaign(Campaign campaign, int fromTimeId, int toTimeId) {
        String stmt = "SELECT SUM(AMOUNT) AS sum from CAMPAIGN_DAILY_SPEND WHERE CAMPAIGN_ID = ? AND DATE_ID >= ? AND DATE_ID <= ?";
        List<QueryParameter> params = new ArrayList<>();
        params.add(new QueryParameter(campaign.getId()));
        params.add(new QueryParameter(fromTimeId));
        params.add(new QueryParameter(toTimeId));
        Number sum = executeAggregateFunctionByNativeQueryPositionalParameters(stmt, params);
        if(sum == null) {
            return BigDecimal.ZERO;
        }
        return (BigDecimal)sum;
    }
    
    //---------------------------------------------------------------------------------------------------------------------------------------------

    protected StringBuilder getCampaignsLinkedToAudienceQuery(boolean countOnly) {
           // Remember, to be able to use sorting on both the native and the later
        // Criteria query that gets the actual entities out, you must alias each table queried
        // using the name of the class that maps it. So, to sort by fields on DMP_SELECTOR, 
        // we must join with it and alias it to DMPSelector 
        return new StringBuilder("SELECT")
                                .append(countOnly ? " COUNT(DISTINCT Campaign.ID)" : " DISTINCT(Campaign.ID)")
                                .append(" FROM CAMPAIGN Campaign, CAMPAIGN_AUDIENCE CampaignAudience")
                                .append(" WHERE Campaign.ID = CampaignAudience.CAMPAIGN_ID AND")
                                .append(" CampaignAudience.AUDIENCE_ID=? AND CampaignAudience.DELETED=0");

    }
    
    @Override
    public Long countCampaignsLinkedToAudience(Audience audience) {
        if(audience == null) {
            return 0L;
        }
        StringBuilder sb = getCampaignsLinkedToAudienceQuery(true);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(audience.getId()));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(sb.toString(), params).longValue();
    }

    @Override
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, FetchStrategy... fetchStrategy) {
        return this.getCampaignsLinkedToAudience(audience, null, null, fetchStrategy);
    }
    
    @Override
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getCampaignsLinkedToAudience(audience, null, sort, fetchStrategy);
    }

    @Override
    public List<Campaign> getCampaignsLinkedToAudience(Audience audience, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getCampaignsLinkedToAudience(audience, page, page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings("unchecked")
    protected List<Campaign> getCampaignsLinkedToAudience(Audience audience, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        if(audience == null) {
            return new ArrayList<Campaign>();
        }
        StringBuilder sb = getCampaignsLinkedToAudienceQuery(false);
        if(sort != null && !sort.isEmpty()) {
            sb.append(" ORDER BY " + sort.toString(true));
        }
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(audience.getId()));
        List<Long> ids = null;
        if(page != null) {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), page.getOffet(), page.getLimit(), params);
        } else {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), params);
        }
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<Campaign>();
        } else {
            return this.getObjectsByIds(ids, sort, fetchStrategy);
        }
    }

    @Override
    public void deleteDailyAndOverallSpend(Campaign campaign){
        String deleteDailySpendStmt = "DELETE from CAMPAIGN_DAILY_SPEND where CAMPAIGN_ID = " + campaign.getId();
        this.executeUpdateNativeQuery(deleteDailySpendStmt);
        String deleteOverallSpendStmt = "DELETE from CAMPAIGN_OVERALL_SPEND where CAMPAIGN_ID = " + campaign.getId();
        this.executeUpdateNativeQuery(deleteOverallSpendStmt);
    }
}
