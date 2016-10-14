package com.adfonic.util;

public enum CreditCardType {
    VISA("Visa", "Visa", 3), 
    MASTERCARD("MasterCard", "MasterCard", 3), 
    DISCOVER("Discover", "Discover", 3), 
    AMEX("American Express", "Amex", 4), ;

    private String description;
    private String paypalName;
    private int cvvLength;

    private CreditCardType(String description, String paypalName, int cvvLength) {
        this.description = description;
        this.paypalName = paypalName;
        this.cvvLength = cvvLength;
    }

    public String getDescription() {
        return description;
    }

    public String getPaypalName() {
        return paypalName;
    }

    public int getCvvLength() {
        return cvvLength;
    }
}
