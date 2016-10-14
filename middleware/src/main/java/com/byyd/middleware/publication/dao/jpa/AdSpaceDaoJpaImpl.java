package com.byyd.middleware.publication.dao.jpa;

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

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;
import com.byyd.middleware.publication.dao.AdSpaceDao;
import com.byyd.middleware.publication.filter.AdSpaceFilter;

@Repository
public class AdSpaceDaoJpaImpl extends BusinessKeyDaoJpaImpl<AdSpace> implements AdSpaceDao {

    @Override
    public Long countAllForPublication(Publication publication) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

        Predicate publicationExpression = criteriaBuilder.equal(root.get(AdSpace_.publication), publication);
        criteriaQuery = criteriaQuery.where(publicationExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AdSpace> getAllForPublication(Publication publication, FetchStrategy... fetchStrategy) {
        return getAllForPublication(publication, null, null, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAllForPublication(Publication publication, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForPublication(publication, null, sort, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAllForPublication(Publication publication, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForPublication(publication, page, page.getSorting(), fetchStrategy);
    }

    protected List<AdSpace> getAllForPublication(Publication publication, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdSpace> criteriaQuery = container.getQuery();
        Root<AdSpace> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate publicationExpression = criteriaBuilder.equal(root.get(AdSpace_.publication), publication);
        criteriaQuery = criteriaQuery.where(publicationExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public List<AdSpace> getAllForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForPublisher(publisher, null, sort, fetchStrategy);
    }

    protected List<AdSpace> getAllForPublisher(Publisher publisher, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdSpace> criteriaQuery = container.getQuery();
        Root<AdSpace> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Join<AdSpace,Publication> publicationJoin = root.join(AdSpace_.publication, JoinType.INNER);

        Predicate publisherPredicate = criteriaBuilder.equal(publicationJoin.get(Publication_.publisher), publisher);
        criteriaQuery = criteriaQuery.where(publisherPredicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Long countAll(AdSpaceFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AdSpace> getAll(AdSpaceFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAll(AdSpaceFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAll(AdSpaceFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<AdSpace> getAll(AdSpaceFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdSpace> criteriaQuery = container.getQuery();
        Root<AdSpace> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
     }

    protected Predicate getPredicate(Root<AdSpace> root, AdSpaceFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;
        Predicate publicationPredicate = null;
        Predicate statusesPredicate = null;
        Predicate excludedIdsPredicate = null;

        if (filter.getName() != null) {
            if (filter.isNameCaseSensitive()) {
                namePredicate = criteriaBuilder.equal(root.get(AdSpace_.name), filter.getName());
            } else {
                namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(AdSpace_.name)), filter.getName().toLowerCase());
            }
        }

        if (filter.getPublication() != null) {
            publicationPredicate = criteriaBuilder.equal(root.get(AdSpace_.publication), filter.getPublication());
        }

        if (CollectionUtils.isNotEmpty(filter.getStatuses())) {
            statusesPredicate = root.get(AdSpace_.status).in(filter.getStatuses());
        }

        if (CollectionUtils.isNotEmpty(filter.getExcludedIds())) {
            excludedIdsPredicate = criteriaBuilder.not(root.get(AdSpace_.id).in(filter.getExcludedIds()));
        }

        return and(namePredicate, publicationPredicate, statusesPredicate, excludedIdsPredicate);
    }

    //------------------------------------------------------------------------------------------
    /**
     *
     * @param root
     * @param publisher
     * @return
     */
    protected Predicate getUnverifiedAdSlotsForPublisherPredicate(Root<AdSpace> root, Publisher publisher) {
        Join<AdSpace,Publication> publicationJoin = root.join(AdSpace_.publication, JoinType.LEFT);

        AdSpace.Status adSpaceStatus = AdSpace.Status.UNVERIFIED;
        List<Publication.Status> publicationStatuses = new ArrayList<Publication.Status>();
        publicationStatuses.add(Publication.Status.ACTIVE);
        publicationStatuses.add(Publication.Status.PAUSED);
        publicationStatuses.add(Publication.Status.PENDING);

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherPredicate = criteriaBuilder.equal(publicationJoin.get(Publication_.publisher), publisher);
        Predicate adSpaceStatusPredicate = criteriaBuilder.equal(root.get(AdSpace_.status), adSpaceStatus);
        Predicate nameNotNullPredicate = criteriaBuilder.isNotNull(root.get(AdSpace_.name));
        Predicate publicationStatusesPredicate = null;

        if(publicationStatuses != null && !publicationStatuses.isEmpty()) {
            for(int i = 0;i < publicationStatuses.size();i++) {
                Publication.Status status = publicationStatuses.get(i);
                Predicate p = criteriaBuilder.equal(publicationJoin.get(Publication_.status), status);
                if(i == 0) {
                    publicationStatusesPredicate = p;
                } else {
                    publicationStatusesPredicate = or(publicationStatusesPredicate, p);
                }
            }
        }
        return and(publisherPredicate, adSpaceStatusPredicate, nameNotNullPredicate, publicationStatusesPredicate);
    }
    /**
     * JDO version:
     *
     *  Query q = getPersistenceManager().newQuery(AdSpace.class, "publication.publisher == p1 && status == p2 && p3.contains(publication.status)");
     *  q.declareParameters("com.adfonic.domain.Publisher p1, com.adfonic.domain.AdSpace$Status p2, java.util.Collection p3");
     *  q.setResult("count(this)");
     *  return (long) (Long) q.executeWithArray(publisher, AdSpace.Status.UNVERIFIED, Arrays.asList(new Publication.Status[] { Publication.Status.ACTIVE, Publication.Status.PAUSED, Publication.Status.PENDING }));
     *
     * Generated SQL from JDO version:
     *
     * SELECT COUNT(`THIS`.`ID`)
     * FROM `AD_SPACE` `THIS`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLISHER` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLISHER`.`ID`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_STATUS` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_STATUS`.`ID`
     * WHERE <1> = `THIS_PUBLICATION_PUBLISHER`.`PUBLISHER_ID`
     * AND `THIS`.`STATUS` = <'UNVERIFIED'>
     * AND ((<'ACTIVE'> = `THIS_PUBLICATION_STATUS`.`STATUS` OR <'PAUSED'> = `THIS_PUBLICATION_STATUS`.`STATUS` OR <'PENDING'> = `THIS_PUBLICATION_STATUS`.`STATUS`))
     *
     * - The "name not null" was required, but not entered in the count query.
     * - There are 2 LOJs in the SQL generated by JDO, but if you look closely, it's actually the same join.
     *
     * Generated SQL from the Criteria version:
     *
     *     select
     *        count(adspace0_.ID) as col_0_0_
     *        from
     *            AD_SPACE adspace0_
     *        left outer join
     *            PUBLICATION publicatio1_
     *                on adspace0_.PUBLICATION_ID=publicatio1_.ID
     *        where
     *            publicatio1_.PUBLISHER_ID=?
     *            and adspace0_.STATUS=?
     *            and (
     *                adspace0_.NAME is not null
     *            )
     *            and (
     *                publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *            ) limit ?
     *
     * @param publisher
     * @return
     */
    @Override
    public Long countUnverifiedAdSlotsForPublisher(Publisher publisher) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

        criteriaQuery = criteriaQuery.where(getUnverifiedAdSlotsForPublisherPredicate(root, publisher));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
   }

    //------------------------------------------------------------------------------------------
    /**
     * JDO version:
     *
     *  Query q = getPersistenceManager().newQuery(AdSpace.class, "publication.publisher == p1 && status == p2 && name != null && p3.contains(publication.status)");
     *  q.declareParameters("com.adfonic.domain.Publisher p1, com.adfonic.domain.AdSpace$Status p2, java.util.Collection p3");
     *  q.setGrouping("publication");
     *  q.setResult("publication, count(this)");
     *  List<Object[]> result = (List<Object[]>) q.executeWithArray(publisher, AdSpace.Status.UNVERIFIED, Arrays.asList(new Publication.Status[] { Publication.Status.ACTIVE, Publication.Status.PAUSED, Publication.Status.PENDING }));
     *
     * Generated SQL from JDO version:
     *
     *    SELECT `THIS`.`PUBLICATION_ID`,COUNT(`THIS`.`ID`)
     *    FROM `AD_SPACE` `THIS`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLISHER` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLISHER`.`ID`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_STATUS` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_STATUS`.`ID`
     *    WHERE <1> = `THIS_PUBLICATION_PUBLISHER`.`PUBLISHER_ID`
     *    AND `THIS`.`STATUS` = <'UNVERIFIED'>
     *    AND `THIS`.`NAME` IS NOT NULL
     *    AND ((<'ACTIVE'> = `THIS_PUBLICATION_STATUS`.`STATUS` OR <'PAUSED'> = `THIS_PUBLICATION_STATUS`.`STATUS` OR <'PENDING'> = `THIS_PUBLICATION_STATUS`.`STATUS`))
     *    GROUP BY `THIS`.`PUBLICATION_ID`
     *
     * Generated SQL from the Criteria version:
     *
     *    select
     *            adspace0_.PUBLICATION_ID as col_0_0_,
     *            count(adspace0_.ID) as col_1_0_,
     *            publicatio2_.ID as ID50_,
     *            // List of Publication fields omitted
     *        from
     *            AD_SPACE adspace0_
     *        left outer join
     *            PUBLICATION publicatio1_
     *                on adspace0_.PUBLICATION_ID=publicatio1_.ID
     *        inner join
     *            PUBLICATION publicatio2_
     *                on adspace0_.PUBLICATION_ID=publicatio2_.ID
     *        where
     *            publicatio1_.PUBLISHER_ID=?
     *            and adspace0_.STATUS=?
     *            and (
     *                adspace0_.NAME is not null
     *            )
     *            and (
     *                publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *            )
     *        group by
     *            adspace0_.PUBLICATION_ID
     *
     * @param publisher
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<Publication,Long> getUnverifiedAdSlotsForPublisherCountMap(Publisher publisher) {
       CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
       CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
       Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

       criteriaQuery = criteriaQuery.where(getUnverifiedAdSlotsForPublisherPredicate(root, publisher));
       criteriaQuery = criteriaQuery.groupBy(root.get(AdSpace_.publication));
       criteriaQuery = criteriaQuery.multiselect(root.get(AdSpace_.publication), criteriaBuilder.count(root));

       Map<Publication,Long> map = new HashMap<Publication,Long>();
       List<Object[]> results = findAllObjects(criteriaQuery);
       if(results != null) {
           for(Object[] row : results) {
               Publication publication = (Publication)row[0];
               Number count = (Number)row[1];
               map.put(publication, count.longValue());
           }
       }
       return map;
    }

    //------------------------------------------------------------------------------------------
    /**
     *
     * @param root
     * @param publisher
     * @param publicationTypes
     * @return
     */
    protected Predicate getHouseAdEligibleAdSlotsForPublisherPredicate(Root<AdSpace> root, Publisher publisher, List<PublicationType> publicationTypes) {
        Join<AdSpace,Publication> publicationJoin = root.join(AdSpace_.publication, JoinType.LEFT);

        AdSpace.Status adSpaceStatus = AdSpace.Status.VERIFIED;
        List<Publication.Status> publicationStatuses = new ArrayList<Publication.Status>();
        publicationStatuses.add(Publication.Status.ACTIVE);
        publicationStatuses.add(Publication.Status.PAUSED);
        publicationStatuses.add(Publication.Status.PENDING);

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publisherPredicate = criteriaBuilder.equal(publicationJoin.get(Publication_.publisher), publisher);
        Predicate adSpaceStatusPredicate = criteriaBuilder.equal(root.get(AdSpace_.status), adSpaceStatus);
        Predicate publicationStatusesPredicate = null;
        Predicate publicationTypesPredicate = null;

        if(publicationStatuses != null && !publicationStatuses.isEmpty()) {
            for(int i = 0;i < publicationStatuses.size();i++) {
                Publication.Status status = publicationStatuses.get(i);
                Predicate p = criteriaBuilder.equal(publicationJoin.get(Publication_.status), status);
                if(i == 0) {
                    publicationStatusesPredicate = p;
                } else {
                    publicationStatusesPredicate = or(publicationStatusesPredicate, p);
                }
            }
        }

        if(publicationTypes != null && !publicationTypes.isEmpty()) {
            for(int i = 0;i < publicationTypes.size();i++) {
                PublicationType publicationType = publicationTypes.get(i);
                Predicate p = criteriaBuilder.equal(publicationJoin.get(Publication_.publicationType), publicationType);
                if(i == 0) {
                    publicationTypesPredicate = p;
                } else {
                    publicationTypesPredicate = or(publicationTypesPredicate, p);
                }
            }
        }

        return and(publisherPredicate, adSpaceStatusPredicate, publicationStatusesPredicate, publicationTypesPredicate);
    }

    /**
     * JDO Version:
     *
     *  Query q = getPersistenceManager().newQuery(AdSpace.class, "publication.publisher == p1 && status == p2 && p3.contains(publication.status) && p4.contains(publication.publicationType)");
     *  q.declareParameters("com.adfonic.domain.Publisher p1, com.adfonic.domain.AdSpace$Status p2, java.util.Collection p3, java.util.Collection p4");
     *  q.setOrdering("publication.name ascending, name ascending");
     *  q.setResult("count(this)");
     *  return (Long)q.executeWithArray(
     *          publisher,
     *          AdSpace.Status.VERIFIED,
     *          Arrays.asList(new Publication.Status[] { Publication.Status.ACTIVE, Publication.Status.PAUSED, Publication.Status.PENDING }),
     *          publicationTypes);
     *
     * Generated SQL from JDO version (with Publisher 1 and PublicationTypes 1 and 3)
     *
     *  SELECT COUNT(`THIS`.`ID`),`THIS_PUBLICATION_NAME`.`NAME`,`THIS`.`NAME`
     *    FROM `AD_SPACE` `THIS`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLISHER` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLISHER`.`ID`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_STATUS` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_STATUS`.`ID`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLICATION_TYPE` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLICATION_TYPE`.`ID`
     *    LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_NAME` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_NAME`.`ID`
     *    WHERE <1> = `THIS_PUBLICATION_PUBLISHER`.`PUBLISHER_ID`
     *    AND `THIS`.`STATUS` = <'VERIFIED'>
     *    AND ((
     *        <'ACTIVE'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     *        OR <'PAUSED'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     *        OR <'PENDING'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     *    ))
     *    AND ((
     *        <1> = `THIS_PUBLICATION_PUBLICATION_TYPE`.`PUBLICATION_TYPE_ID`
     *        OR <3> = `THIS_PUBLICATION_PUBLICATION_TYPE`.`PUBLICATION_TYPE_ID`
     *    ))
     *    ORDER BY `THIS_PUBLICATION_NAME`.`NAME`,`THIS`.`NAME`
     *
     * Generated SQL from the Criteria version:
     *
     *    select
     *            count(adspace0_.ID) as col_0_0_
     *        from
     *            AD_SPACE adspace0_
     *        left outer join
     *            PUBLICATION publicatio1_
     *                on adspace0_.PUBLICATION_ID=publicatio1_.ID
     *        where
     *            publicatio1_.PUBLISHER_ID=?
     *            and adspace0_.STATUS=?
     *            and (
     *                publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *            )
     *            and (
     *                publicatio1_.PUBLICATION_TYPE_ID=?
     *                or publicatio1_.PUBLICATION_TYPE_ID=?
     *            ) limit ?
     *
     * @param publisher
     * @param publicationTypes
     * @return
     */
    @Override
    public Long countHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

        criteriaQuery = criteriaQuery.where(getHouseAdEligibleAdSlotsForPublisherPredicate(root, publisher, publicationTypes));

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    /**
     * JDO Version:
     *
     *  Query q = getPersistenceManager().newQuery(AdSpace.class, "publication.publisher == p1 && status == p2 && p3.contains(publication.status) && p4.contains(publication.publicationType)");
     *  q.declareParameters("com.adfonic.domain.Publisher p1, com.adfonic.domain.AdSpace$Status p2, java.util.Collection p3, java.util.Collection p4");
     *  q.setOrdering("publication.name ascending, name ascending");
     *  return (List<AdSpace>) q.executeWithArray(
     *          publisher,
     *          AdSpace.Status.VERIFIED,
     *          Arrays.asList(new Publication.Status[] { Publication.Status.ACTIVE, Publication.Status.PAUSED, Publication.Status.PENDING }),
     *          publicationTypes);
     *
     * Generated SQL from JDO version (with Publisher 1 and PublicationTypes 1 and 3)
     *
     * SELECT 'com.adfonic.domain.AdSpace' AS NUCLEUS_TYPE,
     *         `THIS`.`BACKFILL_ENABLED`,`THIS`.`COLOR_SCHEME`,`THIS`.`CREATION_TIME`,`THIS`.`EXTERNAL_ID`,`THIS`.`ID`,`THIS`.`NAME` AS NUCORDER1,`THIS`.`REACTIVATION_TIME`,`THIS`.`STATUS`,`THIS`.`UNFILLED_ACTION`,`THIS`.`USE_AD_SIGNIFIER`,`THIS_PUBLICATION_NAME`.`NAME` AS NUCORDER0
     * FROM `AD_SPACE` `THIS`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLISHER` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLISHER`.`ID`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_STATUS` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_STATUS`.`ID`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_PUBLICATION_TYPE` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_PUBLICATION_TYPE`.`ID`
     * LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_NAME` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_NAME`.`ID`
     * WHERE <1> = `THIS_PUBLICATION_PUBLISHER`.`PUBLISHER_ID`
     * AND `THIS`.`STATUS` = <'VERIFIED'>
     * AND ((
     *         <'ACTIVE'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     *         OR <'PAUSED'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     *         OR <'PENDING'> = `THIS_PUBLICATION_STATUS`.`STATUS`
     * ))
     * AND ((
     *         <1> = `THIS_PUBLICATION_PUBLICATION_TYPE`.`PUBLICATION_TYPE_ID`
     *         OR <3> = `THIS_PUBLICATION_PUBLICATION_TYPE`.`PUBLICATION_TYPE_ID`
     * ))
     * ORDER BY NUCORDER0,NUCORDER1
     *
     * Generated SQL from the Criteria version:
     *
     *     select
     *            adspace0_.ID as ID6_,
     *            adspace0_.BACKFILL_ENABLED as BACKFILL2_6_,
     *            adspace0_.COLOR_SCHEME as COLOR3_6_,
     *            adspace0_.CREATION_TIME as CREATION4_6_,
     *            adspace0_.EXTERNAL_ID as EXTERNAL5_6_,
     *            adspace0_.NAME as NAME6_,
     *            adspace0_.PUBLICATION_ID as PUBLICA11_6_,
     *            adspace0_.REACTIVATION_TIME as REACTIVA7_6_,
     *            adspace0_.STATUS as STATUS6_,
     *            adspace0_.UNFILLED_ACTION as UNFILLED9_6_,
     *            adspace0_.USE_AD_SIGNIFIER as USE10_6_
     *        from
     *            AD_SPACE adspace0_
     *        left outer join
     *            PUBLICATION publicatio1_
     *                on adspace0_.PUBLICATION_ID=publicatio1_.ID
     *        where
     *            publicatio1_.PUBLISHER_ID=?
     *            and adspace0_.STATUS=?
     *            and (
     *                publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *                or publicatio1_.STATUS=?
     *            )
     *            and (
     *                publicatio1_.PUBLICATION_TYPE_ID=?
     *                or publicatio1_.PUBLICATION_TYPE_ID=?
     *            )
     *        order by
     *            publicatio1_.NAME asc,
     *            adspace0_.NAME asc
     *
     */
    protected List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdSpace> criteriaQuery = container.getQuery();
        Root<AdSpace> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getHouseAdEligibleAdSlotsForPublisherPredicate(root, publisher, publicationTypes);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, FetchStrategy... fetchStrategy) {
        return getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, null, null, fetchStrategy);
    }

    @Override
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Sorting sort, FetchStrategy... fetchStrategy) {
        return getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, null, sort, fetchStrategy);
    }

    @Override
    public List<AdSpace> getHouseAdEligibleAdSlotsForPublisher(Publisher publisher, List<PublicationType> publicationTypes, Pagination page, FetchStrategy... fetchStrategy) {
        return getHouseAdEligibleAdSlotsForPublisher(publisher, publicationTypes, page, page.getSorting(), fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    protected Predicate getAdSpacesWithNameForPublicationPredicate(Root<AdSpace> root, String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate publicationPredicate = criteriaBuilder.equal(root.get(AdSpace_.publication), publication);
        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.equal(root.get(AdSpace_.name), name);
        } else {
            namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(AdSpace_.name)), name.toLowerCase());
        }
        Predicate adSpacePredicate = null;
        if(excludeAdSpace != null) {
            adSpacePredicate =  criteriaBuilder.notEqual(root.get(AdSpace_.id), excludeAdSpace.getId());
        }
        return and(publicationPredicate, namePredicate, adSpacePredicate);
    }

    @Override
    public Long countAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AdSpace> root = criteriaQuery.from(AdSpace.class);

        Predicate predicate = this.getAdSpacesWithNameForPublicationPredicate(root, name, caseSensitive, publication, excludeAdSpace);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, FetchStrategy... fetchStrategy) {
        return getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, null, null, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, null, sort, fetchStrategy);
    }

    @Override
    public List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Pagination page, FetchStrategy... fetchStrategy) {
        return getAdSpacesWithNameForPublication(name, caseSensitive, publication, excludeAdSpace, page,page.getSorting(), fetchStrategy);
    }

    protected List<AdSpace> getAdSpacesWithNameForPublication(String name, boolean caseSensitive, Publication publication, AdSpace excludeAdSpace, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AdSpace> criteriaQuery = container.getQuery();
        Root<AdSpace> root = container.getRoot();

        Predicate predicate = this.getAdSpacesWithNameForPublicationPredicate(root, name, caseSensitive, publication, excludeAdSpace);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //------------------------------------------------------------------------------------------

    /**
     * Because we need to join with SEGMENT_AD_SPACE, and AdSpace has no reference to Segment, we cannot execute
     * this query with Criteria.
     */
    protected StringBuilder getUnallocatedAdSpaceForPublisherQuery(boolean countOnly) {
        return new StringBuilder("SELECT")
        .append(countOnly ? " COUNT(DISTINCT a.ID)" : " DISTINCT(a.ID)")
        .append(" FROM AD_SPACE a")
        .append(" INNER JOIN PUBLICATION p ON a.PUBLICATION_ID=p.ID")
        .append(" INNER JOIN PUBLICATION_TYPE pt ON pt.ID=p.PUBLICATION_TYPE_ID")
        .append(" LEFT OUTER JOIN SEGMENT_AD_SPACE sa ON sa.AD_SPACE_ID=a.ID")
        .append(" WHERE p.PUBLISHER_ID=?")
        .append(" AND a.STATUS IN ('VERIFIED','DORMANT')")
        .append(" AND p.STATUS IN ('ACTIVE','PENDING','PAUSED')")
        .append(" AND pt.SYSTEM_NAME IN ('IPHONE_APP','ANDROID_APP','IPAD_APP')")
        .append(" AND sa.AD_SPACE_ID is null");
    }

    @Override
    public Long countUnallocatedAdSpaceForPublisher(Publisher publisher) {
        StringBuilder sb = getUnallocatedAdSpaceForPublisherQuery(true);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(publisher.getId()));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(sb.toString(), params).longValue();
    }

    @Override
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, FetchStrategy... fetchStrategy) {
        return getUnallocatedAdSpaceForPublisher(publisher, null, null, fetchStrategy);
    }

    @Override
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Sorting sort, FetchStrategy... fetchStrategy) {
        return getUnallocatedAdSpaceForPublisher(publisher, null, sort, fetchStrategy);
    }

    @Override
    public List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Pagination page, FetchStrategy... fetchStrategy) {
        return getUnallocatedAdSpaceForPublisher(publisher, page,page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings("unchecked")
    protected List<AdSpace> getUnallocatedAdSpaceForPublisher(Publisher publisher, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder sb = getUnallocatedAdSpaceForPublisherQuery(false);
        if(sort != null) {
            sb.append(" " + sort.toString());
        }
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(publisher.getId()));
        List<Number> ids = null;
        if(page != null) {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), page.getOffet(), page.getLimit(), params);
        } else {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), params);
        }
        List<AdSpace> adSpaces = new ArrayList<AdSpace>();
        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                adSpaces.add(this.getById(id.longValue(), fetchStrategy));
            }
        }
        return adSpaces;
    }
}
