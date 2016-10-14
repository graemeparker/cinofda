package com.byyd.middleware.campaign.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignInternalLog;
import com.adfonic.domain.CampaignInternalLog_;
import com.byyd.middleware.campaign.dao.CampaignInternalLogDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class CampaignInternalLogDaoJpaImpl extends BusinessKeyDaoJpaImpl<CampaignInternalLog> implements CampaignInternalLogDao {

    public CampaignInternalLog getByCampaign(Campaign campaign, FetchStrategy... fetchStrategy){
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<CampaignInternalLog> criteriaQuery = container.getQuery();
        Root<CampaignInternalLog> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = criteriaBuilder.equal(root.get(CampaignInternalLog_.campaign), campaign);
        criteriaQuery = criteriaQuery.where(predicate);
        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }
}
