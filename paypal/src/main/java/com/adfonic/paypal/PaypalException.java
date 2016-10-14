package com.adfonic.paypal;

public class PaypalException extends Exception {
    static final long serialVersionUID = 1L;
    
    public PaypalException(String msg) {
        super(msg);
    }
    public PaypalException(String msg, Throwable t) {
        super(msg, t);
    }
}
