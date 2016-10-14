package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="PAYMENT_OPTIONS")
public class PaymentOptions extends BusinessKey {
    private static final long serialVersionUID = 2L;

    public enum PaymentType { PAYPAL, CHEQUE, WIRE_TRANSFER };

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="PAYMENT_TYPE",length=32,nullable=false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @Column(name="PAYMENT_ACCOUNT",length=255,nullable=true)
    private String paymentAccount;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="POSTAL_ADDRESS_ID",nullable=false)
    private PostalAddress postalAddress;

    public long getId() { return id; };
    
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) {
	this.paymentType = paymentType;
    }

    public String getPaymentAccount() { return paymentAccount; }
    public void setPaymentAccount(String paymentAccount) {
	this.paymentAccount = paymentAccount;
    }

    public PostalAddress getPostalAddress() {
	return postalAddress;
    }

    public void setPostalAddress(PostalAddress postalAddress) {
	this.postalAddress = postalAddress;
    }
}
