package com.byyd.middleware.creative.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DisplayType;
import com.byyd.middleware.creative.dao.AssetBundleDao;
import com.byyd.middleware.creative.filter.AssetBundleFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AssetBundleDaoJpaImpl extends BusinessKeyDaoJpaImpl<AssetBundle> implements AssetBundleDao {

    protected Predicate getPredicate(Root<AssetBundle> root, AssetBundleFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Creative creative = filter.getCreative();
        List<DisplayType> includeDisplayTypes = filter.getIncludeDisplayTypes();
        List<DisplayType> excludeDisplayTypes = filter.getExcludeDisplayTypes();

        Predicate creativePredicate = null;
        Predicate includeDisplayTypesPredicate = null;
        Predicate excludeDisplayTypesPredicate = null;

        if(creative != null) {
            creativePredicate = criteriaBuilder.equal(root.get(AssetBundle_.creative), creative);
        }
        if(!CollectionUtils.isEmpty(includeDisplayTypes)) {
             includeDisplayTypesPredicate = root.get(AssetBundle_.displayType).in(includeDisplayTypes);
        }
        if(!CollectionUtils.isEmpty(excludeDisplayTypes)) {
             includeDisplayTypesPredicate = criteriaBuilder.not(root.get(AssetBundle_.displayType).in(excludeDisplayTypes));
        }

        return and(creativePredicate, includeDisplayTypesPredicate, excludeDisplayTypesPredicate);
    }

    @Override
    public Long countAll(AssetBundleFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AssetBundle> root = criteriaQuery.from(AssetBundle.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AssetBundle> getAll(AssetBundleFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<AssetBundle> getAll(AssetBundleFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<AssetBundle> getAll(AssetBundleFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<AssetBundle> getAll(AssetBundleFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AssetBundle> criteriaQuery = container.getQuery();
        Root<AssetBundle> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
