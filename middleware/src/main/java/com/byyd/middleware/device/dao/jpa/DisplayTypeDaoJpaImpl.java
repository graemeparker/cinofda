package com.byyd.middleware.device.dao.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.DisplayType;
import com.byyd.middleware.device.dao.DisplayTypeDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DisplayTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<DisplayType> implements DisplayTypeDao {

    @Override
    public DisplayType getBySystemName(String systemName) {
        CriteriaQueryContainer container = createCriteriaQuery();
            CriteriaQuery<DisplayType> criteriaQuery = container.getQuery();
            Root<DisplayType> root = container.getRoot();
            CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
            Predicate predicate = criteriaBuilder.equal(root.get("systemName"), systemName);
            criteriaQuery = criteriaQuery.where(predicate);
            CriteriaQuery<DisplayType> select = criteriaQuery.select(root);

            return find(select);

    }

}
