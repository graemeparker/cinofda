package com.adfonic.adserver.rtb.dec;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestOpenXUtil {
    private static final String ENCRYPTION_KEY = "sIxwz7yw62yrfoLGt12lIHKuYrK/S5kLuApI2BQe7Ac=";
    private static final String INTEGRITY_KEY = "v3fsVcMBMMHYzRhi7SpM0sdqwzvAxM6KPTu9OtVod5I=";

    private OpenXUtil openXUtil;
    
    @Before
    public void runBeforeEachTest() {
        openXUtil = new OpenXUtil(ENCRYPTION_KEY, INTEGRITY_KEY);
    }

    @Test
    public void microsToUSD() {
        assertEquals(0.123, OpenXUtil.cpiMicrosToCpmUSD(123).doubleValue(), 0.0);
    }
    
    @Test
    public void decryptPrice_normal() {
        assertEquals(920000, openXUtil.decryptPrice("AAABOzsyKVAEmMH-gRpXLsvlTGjJNxPQ1Iu0_Q"));
    }

    @Test(expected=IllegalStateException.class)
    public void decryptPrice_invalid() {
        openXUtil.decryptPrice("ZZZBOzsyKVAEmMH-gRpXLsvlTGjJNxPQ1Iu0_Q");
    }
}