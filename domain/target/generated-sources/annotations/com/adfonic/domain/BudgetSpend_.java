package com.adfonic.domain;

import java.math.BigDecimal;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BudgetSpend.class)
public abstract class BudgetSpend_ {

	public static volatile SingularAttribute<BudgetSpend, BigDecimal> amount;
	public static volatile SingularAttribute<BudgetSpend, BigDecimal> budget;

}

