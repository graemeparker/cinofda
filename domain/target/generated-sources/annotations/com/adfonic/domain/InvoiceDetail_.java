package com.adfonic.domain;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(InvoiceDetail.class)
public abstract class InvoiceDetail_ {

	public static volatile SingularAttribute<InvoiceDetail, Double> advertiserVat;
	public static volatile SingularAttribute<InvoiceDetail, Double> cost;
	public static volatile SingularAttribute<InvoiceDetail, InvoiceKey> primaryKey;

}

