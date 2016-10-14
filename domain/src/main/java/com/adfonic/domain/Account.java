package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.*;

@Entity
@Table(name="ACCOUNT")
public class Account extends BusinessKey {
    private static final long serialVersionUID = 2L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="ACCOUNT_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Column(name="BALANCE",nullable=false)
    private BigDecimal balance;

    {
        this.balance = BigDecimal.ZERO;
    }

    Account() {}

    public Account(AccountType accountType) {
        this.accountType = accountType;
    }

    public long getId() { return id; };

    public AccountType getAccountType() {
        return accountType;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }

    /* There's intentionally no balance setter in here, since the only
       way it ever gets updated is done via atomic database updates...so
       it plays nice with the updating going on in datacollector.  We no
       longer allow get-add-set or get-subtract-set ops on this, since
       that could possibly overwrite data with stale values. */
}
