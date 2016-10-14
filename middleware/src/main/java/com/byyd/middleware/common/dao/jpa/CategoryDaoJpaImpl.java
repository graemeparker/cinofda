package com.byyd.middleware.common.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Category;
import com.adfonic.domain.Category_;
import com.byyd.middleware.common.dao.CategoryDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class CategoryDaoJpaImpl extends BusinessKeyDaoJpaImpl<Category> implements CategoryDao {

    @Override
    public Long countAllForParentIsNull() {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        Predicate parentExpression = criteriaBuilder.isNull(root.get(Category_.parent));
        criteriaQuery = criteriaQuery.where(parentExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Category> getAllForParentIsNull(FetchStrategy... fetchStrategy) {
        return getAllForParentIsNull(null, null, fetchStrategy);
    }

    @Override
    public List<Category> getAllForParentIsNull(Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForParentIsNull(null, sort, fetchStrategy);
    }

    @Override
    public List<Category> getAllForParentIsNull(Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForParentIsNull(page, page.getSorting(), fetchStrategy);
    }

    protected List<Category> getAllForParentIsNull(Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Category> criteriaQuery = container.getQuery();
        Root<Category> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate parentExpression = criteriaBuilder.isNull(root.get(Category_.parent));
        criteriaQuery = criteriaQuery.where(parentExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }



    @Override
    public Long countAllForParent(Category parent) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        Predicate parentExpression = criteriaBuilder.equal(root.get(Category_.parent), parent);
        criteriaQuery = criteriaQuery.where(parentExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Category> getAllForParent(Category parent, FetchStrategy... fetchStrategy) {
        return getAllForParent(parent, null, null, fetchStrategy);
    }

    @Override
    public List<Category> getAllForParent(Category parent, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForParent(parent, null, sort, fetchStrategy);
    }

    @Override
    public List<Category> getAllForParent(Category parent, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForParent(parent, page, page.getSorting(), fetchStrategy);
    }

    protected List<Category> getAllForParent(Category parent, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Category> criteriaQuery = container.getQuery();
        Root<Category> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate parentExpression = criteriaBuilder.equal(root.get(Category_.parent), parent);
        criteriaQuery = criteriaQuery.where(parentExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    //-------------------------------------------------------------------------------------------------------------------

    @Override
    @SuppressWarnings("rawtypes")
    public Category getParentCategoryForCategory(Category category, FetchStrategy... fetchStrategy) {
        String sql = "SELECT PARENT_ID FROM CATEGORY WHERE ID = ?";
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(category.getId()));
        List resultList = this.findByNativeQueryPositionalParameters(sql, params);
        if(resultList == null || resultList.isEmpty()) {
            return null;
        }
        Number parentId = (Number)resultList.get(0);
        return this.getById(parentId.longValue(), fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Category getByIabId(String iabId, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Category> criteriaQuery = container.getQuery();
        Root<Category> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate iabIdPredicate = criteriaBuilder.equal(root.get(Category_.iabId), iabId);
        criteriaQuery = criteriaQuery.where(iabIdPredicate);
        CriteriaQuery<Category> select = criteriaQuery.select(root);

        return find(select);
    }
}
