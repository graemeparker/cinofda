package com.byyd.middleware.account.filter;

import java.util.Date;

import com.adfonic.domain.Account;
import com.adfonic.domain.TransactionType;
import com.adfonic.util.Range;

public class AccountDetailFilter {

    private Account account;
    private Range<Date> dateRange;
    private TransactionType transactionType;
    private Date fromDate;
    private Date toDate;

    public Account getAccount() {
        return account;
    }
    public AccountDetailFilter setAccount(Account account) {
        this.account = account;
        return this;
    }
    public Range<Date> getDateRange() {
        return dateRange;
    }
    public AccountDetailFilter setDateRange(Range<Date> dateRange) {
        this.dateRange = dateRange;
        return this;
    }
    public TransactionType getTransactionType() {
        return transactionType;
    }
    public AccountDetailFilter setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }
    public Date getFromDate() {
        return fromDate;
    }
    public AccountDetailFilter setFromDate(Date fromDate) {
        this.fromDate = (fromDate == null ? null : new Date(fromDate.getTime()));
        return this;
    }
    public Date getToDate() {
        return toDate;
    }
    public AccountDetailFilter setToDate(Date toDate) {
        this.toDate =  (toDate == null ? null : new Date(toDate.getTime()));
        return this;
    }


}
