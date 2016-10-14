package com.adfonic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class TestValidationUtils {
    @Test
    public void testIsValidEmailAddress() {
        assertFalse(ValidationUtils.isValidEmailAddress(null));
        assertFalse(ValidationUtils.isValidEmailAddress(""));
        assertFalse(ValidationUtils.isValidEmailAddress(" "));
        assertFalse(ValidationUtils.isValidEmailAddress("@"));
        assertFalse(ValidationUtils.isValidEmailAddress("@blah.com"));
        assertFalse(ValidationUtils.isValidEmailAddress("."));
        assertFalse(ValidationUtils.isValidEmailAddress(".foo.com"));
        assertFalse(ValidationUtils.isValidEmailAddress("one.two@nodotlater"));
        assertFalse(ValidationUtils.isValidEmailAddress("anything@endswithdot."));
        assertFalse(ValidationUtils.isValidEmailAddress("one.two@nodotlater..com"));

        assertTrue(ValidationUtils.isValidEmailAddress("foo@bar.com"));
        assertTrue(ValidationUtils.isValidEmailAddress("foo@bar.baz.biff.com"));
        assertTrue(ValidationUtils.isValidEmailAddress("foo.bar.baz.biff@foo.bar.baz.biff"));
        assertTrue(ValidationUtils.isValidEmailAddress("mkyong@1.com"));
        assertTrue(ValidationUtils.isValidEmailAddress("a@b.c"));

        assertFalse(ValidationUtils.isValidEmailAddress("a@foo@bar.com"));
        assertFalse(ValidationUtils.isValidEmailAddress("a@foo@bar"));
        assertFalse(ValidationUtils.isValidEmailAddress("a@foo.bar@baz"));
        assertFalse(ValidationUtils.isValidEmailAddress("a@foo.bar@baz.com"));
    }

    @Test
    public void testIsValidClickToCallNumber() {
        assertFalse(ValidationUtils.isValidClickToCallNumber(null));
        assertFalse(ValidationUtils.isValidClickToCallNumber(""));
        assertFalse(ValidationUtils.isValidClickToCallNumber("1"));
        assertFalse(ValidationUtils.isValidClickToCallNumber("xxxxx"));

        // The rest is covered by isValidPhoneNumber, but for 100% code
        // coverage let's at least make sure that line gets invoked.
        assertTrue(ValidationUtils.isValidClickToCallNumber("+15025551212"));
    }

    @Test
    public void testIsValidPhoneToCallNumber() {
        assertFalse(ValidationUtils.isValidPhoneToCallNumber(null));
        assertFalse(ValidationUtils.isValidPhoneToCallNumber(""));
        assertFalse(ValidationUtils.isValidPhoneToCallNumber("+"));
        assertFalse(ValidationUtils.isValidPhoneToCallNumber("+1"));
        assertFalse(ValidationUtils.isValidPhoneToCallNumber("+B"));

        // The rest is covered by isValidPhoneNumber, but for 100% code
        // coverage let's at least make sure that line gets invoked.
        assertTrue(ValidationUtils.isValidClickToCallNumber("+15025551212"));
    }

    @Test
    public void testIsValidPhoneNumber() {
        assertFalse(ValidationUtils.isValidPhoneNumber(null));
        assertFalse(ValidationUtils.isValidPhoneNumber("1234567")); // no
                                                                    // leading +

        assertTrue(ValidationUtils.isValidPhoneNumber("+12345678900"));

        // case '(': case ')': case ' ': case '-': case '/':
        // case '0': case '1': case '2': case '3': case '4': case '5': case '6':
        // case '7': case '8': case '9':
        assertTrue(ValidationUtils.isValidPhoneNumber("+1/(234) 567-8900"));

        // default:
        assertFalse(ValidationUtils.isValidPhoneNumber("+12345678900 it should break at the space"));

        assertFalse(ValidationUtils.isValidPhoneNumber("+123456789001")); // extra
                                                                          // digit,
                                                                          // length
                                                                          // !=
                                                                          // 11

        // British numbers are code + six to ten digits
        assertTrue(ValidationUtils.isValidPhoneNumber("+44123456"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+441234567"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+4412345678"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+44123456789"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+441234567890"));
        assertTrue(ValidationUtils.isValidPhoneNumber("+4412345678901")); // too
                                                                          // long
        assertFalse(ValidationUtils.isValidPhoneNumber("+4412345")); // too
                                                                     // short

        // rest of world must be >7
        assertTrue(ValidationUtils.isValidPhoneNumber("+87654321"));
        assertFalse(ValidationUtils.isValidPhoneNumber("+7654321"));
    }

    @Test
    public void testIsValidPhoneNumber_withMustStartWithPlus() {
        assertTrue(ValidationUtils.isValidPhoneNumber("+12345678900", true));
        assertFalse(ValidationUtils.isValidPhoneNumber("12345678900", true));
        assertTrue(ValidationUtils.isValidPhoneNumber("+12345678900", false));
        assertTrue(ValidationUtils.isValidPhoneNumber("12345678900", false));
    }

    @Test
    public void testIsValidURL() {
        assertFalse(ValidationUtils.isValidURL(null));
        assertFalse(ValidationUtils.isValidURL(""));
        assertFalse(ValidationUtils.isValidURL(" "));
        assertFalse(ValidationUtils.isValidURL("http://this has space"));
        assertFalse(ValidationUtils.isValidURL("http://something.com/this has space"));
        assertFalse(ValidationUtils.isValidURL("http://something.com/whatever/this has space"));
        assertFalse(ValidationUtils.isValidURL("http://something.com/whatever/this+has space"));
        assertFalse(ValidationUtils.isValidURL("http://something.com/whatever/thishasspace "));
        assertFalse(ValidationUtils.isValidURL("http://something.com "));

        String firstPart = "http://foo.com/";
        assertTrue(ValidationUtils.isValidURL(firstPart + StringUtils.repeat("a", ValidationUtils.URL_MAX_LENGTH - firstPart.length())));
        assertFalse(ValidationUtils.isValidURL(firstPart + StringUtils.repeat("a", 1 + ValidationUtils.URL_MAX_LENGTH - firstPart.length()))); // too
                                                                                                                                               // long

        assertFalse(ValidationUtils.isValidURL("http://"));
        assertFalse(ValidationUtils.isValidURL("https://"));
        assertFalse(ValidationUtils.isValidURL("http://."));
        assertFalse(ValidationUtils.isValidURL("http://.."));
        assertFalse(ValidationUtils.isValidURL("http://../"));
        assertFalse(ValidationUtils.isValidURL("http://../something"));

        assertTrue(ValidationUtils.isValidURL("http://www.something.com/whatever"));
        assertTrue(ValidationUtils.isValidURL("https://www.something.com/whatever"));

        assertTrue(ValidationUtils.isValidURL("market://12345"));
        assertFalse(ValidationUtils.isValidURL("market://1234")); // must be at
                                                                  // least 14
                                                                  // long

        assertTrue(ValidationUtils.isValidURL("plugin://bean?something=foobar"));
        assertTrue(ValidationUtils.isValidURL("plugin://bean"));

        assertFalse(ValidationUtils.isValidURL("funkdat://whatever.com/blah"));
    }
}
