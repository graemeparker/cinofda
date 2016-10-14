package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestStringUtils {

    @Test
    public void testConcat() {
        String s1 = "Testing" + System.currentTimeMillis();
        String s2 = "Blah";

        String expected = s1 + s2;
        String result = StringUtils.concat(s1, s2);
        assertEquals(result, expected);
    }

    @Test
    public void testCapitalize() {
        String s = "testing capitalize";
        String expected = "Testing capitalize";
        String result = StringUtils.capitalize(s);
        assertEquals(result, expected);
    }

    @Test
    public void testToThousandsString() {
        int number = 0;

        number = 499;
        assertEquals("", StringUtils.toThousandsString(number));
        number = 500;
        assertEquals("1 K", StringUtils.toThousandsString(number));
        number = 999;
        assertEquals("1 K", StringUtils.toThousandsString(number));
        number = 999499;
        assertEquals("999 K", StringUtils.toThousandsString(number));
        number = 999500;
        assertEquals("1 M", StringUtils.toThousandsString(number));
        number = 8000000;
        assertEquals("8 M", StringUtils.toThousandsString(number));
    }

}
