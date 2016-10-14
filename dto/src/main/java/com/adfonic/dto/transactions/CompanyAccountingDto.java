package com.adfonic.dto.transactions;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

/**
 * Meant to be used in transaction management
 *
 * @author pierre
 *
 */
public class CompanyAccountingDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source("individual")
    private boolean individual;

    @Source("isInvoiceDateInGMT")
    private boolean invoiceDateInGMT;

    @Source("defaultTimeZone")
    private String defaultTimeZone;

    @Source("creationTime")
    private Date creationTime;

    @Source("creditLimit")
    private BigDecimal creditLimit;

    @Source("discount")
    private BigDecimal discount;

    @Source("postPayActivationDate")
    private Date postPayActivationDate;

    @Source("postPayTermDays")
    private Integer postPayTermDays;

    @DTOCascade
    @Source("paymentOptions")
    private PaymentOptionsDto paymentOptions;

    public boolean isInvoiceDateInGMT() {
        return this.invoiceDateInGMT;
    }

    public void setInvoiceDateInGMT(boolean invoiceDateInGMT) {
        this.invoiceDateInGMT = invoiceDateInGMT;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = (creationTime == null ? null : new Date(creationTime.getTime()));
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Date getPostPayActivationDate() {
        return postPayActivationDate;
    }

    public void setPostPayActivationDate(Date postPayActivationDate) {
        this.postPayActivationDate = (postPayActivationDate == null ? null : new Date(postPayActivationDate.getTime()));
    }

    public Integer getPostPayTermDays() {
        return postPayTermDays;
    }

    public void setPostPayTermDays(Integer postPayTermDays) {
        this.postPayTermDays = postPayTermDays;
    }

    public PaymentOptionsDto getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(PaymentOptionsDto paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    public boolean isIndividual() {
        return individual;
    }

    public void setIndividual(boolean individual) {
        this.individual = individual;
    }
}
