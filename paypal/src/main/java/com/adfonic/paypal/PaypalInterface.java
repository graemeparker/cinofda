package com.adfonic.paypal;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.adfonic.util.CreditCardType;
import com.adfonic.util.CreditCardUtils;
import com.adfonic.util.HostUtils;
import com.adfonic.util.HttpUtils;

public class PaypalInterface {

    private static final Logger LOG = Logger.getLogger(PaypalInterface.class.getName());
    
    private static final String PARAM_VERSION        = "VERSION";
    private static final String PARAM_METHOD         = "METHOD";
    private static final String PARAM_RETURNURL      = "RETURNURL";
    private static final String PARAM_CANCELURL      = "CANCELURL";
    private static final String PARAM_NOSHIPPING     = "NOSHIPPING";
    private static final String PARAM_PAYMENTACTION  = "PAYMENTACTION";
    private static final String PARAM_AMT            = "AMT";
    private static final String PARAM_CURRENCYCODE   = "CURRENCYCODE";
    private static final String PARAM_DESC           = "DESC";
    private static final String PARAM_INVNUM         = "INVNUM";
    private static final String PARAM_IPADDRESS      = "IPADDRESS";
    private static final String PARAM_CVV2MATCH      = "CVV2MATCH";
    private static final String PARAM_AVSCODE        = "AVSCODE";
    private static final String PARAM_TRANSACTIONID  = "TRANSACTIONID";
    private static final String PARAM_CVV2           = "CVV2";
    private static final String PARAM_ACCT           = "ACCT";
    private static final String PARAM_COUNTRYCODE    = "COUNTRYCODE";
    private static final String PARAM_ZIP            = "ZIP";
    private static final String PARAM_STATE          = "STATE";
    private static final String PARAM_CITY           = "CITY";
    private static final String PARAM_STREET2        = "STREET2";
    private static final String PARAM_STREET         = "STREET";
    private static final String PARAM_LASTNAME       = "LASTNAME";
    private static final String PARAM_FIRSTNAME      = "FIRSTNAME";
    private static final String PARAM_EXPDATE        = "EXPDATE";
    private static final String PARAM_CREDITCARDTYPE = "CREDITCARDTYPE";
    private static final String PARAM_NOTE           = "NOTE";
    private static final String PARAM_REFUNDTYPE     = "REFUNDTYPE";
    private static final String PARAM_SIGNATURE      = "SIGNATURE";
    private static final String PARAM_PWD            = "PWD";
    private static final String PARAM_USER           = "USER";
    private static final String PARAM_PAYERID        = "PAYERID";
    private static final String PARAM_TOKEN          = "TOKEN";
    private static final String PARAM_SOFTDESCRIPTOR = "SOFTDESCRIPTOR";
    private static final String PARAM_REFERENCEID    = "REFERENCEID";
    private static final String PARAM_REFUNDTRANSACTIONID = "REFUNDTRANSACTIONID";
    private static final String PARAM_L_AMT0         = "L_AMT0";
    private static final String PARAM_L_LONGMESSAGE  = "L_LONGMESSAGE";
    private static final String PARAM_L_SHORTMESSAGE = "L_SHORTMESSAGE";
    private static final String PARAM_L_ERRORCODE    = "L_ERRORCODE";
    private static final String PARAM_ACK            = "ACK";
    
    private static final String METHOD_REFUND_TRANSACTION           = "RefundTransaction";
    private static final String METHOD_GET_BALANCE                  = "GetBalance";
    private static final String METHOD_DO_EXPRESS_CHECKOUT_PAYMENT  = "DoExpressCheckoutPayment";
    private static final String METHOD_GET_EXPRESS_CHECKOUT_DETAILS = "GetExpressCheckoutDetails";
    private static final String METHOD_SET_EXPRESS_CHECKOUT         = "SetExpressCheckout";
    private static final String METHOD_DO_REFERENCE_TRANSACTION     = "DoReferenceTransaction";
    private static final String METHOD_DO_DIRECT_PAYMENT            = "DoDirectPayment";
    
    private static final String NVP_API_VERSION = "56.0";
    private static final DecimalFormat EXP_DATE_MONTH_FORMAT = new DecimalFormat("00");
    private static final DecimalFormat EXP_DATE_YEAR_FORMAT  = new DecimalFormat("0000");
    private static final DecimalFormat AMOUNT_FORMAT         = new DecimalFormat("0.00");
    private static final int EXP_YEAR_TO_ADD = 2000;
    private static final int EXP_YEAR_100 = 100;
    
    private final String apiUser;
    private final String apiPassword;
    private final String apiSignature;
    private final String apiUrl;
    private final String expressCheckoutUrl;
    
    public PaypalInterface(String apiUser, String apiPassword, String apiSignature, String apiUrl, String expressCheckoutUrl) {
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
        this.apiSignature = apiSignature;
        this.apiUrl = apiUrl;
        this.expressCheckoutUrl = expressCheckoutUrl;
    }

    public enum PaymentAction {
        AUTHORIZATION("Authorization"),
        SALE("Sale");
        
        private final String value;
        
        private PaymentAction(String value) {
            this.value = value;
        }
        
        public String value() {
            return value;
        }
    }

    public enum Currency {
        AUD,
        CAD,
        CZK,
        DKK,
        EUR,
        HKD,
        HUF,
        ILS,
        JPY,
        MXN,
        NOK,
        NZD,
        PLN,
        GBP,
        SGD,
        SEK,
        CHF,
        USD,
    }

    

    public PaypalCreditCardResult doDirectPayment(PaymentAction paymentAction,
                                                  String firstName,
                                                  String lastName,
                                                  String address1,
                                                  String address2,
                                                  String city,
                                                  String state,
                                                  String postalCode,
                                                  String countryCode,
                                                  String ipAddress,
                                                  String description,
                                                  String invoiceNumber,
                                                  BigDecimal amount,
                                                  Currency currency,
                                                  CreditCardType ccType,
                                                  String ccNumber,
                                                  int expMonth,
                                                  int expYear,
                                                  String cvv) throws PaypalException {
        if (cvv != null && !"".equals(cvv) &&
            cvv.length() != ccType.getCvvLength()) {
            throw new InvalidCvvException("CVV supplied has length of " + cvv.length() + " but " + ccType.getDescription() + " requires a length of " + ccType.getCvvLength());
        }

        // Make sure the ccNumber is all digits
        String localccNumber = CreditCardUtils.onlyDigits(ccNumber);

        // Make sure it's a valid credit card number
        if (!CreditCardUtils.isValidCreditCardNumber(localccNumber)) {
            throw new InvalidCreditCardNumberException("Credit card supplied is not a valid credit card number");
        }

        String localIpAddress = ipAddress;
        if (localIpAddress == null) {
            localIpAddress = HostUtils.getHostAddress();
            LOG.info("IP address not specified, using " + localIpAddress);
        }

        // Make a MMYYYY expiration date string
        int localexpYear = expYear;
        if (localexpYear < EXP_YEAR_100) {
            localexpYear += EXP_YEAR_TO_ADD;
        }
        String expDateString = EXP_DATE_MONTH_FORMAT.format(expMonth) + EXP_DATE_YEAR_FORMAT.format(localexpYear);

        // Make an amount string with two decimal places
        String amountString = AMOUNT_FORMAT.format(amount.doubleValue());

        // Set up the result we'll be returning to the caller
        PaypalCreditCardResult result = new PaypalCreditCardResult();
        result.setCreditCardType(ccType);
        result.setLastFourDigits(localccNumber.substring(localccNumber.length() - 4));

        LOG.info("Attempting to charge $" + amountString + " to " + ccType.getDescription() + " -" + result.getLastFourDigits() + " for " + firstName + " " + lastName);

        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_DO_DIRECT_PAYMENT);
        params.put(PARAM_PAYMENTACTION, paymentAction.value());
        params.put(PARAM_IPADDRESS, localIpAddress);
        params.put(PARAM_CREDITCARDTYPE, ccType.getPaypalName());
        params.put(PARAM_EXPDATE, expDateString);
        params.put(PARAM_FIRSTNAME, StringUtils.defaultString(firstName));
        params.put(PARAM_LASTNAME, StringUtils.defaultString(lastName));
        params.put(PARAM_AMT, amountString);
        params.put(PARAM_CURRENCYCODE, currency == null ? Currency.USD.name() : currency.name());
        params.put(PARAM_DESC, StringUtils.defaultString(description));
        params.put(PARAM_INVNUM, StringUtils.defaultString(invoiceNumber));
        params.put(PARAM_STREET, StringUtils.defaultString(address1));
        params.put(PARAM_STREET2, StringUtils.defaultString(address2));
        params.put(PARAM_CITY, StringUtils.defaultString(city));
        params.put(PARAM_STATE, StringUtils.defaultString(state));
        params.put(PARAM_ZIP, StringUtils.defaultString(postalCode));
        if (countryCode != null) {
            params.put(PARAM_COUNTRYCODE, countryCode);
        }
        LOG.info("Params: " + params);

        // Add these *after* we log the params...they're "sensitive" and
        // should not appear in any logfile
        params.put(PARAM_ACCT, localccNumber);
        params.put(PARAM_CVV2, StringUtils.defaultString(cvv));

        Map<String,String> response = getNvpResponse(params, "Direct payment", true);

        // It went through...snag a few key details from the response
        String resultAmount = response.get(PARAM_AMT);

        // Make sure the response amount equals the requested amount
        if (!amountString.equals(resultAmount)) {
            LOG.warning("Result amount (" + resultAmount + ") doesn't match passed-in amount string (" + amountString + ")");
        }

        // Set the remaining fields in the result object and return it
        result.setTransactionId(response.get(PARAM_TRANSACTIONID));
        result.setAvsCode(response.get(PARAM_AVSCODE));
        result.setCvv2Match(response.get(PARAM_CVV2MATCH));
        return result;
    }
    
    public PaypalCreditCardResult doReferenceTransaction(String referenceTransactionId,
                                                         PaymentAction paymentAction,
                                                         String softDescriptor,
                                                         String ipAddress,
                                                         String description,
                                                         String invoiceNumber,
                                                         BigDecimal amount,
                                                         Currency currency) throws PaypalException {
        return doReferenceTransaction(referenceTransactionId,
                                      paymentAction,
                                      softDescriptor,
                                      null,
                                      null,
                                      null,
                                      null,
                                      null,
                                      null,
                                      null,
                                      null,
                                      ipAddress,
                                      description,
                                      invoiceNumber,
                                      amount,
                                      currency);
    }
    
    public PaypalCreditCardResult doReferenceTransaction(String referenceTransactionId,
                                                         PaymentAction paymentAction,
                                                         String softDescriptor,
                                                         String firstName,
                                                         String lastName,
                                                         String address1,
                                                         String address2,
                                                         String city,
                                                         String state,
                                                         String postalCode,
                                                         String countryCode,
                                                         String ipAddress,
                                                         String description,
                                                         String invoiceNumber,
                                                         BigDecimal amount,
                                                         Currency currency) throws PaypalException {
        String localIpAddress = ipAddress;
        if (localIpAddress == null) {
            localIpAddress = HostUtils.getHostAddress();
            LOG.info("IP address not specified, using " + localIpAddress);
        }

        // Make an amount string with two decimal places
        String amountString = AMOUNT_FORMAT.format(amount.doubleValue());

        // Set up the result we'll be returning to the caller
        PaypalCreditCardResult result = new PaypalCreditCardResult();

        LOG.info("Attempting to charge $" + amountString + " based on reference transaction ID " + referenceTransactionId + " for " + firstName + " " + lastName);
        
        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_DO_REFERENCE_TRANSACTION);
        params.put(PARAM_REFERENCEID, referenceTransactionId);
        params.put(PARAM_PAYMENTACTION, paymentAction.value());
        params.put(PARAM_SOFTDESCRIPTOR, StringUtils.defaultString(softDescriptor));
        params.put(PARAM_IPADDRESS, localIpAddress);
        params.put(PARAM_AMT, amountString);
        params.put(PARAM_CURRENCYCODE, currency == null ? Currency.USD.name() : currency.name());
        params.put(PARAM_FIRSTNAME, StringUtils.defaultString(firstName));
        params.put(PARAM_LASTNAME, StringUtils.defaultString(lastName));
        params.put(PARAM_DESC, StringUtils.defaultString(description));
        params.put(PARAM_INVNUM, StringUtils.defaultString(invoiceNumber));
        params.put(PARAM_STATE, StringUtils.defaultString(state));
        params.put(PARAM_ZIP, StringUtils.defaultString(postalCode));
        params.put(PARAM_STREET, StringUtils.defaultString(address1));
        params.put(PARAM_STREET2, StringUtils.defaultString(address2));
        params.put(PARAM_CITY, StringUtils.defaultString(city));
        if (countryCode != null) {
            params.put(PARAM_COUNTRYCODE, countryCode);
        }
        // This is a hack to get around a wacky bug that surfaced in the summer
        // of 2010.  When we first wrote all of this stuff in April 2009, it
        // worked fine without passing a CVV2 value.  Sometime between then and
        // now, it broke.  Ironically, this was a known bug in 2008 according
        // to some Paypal developer forums.  So it was fixed, and now it's
        // broken again.  But hey, Paypal seems to swallow CVV2=000, and not
        // only that, but they even return CVV2MATCH=M for that (M is matched).
        // Wacky.  Stopgap...
        params.put(PARAM_CVV2, "000");
        LOG.info("Params: " + params);

        Map<String,String> response = getNvpResponse(params, "Reference Transaction", true);
        
        // It went through...snag a few key details from the response
        String resultAmount = response.get(PARAM_AMT);

        // Make sure the response amount equals the requested amount
        if (!amountString.equals(resultAmount)) {
            LOG.warning("Result amount (" + resultAmount + ") doesn't match passed-in amount string (" + amountString + ")");
        }

        // Set the remaining fields in the result object and return it
        result.setTransactionId(response.get(PARAM_TRANSACTIONID));
        result.setAvsCode(response.get(PARAM_AVSCODE));
        result.setCvv2Match(response.get(PARAM_CVV2MATCH));
        return result;
    }

    /** First step in the express checkout scenario.  Call this method to
        generate a token for use in subsequent EC steps.
        @return a token that can be used with other EC methods
    */
    public String setExpressCheckout(String returnUrl,
                                     String cancelUrl,
                                     PaymentAction paymentAction,
                                     String description,
                                     String invoiceNumber,
                                     BigDecimal amount,
                                     Currency currency) throws PaypalException {
        // Make an amount string with two decimal places
        String amountString = AMOUNT_FORMAT.format(amount.doubleValue());

        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_SET_EXPRESS_CHECKOUT);
        params.put(PARAM_RETURNURL, returnUrl);
        params.put(PARAM_CANCELURL, cancelUrl);
        params.put(PARAM_NOSHIPPING, "1"); // Don't bother showing shipping fields
        params.put(PARAM_PAYMENTACTION, paymentAction.value());
        params.put(PARAM_AMT, amountString);
        params.put(PARAM_CURRENCYCODE, currency == null ? Currency.USD.name() : currency.name());
        params.put(PARAM_DESC, StringUtils.defaultString(description));
        params.put(PARAM_INVNUM, StringUtils.defaultString(invoiceNumber));
        LOG.info("Params: " + params);
        
        Map<String,String> response = getNvpResponse(params, "Express Checkout", true);

        // It went through...snag the "TOKEN" from the response
        String token = response.get(PARAM_TOKEN);
        if (token == null) {
            throw new PaypalException("No TOKEN returned in response: " + params);
        }
        return token;
    }

    /** 
     * Generate the full URL that should be used when bouncing a user over
     * to Paypal to authenticate for an express checkout payment.
     * 
     * @param token the token returned by setExpressCheckout
     * @return the full URL where we should send the user to authenticate
     */
    public String getExpressCheckoutUrl(String token) {
        StringBuilder bld = new StringBuilder();
        bld.append(expressCheckoutUrl);
        if (expressCheckoutUrl.indexOf('?') != -1) {
            bld.append('&');
        } else {
            bld.append('?');
        }
        bld.append("token=");
        try {
            bld.append(java.net.URLEncoder.encode(token, "utf-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            throw new PaypalRuntimeException(e);
        }
        return bld.toString();
    }

    /** Fetch EC payer/payment details from Paypal.  This should be called
        after we have first called setExpressCheckout, and then bounced the
        user over to Paypal to authenticate...they're back and we need to
        see their details.
        @param token the token generated by setExpressCheckout
        @return the set of payer/payment details
    */
    public PaypalExpressCheckoutDetails getExpressCheckoutDetails(String token) throws PaypalException {
        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_GET_EXPRESS_CHECKOUT_DETAILS);
        params.put(PARAM_TOKEN, token);
        LOG.info("Params: " + params);

        Map<String,String> response = getNvpResponse(params, "Express Checkout Details", true);
        
        return new PaypalExpressCheckoutDetails(response);
    }

    /** Actually process the express checkout payment */
    public PaypalExpressCheckoutResult doExpressCheckoutPayment(String token,
                                                                PaymentAction paymentAction,
                                                                String paypalPayerId,
                                                                String description,
                                                                String invoiceNumber,
                                                                BigDecimal amount,
                                                                Currency currency) throws PaypalException {
        // Make an amount string with two decimal places
        String amountString = AMOUNT_FORMAT.format(amount.doubleValue());

        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_DO_EXPRESS_CHECKOUT_PAYMENT);
        params.put(PARAM_TOKEN, token);
        params.put(PARAM_PAYMENTACTION, paymentAction.value());
        params.put(PARAM_PAYERID, paypalPayerId);
        params.put(PARAM_AMT, amountString);
        params.put(PARAM_CURRENCYCODE, currency == null ? Currency.USD.name() : currency.name());
        params.put(PARAM_DESC, StringUtils.defaultString(description));
        params.put(PARAM_INVNUM, StringUtils.defaultString(invoiceNumber));
        LOG.info("Params: " + params);

        Map<String,String> response = getNvpResponse(params, "Express Checkout Payment", true);
        
        return new PaypalExpressCheckoutResult(response);
    }
    
    public double getBalance() throws PaypalException {
        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_GET_BALANCE);
        
        Map<String,String> response = getNvpResponse(params, "Get Balance", false);

        return Double.parseDouble(response.get(PARAM_L_AMT0));
    }
    
    public String refundTransaction(String transactionId, String note) throws PaypalException {
        return refundTransaction(transactionId, null, note);
    }
    
    public String refundTransaction(String transactionId, Double partialAmount, String note) throws PaypalException {
        // Set up the post parameters
        Map<String,String> params = new LinkedHashMap<String,String>();
        params.put(PARAM_VERSION, NVP_API_VERSION);
        params.put(PARAM_METHOD, METHOD_REFUND_TRANSACTION);
        params.put(PARAM_TRANSACTIONID, transactionId);
        if (partialAmount != null) {
            params.put(PARAM_REFUNDTYPE, "Partial");
            // Make an amount string with two decimal places
            String amountString = AMOUNT_FORMAT.format(partialAmount);
            params.put(PARAM_AMT, amountString);
        } else {
            params.put(PARAM_REFUNDTYPE, "Full");
        }
        if (note != null && !"".equals(note)) {
            params.put(PARAM_NOTE, note);
        }
        LOG.info("Params: " + params);
        
        Map<String,String> response = getNvpResponse(params, "Refund Transaction", false);

        String refundTransactionId = response.get(PARAM_REFUNDTRANSACTIONID);
        if (refundTransactionId == null) {
            throw new PaypalRuntimeException("RefundTransaction returned no REFUNDTRANSACTIONID parameter");
        }
        return refundTransactionId;
    }
    
    private Map<String,String> getNvpResponse(Map<String,String> params, String methodName, boolean showWarning) throws PaypalException{
        addSensitiveParams(params);
        
        // Post to the Paypal HTTP resource and grab the response
        String nvpResponse = post(apiUrl, params);

        // The response should be URL-encoded name/value pairs
        LOG.info("Decoding NVP response: " + nvpResponse);
        Map<String,String> response = HttpUtils.decodeParams(nvpResponse);

        // Check the "ACK" value
        checkAck(response, methodName, showWarning);
        
        return response;
    }
    
    private void addSensitiveParams(Map<String, String> params) {
        // Add these *after* we log the params...they're "sensitive" and
        // should not appear in any logfile
        params.put(PARAM_USER, apiUser);
        params.put(PARAM_PWD, apiPassword);
        params.put(PARAM_SIGNATURE, apiSignature);
    }
    
    private void checkAck(Map<String, String> response, String methodName, boolean showWarning) {
        String ack = response.get(PARAM_ACK);
        if (!"Success".equals(ack) && !"SuccessWithWarning".equals(ack)) {
            StringBuilder bld = new StringBuilder();
            
            int k=0;
            String errorCode = response.get(PARAM_L_ERRORCODE + k);
            while(StringUtils.isNotBlank(errorCode)){
                String shortMessage = response.get(PARAM_L_SHORTMESSAGE + k);
                String longMessage = response.get(PARAM_L_LONGMESSAGE + k);

                if (bld.length() > 0) {
                    bld.append("; ");
                }
                bld.append("Error ");
                bld.append(String.valueOf(k));
                bld.append(" code=");
                bld.append(errorCode);
                bld.append(", ");
                bld.append(shortMessage);
                bld.append(" (");
                bld.append(longMessage);
                bld.append(")");
                
                k++;
                errorCode = response.get(PARAM_L_ERRORCODE + k);
            }
            String errorMessage = bld.toString();
            if (showWarning){
                LOG.warning(methodName + " failed, ACK=" + ack + "; " + errorMessage);
            }
            throw new PaypalRuntimeException(methodName + " failed, ACK=" + ack + "; " + errorMessage);
        }
    }

    private static String post(String url, Map<String,String> params) throws PaypalException {
        HttpEntity httpEntity = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(HttpUtils.toNameValuePairList(params), StandardCharsets.UTF_8.name()));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES ) {   //HTTP CODE = 300
                throw new PaypalException(httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
            }
            return EntityUtils.toString(httpEntity);
        } catch (java.io.IOException e) {
            throw new PaypalException("Failed to issue HTTP request", e);
        } finally {
            if (httpEntity != null) {
                EntityUtils.consumeQuietly(httpEntity);
            }
        }
    }
    
    public static class InvalidCvvException extends PaypalException {
        static final long serialVersionUID = 0L;
        private InvalidCvvException(String msg) {
            super(msg);
        }
    }

    public static class InvalidCreditCardNumberException extends PaypalException {
        static final long serialVersionUID = 0L;
        private InvalidCreditCardNumberException(String msg) {
            super(msg);
        }
    }
    
    public static class PaypalRuntimeException extends RuntimeException {
        static final long serialVersionUID = 0L;
        private PaypalRuntimeException(String msg) {
            super(msg);
        }
        private PaypalRuntimeException(Throwable t){
            super(t);
        }
    }
}
