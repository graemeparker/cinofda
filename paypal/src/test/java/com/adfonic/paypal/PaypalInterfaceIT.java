package com.adfonic.paypal;

import java.math.BigDecimal;

import org.junit.Test;

import com.adfonic.util.CreditCardType;

public class PaypalInterfaceIT {
    private PaypalInterface paypalInterface;

    @Test
    public void testPayment() throws PaypalException {
        PaypalCreditCardResult result;

        // AUTH $1.00
        result = 
            paypalInterface.doDirectPayment(PaypalInterface.PaymentAction.AUTHORIZATION,
                                            "Mahboobur",
                                            "Rahman",
                                            "1 Whatever St.",
                                            null,
                                            "Georgetown",
                                            "KY",
                                            "40324",
                                            "US",
                                            "127.0.0.1",
                                            null, // description
                                            null, // invoiceNumber
                                            new BigDecimal(1.00),
                                            PaypalInterface.Currency.USD,
                                            CreditCardType.VISA,
                                            "4675278808836033",
                                            7,
                                            2015,
                                            "411");
        System.out.println("AUTHORIZE was successful, result=" + result);
        String transactionId = result.getTransactionId();

        // Do a by-reference SALE for $123.45
        result =
            paypalInterface.doReferenceTransaction(transactionId,
                                                   PaypalInterface.PaymentAction.SALE,
                                                   "Adfonic",
                                                   "127.0.0.1",
                                                   null, // description
                                                   null, // invoiceNumber
                                                   new BigDecimal(123.45),
                                                   PaypalInterface.Currency.USD);
        System.out.println("By-reference SALE was successful, result=" + result);
    }

    @Test
    public void testGetBalance() throws PaypalException {
        double balance = paypalInterface.getBalance();
        System.out.println("Balance: " + balance);
    }
}
