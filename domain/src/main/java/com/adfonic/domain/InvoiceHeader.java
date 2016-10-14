package com.adfonic.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="INVOICE_HEADER")
public class InvoiceHeader extends BusinessKey {
    private static final long serialVersionUID = 515L;
	
    @Id @GeneratedValue @Column(name="Id")
    private long id;

    @Column(name="INVOICE_HEADER", length=32, nullable=false)
    private String invoiceHeader; 
    
    @Column(name="ADVERTISER_ID", nullable=false)
    private long advertiserId;
    
    @Column(name="TOTAL_COST", nullable=false)
    private double totalCost;
    
    @Column(name="TOTAL_TAX", nullable=false)
    private double totalTax;
    
    @Column(name="TOTAL_INVOICE", nullable=false)
    private double totalInvoice;
    
    @Column(name="INVOICE_DATE", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date invoiceDate;
    
    @Column(name="INVOICE_PERIOD", nullable=true)
    private int invoicePeriod;

    @Column(name="HEADER_NOTES",length=2048, nullable=true)
    private String HeaderNotes;
    
    @Column(name="FOOTER_NOTES",length=2048, nullable=true)
    private String footerNotes;    
    
    @Column(name="INVOICE_TIMEZONE", length=255,nullable=false)
    private String invoiceTimeZone;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getInvoiceHeader() {
		return invoiceHeader;
	}

	public void setInvoiceHeader(String invoiceHeader) {
		this.invoiceHeader = invoiceHeader;
	}

	public long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(long theadvertiserId) {
		advertiserId = theadvertiserId;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public double getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(double totalTax) {
		this.totalTax = totalTax;
	}

	public double getTotalInvoice() {
		return totalInvoice;
	}

	public void setTotalInvoice(double totalInvoice) {
		this.totalInvoice = totalInvoice;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public int getInvoicePeriod() {
		return invoicePeriod;
	}

	public void setInvoicePeriod(int invoicePeriod) {
		this.invoicePeriod = invoicePeriod;
	}

	public String getHeaderNotes() {
		return HeaderNotes;
	}

	public void setHeaderNotes(String headerNotes) {
		HeaderNotes = headerNotes;
	}

	public String getFooterNotes() {
		return footerNotes;
	}

	public void setFooterNotes(String footerNotes) {
		this.footerNotes = footerNotes;
	}

	public String getInvoiceTimeZone() {
		return invoiceTimeZone;
	}

	public void setInvoiceTimeZone(String invoiceTimeZone) {
		this.invoiceTimeZone = invoiceTimeZone;
	}
    
}
