package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

public class TestBasicAuthUtils {
    private static final String base64(String value) {
        return Base64.encodeString(value);
    }

    @Test
    public void testGenerateAuthorizationHeader() {
        for (int k = 0; k < 1000; ++k) {
            String userid = UUID.randomUUID().toString();
            String password = UUID.randomUUID().toString();
            String expected = "Basic " + base64(userid + ":" + password);
            assertEquals(expected, BasicAuthUtils.generateAuthorizationHeader(userid, password));
        }
    }

    @Test(expected = BasicAuthUtils.AuthorizationFormatException.class)
    public void testDecodeAuthorizationHeader01_invalidFormat() {
        String authorizationHeader = "blah";
        BasicAuthUtils.decodeAuthorizationHeader(authorizationHeader);
        fail("should have thrown");
    }

    @Test(expected = BasicAuthUtils.CredentialFormatException.class)
    public void testDecodeAuthorizationHeader02_invalidCredentialFormat() {
        String authorizationHeader = "Basic " + base64("onlyUseridNoPassword:");
        BasicAuthUtils.decodeAuthorizationHeader(authorizationHeader);
        fail("should have thrown");
    }

    @Test
    public void testDecodeAuthorizationHeader03_valid() {
        for (int k = 0; k < 1000; ++k) {
            String userid = UUID.randomUUID().toString();
            String password = UUID.randomUUID().toString();
            String authorizationHeader = "Basic " + base64(userid + ":" + password);
            String[] decoded = BasicAuthUtils.decodeAuthorizationHeader(authorizationHeader);
            assertEquals(2, decoded.length);
            assertEquals(userid, decoded[0]);
            assertEquals(password, decoded[1]);
        }
    }
}
