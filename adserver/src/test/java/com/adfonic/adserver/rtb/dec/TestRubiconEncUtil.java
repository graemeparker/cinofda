package com.adfonic.adserver.rtb.dec;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestRubiconEncUtil {

    private RubiconEncUtil rbcnEncUtil;


    @Before
    public void setUp() {
        rbcnEncUtil = new RubiconEncUtil("rubicon");
    }

    char[] password = new char[] { 'r', 'u', 'b', 'i', 'c', 'o', 'n' };

    private final String[] samplePrices = new String[] {
            "06.93308",
            "01.34821",
            "12.31345",
            "00.23913",
            "102.9000",
            "149.1341" };
    

    @Test
    public void verifyCycleWithSamplePrices() {
        for (String price : samplePrices) {
            assertEquals(price, rbcnEncUtil.decrypt(rbcnEncUtil.encrypt(price)));

        }
    }

}
