package com.byyd.middleware.creative.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Destination_;
import com.byyd.middleware.creative.dao.DestinationDao;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DestinationDaoJpaImpl extends BusinessKeyDaoJpaImpl<Destination> implements DestinationDao {

    protected Predicate getPredicate(Root<Destination> root, DestinationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate advertiserPredicate = null;
        Predicate destinationTypesPredicate = null;
        Predicate dataPredicate = null;

        if (filter.getAdvertiser() != null) {
            advertiserPredicate = criteriaBuilder.equal(root.get(Destination_.advertiser), filter.getAdvertiser());
        }

        if (CollectionUtils.isNotEmpty(filter.getDestinationTypes())) {
            destinationTypesPredicate = root.get(Destination_.destinationType).in(filter.getDestinationTypes());
        }
        
        if (!StringUtils.isEmpty(filter.getData())) {
            dataPredicate = criteriaBuilder.equal(root.get(Destination_.data), filter.getData());
        }
        
        return and(advertiserPredicate, destinationTypesPredicate, dataPredicate);
    }


    @Override
    public Long countAll(DestinationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Destination> root = criteriaQuery.from(Destination.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Destination> getAll(DestinationFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Destination> getAll(DestinationFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Destination> getAll(DestinationFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<Destination> getAll(DestinationFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Destination> criteriaQuery = container.getQuery();
        Root<Destination> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    protected Predicate getForAdvertiserAndDestinationTypeAndDataPredicate(Root<Destination> root, Advertiser advertiser, DestinationType destinationType, String data) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate advertiserExpression = criteriaBuilder.equal(root.get(Destination_.advertiser), advertiser);
        Predicate destinationTypeExpression = criteriaBuilder.equal(root.get(Destination_.destinationType), destinationType);
        Predicate dataExpression = criteriaBuilder.equal(root.get(Destination_.data), data);
        return  and(advertiserExpression, destinationTypeExpression, dataExpression);
    }
    
    protected boolean destinationHasSameBeacons(Destination destination, List<BeaconUrl> beacons){
        if(destination.getBeaconUrls().size()!=beacons.size()){
            return false;
        }
        boolean foundAllBeacons = true;
        for(BeaconUrl b : beacons){
            boolean beaconFound=false;
            for(BeaconUrl db : destination.getBeaconUrls()){
                if(b.getUrl().equals(db.getUrl())){
                    beaconFound=true;
                    break;
                }
            }
            if(!beaconFound){
                foundAllBeacons = false;
            }
        }
        return foundAllBeacons;
    }

    @Override
    public Long countForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, List<BeaconUrl> beacons) {
        return Long.valueOf(getForAdvertiserAndDestinationTypeAndData( advertiser, destinationType, data, beacons).size());
    }

    @Override
    public List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy) {
        return this.getForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, null, null, beacons, fetchStrategy);
    }
    
    @Override
    public List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, FetchStrategy... fetchStrategy) {
        return this.getForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, null, null, null, fetchStrategy);
    }

    @Override
    public List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, Sorting sort, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy) {
        return this.getForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, null, sort, beacons, fetchStrategy);
    }

    @Override
    public List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, Pagination page, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy) {
        return this.getForAdvertiserAndDestinationTypeAndData(advertiser, destinationType, data, page, page.getSorting(), beacons, fetchStrategy);
    }

    public List<Destination> getForAdvertiserAndDestinationTypeAndData(Advertiser advertiser, DestinationType destinationType, String data, Pagination page, Sorting sort, List<BeaconUrl> beacons, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Destination> criteriaQuery = container.getQuery();
        Root<Destination> root = container.getRoot();

        Predicate predicate = getForAdvertiserAndDestinationTypeAndDataPredicate(root, advertiser, destinationType, data);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);
        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        //return destinations which have exactly the same beacons
        List<Destination> destinations = findAll(criteriaQuery, page);
        List<Destination> result = new ArrayList<Destination>();
        for(Destination destination : destinations){
            if(destinationHasSameBeacons(destination, beacons)){
                result.add(destination);
            }
        }
        return result;
    }
}
