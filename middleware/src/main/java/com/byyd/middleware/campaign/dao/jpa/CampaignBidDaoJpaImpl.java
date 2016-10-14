package com.byyd.middleware.campaign.dao.jpa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdAction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignBid_;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Country;
import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.Vendor;
import com.byyd.middleware.campaign.dao.CampaignBidDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignBidDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignBid> implements CampaignBidDao {

    private static final Logger LOG = Logger.getLogger(CampaignBidDaoJpaImpl.class.getName());

    @Override
    public List<BigDecimal> getReferenceBids(Campaign campaign, BidType bidType) {
        CriteriaQuery<CampaignBid> criteriaQuery = buildCriteriaQuery(campaign);
        List<CampaignBid> list = this.findAll(criteriaQuery);

        return getSaneBids(list, bidType);
    }

    private List<BigDecimal> getSaneBids(List<CampaignBid> list, BidType bidType) {
        BigDecimal bidTypeQuantity = new BigDecimal(bidType.getQuantity());
        BigDecimal bidTypeConv = bidType.getAdAction().getEstimatedConversionRate();

        List<BigDecimal> saneBids = new ArrayList<BigDecimal>();

        for (CampaignBid bid : list) {
            BigDecimal amount = bid.getAmount();
            BidType rowBidType = bid.getBidType();
            if (!bidType.equals(rowBidType)) {
                BigDecimal btQuantity = new BigDecimal(rowBidType.getQuantity());
                AdAction adAction = rowBidType.getAdAction();
                BigDecimal btConv = adAction.getEstimatedConversionRate();

                // Convert values from other quantities based on estimated rate
                amount = amount
                    .multiply(btConv)
                    .divide(bidTypeConv, RoundingMode.HALF_UP)
                    .divide(btQuantity, RoundingMode.HALF_UP)
                    .multiply(bidTypeQuantity)
                    .setScale(2, RoundingMode.HALF_UP);
            }
            saneBids.add(amount);
        }
        LOG.fine("saneBids.size=" + saneBids.size());
        Collections.sort(saneBids);
        return saneBids;
    }

    private CriteriaQuery<CampaignBid> buildCriteriaQuery(Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<CampaignBid> criteriaQuery = criteriaBuilder.createQuery(CampaignBid.class);
        Root<CampaignBid> root = criteriaQuery.from(CampaignBid.class);

        Join<CampaignBid, Campaign> campaignJoin = root.join(CampaignBid_.campaign);
        
        // Start with campaigns that
        // 1. are SCHEDULED, ACTIVE, or PAUSED,
        Predicate campaignStatusPredicate = buildCampaignStatusPredicate(criteriaBuilder, campaignJoin);

        // 2. and will have started by our end date (if we have one):
        //   -- however, for SCHEDULED/ACTIVE/PAUSED, startDate will not be null
        Predicate startDatePredicate = buildStartDatePredicate(campaign, criteriaBuilder, root);

        // 3. and will not have ended by our start date (if we have one):

        Predicate endDatePredicate = buildEndDatePredicate(campaign, criteriaBuilder, root);

        // 4. and aren't this same campaign
        Predicate notSameCampaignPredicate = criteriaBuilder.notEqual(root.get(CampaignBid_.campaign), campaign);

        // 5. and are targeting at least one of the same networks
        Predicate transparentNetworksPredicate = buildTransparentNetworksPredicate(campaign, criteriaBuilder, campaignJoin);

        Join<Campaign, Segment> segmentJoin = null;
        Segment cs = campaign.getSegments().get(0);

        // No countries specified or countries has overlap
        Predicate segmentCountriesPredicate = null;
        if (!cs.getCountries().isEmpty()) {
            if(segmentJoin == null) {
                segmentJoin = campaignJoin.join(Campaign_.segments, JoinType.LEFT);
            }
            segmentCountriesPredicate = buildSegmentCountriesPredicate(criteriaBuilder, segmentJoin, cs);
        }

        // No operators specified or operators has overlap
        Predicate segmentOperatorsPredicate = null;
        if (!cs.getOperators().isEmpty()) {
            if(segmentJoin == null) {
                segmentJoin = campaignJoin.join(Campaign_.segments, JoinType.LEFT);
            }
            segmentCountriesPredicate = buildSegmentOperatorsPredicate(criteriaBuilder, segmentJoin, cs);
        }

        Predicate segmentModelsVendorsPredicate = null;
        if (!cs.getModels().isEmpty() || !cs.getVendors().isEmpty()) {
            // For each model, find campaigns whose segments targets that model, or that model's vendor
            if(segmentJoin == null) {
                segmentJoin = campaignJoin.join(Campaign_.segments, JoinType.LEFT);
            }
            segmentModelsVendorsPredicate = buildSegmentModelsVendorsPredicate(cs, criteriaBuilder, segmentJoin);
        }
        
        // If 100% of one gender specified, exclude 100% of opposite gender
        Predicate segmentGenderMixPredicate = buildSegmentGenderMixPredicate(criteriaBuilder, campaignJoin, segmentJoin, cs);

        Predicate predicate = and(
                    campaignStatusPredicate,
                    startDatePredicate,
                    endDatePredicate,
                    notSameCampaignPredicate,
                    transparentNetworksPredicate,
                    segmentCountriesPredicate,
                    segmentOperatorsPredicate,
                    segmentModelsVendorsPredicate,
                    segmentGenderMixPredicate
                );

        criteriaQuery = criteriaQuery.where(predicate);
        criteriaQuery = criteriaQuery.select(root);
        return criteriaQuery;
    }
    
    private Predicate buildCampaignStatusPredicate(CriteriaBuilder criteriaBuilder, Join<CampaignBid, Campaign> campaignJoin) {
        return or(
                    criteriaBuilder.equal(campaignJoin.get(Campaign_.status), Campaign.Status.ACTIVE),
                    criteriaBuilder.equal(campaignJoin.get(Campaign_.status), Campaign.Status.PAUSED)
                );
    }
    
    private Predicate buildStartDatePredicate(Campaign campaign, CriteriaBuilder criteriaBuilder, Root<CampaignBid> root) {
        Date d1 = campaign.getEndDate();
        if (d1 == null) {
            d1 = new Date();
        }
        return criteriaBuilder.lessThan(root.get(CampaignBid_.startDate), d1);
    }

    private Predicate buildEndDatePredicate(Campaign campaign, CriteriaBuilder criteriaBuilder, Root<CampaignBid> root) {
        Date d2 = campaign.getStartDate();
        if (d2 == null) {
            d2 = new Date();
        }
        return or(
                criteriaBuilder.isNull(root.get(CampaignBid_.endDate)),
                criteriaBuilder.greaterThan(root.get(CampaignBid_.endDate), d2)
                );
    }
    
    private Predicate buildTransparentNetworksPredicate(Campaign campaign, CriteriaBuilder criteriaBuilder, Join<CampaignBid, Campaign> campaignJoin) {
        Predicate transparentNetworksPredicate = null;
        if (campaign.getTransparentNetworks().isEmpty()) {
            transparentNetworksPredicate = criteriaBuilder.isEmpty(campaignJoin.get(Campaign_.transparentNetworks));
        }else{
             // Enumerate the networks that this campaign has targeted
             // in order to find any overlap.
             for(TransparentNetwork tn : campaign.getTransparentNetworks()) {
                 Predicate p = criteriaBuilder.isMember(tn, campaignJoin.get(Campaign_.transparentNetworks));
                 if(transparentNetworksPredicate == null) {
                     transparentNetworksPredicate = p;
                 } else {
                     transparentNetworksPredicate = or(transparentNetworksPredicate, p);
                 }
             }
        }
        return transparentNetworksPredicate;
    }
    
    private Predicate buildSegmentCountriesPredicate(CriteriaBuilder criteriaBuilder, Join<Campaign, Segment> segmentJoin, Segment cs) {
        Predicate segmentCountriesPredicate;
        Predicate countriesOrPredicate = null;
        for(Country c : cs.getCountries()) {
            Predicate p = criteriaBuilder.isMember(c, segmentJoin.get(Segment_.countries));
            if(countriesOrPredicate == null) {
                countriesOrPredicate = p;
            } else {
                countriesOrPredicate = or(countriesOrPredicate, p);
            }
        }
        segmentCountriesPredicate = or(
                    criteriaBuilder.isEmpty(segmentJoin.get(Segment_.countries)),
                    countriesOrPredicate
                );
        return segmentCountriesPredicate;
    }
    
    private Predicate buildSegmentOperatorsPredicate(CriteriaBuilder criteriaBuilder, Join<Campaign, Segment> segmentJoin, Segment cs) {
        Predicate segmentCountriesPredicate;
        Predicate operatorsOrPredicate = null;
        for(Operator o : cs.getOperators()) {
            Predicate p = criteriaBuilder.isMember(o, segmentJoin.get(Segment_.operators));
            if(operatorsOrPredicate == null) {
                operatorsOrPredicate = p;
            } else {
                operatorsOrPredicate = or(operatorsOrPredicate, p);
            }
        }
        segmentCountriesPredicate = or(
                    criteriaBuilder.isEmpty(segmentJoin.get(Segment_.operators)),
                    operatorsOrPredicate
                );
        return segmentCountriesPredicate;
    }

    private Predicate buildSegmentModelsVendorsPredicate(Segment cs, CriteriaBuilder criteriaBuilder, Join<Campaign, Segment> segmentJoin) {
        Predicate segmentModelsVendorsPredicate;
        Predicate modelsOrPredicate = null;
        for (Model m : cs.getModels()) {
            Predicate p1 = criteriaBuilder.isMember(m, segmentJoin.get(Segment_.models));
            Predicate p2 = criteriaBuilder.isMember(m.getVendor(), segmentJoin.get(Segment_.vendors));
            Predicate p = or(p1, p2);
            if(modelsOrPredicate == null) {
                modelsOrPredicate = p;
            } else {
                modelsOrPredicate = or(modelsOrPredicate, p);
            }
        }

        Predicate vendorsOrPredicate = null;
        if(!CollectionUtils.isEmpty(cs.getVendors())) {
            // For each vendor, find campaigns whose segments targets that vendor, or any of that vendor's models
            Join <Segment, Model> modelsJoin = segmentJoin.join(Segment_.models, JoinType.LEFT);
            for (Vendor v : cs.getVendors()) {
                Predicate p1 = criteriaBuilder.equal(modelsJoin.get(Model_.vendor), v);
                Predicate p2 = criteriaBuilder.isMember(v, segmentJoin.get(Segment_.vendors));
                Predicate p = or(p1, p2);
                if(vendorsOrPredicate == null) {
                    vendorsOrPredicate = p;
                } else {
                    vendorsOrPredicate = or(vendorsOrPredicate, p);
                }
            }
        }
        segmentModelsVendorsPredicate = or(
                    and(criteriaBuilder.isEmpty(segmentJoin.get(Segment_.models)),criteriaBuilder.isEmpty(segmentJoin.get(Segment_.vendors))),
                    modelsOrPredicate,
                    vendorsOrPredicate
                );
        return segmentModelsVendorsPredicate;
    }
    
    private Predicate buildSegmentGenderMixPredicate(CriteriaBuilder criteriaBuilder, Join<CampaignBid, Campaign> campaignJoin, Join<Campaign, Segment> segmentJoin, Segment cs) {
        Join<Campaign, Segment> localSegmentJoin = segmentJoin;
        Predicate segmentGenderMixPredicate = null;
        if (cs.getGenderMix().floatValue() == 0.0f) {
            if(localSegmentJoin == null) {
                localSegmentJoin = campaignJoin.join(Campaign_.segments);
            }
            segmentGenderMixPredicate = criteriaBuilder.notEqual(localSegmentJoin.get(Segment_.genderMix), 1.0f);
        } else if (cs.getGenderMix().floatValue() == 1.0f) {
            if(localSegmentJoin == null) {
                localSegmentJoin = campaignJoin.join(Campaign_.segments);
            }
            segmentGenderMixPredicate = criteriaBuilder.notEqual(localSegmentJoin.get(Segment_.genderMix), 0.0f);
        }
        return segmentGenderMixPredicate;
    }
    
    //-----------------------------------------------------------------------------------------------------------------
    
    @Override
    public Long countAllForCampaign(Campaign campaign) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<CampaignBid> root = criteriaQuery.from(CampaignBid.class);

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignBid_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<CampaignBid> getAllForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, null, fetchStrategy);
    }

    @Override
    public List<CampaignBid> getAllForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<CampaignBid> getAllForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCampaign(campaign, null, sort, fetchStrategy);
    }

    protected List<CampaignBid> getAllForCampaign(Campaign campaign, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignBid> criteriaQuery = container.getQuery();
        Root<CampaignBid> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignBid_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
