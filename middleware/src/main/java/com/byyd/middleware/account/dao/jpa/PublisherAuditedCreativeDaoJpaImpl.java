package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative_;
import com.byyd.middleware.account.dao.PublisherAuditedCreativeDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class PublisherAuditedCreativeDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublisherAuditedCreative> implements PublisherAuditedCreativeDao {

    @Override
    public PublisherAuditedCreative getByPublisherAndCreative(Publisher publisher, Creative creative, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublisherAuditedCreative> query = container.getQuery();
        Root<PublisherAuditedCreative> root = container.getRoot();
        CriteriaBuilder builder = getTransactionalEntityManager().getCriteriaBuilder();
        query = query.where(and(builder.equal(root.get(PublisherAuditedCreative_.publisher), publisher)), 
                                builder.equal(root.get(PublisherAuditedCreative_.creative), creative));

        return find(query.select(root));
    }

    @Override
    public List<PublisherAuditedCreative> getByCreative(Creative creative, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublisherAuditedCreative> query = container.getQuery();
        Root<PublisherAuditedCreative> root = container.getRoot();
        CriteriaBuilder builder = getTransactionalEntityManager().getCriteriaBuilder();
        
        query = query.where(builder.equal(root.get(PublisherAuditedCreative_.creative), creative));

        return findAll(query);
    }

}
