package com.adfonic.dto.transactions;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.PaymentOptions.PaymentType;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.address.PostalAddressDto;

public class PaymentOptionsDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source("paymentType")
    private PaymentType paymentType;
    @Source("paymentAccount")
    private String paymentAccount;
    @DTOCascade
    @Source("postalAddress")
    private PostalAddressDto postalAddress;

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount;
    }

    public PostalAddressDto getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddressDto postalAddress) {
        this.postalAddress = postalAddress;
    }

}
