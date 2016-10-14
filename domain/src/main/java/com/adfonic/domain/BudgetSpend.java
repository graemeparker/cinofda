package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.*;

/**
 * This class is read-only because budget expenditure is maintained via
 * stored procedures.
 */
@Embeddable
public class BudgetSpend implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name="BUDGET",nullable=false)
    private BigDecimal budget;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;

    public BigDecimal getBudget() { return budget; }

    public BigDecimal getAmount() { return amount; }
}
