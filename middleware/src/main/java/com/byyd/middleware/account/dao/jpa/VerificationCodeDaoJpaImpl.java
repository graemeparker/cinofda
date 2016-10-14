package com.byyd.middleware.account.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.adfonic.domain.VerificationCode_;
import com.byyd.middleware.account.dao.VerificationCodeDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class VerificationCodeDaoJpaImpl extends BusinessKeyDaoJpaImpl<VerificationCode> implements VerificationCodeDao {

    @Override
    public Long countAllForUser(User user) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<VerificationCode> root = criteriaQuery.from(VerificationCode.class);

        Predicate userExpression = criteriaBuilder.equal(root.get(VerificationCode_.user), user);
        criteriaQuery = criteriaQuery.where(userExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<VerificationCode> getAllForUser(User user, FetchStrategy... fetchStrategy) {
        return getAllForUser(user, null, null, fetchStrategy);
    }

    @Override
    public List<VerificationCode> getAllForUser(User user, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllForUser(user, null, sort, fetchStrategy);
    }

    @Override
    public List<VerificationCode> getAllForUser(User user, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllForUser(user, page, page.getSorting(), fetchStrategy);
    }

    protected List<VerificationCode> getAllForUser(User user, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<VerificationCode> criteriaQuery = container.getQuery();
        Root<VerificationCode> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate userExpression = criteriaBuilder.equal(root.get(VerificationCode_.user), user);
        criteriaQuery = criteriaQuery.where(userExpression);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public VerificationCode getForCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<VerificationCode> criteriaQuery = container.getQuery();
        Root<VerificationCode> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate codeTypeExpression = criteriaBuilder.equal(root.get(VerificationCode_.codeType), codeType);
        Predicate codeValueExpression = criteriaBuilder.equal(root.get(VerificationCode_.code), codeValue);
        criteriaQuery = criteriaQuery.where(criteriaBuilder.and(codeTypeExpression, codeValueExpression));

        criteriaQuery = criteriaQuery.select(root);

         return find(criteriaQuery);
    }

    @Override
    public VerificationCode getForCodeValue(String codeValue, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<VerificationCode> criteriaQuery = container.getQuery();
        Root<VerificationCode> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate codeValueExpression = criteriaBuilder.equal(root.get(VerificationCode_.code), codeValue);
        criteriaQuery = criteriaQuery.where(criteriaBuilder.and(codeValueExpression));
        criteriaQuery = criteriaQuery.select(root);
        return find(criteriaQuery);
    }

}
