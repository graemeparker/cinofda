package com.adfonic.domain;

import com.adfonic.domain.PaymentOptions.PaymentType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PaymentOptions.class)
public abstract class PaymentOptions_ {

	public static volatile SingularAttribute<PaymentOptions, PostalAddress> postalAddress;
	public static volatile SingularAttribute<PaymentOptions, Long> id;
	public static volatile SingularAttribute<PaymentOptions, String> paymentAccount;
	public static volatile SingularAttribute<PaymentOptions, PaymentType> paymentType;

}

