package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(InvoiceKey.class)
public abstract class InvoiceKey_ {

	public static volatile SingularAttribute<InvoiceKey, String> actionType;
	public static volatile SingularAttribute<InvoiceKey, Long> campaignId;
	public static volatile SingularAttribute<InvoiceKey, Integer> gmtTimeId;
	public static volatile SingularAttribute<InvoiceKey, Integer> advertiserTimeId;
	public static volatile SingularAttribute<InvoiceKey, Long> invoiceHeaderId;

}

