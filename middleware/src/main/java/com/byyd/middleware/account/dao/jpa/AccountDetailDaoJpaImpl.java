package com.byyd.middleware.account.dao.jpa;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Account;
import com.adfonic.domain.AccountDetail;
import com.adfonic.domain.AccountDetail_;
import com.adfonic.domain.TransactionType;
import com.adfonic.util.Range;
import com.byyd.middleware.account.dao.AccountDetailDao;
import com.byyd.middleware.account.filter.AccountDetailFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class AccountDetailDaoJpaImpl extends BusinessKeyDaoJpaImpl<AccountDetail> implements AccountDetailDao {

    protected Predicate getPredicate(Root<AccountDetail> root, AccountDetailFilter filter) {
        Account account = filter.getAccount();
        Range<Date> dateRange = filter.getDateRange();
        TransactionType transactionType = filter.getTransactionType();
        Date fromDate = filter.getFromDate();
        Date toDate = filter.getToDate();

        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate accountExpression = null;
        Predicate dateRangeExpression = null;
        Predicate transactionTypeExpression = null;
        Predicate fromDateExpression = null;
        Predicate toDateExpression = null;

        if(account != null) {
            accountExpression = criteriaBuilder.equal(root.get(AccountDetail_.account), account);
         }
        if(dateRange != null) {
            Date from = dateRange.getStart();
            Date to = dateRange.getEnd();
            Predicate fromExpression = null;
            Predicate toExpression = null;
            if(from != null) {
                fromExpression = criteriaBuilder.greaterThanOrEqualTo(root.get(AccountDetail_.transactionTime), from);
            }
            if(to != null) {
                toExpression = criteriaBuilder.lessThanOrEqualTo(root.get(AccountDetail_.transactionTime), to);
            }
            if(fromExpression != null && toExpression != null) {
                dateRangeExpression = and(fromExpression, toExpression);
            } else if(fromExpression != null) {
                dateRangeExpression = fromExpression;
            } else if(toExpression != null) {
                dateRangeExpression = toExpression;
            }
        } else {
            // No range, we can test unique dates
            if(fromDate != null) {
                fromDateExpression = criteriaBuilder.greaterThanOrEqualTo(root.get(AccountDetail_.transactionTime), fromDate);
            }
            if(toDate != null) {
                toDateExpression = criteriaBuilder.lessThanOrEqualTo(root.get(AccountDetail_.transactionTime), toDate);
            }
        }
        if(transactionType != null) {
            transactionTypeExpression = criteriaBuilder.equal(root.get(AccountDetail_.transactionType), transactionType);
        }

        return and(accountExpression, dateRangeExpression, transactionTypeExpression, fromDateExpression, toDateExpression);
    }

    @Override
    public Long countAll(AccountDetailFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AccountDetail> root = criteriaQuery.from(AccountDetail.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<AccountDetail> getAll(AccountDetailFilter filter, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<AccountDetail> getAll(AccountDetailFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<AccountDetail> getAll(AccountDetailFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return getAll(filter, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<AccountDetail> getAll(AccountDetailFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<AccountDetail> criteriaQuery = container.getQuery();
        Root<AccountDetail> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    @Override
    public BigDecimal getBalanceAsOfDate(Account account, boolean postPay, Date date) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);
        Root<AccountDetail> root = criteriaQuery.from(AccountDetail.class);

        AccountDetailFilter filter = new AccountDetailFilter();
        filter.setToDate(date);
        filter.setAccount(account);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        if(postPay) {
            criteriaQuery = criteriaQuery.select(criteriaBuilder.sum(root.get(AccountDetail_.amount)));
        } else {
            criteriaQuery = criteriaQuery.select(criteriaBuilder.sum(root.get(AccountDetail_.total)));
        }

        return executeBigDecimalAggregateFunction(criteriaQuery);
    }
}
