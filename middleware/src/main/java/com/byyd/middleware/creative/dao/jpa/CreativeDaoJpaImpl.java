package com.byyd.middleware.creative.dao.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdfonicUser_;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Destination_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative_;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.User_;
import com.byyd.middleware.creative.dao.CreativeDao;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.filter.PublisherAuditedCreativeFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class CreativeDaoJpaImpl extends BusinessKeyDaoJpaImpl<Creative> implements CreativeDao {
    
    protected static final String AD_MANAGEMENT_APPROVED = "APPROVED";
    protected static final String AD_MANAGEMENT_DENIED = "DENIED";

    //---------------------------------------------------------------------------------------------------------------------------------------------

    protected StringBuilder getApprovedCreativesForPublicationQuery(boolean countOnly) {
        return new StringBuilder("SELECT")
            .append(countOnly ? " COUNT(DISTINCT CREATIVE_ID)" : " DISTINCT(CREATIVE_ID) AS ID")
            .append(" FROM AD_MANAGEMENT pac")
            .append(" INNER JOIN CREATIVE ON CREATIVE.ID=pac.CREATIVE_ID")
            .append(" INNER JOIN CAMPAIGN ON CAMPAIGN.ID=CREATIVE.CAMPAIGN_ID")
            .append(" WHERE PUBLICATION_ID=? AND pac.STATUS='" + AD_MANAGEMENT_APPROVED + "'");
    }

    @Override
    public Long countApprovedCreativesForPublication(Publication publication) {
        StringBuilder query = getApprovedCreativesForPublicationQuery(true);
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(publication.getId()));
        Number count = this.executeAggregateFunctionByNativeQueryPositionalParameters(query.toString(), list);
        return count.longValue();
    }

    @Override
    public List<Creative> getApprovedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return this.getApprovedCreativesForPublication(publication, null, null, fetchStrategy);
    }

    @Override
    public List<Creative> getApprovedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
           return this.getApprovedCreativesForPublication(publication, null, sort, fetchStrategy);
    }

    @Override
    public List<Creative> getApprovedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
           return this.getApprovedCreativesForPublication(publication, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<Integer> getAllApprovedCreativesForPublication(Publication publication) {
        StringBuilder query = getApprovedCreativesForPublicationQuery(false);
        return getAllCreativesForPublication(publication, query);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------

    protected StringBuilder getDeniedCreativesForPublicationQuery(boolean countOnly) {
        return new StringBuilder("SELECT")
            .append(countOnly ? " COUNT(DISTINCT CREATIVE_ID)" : " DISTINCT(CREATIVE_ID) AS ID")
            //.append(" FROM PUBLICATION_DENIED_CREATIVE e")
            .append(" FROM AD_MANAGEMENT e")
            .append(" INNER JOIN CREATIVE ON CREATIVE.ID=e.CREATIVE_ID")
            .append(" INNER JOIN CAMPAIGN ON CAMPAIGN.ID=CREATIVE.CAMPAIGN_ID")
            .append(" WHERE e.PUBLICATION_ID=? AND e.STATUS='" + AD_MANAGEMENT_DENIED + "'");
    }

    @Override
    public Long countDeniedCreativesForPublication(Publication publication) {
        StringBuilder query = getDeniedCreativesForPublicationQuery(true);
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(publication.getId()));
        Number count = this.executeAggregateFunctionByNativeQueryPositionalParameters(query.toString(), list);
        return count.longValue();
    }

    @Override
    public List<Creative> getDeniedCreativesForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return this.getDeniedCreativesForPublication(publication, null, null, fetchStrategy);
    }

    @Override
    public List<Creative> getDeniedCreativesForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
           return this.getDeniedCreativesForPublication(publication, null, sort, fetchStrategy);
    }

    @Override
    public List<Creative> getDeniedCreativesForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
           return this.getDeniedCreativesForPublication(publication, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<Integer> getAllDeniedCreativesForPublication(Publication publication) {
        StringBuilder query = getDeniedCreativesForPublicationQuery(false);
        return getAllCreativesForPublication(publication, query);
    }
    
    protected List<Creative> getApprovedCreativesForPublication(Publication publication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder query = getApprovedCreativesForPublicationQuery(false);
        return getCreativesForPublication(query, publication, page, sort, fetchStrategy);
    }
    
    protected List<Creative> getDeniedCreativesForPublication(Publication publication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder query = getDeniedCreativesForPublicationQuery(false);
        return getCreativesForPublication(query, publication, page, sort, fetchStrategy);
    }
    
    @SuppressWarnings("unchecked")
    private List<Creative> getCreativesForPublication(StringBuilder creativeQuery, Publication publication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(publication.getId()));
        if(sort != null) {
            creativeQuery.append(" order by " + sort.toString());
        }
        List<Number> ids = this.findByNativeQueryPositionalParameters(creativeQuery.toString(), page, list);
        List<Creative> creatives = new ArrayList<Creative>();
        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                creatives.add(this.getById(id.longValue(), fetchStrategy));
            }
        }
        return creatives;
    }
    
    @SuppressWarnings("unchecked")
    private List<Integer> getAllCreativesForPublication(Publication publication, StringBuilder query) {
        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(publication.getId()));
        List<Integer> ids = this.findByNativeQueryPositionalParameters(query.toString(), list);
        return ids != null ? ids : new ArrayList<Integer>();
    }


    /**
     * Implemented as natives because to do this through Hibernate, the Publication should have all its approved and denied creatives
     * loaded in its collections, just so one can be moved from one collection to the other, and the tables states synced through a publicatio
     * update. For publications with lots of creatives, this was utterly impractical.
     */
    @Override
    public boolean approveCreativeForPublication(Publication publication, Creative creative) {
        String deleteStmt = "DELETE FROM AD_MANAGEMENT WHERE CREATIVE_ID = " + creative.getId() + " AND PUBLICATION_ID = " + publication.getId() + " AND STATUS='" + AD_MANAGEMENT_DENIED + "'";
        String insertStmt = "INSERT INTO AD_MANAGEMENT(CREATIVE_ID, PUBLICATION_ID, STATUS) VALUES(" + creative.getId() + ", " + publication.getId() + ", '" + AD_MANAGEMENT_APPROVED + "')";
        int noDeleted = this.executeUpdateNativeQuery(deleteStmt).intValue();
        int noInserted = this.executeUpdateNativeQuery(insertStmt).intValue();
        return noDeleted > 0 && noInserted > 0;
    }

    @Override
    public boolean denyCreativeForPublication(Publication publication, Creative creative) {
        String deleteStmt = "DELETE FROM AD_MANAGEMENT WHERE CREATIVE_ID = " + creative.getId() + " AND PUBLICATION_ID = " + publication.getId() + " AND STATUS='" + AD_MANAGEMENT_APPROVED + "'";
        String insertStmt = "INSERT INTO AD_MANAGEMENT(CREATIVE_ID, PUBLICATION_ID, STATUS) VALUES(" + creative.getId() + ", " + publication.getId() + ", '" + AD_MANAGEMENT_DENIED + "')";
        int noDeleted = this.executeUpdateNativeQuery(deleteStmt).intValue();
        int noInserted = this.executeUpdateNativeQuery(insertStmt).intValue();
        return noDeleted > 0 && noInserted > 0;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------

    protected Predicate getCreativesWithNameForCampaignPredicate(String name, Campaign campaign, Creative excludeCreative, Root<Creative> root) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate campaignExpression = criteriaBuilder.equal(root.get(Creative_.campaign), campaign);
        Predicate nameExpression = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Creative_.name)), name.toLowerCase());
        Predicate creativeExpression = null;
        if(excludeCreative != null) {
            creativeExpression =  criteriaBuilder.notEqual(root.get(Creative_.id), excludeCreative.getId());
        }

        return and(campaignExpression, nameExpression, creativeExpression);
    }

    @Override
    public Long countCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Creative> root = criteriaQuery.from(Creative.class);

        Predicate predicate = getCreativesWithNameForCampaignPredicate(name, campaign, excludeCreative, root);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, FetchStrategy... fetchStrategy) {
        return getCreativesWithNameForCampaign(name, campaign, excludeCreative, null, null, fetchStrategy);
    }

    @Override
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Sorting sort, FetchStrategy... fetchStrategy) {
        return getCreativesWithNameForCampaign(name, campaign, excludeCreative, null, sort, fetchStrategy);
    }

    @Override
    public List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Pagination page, FetchStrategy... fetchStrategy) {
        return getCreativesWithNameForCampaign(name, campaign, excludeCreative, page, page.getSorting(), fetchStrategy);
    }

    protected List<Creative> getCreativesWithNameForCampaign(String name, Campaign campaign, Creative excludeCreative, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Creative> criteriaQuery = container.getQuery();
        Root<Creative> root = container.getRoot();

        Predicate predicate = getCreativesWithNameForCampaignPredicate(name, campaign, excludeCreative, root);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    protected Predicate getPredicate(Root<Creative> root, CreativeFilter filter, Map<String, Join> joins) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        Predicate campaignPredicate = getCampaignPredicate(root, filter, criteriaBuilder);
        Predicate creativePredicate = getCreativePredicate(root, filter, criteriaBuilder);
        Predicate publisherAuditedPredicate = getPublisherAuditedPredicate(root, filter, criteriaBuilder, joins);
        
        return and(campaignPredicate, creativePredicate, publisherAuditedPredicate);
    }

    private Predicate getCreativePredicate(Root<Creative> root, CreativeFilter filter, CriteriaBuilder criteriaBuilder){
        Predicate statusesPredicate = null;
        Predicate containsNamePredicate = null;
        Predicate namePredicate = null;
        Predicate includedIdsPredicate = null;
        Predicate excludedIdsPredicate = null;
        Predicate assignedToFullNameContainsPredicate = null;
        Predicate destinationContainsPredicate = null;
        Predicate countryNameContainsPredicate = null;
        Predicate countryTargetingGlobalPredicate = null;
        Predicate externalIdContainsPredicate = null;
        
        if (CollectionUtils.isNotEmpty(filter.getStatuses())) {
            statusesPredicate = root.get(Creative_.status).in(filter.getStatuses());
        }

        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(Creative_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Creative_.name)), filter.getName().toLowerCase());
            }
        }
            
        if (filter.getContainsName() != null) {
            if (filter.isContainsNameCaseSensitive()) {
                containsNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.name)), "%" + filter.getContainsName().toLowerCase() + "%");
            } else {
                containsNamePredicate = criteriaBuilder.like(root.get(Creative_.name), "%" + filter.getContainsName() + "%");
            }
        }        

        if (CollectionUtils.isNotEmpty(filter.getExcludedIds())) {
            excludedIdsPredicate = criteriaBuilder.not(root.get(Creative_.id).in(filter.getExcludedIds()));
        }

        if (CollectionUtils.isNotEmpty(filter.getIncludedIds())) {
            includedIdsPredicate = root.get(Creative_.id).in(filter.getIncludedIds());
        }
        
        if (filter.getAssignedToFullNameContains() != null) {
            assignedToFullNameContainsPredicate = criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.assignedTo).get(AdfonicUser_.firstName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"), criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.assignedTo).get(AdfonicUser_.lastName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"));
        }

        if (filter.getDestinationContains() != null) {
            destinationContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.destination).get(Destination_.data)), "%" + filter.getDestinationContains().toLowerCase() + "%");
        }

        if (filter.getCountryNameContains() != null) {
            countryNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.join(Creative_.segment).join(Segment_.countries).get(Country_.name)), "%" + filter.getCountryNameContains() + "%");
        }

        if (filter.getCountryTargetingGlobal() != null) {
            if (filter.getCountryTargetingGlobal().booleanValue()) {
                countryTargetingGlobalPredicate = criteriaBuilder.isEmpty(root.join(Creative_.segment).get(Segment_.countries));
            } else {
                countryTargetingGlobalPredicate = criteriaBuilder.isNotEmpty(root.join(Creative_.segment).get(Segment_.countries));
            }
        }

        if (filter.getExternalIdContains() != null) {
            externalIdContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.externalID)), "%" + filter.getExternalIdContains().toLowerCase() + "%");
        }

        return and(statusesPredicate, containsNamePredicate, namePredicate, excludedIdsPredicate, 
                   includedIdsPredicate, assignedToFullNameContainsPredicate, destinationContainsPredicate, 
                   countryNameContainsPredicate, countryTargetingGlobalPredicate, externalIdContainsPredicate);
        
    }
    
    private Predicate getCampaignPredicate(Root<Creative> root, CreativeFilter filter, CriteriaBuilder criteriaBuilder){
        
        Predicate campaignPredicate = null;
        Predicate campaignIdsPredicate = null;
        Predicate companyNameContainsPredicate = null;
        Predicate advertisersPredicate = null;
        Predicate advertiserIsKeyPredicate = null;
        Predicate accountManagerEmailContainsPredicate = null;
        Predicate campaignNameContainsPredicate = null;
        Predicate campaignAdvertiserDomainContainsPredicate = null;
        
        if (filter.getCampaign() != null) {
            campaignPredicate = criteriaBuilder.equal(root.get(Creative_.campaign), filter.getCampaign());
        }

        if (filter.getCampaignIds() != null) {
            campaignIdsPredicate = root.get(Creative_.campaign).get(Campaign_.id).in(filter.getCampaignIds());
        }
        
        if (filter.getCompanyNameContains() != null) {
            companyNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.campaign).get(Campaign_.advertiser).get(Advertiser_.company).get(Company_.name)), "%" + filter.getCompanyNameContains().toLowerCase() + "%");
        }

        if (filter.getAdvertiserIsKey() != null) {
            advertiserIsKeyPredicate = criteriaBuilder.equal(root.get(Creative_.campaign).get(Campaign_.advertiser).get(Advertiser_.key), filter.getAdvertiserIsKey());
        }
        
        if(filter.isFilterByAdvertisers()){
            advertisersPredicate = root.get(Creative_.campaign).get(Campaign_.advertiser).in(filter.getAdvertisers());
        }
        
        if (filter.getAccountManagerEmailContains() != null) {
            accountManagerEmailContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.campaign).get(Campaign_.advertiser).get(Advertiser_.company).get(Company_.accountManager).get(User_.email)), "%" + filter.getAccountManagerEmailContains().toLowerCase() + "%");
        }
        
        if (filter.getCampaignNameContains() != null) {
            campaignNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.campaign).get(Campaign_.name)), "%" + filter.getCampaignNameContains().toLowerCase() + "%");
        }
        
        if (filter.getCampaignAdvertiserDomainContains() != null) {
            campaignAdvertiserDomainContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Creative_.campaign).get(Campaign_.advertiserDomain)), "%" + filter.getCampaignAdvertiserDomainContains().toLowerCase() + "%");
        }
        
        return and(campaignPredicate, campaignIdsPredicate, companyNameContainsPredicate, 
                   advertiserIsKeyPredicate, advertisersPredicate, accountManagerEmailContainsPredicate, 
                   campaignNameContainsPredicate, campaignAdvertiserDomainContainsPredicate);
    }
    
    private Predicate getPublisherAuditedPredicate(Root<Creative> root, CreativeFilter filter, CriteriaBuilder criteriaBuilder, Map<String, Join> joins) {
        Predicate publisherAuditedPredicate = null;
        
        if (filter.getPublisherAuditedCreativeFilters()!=null){
            for (PublisherAuditedCreativeFilter publisherAuditedCreativeFilter : filter.getPublisherAuditedCreativeFilters()){
                Join<Creative, PublisherAuditedCreative> publisherAuditedCreativeJoin = root.join(Creative_.publishersAuditedCreative, JoinType.LEFT);
                
                // Publisher predicate (IN and NOT IN)
                Predicate publisherInPredicate = null;
                if (publisherAuditedCreativeFilter.getPublishers()!=null){
                    publisherInPredicate = publisherAuditedCreativeJoin.get(PublisherAuditedCreative_.publisher).in(publisherAuditedCreativeFilter.getPublishers());
                }
                
                // Status predicate (IN and NOT IN)
                Predicate statusesInPredicate = null;
                if (publisherAuditedCreativeFilter.getStatuses()!=null){
                    statusesInPredicate = publisherAuditedCreativeJoin.get(PublisherAuditedCreative_.status).in(publisherAuditedCreativeFilter.getStatuses());
                    if (publisherAuditedCreativeFilter.getExcludeStatuses()){
                        statusesInPredicate = criteriaBuilder.not(statusesInPredicate);
                    }
                }
                
                // Retrieve also null values
                Predicate publisherNullPredicate = null;
                Predicate statusesNullPredicate = null;
                if (publisherAuditedCreativeFilter.getRetrieveNullValuesAlso()){
                    publisherNullPredicate = publisherAuditedCreativeJoin.get(PublisherAuditedCreative_.publisher).isNull();
                    statusesNullPredicate = publisherAuditedCreativeJoin.get(PublisherAuditedCreative_.status).isNull();
                }
                
                // (adx.STATUS IN ('PENDING') OR adx.STATUS IS NULL) 
                Predicate publisherPredicate = or(publisherInPredicate, publisherNullPredicate);
                Predicate statusesPredicate = or(statusesInPredicate, statusesNullPredicate);
                
                publisherAuditedPredicate = and(publisherAuditedPredicate, and(publisherPredicate, statusesPredicate));
                
                // To be removed
                joins.put(publisherAuditedCreativeFilter.getSortingAlias(), publisherAuditedCreativeJoin);
            }
        }
        
        return publisherAuditedPredicate;
    }

    @Override
    public Long countAll(CreativeFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Creative> root = criteriaQuery.from(Creative.class);
        Map<String, Join> joins = new HashMap<>();

        Predicate predicate = getPredicate(root, filter, joins);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }
    
    @Override
    public List<Creative> getAll(CreativeFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Creative> getAll(CreativeFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Creative> getAll(CreativeFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<Creative> getAll(CreativeFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Creative> criteriaQuery = container.getQuery();
        Root<Creative> root = container.getRoot();
        Map<String, Join> joins = new HashMap<>();
        
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter, joins);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root).distinct(true);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort, joins);
        return findAll(criteriaQuery, page);
    }
    
}
