package com.adfonic.dto.transactions;

import java.math.BigDecimal;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.account.AccountDto;

public class AdvertiserAccountingDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("account")
    private AccountDto account;

    @DTOCascade
    @Source("company")
    private CompanyAccountingDto company;

    @Source("dailyBudget")
    private BigDecimal dailyBudget; // null if disabled

    @Source("notifyLimit")
    private BigDecimal notifyLimit; // null if disabled

    @Source("notifyAdditionalEmails")
    private String notifyAdditionalEmails;

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }

    public CompanyAccountingDto getCompany() {
        return company;
    }

    public void setCompany(CompanyAccountingDto company) {
        this.company = company;
    }

    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public BigDecimal getNotifyLimit() {
        return notifyLimit;
    }

    public void setNotifyLimit(BigDecimal notifyLimit) {
        this.notifyLimit = notifyLimit;
    }

    public String getNotifyAdditionalEmails() {
        return notifyAdditionalEmails;
    }

    public void setNotifyAdditionalEmails(String notifyAdditionalEmails) {
        this.notifyAdditionalEmails = notifyAdditionalEmails;
    }

}
