package com.adfonic.paypal;

import java.math.BigDecimal;
import java.util.Map;

public class PaypalExpressCheckoutDetails {
    private String token;
    private String description;
    private String invoiceNumber;
    private String email;
    private String payerId;
    private BigDecimal amount;
    private PaypalInterface.Currency currency;

    PaypalExpressCheckoutDetails(Map<String,String> response) {
        token = response.get("TOKEN");
        description = response.get("DESC");
        invoiceNumber = response.get("INVNUM");
        email = response.get("EMAIL");
        payerId = response.get("PAYERID");
        amount = new BigDecimal(Double.parseDouble(response.get("AMT")));
        try {
            currency =
                PaypalInterface.Currency.valueOf(response.get("CURRENCY"));
        }catch (Exception ignored) {
            // do nothing
        }
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPayerId() {
        return payerId;
    }
    public void setPayerId(String payerId) {
        this.payerId = payerId;
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

    @Override
    public String toString() {
        return "PaypalExpressCheckoutDetails{" +
            "token=" + token +
            ",description=" + description +
            ",invoiceNumber=" + invoiceNumber +
            ",email=" + email +
            ",payerId=" + payerId +
            ",amount=" + amount +
            ",currency=" + currency + "}";
    }
}
