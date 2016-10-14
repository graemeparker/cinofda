package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name="ACCOUNT_DETAIL")
public class AccountDetail extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_ID",nullable=false)
    private Account account;
    @Column(name="TRANSACTION_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;
    @Column(name="TAX",nullable=true)
    private BigDecimal tax;
    @Column(name="TOTAL",nullable=false)
    private BigDecimal total;
    @Column(name="DESCRIPTION",length=255,nullable=true)
    private String description;
    @Column(name="REFERENCE",length=255,nullable=true)
    private String reference;
    @Column(name="OPPORTUNITY",length=255,nullable=true)
    private String opportunity;
    @Column(name="TRANSACTION_TIME",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;

    public long getId() { return id; };
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public TransactionType getTransactionType() {
	return transactionType;
    }
    public void setTransactionType(TransactionType transactionType) {
	this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
	this.description = description;
    }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

	public String getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(String opportunity) {
		this.opportunity = opportunity;
	}

	public Date getTransactionTime() { return transactionTime; }
    public void setTransactionTime(Date transactionTime) { 
	this.transactionTime = transactionTime;
    }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
