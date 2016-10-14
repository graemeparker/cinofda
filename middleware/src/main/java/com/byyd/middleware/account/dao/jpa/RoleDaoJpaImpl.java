package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Role;
import com.adfonic.domain.Role.RoleType;
import com.adfonic.domain.Role_;
import com.byyd.middleware.account.dao.RoleDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class RoleDaoJpaImpl extends BusinessKeyDaoJpaImpl<Role> implements RoleDao {

    protected Predicate getRolesForForRoleTypePredicate(Root<Role> root, RoleType roleType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        return criteriaBuilder.equal(root.get(Role_.roleType), roleType);
    }

    @Override
    public Long countAllRoles(RoleType roleType) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Role> root = criteriaQuery.from(Role.class);

        Predicate predicate = getRolesForForRoleTypePredicate(root, roleType);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Role> getAllRoles(RoleType roleType, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(roleType, null, null, fetchStrategy);
    }

    @Override
    public List<Role> getAllRoles(RoleType roleType, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(roleType, null, sort, fetchStrategy);
    }

    @Override
    public List<Role> getAllRoles(RoleType roleType, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(roleType, page, page.getSorting(), fetchStrategy);
    }

    protected List<Role> getAllRoles(RoleType roleType, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Role> criteriaQuery = container.getQuery();
        Root<Role> root = container.getRoot();

        if (roleType!=null){
            Predicate predicate = getRolesForForRoleTypePredicate(root, roleType);
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
