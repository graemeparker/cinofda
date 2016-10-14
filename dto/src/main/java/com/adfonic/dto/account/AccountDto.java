package com.adfonic.dto.account;

import org.jdto.annotation.Source;

import com.adfonic.domain.AccountType;
import com.adfonic.dto.BusinessKeyDTO;

public class AccountDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source("accountType")
    private AccountType accountType;

    @Source(value = "balance")
    private String balance;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

}
