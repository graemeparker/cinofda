package com.adfonic.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicAuthUtils {
    // The Authorization header is like:
    // Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile("^Basic\\s+(.+)$");

    // The base64 decoded data is like: Aladdin:open sesame
    static final Pattern DECODED_USER_ID_PWD_PATTERN = Pattern.compile("^([^:]+):(.+)$");
    
    private BasicAuthUtils(){
    }

    /**
     * Generate the RFC2617-compliant value of an Authorization header for basic
     * auth using a given userid and password
     */
    public static String generateAuthorizationHeader(String userid, String password) {
        return "Basic " + Base64.encodeString(userid + ":" + password);
    }

    /**
     * Decode a supplied basic auth Authorization header value into its
     * respective decoded plain-text userid and password
     * 
     * @return a two-element String array containing the userid and password
     * @throws InvalidArgumentException
     *             if the supplied header value does not conform to the format
     *             as specified in RFC2617
     */
    public static String[] decodeAuthorizationHeader(String authorization) {
        Matcher matcher = AUTHORIZATION_HEADER_PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            throw new AuthorizationFormatException("Invalid authorization header format");
        }

        String encodedCredentials = matcher.group(1);
        String decodedCredentials = Base64.decodeString(encodedCredentials);
        matcher = DECODED_USER_ID_PWD_PATTERN.matcher(decodedCredentials);
        if (!matcher.matches()) {
            throw new CredentialFormatException("Invalid Basic credentials format");
        }

        return new String[] { matcher.group(1), matcher.group(2) };
    }

    public static class BasicAuthException extends IllegalArgumentException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        BasicAuthException(String msg) {
            super(msg);
        }
    }

    public static class AuthorizationFormatException extends BasicAuthException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        AuthorizationFormatException(String msg) {
            super(msg);
        }
    }

    public static class CredentialFormatException extends BasicAuthException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        CredentialFormatException(String msg) {
            super(msg);
        }
    }
}
