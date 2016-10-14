package com.adfonic.paypal;

import com.adfonic.util.CreditCardType;

public class PaypalCreditCardResult {
    private CreditCardType creditCardType;
    private String lastFourDigits;
    private String avsCode;
    private String cvv2Match;
    private String transactionId;

    public CreditCardType getCreditCardType() {
        return creditCardType;
    }
    public void setCreditCardType(CreditCardType creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }
    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getAvsCode() {
        return avsCode;
    }
    public void setAvsCode(String avsCode) {
        this.avsCode = avsCode;
    }

    public String getCvv2Match() {
        return cvv2Match;
    }
    public void setCvv2Match(String cvv2Match) {
        this.cvv2Match = cvv2Match;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "PaypalResult{creditCardType=" + creditCardType +
            ",lastFourDigits=" + lastFourDigits +
            ",avsCode=" + avsCode +
            ",cvv2Match=" + cvv2Match +
            ",transactionId=" + transactionId + "}";
    }
}
