package com.adfonic.dto.transactions;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.TransactionType;
import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.account.AccountDto;

public class AccountDetailDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("account")
    private AccountDto account;

    @Source("transactionType")
    private TransactionType transactionType;

    @Source("amount")
    private BigDecimal amount;

    @Source("tax")
    private BigDecimal tax;

    @Source("total")
    private BigDecimal total;

    @Source("description")
    private String description;

    @Source("reference")
    private String reference;
    
    @Source("opportunity")
    private String opportunity;

    @Source("transactionTime")
    private Date transactionTime;

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(String opportunity) {
		this.opportunity = opportunity;
	}

	public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime =  (transactionTime == null ? null : new Date(transactionTime.getTime()));
    }

}
