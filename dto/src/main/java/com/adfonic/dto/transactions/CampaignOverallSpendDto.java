package com.adfonic.dto.transactions;

import java.math.BigDecimal;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CampaignOverallSpendDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source("budget")
    private BigDecimal budget;

    @Source("amount")
    private BigDecimal amount;

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
