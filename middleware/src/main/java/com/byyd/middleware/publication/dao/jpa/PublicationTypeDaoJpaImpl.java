package com.byyd.middleware.publication.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.PublicationType;
import com.adfonic.domain.PublicationType_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.publication.dao.PublicationTypeDao;

@Repository
public class PublicationTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<PublicationType> implements PublicationTypeDao {
    @Override
    public PublicationType getBySystemName(String systemName, FetchStrategy ... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublicationType> criteriaQuery = container.getQuery();
        Root<PublicationType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get(PublicationType_.systemName), systemName);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<PublicationType> select = criteriaQuery.select(root);

        return find(select);
    }

    protected Predicate getSystemNamesPredicate(Root<PublicationType> root, List<String> systemNames) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate systemNamesPredicate = null;
        if(systemNames != null && !systemNames.isEmpty()) {
             for(int i = 0;i < systemNames.size();i++) {
                String systemName = systemNames.get(i);
                Predicate p = criteriaBuilder.equal(root.get(PublicationType_.systemName), systemName);
                if(i == 0) {
                    systemNamesPredicate = p;
                } else {
                    systemNamesPredicate = criteriaBuilder.or(systemNamesPredicate, p);
                }
            }
        }
        return systemNamesPredicate;
    }

    @Override
    public Long countForSystemNames(List<String> systemNames) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<PublicationType> root = criteriaQuery.from(PublicationType.class);

        Predicate predicate = getSystemNamesPredicate(root, systemNames);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<PublicationType> getForSystemNames(List<String> systemNames, FetchStrategy... fetchStrategy) {
        return getForSystemNames(systemNames, null, null, fetchStrategy);
    }

    @Override
    public List<PublicationType> getForSystemNames(List<String> systemNames, Sorting sort, FetchStrategy... fetchStrategy) {
        return getForSystemNames(systemNames, null, sort, fetchStrategy);
    }

    @Override
    public List<PublicationType> getForSystemNames(List<String> systemNames, Pagination page, FetchStrategy... fetchStrategy) {
        return getForSystemNames(systemNames, page, page.getSorting(), fetchStrategy);
    }

    protected List<PublicationType> getForSystemNames(List<String> systemNames, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<PublicationType> criteriaQuery = container.getQuery();
        Root<PublicationType> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getSystemNamesPredicate(root, systemNames);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
}
