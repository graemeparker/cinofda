package com.adfonic.paypal;

import java.math.BigDecimal;

import org.junit.Test;

public class ExpressCheckoutIT {
    private PaypalInterface paypalInterface;

    @Test
    public void test() throws PaypalException {
        BigDecimal amount = new BigDecimal(123.45);
        
        String token =
            paypalInterface.setExpressCheckout("http://adfonic.com/return",
                                               "http://adfonic.com/cancel",
                                               PaypalInterface.PaymentAction.SALE,
                                               null, // description
                                               null, // invoice #
                                               amount,
                                               PaypalInterface.Currency.USD);
        System.out.println("Token = " + token);

        String url = paypalInterface.getExpressCheckoutUrl(token);
        System.out.println("Express Checkout URL: " + url);

        /*
        PaypalExpressCheckoutDetails details =
            paypalInterface.getExpressCheckoutDetails(token);
        System.out.println(details);

        PaypalExpressCheckoutResult result =
            paypalInterface.doExpressCheckoutPayment(token,
                                                     PaypalInterface.PaymentAction.SALE,
                                                     details.getPayerId(),
                                                     null, // description
                                                     null, // invoice #
                                                     amount,
                                                     PaypalInterface.Currency.USD);
        System.out.println(result);
        */
    }
}
