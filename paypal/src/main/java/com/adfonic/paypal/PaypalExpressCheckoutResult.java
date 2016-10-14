package com.adfonic.paypal;

import java.math.BigDecimal;
import java.util.Map;

public class PaypalExpressCheckoutResult {
    //private String token;
    private String transactionId;
    private String transactionType;
    private String paymentType;
    private String orderTime;
    private BigDecimal amount;
    private PaypalInterface.Currency currency;
    private String paymentStatus;
    private String pendingReason;
    private String reasonCode;

    PaypalExpressCheckoutResult(Map<String,String> response) {
        //token = response.get("TOKEN");
        transactionId = response.get("TRANSACTIONID");
        transactionType = response.get("TRANSACTIONTYPE");
        paymentType = response.get("PAYMENTTYPE");
        orderTime = response.get("ORDERTIME");
        amount = new BigDecimal(Double.parseDouble(response.get("AMT")));
        try {
            currency =
                PaypalInterface.Currency.valueOf(response.get("CURRENCY"));
        }catch (Exception ignored) {
            //do nothing
        }
        paymentStatus = response.get("PAYMENTSTATUS");
        pendingReason = response.get("PENDINGREASON");
        reasonCode = response.get("REASONCODE");
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getPaymentType() {
        return paymentType;
    }
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getOrderTime() {
        return orderTime;
    }
    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaypalInterface.Currency getCurrency() {
        return currency;
    }
    public void setCurrency(PaypalInterface.Currency currency) {
        this.currency = currency;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPendingReason() {
        return pendingReason;
    }
    public void setPendingReason(String pendingReason) {
        this.pendingReason = pendingReason;
    }

    public String getReasonCode() {
        return reasonCode;
    }
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    @Override
    public String toString() {
        return "PaypalExpressCheckoutResult{" +
            "transactionId=" + transactionId +
            ",transactionType=" + transactionType +
            ",paymentType=" + paymentType +
            ",orderTime=" + orderTime +
            ",amount=" + amount +
            ",currency=" + currency +
            ",paymentStatus=" + paymentStatus +
            ",pendingReason=" + pendingReason +
            ",reasonCode=" + reasonCode + "}";
    }
}
