package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CurrencyExchangeRate.class)
public abstract class CurrencyExchangeRate_ {

	public static volatile SingularAttribute<CurrencyExchangeRate, Date> lastUpdated;
	public static volatile SingularAttribute<CurrencyExchangeRate, BigDecimal> currentExchangeRate;
	public static volatile SingularAttribute<CurrencyExchangeRate, String> toCurrencyCode;
	public static volatile SingularAttribute<CurrencyExchangeRate, BigDecimal> minThreshold;
	public static volatile SingularAttribute<CurrencyExchangeRate, Long> id;
	public static volatile SingularAttribute<CurrencyExchangeRate, BigDecimal> maxThreshold;
	public static volatile SingularAttribute<CurrencyExchangeRate, Boolean> defaultConversion;
	public static volatile SingularAttribute<CurrencyExchangeRate, String> fromCurrencyCode;

}

