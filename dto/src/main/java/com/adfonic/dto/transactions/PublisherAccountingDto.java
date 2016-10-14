package com.adfonic.dto.transactions;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.account.AccountDto;

public class PublisherAccountingDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("account")
    private AccountDto account;

    @DTOCascade
    @Source("company")
    private CompanyAccountingDto company;

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

}
