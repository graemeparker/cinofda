package com.adfonic.presentation;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestUserInterfaceUtils {

    @Test
    public void testColorGradient() {
        String colorStart = "39B54A";
        String colorEnd = "C9F7CB";
        double percent = 0.5;

        int iCs = Integer.parseInt(colorStart, 16);
        int iCe = Integer.parseInt(colorEnd, 16);

        String result = UserInterfaceUtils.colorGradient(colorStart,colorEnd,percent);
        int ir = Integer.parseInt(result, 16);

        assertEquals((iCs + iCe) / 2, ir);
    }

    @Test
    public void testUrlEncode() {
    	String url = "http://www.domain.com/a file with spaces.txt";
    	String expected = "http%3A%2F%2Fwww.domain.com%2Fa+file+with+spaces.txt";
    	String result = UserInterfaceUtils.urlEncode(url, "");
    	assertEquals(expected, result);
    }
}
