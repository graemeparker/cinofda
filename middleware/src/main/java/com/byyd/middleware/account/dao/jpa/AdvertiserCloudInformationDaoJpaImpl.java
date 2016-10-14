package com.byyd.middleware.account.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;
import com.adfonic.domain.AdvertiserCloudInformation_;
import com.byyd.middleware.account.dao.AdvertiserCloudInformationDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AdvertiserCloudInformationDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdvertiserCloudInformation> implements AdvertiserCloudInformationDao {
    
    @Override
    public AdvertiserCloudInformation getById(Long id, FetchStrategy... fetchStrategy) {
        // implement similar query than getByUser method but doing an inner join by user_.id
        return null;
    }

    @Override
    public AdvertiserCloudInformation getByAdvertiser(Advertiser advertiser, FetchStrategy... fetchStrategy) {
        AdvertiserCloudInformation advertiserCloudInformation = null;
        if (advertiser!=null){
            CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
            CriteriaQuery<AdvertiserCloudInformation> criteriaQuery = container.getQuery();
            Root<AdvertiserCloudInformation> root = container.getRoot(); 
            CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
            criteriaQuery.where(criteriaBuilder.equal(root.get(AdvertiserCloudInformation_.advertiser), advertiser));
            criteriaQuery = criteriaQuery.select(root);
            advertiserCloudInformation = find(criteriaQuery);
        }
        return advertiserCloudInformation;
    }
}
