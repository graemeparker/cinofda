package com.adfonic.domain;

import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(InvoiceHeader.class)
public abstract class InvoiceHeader_ {

	public static volatile SingularAttribute<InvoiceHeader, String> invoiceHeader;
	public static volatile SingularAttribute<InvoiceHeader, Double> totalTax;
	public static volatile SingularAttribute<InvoiceHeader, Double> totalInvoice;
	public static volatile SingularAttribute<InvoiceHeader, String> invoiceTimeZone;
	public static volatile SingularAttribute<InvoiceHeader, Integer> invoicePeriod;
	public static volatile SingularAttribute<InvoiceHeader, String> HeaderNotes;
	public static volatile SingularAttribute<InvoiceHeader, Long> id;
	public static volatile SingularAttribute<InvoiceHeader, Date> invoiceDate;
	public static volatile SingularAttribute<InvoiceHeader, Double> totalCost;
	public static volatile SingularAttribute<InvoiceHeader, String> footerNotes;
	public static volatile SingularAttribute<InvoiceHeader, Long> advertiserId;

}

