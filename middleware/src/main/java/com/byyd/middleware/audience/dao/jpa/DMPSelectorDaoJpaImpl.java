package com.byyd.middleware.audience.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPSelector_;
import com.byyd.middleware.audience.dao.DMPSelectorDao;
import com.byyd.middleware.audience.filter.DMPSelectorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class DMPSelectorDaoJpaImpl extends BusinessKeyDaoJpaImpl<DMPSelector> implements DMPSelectorDao {

    protected Predicate getPredicate(Root<DMPSelector> root, DMPSelectorFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate dmpAttributePredicate = null;
        Predicate namePredicate = null;
        Predicate hiddenPredicate = null;
        Predicate muidSegmentIdPredicate = null;
        Predicate externalIdPredicate = null;
        Predicate dmpVendorIdPredicate = null;
        
        if(!StringUtils.isEmpty(filter.getName())) {
            if(filter.getNameLikeSpec() == null) {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.equal(
                            root.get(DMPSelector_.name), filter.getName());
                } else {
                    namePredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(DMPSelector_.name)),
                            filter.getName().toLowerCase());
                }
            } else {
                if (filter.isNameCaseSensitive()) {
                    namePredicate = criteriaBuilder.like(root.get(DMPSelector_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(DMPSelector_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
                }
            }
        }
        
        if(!StringUtils.isEmpty(filter.getExternalId())) {
            if(filter.getExternalIdLikeSpec() == null) {
                if (filter.isExternalIdCaseSensitive()) {
                    externalIdPredicate = criteriaBuilder.equal(
                            root.get(DMPSelector_.externalID), filter.getExternalId());
                } else {
                    externalIdPredicate = criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(DMPSelector_.externalID)),
                            filter.getExternalId().toLowerCase());
                }
            } else {
                if (filter.isExternalIdCaseSensitive()) {
                    externalIdPredicate = criteriaBuilder.like(root.get(DMPSelector_.externalID), filter.getExternalIdLikeSpec().getPattern(filter.getExternalId()));
                } else {
                    externalIdPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(DMPSelector_.externalID)), filter.getExternalIdLikeSpec().getPattern(filter.getExternalId()).toLowerCase());
                }
            }
        }

        
        if(filter.getDMPAttribute() != null) {
            dmpAttributePredicate = criteriaBuilder.equal(root.get(DMPSelector_.dmpAttribute), filter.getDMPAttribute());
        }
        
        if(filter.getHidden() != null) {
            if(filter.getHidden()) {
                hiddenPredicate = criteriaBuilder.isTrue(root.get(DMPSelector_.hidden));
            } else {
                hiddenPredicate = criteriaBuilder.isFalse(root.get(DMPSelector_.hidden));
            }
        }
        
        if(filter.getMuidSegmentId() != null) {
            muidSegmentIdPredicate = criteriaBuilder.equal(root.get(DMPSelector_.muidSegmentId), filter.getMuidSegmentId());
        }
        
        if (filter.getDmpVendorId() != null) {
            dmpVendorIdPredicate = criteriaBuilder.equal(root.get(DMPSelector_.dmpVendorId), filter.getDmpVendorId());
        }

        return and(dmpAttributePredicate, namePredicate, hiddenPredicate, muidSegmentIdPredicate, externalIdPredicate, dmpVendorIdPredicate);
    }
    
    @Override
    public Long countAll(DMPSelectorFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<DMPSelector> root = criteriaQuery.from(DMPSelector.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<DMPSelector> getAll(DMPSelectorFilter filter, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<DMPSelector> getAll(DMPSelectorFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<DMPSelector> getAll(DMPSelectorFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    protected List<DMPSelector> getAll(DMPSelectorFilter filter, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<DMPSelector> criteriaQuery = container.getQuery();
        Root<DMPSelector> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        if(predicate != null) {
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    
    //--------------------------------------------------------------------------------------------------------------------

    protected StringBuilder getDMPSelectorsForDMPAudienceQuery(boolean countOnly) {
        // Remember, to be able to use sorting on both the native and the later
        // Criteria query that gets the actual entities out, you must alias each table queried
        // using the name of the class that maps it. So, to sort by fields on DMP_SELECTOR, 
        // we must join with it and alias it to DMPSelector 
        return new StringBuilder("SELECT")
                                .append(countOnly ? " COUNT(DISTINCT DMPSelector.ID)" : " DISTINCT(DMPSelector.ID)")
                                .append(" FROM DMP_AUDIENCE_DMP_SELECTOR l, DMP_SELECTOR DMPSelector")
                                .append(" WHERE DMPSelector.ID = l.DMP_SELECTOR_ID AND l.DMP_AUDIENCE_ID=?");
    }

    @Override
    public Long countDMPSelectorsForDMPAudience(DMPAudience dmpAudience) {
        StringBuilder sb = getDMPSelectorsForDMPAudienceQuery(true);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(dmpAudience.getId()));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(sb.toString(), params).longValue();
    }

    @Override
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, FetchStrategy... fetchStrategy) {
        return this.getDMPSelectorsForDMPAudience(dmpAudience, null, null, fetchStrategy);
    }
    
    @Override
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getDMPSelectorsForDMPAudience(dmpAudience, null, sort, fetchStrategy);
    }

    @Override
    public List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getDMPSelectorsForDMPAudience(dmpAudience, page, page.getSorting(), fetchStrategy);
    }

    @SuppressWarnings("unchecked")
    protected List<DMPSelector> getDMPSelectorsForDMPAudience(DMPAudience dmpAudience, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder sb = getDMPSelectorsForDMPAudienceQuery(false);
        if(sort != null && !sort.isEmpty()) {
            sb.append(" ORDER BY " + sort.toString(true));
        }
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(dmpAudience.getId()));
        List<Long> ids = null;
        if(page != null) {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), page.getOffet(), page.getLimit(), params);
        } else {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), params);
        }
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<DMPSelector>();
        } else {
            return this.getObjectsByIds(ids, sort, fetchStrategy);
        }
    }

    @Override
    public DMPSelector getByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<DMPSelector> criteriaQuery = container.getQuery();
        Root<DMPSelector> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate externalIdPredicate = criteriaBuilder.equal(root.get(DMPSelector_.externalID), externalId);
        Predicate dmpVendorIdPredicate = criteriaBuilder.equal(root.get(DMPSelector_.dmpVendorId), dmpVendorId);

        criteriaQuery = criteriaQuery.where(and(externalIdPredicate, dmpVendorIdPredicate));
        CriteriaQuery<DMPSelector> select = criteriaQuery.select(root);

        return find(select);
    }



}
