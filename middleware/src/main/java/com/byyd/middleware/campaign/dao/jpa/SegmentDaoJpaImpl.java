package com.byyd.middleware.campaign.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.byyd.middleware.campaign.dao.SegmentDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class SegmentDaoJpaImpl extends BusinessKeyDaoJpaImpl<Segment> implements SegmentDao {

    @Override
    public boolean isAdSpaceAlreadyAllocated(Segment segment, AdSpace adSpace) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Segment> root = criteriaQuery.from(Segment.class);

        Predicate idPredicate = criteriaBuilder.notEqual(root.get(Segment_.id), segment.getId());
        Predicate adSpacePredicate = criteriaBuilder.isMember(adSpace, root.get(Segment_.adSpaces));

        criteriaQuery = criteriaQuery.where(and(idPredicate, adSpacePredicate));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        Long count = executeLongAggregateFunction(criteriaQuery);
        return count > 0;
    }
    
    //------------------------------------------------------------------------------------------
    // Native aggregate queries to determine the size of some of the collections hanging off of 
    // Segment without actually loading the data
    //------------------------------------------------------------------------------------------

    @Override
    public Long countBrowsersForSegment(Segment segment) {
        String query = "select count(*) from SEGMENT_BROWSER where SEGMENT_ID = " + segment.getId();
        return this.executeAggregateFunctionByNativeQuery(query).longValue();
    }

    @Override
    public Long countCategoriesForSegment(Segment segment) {
        String query = "select count(*) from SEGMENT_CATEGORY where SEGMENT_ID = " + segment.getId();
        return this.executeAggregateFunctionByNativeQuery(query).longValue();
    }

    @Override
    public Long countExcludedCategoriesForSegment(Segment segment) {
        String query = "select count(*) from SEGMENT_EXCLUDED_CATEGORY where SEGMENT_ID = " + segment.getId();
        return this.executeAggregateFunctionByNativeQuery(query).longValue();
    }

    @Override
    public Long countChannelsForSegment(Segment segment) {
        String query = "select count(*) from SEGMENT_CHANNEL where SEGMENT_ID = " + segment.getId();
        return this.executeAggregateFunctionByNativeQuery(query).longValue();
    }
    
     @Override
    public Long countCountriesForSegment(Segment segment) {
        String query = "select count(*) from SEGMENT_COUNTRY where SEGMENT_ID = " + segment.getId();
        return this.executeAggregateFunctionByNativeQuery(query).longValue();
    }

     @Override
    public Long countGeotargetsForSegment(Segment segment) {
         String query = "select count(*) from SEGMENT_GEOTARGET where SEGMENT_ID = " + segment.getId();
         return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

     @Override
    public Long countIpAddressesForSegment(Segment segment) {
         String query = "select count(*) from SEGMENT_IP_ADDRESS where SEGMENT_ID = " + segment.getId();
         return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

     @Override
    public Long countModelsForSegment(Segment segment) {
         String query = "select count(*) from SEGMENT_MODEL where SEGMENT_ID = " + segment.getId();
         return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }
     
     @Override
    public Long countExcludedModelsForSegment(Segment segment) {
          String query = "select count(*) from SEGMENT_EXCLUDED_MODEL where SEGMENT_ID = " + segment.getId();
          return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }
     
     @Override
    public Long countOperatorsForSegment(Segment segment) {
           String query = "select count(*) from SEGMENT_OPERATOR where SEGMENT_ID = " + segment.getId();
           return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

     @Override
    public Long countPlatformsForSegment(Segment segment) {
            String query = "select count(*) from SEGMENT_PLATFORM where SEGMENT_ID = " + segment.getId();
            return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

     @Override
    public Long countPublishersForSegment(Segment segment) {
         String query = "select count(*) from SEGMENT_PUBLISHER where SEGMENT_ID = " + segment.getId();
         return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

     @Override
    public Long countVendorsForSegment(Segment segment) {
           String query = "select count(*) from SEGMENT_VENDOR where SEGMENT_ID = " + segment.getId();
           return this.executeAggregateFunctionByNativeQuery(query).longValue();
     }

}
