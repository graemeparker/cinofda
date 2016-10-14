package com.byyd.middleware.device.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.DeviceIdentifierType_;
import com.byyd.middleware.device.dao.DeviceIdentifierTypeDao;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class DeviceIdentifierTypeDaoJpaImpl extends BusinessKeyDaoJpaImpl<DeviceIdentifierType> implements DeviceIdentifierTypeDao {

    @Override
    public List<DeviceIdentifierType> getAllNonHidden() {
        return getAllNonHidden(new Sorting(SortOrder.asc("precedenceOrder")));
    }

    @Override
    public List<DeviceIdentifierType> getAllNonHidden(Sorting sort) {
        CriteriaQueryContainer container = createCriteriaQuery();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Root<DeviceIdentifierType> root = container.getRoot();
        Predicate predicate = criteriaBuilder.equal(root.get(DeviceIdentifierType_.hidden), false);
        CriteriaQuery<DeviceIdentifierType> criteriaQuery = container.getQuery();
        criteriaQuery = criteriaQuery.where(predicate);
        criteriaQuery = criteriaQuery.select(root);
        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery);
    }
}
