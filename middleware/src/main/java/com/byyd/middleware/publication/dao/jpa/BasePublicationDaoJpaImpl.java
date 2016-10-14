package com.byyd.middleware.publication.dao.jpa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AdfonicUser_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.PublicationType_;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;
import com.byyd.middleware.publication.dao.BasePublicationDao;
import com.byyd.middleware.publication.filter.PublicationFilter;

public class BasePublicationDaoJpaImpl extends BusinessKeyDaoJpaImpl<Publication> implements BasePublicationDao {
    
    public BasePublicationDaoJpaImpl() {
        super();
        this.setType(Publication.class);
    }

    @Override
    public Publication getByName(String name, Publisher publisher, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = criteriaBuilder.equal(root.get(Publication_.name), name);
        Predicate publisherPredicate = criteriaBuilder.equal(root.get(Publication_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(and(publisherPredicate, namePredicate));
        CriteriaQuery<Publication> select = criteriaQuery.select(root);

        return find(select);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Publication getByPublisherAndRtbId(Publisher publisher, String rtbId, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherPredicate = criteriaBuilder.equal(root.get(Publication_.publisher), publisher);
        Predicate rtbIdPredicate = criteriaBuilder.equal(root.get(Publication_.rtbId), rtbId);
        criteriaQuery = criteriaQuery.where(and(publisherPredicate, rtbIdPredicate));
        CriteriaQuery<Publication> select = criteriaQuery.select(root);

        return find(select);
    }

    //------------------------------------------------------------------------------------------

    protected Predicate getForStatusPredicate(Root<Publication> root,Publisher publisher, List<Publication.Status> statuses) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherExpression = null;
        Predicate statusesExpression = null;

        if(publisher != null) {
            publisherExpression = criteriaBuilder.equal(root.get(Publication_.publisher), publisher);
        }
        if(statuses != null && !statuses.isEmpty()) {
            for(int i = 0;i < statuses.size();i++) {
                Publication.Status status = statuses.get(i);
                Predicate p = criteriaBuilder.equal(root.get(Publication_.status), status);
                if(i == 0) {
                    statusesExpression = p;
                } else {
                    statusesExpression = or(statusesExpression, p);
                }
            }
        }
        return and(publisherExpression, statusesExpression);
    }

    @Override
    public Long countForStatus(Publisher publisher, List<Publication.Status> statuses) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Publication> root = criteriaQuery.from(Publication.class);

        Predicate predicate = getForStatusPredicate(root, publisher, statuses);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, FetchStrategy... fetchStrategy) {
        return this.getForStatus(publisher, statuses, null, null, fetchStrategy);
    }

    @Override
    public List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, Sorting sort, FetchStrategy... fetchStrategy) {
          return this.getForStatus(publisher, statuses, null, sort, fetchStrategy);
    }

    @Override
    public List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, Pagination page, FetchStrategy... fetchStrategy) {
          return this.getForStatus(publisher, statuses, page, page.getSorting(), fetchStrategy);
    }

    protected List<Publication> getForStatus(Publisher publisher, List<Publication.Status> statuses, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getForStatusPredicate(root, publisher, statuses);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public List<Publication> getAllLike(String name, Pagination page, FetchStrategy... fetchStrategy){
        return getAllLike(name, page, page.getSorting(), fetchStrategy);
    }

    public List<Publication> getAllLike(String name, Pagination page, Sorting sort, FetchStrategy... fetchStrategy){
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate nameExpression = criteriaBuilder.like(root.get(Publication_.name), name);
        criteriaQuery = criteriaQuery.where(nameExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Publication,Long> getPublicationsWithPendingAdsMapForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        StringBuilder sb = new StringBuilder("CALL")
            .append(" proc_return_publications_for_ad_management_new_table(?)");

        List<QueryParameter> list = new ArrayList<QueryParameter>();
        list.add(new QueryParameter(publisher.getId()));

        List<Object[]> results = this.findByNativeQueryPositionalParameters(sb.toString(), list);

        Map<Publication,Long> map = new HashMap<Publication,Long>();
        if (results != null) {
            for (Object[] row : results) {
                Integer id = (Integer)row[0];
                BigInteger count = (BigInteger)row[1];
                Publication p = this.getById(id.longValue(), fetchStrategy);
                map.put(p, count.longValue());
            }
        }
        return map;

    }

    //------------------------------------------------------------------------------------------

    protected Predicate getPublicationsWithNameForPublisherPredicate(Root<Publication> root, String name, boolean caseSensitive, Publisher publisher, Publication excludePublication) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherPredicate = criteriaBuilder.equal(root.get(Publication_.publisher), publisher);
        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.like(root.get(Publication_.name), name);
        } else {
            namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.name)), name.toLowerCase());
        }
        Predicate publicationPredicate = null;
        if(excludePublication != null) {
            publicationPredicate =  criteriaBuilder.notEqual(root.get(Publication_.id), excludePublication.getId());
        }
        Predicate predicate = and(publisherPredicate, namePredicate);
        if(publicationPredicate != null) {
            predicate = and(predicate, publicationPredicate);
        }
        return predicate;
    }

    @Override
    public Long countPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Publication> root = criteriaQuery.from(Publication.class);

        Predicate predicate = this.getPublicationsWithNameForPublisherPredicate(root, name, caseSensitive, publisher, excludePublication);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, FetchStrategy... fetchStrategy) {
        return getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, null, null, fetchStrategy);
    }

    @Override
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Sorting sort, FetchStrategy... fetchStrategy) {
        return getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, null, sort, fetchStrategy);
    }

    @Override
    public List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Pagination page, FetchStrategy... fetchStrategy) {
        return getPublicationsWithNameForPublisher(name, caseSensitive, publisher, excludePublication, page,page.getSorting(), fetchStrategy);
    }

    protected List<Publication> getPublicationsWithNameForPublisher(String name, boolean caseSensitive, Publisher publisher, Publication excludePublication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();

        Predicate predicate = this.getPublicationsWithNameForPublisherPredicate(root, name, caseSensitive, publisher, excludePublication);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public Long countAll(PublicationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Publication> root = criteriaQuery.from(Publication.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Publication> getAll(PublicationFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Publication> getAll(PublicationFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<Publication> getAll(PublicationFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<Publication> getAll(PublicationFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Publication> criteriaQuery = container.getQuery();
        Root<Publication> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    /**
     * Notes about this method: this is an example of usage of explicit join objects instead to .get() in a daisy chain. 
     * Using the get() calls yielded a series of cross joins on the tables involved, I assume because .get() does not qualify
     * the join to use, and Hibernate "cover its ass" by issuing cross joins across the board. We looked for a way to avoid this
     * in relation to https://tickets.adfonic.com/browse/AO-289, as we thought these cross joins were the cause of the issue.
     * They actually werent, but regardless, if we even need to prever cross joins generated for queries built in this manner,
     * this is a good example of how to do it.
     * 
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getPredicate(Root<Publication> root, PublicationFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        
        Predicate publisherPredicate = null;
        Predicate rtbIdPredicate = null;
        Predicate includedIdsPredicate = null;
        Predicate assignedToFullNameContainsPredicate = null;
        Predicate companyNameContainsPredicate = null;
        Predicate publisherIsKeyPredicate = null;
        Predicate externalIdContainsPredicate = null;
        Predicate publicationTypeNameContainsPredicate = null;
        Predicate autoApprovalPredicate = null;
        Predicate rtbIdNotNullPredicate = null;
        Predicate geApprovalDatePredicate = null;
        
        Join<Publication, Publisher> publisherJoin = null;
        Join<Publisher, Company> publisherCompanyJoin = null;
        
        Join<Publication, PublicationType> publicationTypeJoin = null;

        Predicate namePredicate = getNamePredicate(root, filter, criteriaBuilder);

        Predicate nameLikePredicate = getNameLikePredicate(root, filter, criteriaBuilder);
        
        Predicate friendlyNamePredicate = null;
        if(!StringUtils.isEmpty(filter.getFriendlyName())) {
            friendlyNamePredicate = getFriendlyNamePredicate(root, filter, criteriaBuilder);
        }

        if (filter.getPublisher() != null) {
            publisherJoin = root.join(Publication_.publisher, JoinType.INNER);
            publisherPredicate = criteriaBuilder.equal(root.get(Publication_.publisher), filter.getPublisher());
        }

        Predicate statusesPredicate = getStatusesPredicate(root, filter, criteriaBuilder);

        Predicate adOpsStatusesPredicate = getAdOpsStatusesPredicate(root, filter, criteriaBuilder);

        if (filter.getRtbId() != null) {
            rtbIdPredicate = criteriaBuilder.equal(root.get(Publication_.rtbId), filter.getRtbId());
        }

        if (CollectionUtils.isNotEmpty(filter.getIncludedIds())) {
            includedIdsPredicate = root.get(Publication_.id).in(filter.getIncludedIds());
        }

        if (filter.getAssignedToFullNameContains() != null) {
            assignedToFullNameContainsPredicate = criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.assignedTo).get(AdfonicUser_.firstName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"), criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.assignedTo).get(AdfonicUser_.lastName)), "%" + filter.getAssignedToFullNameContains().toLowerCase() + "%"));
        }

        if (filter.getCompanyNameContains() != null) {
            if(publisherJoin == null) {
                publisherJoin = root.join(Publication_.publisher, JoinType.INNER);
            }
            publisherCompanyJoin = publisherJoin.join(Publisher_.company, JoinType.INNER);
            companyNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(publisherCompanyJoin.get(Company_.name)), "%" + filter.getCompanyNameContains().toLowerCase() + "%");
        }

        if (filter.getPublisherIsKey() != null) {
            if(publisherJoin == null) {
                publisherJoin = root.join(Publication_.publisher, JoinType.INNER);
            }
            publisherIsKeyPredicate = criteriaBuilder.equal(publisherJoin.get(Publisher_.key), filter.getPublisherIsKey());
        }

        if (filter.getExternalIdContains() != null) {
            externalIdContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.externalID)), "%" + filter.getExternalIdContains().toLowerCase() + "%");
        }

        Predicate accountManagerEmailContainsPredicate = null;
        if (filter.getAccountManagerEmailContains() != null) {
               if(publisherJoin == null) {
                publisherJoin = root.join(Publication_.publisher, JoinType.INNER);
            }
               if(publisherCompanyJoin == null) {
                   publisherCompanyJoin = publisherJoin.join(Publisher_.company, JoinType.INNER);
               }
               Join<Company, User> publisherCompanyUserJoin = publisherCompanyJoin.join(Company_.accountManager, JoinType.LEFT);
               accountManagerEmailContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(publisherCompanyUserJoin.get(User_.email)), "%" + filter.getAccountManagerEmailContains().toLowerCase() + "%");
        }
        
        if (filter.getPublicationTypeNameContains() != null) {
            publicationTypeJoin = root.join(Publication_.publicationType, JoinType.INNER);
            publicationTypeNameContainsPredicate = criteriaBuilder.like(criteriaBuilder.lower(publicationTypeJoin.get(PublicationType_.name)), "%" + filter.getPublicationTypeNameContains() + "%");
        }

        if (filter.isAutoApproval() != null) {
            autoApprovalPredicate = criteriaBuilder.equal(root.get(Publication_.autoApproval), filter.isAutoApproval());
        }
        
        if (filter.isRtbIdNotNull()) {
            rtbIdNotNullPredicate = criteriaBuilder.isNotNull(root.get(Publication_.rtbId));
        }
        
        if (filter.getGreaterThanOrEqualToApprovalDate() != null) {
            geApprovalDatePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get(Publication_.approvedDate), filter.getGreaterThanOrEqualToApprovalDate());
        }
        
        Predicate safetyLevelPredicate = getSafetyLevelPredicate(root, filter, criteriaBuilder);
        
        return and(namePredicate, nameLikePredicate, friendlyNamePredicate, publisherPredicate, statusesPredicate, adOpsStatusesPredicate, rtbIdPredicate, includedIdsPredicate, assignedToFullNameContainsPredicate, companyNameContainsPredicate, publisherIsKeyPredicate, externalIdContainsPredicate, accountManagerEmailContainsPredicate, publicationTypeNameContainsPredicate, autoApprovalPredicate, rtbIdNotNullPredicate, geApprovalDatePredicate, safetyLevelPredicate);
    }

    private Predicate getAdOpsStatusesPredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate adOpsStatusesPredicate = null;
        if (filter.getAdOpsStatuses() != null) {
            for (Publication.AdOpsStatus adOpsStatus : filter.getAdOpsStatuses()) {
                Predicate p = criteriaBuilder.equal(root.get(Publication_.adOpsStatus), adOpsStatus);
                if (adOpsStatusesPredicate == null) {
                    adOpsStatusesPredicate = p;
                } else {
                    adOpsStatusesPredicate = or(adOpsStatusesPredicate, p);
                }
            }
        }
        return adOpsStatusesPredicate;
    }

    private Predicate getStatusesPredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate statusesPredicate = null;
        if (filter.getStatuses() != null) {
            for (Publication.Status status : filter.getStatuses()) {
                Predicate p = criteriaBuilder.equal(root.get(Publication_.status), status);
                if (statusesPredicate == null) {
                    statusesPredicate = p;
                } else {
                    statusesPredicate = or(statusesPredicate, p);
                }
            }
        }
        return statusesPredicate;
    }

    private Predicate getNamePredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate namePredicate = null;
        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(Publication_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Publication_.name)), filter.getName().toLowerCase());
            }
        }
        return namePredicate;
    }

    private Predicate getNameLikePredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate nameLikePredicate = null;
        if (filter.getNameLike() != null) {
            if (filter.isNameLikeCaseSensitive()) {
                nameLikePredicate = criteriaBuilder.like(root.get(Publication_.name), filter.getNameLike());
            } else {
                nameLikePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.name)), filter.getNameLike().toLowerCase());
            }
        }
        return nameLikePredicate;
    }

    private Predicate getFriendlyNamePredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {

        Predicate p1 = null;
        if(filter.getFriendlyNameLikeSpec() == null) {
            if (filter.isNameCaseSensitive()) {
                p1 = criteriaBuilder.equal(
                        root.get(Publication_.friendlyName), filter.getFriendlyName());
            } else {
                p1 = criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get(Publication_.friendlyName)),
                        filter.getFriendlyName().toLowerCase());
            }
        } else {
            if (filter.isNameLikeCaseSensitive()) {
                p1 = criteriaBuilder.like(root.get(Publication_.friendlyName), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()));
            } else {
                p1 = criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.friendlyName)), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()).toLowerCase());
            }
        }
        
        Predicate p2 = null;
        if(filter.isFriendlyNameAppliesToName()) {
            if(filter.getFriendlyNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    p2 = criteriaBuilder.equal(
                            root.get(Publication_.name), filter.getFriendlyName());
                } else {
                    p2 = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(Publication_.name)),
                            filter.getFriendlyName().toLowerCase());
                }
            } else {
                if (filter.isNameLikeCaseSensitive()) {
                    p2 = criteriaBuilder.like(root.get(Publication_.name), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()));
                } else {
                    p2 = criteriaBuilder.like(criteriaBuilder.lower(root.get(Publication_.name)), filter.getFriendlyNameLikeSpec().getPattern(filter.getFriendlyName()).toLowerCase());
                }
            }
        }
        
        return or(p1, p2);
    }
    
    private Predicate getSafetyLevelPredicate(Root<Publication> root, PublicationFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate safetyLevelPredicate = null;
        if (filter.getSafetyLevels()!=null){
            for (Publication.PublicationSafetyLevel safetyLevel : filter.getSafetyLevels()) {
                Predicate p = criteriaBuilder.equal(root.get(Publication_.safetyLevel), safetyLevel);
                if (safetyLevelPredicate == null) {
                    safetyLevelPredicate = p;
                } else {
                    safetyLevelPredicate = or(safetyLevelPredicate, p);
                }
            }
        }
        return safetyLevelPredicate;
    }

    //------------------------------------------------------------------------------------------

    protected StringBuilder getPublicationsForPublicationListQuery(boolean countOnly) {
        return getPublicationsForPublicationListQuery(countOnly, null);
    }
    
    /**
     * NOTE: the aliases used in the joins are spelled exactly like the class name of the entity mapping 
     * each table. See SortOrder.toString() for details.
     * 
     * @param countOnly
     * @param sort
     * @return
     */
    protected StringBuilder getPublicationsForPublicationListQuery(boolean countOnly, Sorting sort) {
        StringBuilder sb = new StringBuilder("SELECT")
        .append(countOnly ? " COUNT(DISTINCT l.PUBLICATION_ID)" : " DISTINCT(l.PUBLICATION_ID)")
        .append(" FROM PUBLICATION_LIST_PUBLICATION l")
        .append(" INNER JOIN PUBLICATION Publication ON l.PUBLICATION_ID = Publication.ID");
        if(sort != null) {
            Set<Class<?>> clazzes = sort.getClazzes();
            if(clazzes.contains(Publisher.class)) {
                sb.append(" INNER JOIN PUBLISHER Publisher ON Publication.PUBLISHER_ID = Publisher.ID");
            }
            if(clazzes.contains(PublicationType.class)) {
                sb.append(" INNER JOIN PUBLICATION_TYPE PublicationType ON Publication.PUBLICATION_TYPE_ID = PublicationType.ID");
            }
        }
        sb.append(" WHERE l.PUBLICATION_LIST_ID=?");
        return sb;
    }
    
    @Override
    public Long countPublicationsForPublicationList(PublicationList publicationList) {
        StringBuilder sb = getPublicationsForPublicationListQuery(true);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(publicationList.getId()));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(sb.toString(), params).longValue();
    }

    @Override
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, FetchStrategy... fetchStrategy) {
        return this.getPublicationsForPublicationList(publicationList, null, null, fetchStrategy);
    }
    
    @Override
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getPublicationsForPublicationList(publicationList, null, sort, fetchStrategy);
    }

    @Override
    public List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getPublicationsForPublicationList(publicationList, page, page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings("unchecked")
    protected List<Publication> getPublicationsForPublicationList(PublicationList publicationList, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder sb = getPublicationsForPublicationListQuery(false, sort);
        if(sort != null && !sort.isEmpty()) {
            sb.append(" ORDER BY " + sort.toString(true));
        }
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(publicationList.getId()));
        List<Long> ids = null;
        if(page != null) {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), page.getOffet(), page.getLimit(), params);
        } else {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), params);
        }
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<Publication>();
        } else {
            return this.getObjectsByIds(ids, sort, fetchStrategy);
        }
    }



}
