package com.byyd.middleware.publication.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationBundle;
import com.adfonic.domain.PublicationBundle_;
import com.adfonic.domain.Publication_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationBundleDao;
import com.byyd.middleware.publication.filter.PublicationBundleFilter;

@Repository
public class PublicationBundleDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationBundle> implements PublicationBundleDao {

    protected Predicate getPredicate(Root<PublicationBundle> root, PublicationBundleFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        Predicate idsPredicate = null;
        Predicate namePredicate = null;
        Predicate publicationsPredicate = null;
        Predicate disclosedPubsPredicate = null;
        Predicate approvedPubsPredicate = null;
        Predicate publicationIdPredicate = null;
        
        // Ids predicate
        if (filter.getIds()!=null){
            idsPredicate = root.get(PublicationBundle_.id).in(filter.getIds());
        }
        
        // Name predicate
        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(root.get(PublicationBundle_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(PublicationBundle_.name)), filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(PublicationBundle_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationBundle_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }
        
        // Publications predicate
        if (CollectionUtils.isNotEmpty(filter.getPublications())){
            List<Long> publicationsIds = new ArrayList<Long> (filter.getPublications().size());
            for(Publication pub : filter.getPublications()){
                publicationsIds.add(pub.getId());
            }
            publicationsPredicate = root.join(PublicationBundle_.publications).get(Publication_.id).in(publicationsIds);
        }   
        
        // Disclosed/Undisclosed publications predicate
        if (filter.getDisclosedPublications()!=null){
            disclosedPubsPredicate = criteriaBuilder.equal(root.join(PublicationBundle_.publications).get(Publication_.disclosed),filter.getDisclosedPublications());
        }
        
        // Approved/Unapproved publications predicate
        if (filter.getApprovedPublications()!=null){
            approvedPubsPredicate = criteriaBuilder.equal(root.join(PublicationBundle_.publications).get(Publication_.status),Publication.Status.ACTIVE);
            if (!filter.getApprovedPublications()){
                approvedPubsPredicate = criteriaBuilder.not(approvedPubsPredicate);
            }
        }
        
        // PublicationId predicate
        if (filter.getPublicationId()!=null){
            publicationIdPredicate = criteriaBuilder.equal(root.join(PublicationBundle_.publications).get(Publication_.id), filter.getPublicationId());
        }
        
        return and(idsPredicate, namePredicate, publicationsPredicate, disclosedPubsPredicate, approvedPubsPredicate, publicationIdPredicate);
    }
    
    @Override
    public Long countAll(PublicationBundleFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PublicationBundle> root = criteriaQuery.from(PublicationBundle.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<PublicationBundle> getAll(PublicationBundleFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<PublicationBundle> getAll(PublicationBundleFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, (page==null?null:page.getSorting()), fetchStrategy);
    }

    protected List<PublicationBundle> getAll(PublicationBundleFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublicationBundle> criteriaQuery = container.getQuery();
        Root<PublicationBundle> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
