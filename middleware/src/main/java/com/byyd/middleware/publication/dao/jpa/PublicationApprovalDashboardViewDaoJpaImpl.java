package com.byyd.middleware.publication.dao.jpa;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationApprovalDashboardView;
import com.adfonic.domain.PublicationApprovalDashboardView_;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationApprovalDashboardViewDao;
import com.byyd.middleware.publication.filter.PublicationFilter;

@Repository
public class PublicationApprovalDashboardViewDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationApprovalDashboardView> implements PublicationApprovalDashboardViewDao {
    
    private static final transient Logger LOG = Logger.getLogger(PublicationApprovalDashboardViewDaoJpaImpl.class.getName());

    @Autowired(required=false)
    @Qualifier("readOnlyEntityManagerFactory")
    private EntityManagerFactory entityManagerFactory;
    
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory == null) {
            LOG.severe("It seems no EntityManagerFactory with name readOnlyEntityManagerFactory was defined in the context used to boot this application");
        }
        return entityManagerFactory;
    }

    @Override
    public Long countAll(PublicationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PublicationApprovalDashboardView> root = criteriaQuery.from(PublicationApprovalDashboardView.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<PublicationApprovalDashboardView> getAll(PublicationFilter filter) {
        return getAll(filter, null, null);
    }

    @Override
    public List<PublicationApprovalDashboardView> getAll(PublicationFilter filter, Pagination page) {
        return getAll(filter, page, page.getSorting());
    }

    @Override
    public List<PublicationApprovalDashboardView> getAll(PublicationFilter filter, Sorting sort) {
        return getAll(filter, null, sort);
    }

    protected List<PublicationApprovalDashboardView> getAll(PublicationFilter filter, Pagination page, Sorting sort) {
        CriteriaQueryContainer container = createCriteriaQuery();
        CriteriaQuery<PublicationApprovalDashboardView> criteriaQuery = container.getQuery();
        Root<PublicationApprovalDashboardView> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    /**
     * Same Filter class as is used in BasePublicationDaoJpaImpl, but what it triggers is of course changed
     * based on the definition of the view. There are no more joins triggered here either, of course.
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getPredicate(Root<PublicationApprovalDashboardView> root, PublicationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;
        Predicate nameLikePredicate = null;
        Predicate friendlyNamePredicate = null;
        //Predicate publisherPredicate = null;
        Predicate statusesPredicate = null;
        Predicate adOpsStatusesPredicate = null;
        //Predicate rtbIdPredicate = null;
        Predicate includedIdsPredicate = null;
        Predicate assignedToFullNameContainsPredicate = null;
        Predicate companyNameContainsPredicate = null;
        Predicate publisherIsKeyPredicate = null;
        Predicate externalIdContainsPredicate = null;
        Predicate accountManagerEmailContainsPredicate = null;
        Predicate publicationTypeNameContainsPredicate = null;
        //Predicate autoApprovalPredicate = null;
        
        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(PublicationApprovalDashboardView_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.name)), filter.getName().toLowerCase());
            }
        }

        if (filter.getNameLike() != null) {
            if (filter.isNameLikeCaseSensitive()) {
                nameLikePredicate = criteriaBuilder.like(root.get(PublicationApprovalDashboardView_.name), filter.getNameLike());
            } else {
                nameLikePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.name)), filter.getNameLike().toLowerCase());
            }
        }
        
        if(!StringUtils.isEmpty(filter.getFriendlyName())) {
            Predicate p1 = null;
            Predicate p2 = null;
            if(filter.getFriendlyNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    p1 = criteriaBuilder.equal(
                            root.get(PublicationApprovalDashboardView_.friendlyName), filter.getFriendlyName());
                } else {
                    p1 = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.friendlyName)),
                            filter.getFriendlyName().toLowerCase());
                }
            } else {
                if (filter.isNameLikeCaseSensitive()) {
                    p1 = criteriaBuilder.like(root.get(PublicationApprovalDashboardView_.friendlyName), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()));
                } else {
                    p1 = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.friendlyName)), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()).toLowerCase());
                }
            }
            if(filter.isFriendlyNameAppliesToName()) {
                if(filter.getFriendlyNameLikeSpec() == null) {
                    if (filter.isNameCaseSensitive()) {
                        p2 = criteriaBuilder.equal(
                                root.get(PublicationApprovalDashboardView_.name), filter.getFriendlyName());
                    } else {
                        p2 = criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.name)),
                                filter.getFriendlyName().toLowerCase());
                    }
                } else {
                    if (filter.isNameLikeCaseSensitive()) {
                        p2 = criteriaBuilder.like(root.get(PublicationApprovalDashboardView_.name), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()));
                    } else {
                        p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.name)), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()).toLowerCase());
                    }
                }
            }
            friendlyNamePredicate = or(p1, p2);
        }

        // Not needed
        //if (filter.getPublisher() != null) {
        //    publisherJoin = root.join(Publication_.publisher, JoinType.INNER);
        //    publisherPredicate = criteriaBuilder.equal(root.get(Publication_.publisher), filter.getPublisher());
        //}

        if (filter.getStatuses() != null) {
            for (Publication.Status status : filter.getStatuses()) {
                Predicate p = criteriaBuilder.equal(root.get(PublicationApprovalDashboardView_.status), status);
                if (statusesPredicate == null) {
                    statusesPredicate = p;
                } else {
                    statusesPredicate = or(statusesPredicate, p);
                }
            }
        }

        if (filter.getAdOpsStatuses() != null) {
            for (Publication.AdOpsStatus adOpsStatus : filter.getAdOpsStatuses()) {
                Predicate p = criteriaBuilder.equal(root.get(PublicationApprovalDashboardView_.adOpsStatus), adOpsStatus);
                if (adOpsStatusesPredicate == null) {
                    adOpsStatusesPredicate = p;
                } else {
                    adOpsStatusesPredicate = or(adOpsStatusesPredicate, p);
                }
            }
        }

        // Not needed
        //if (filter.getRtbId() != null) {
        //    rtbIdPredicate = criteriaBuilder.equal(root.get(Publication_.rtbId), filter.getRtbId());
        //}

        if (CollectionUtils.isNotEmpty(filter.getIncludedIds())) {
            includedIdsPredicate = root.get(PublicationApprovalDashboardView_.id).in(filter.getIncludedIds());
        }

        if (filter.getAssignedToFullNameContains() != null) {
            assignedToFullNameContainsPredicate = 
                    criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.assignedToFirstName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"
                        ), 
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.assignedToLastName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"
                        )
                    );
        }

        if (filter.getCompanyNameContains() != null) {
            companyNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.companyName)), "%" + filter.getCompanyNameContains().toLowerCase() + "%");
        }

        if (filter.getPublisherIsKey() != null) {
            publisherIsKeyPredicate = criteriaBuilder.equal(root.get(PublicationApprovalDashboardView_.publisherIsKey), filter.getPublisherIsKey());
        }

        if (filter.getExternalIdContains() != null) {
            externalIdContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.externalID)), "%" + filter.getExternalIdContains().toLowerCase() + "%");
        }

        if (filter.getAccountManagerEmailContains() != null) {
               accountManagerEmailContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.accountManagerEmail)), "%" + filter.getAccountManagerEmailContains().toLowerCase() + "%");
        }
        
        if (filter.getPublicationTypeNameContains() != null) {
            publicationTypeNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PublicationApprovalDashboardView_.publicationTypeName)), "%" + filter.getPublicationTypeNameContains() + "%");
        }

        //if (filter.isAutoApproval() != null) {
        //    autoApprovalPredicate = criteriaBuilder.equal(root.get(Publication_.autoApproval), filter.isAutoApproval());
        //}
        return and(
                namePredicate, 
                nameLikePredicate, 
                friendlyNamePredicate, 
                //publisherPredicate, 
                statusesPredicate, 
                adOpsStatusesPredicate, 
                //rtbIdPredicate, 
                includedIdsPredicate, 
                assignedToFullNameContainsPredicate, 
                companyNameContainsPredicate, 
                publisherIsKeyPredicate, 
                externalIdContainsPredicate, 
                accountManagerEmailContainsPredicate,
                publicationTypeNameContainsPredicate 
                //autoApprovalPredicate
            );
    }


}
