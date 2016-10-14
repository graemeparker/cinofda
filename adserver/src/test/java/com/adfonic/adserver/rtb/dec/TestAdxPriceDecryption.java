package com.adfonic.adserver.rtb.dec;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestAdxPriceDecryption {

    private AdXEncUtil adxEncUtil;

    private static final String SAMP_ENCRYPTION_KEY = "sIxwz7yw62yrfoLGt12lIHKuYrK/S5kLuApI2BQe7Ac=";
    private static final String SAMP_INTEGRITY_KEY = "v3fsVcMBMMHYzRhi7SpM0sdqwzvAxM6KPTu9OtVod5I=";

    private final Map<String, BigDecimal> sampEncMap = new HashMap<>();
    {
        sampEncMap.put("UgoebQAGT7YKRQamAAAW4PvsqfVIH6vREmzUxA", BigDecimal.valueOf(0.240));
        sampEncMap.put("UgoecAABgA8KKsgOAAAnf5grTphZoBExvI_KgQ", BigDecimal.valueOf(0.398));
        sampEncMap.put("Ugq_2wAF_IQKKs-QAABuAgFrBTv-pZtJyQNHzg", BigDecimal.valueOf(0.020));
        sampEncMap.put("Ugq_2wAGBioKKm1CAABIjavAlNsKOEqYGkrTJQ", BigDecimal.valueOf(0.150));
        sampEncMap.put("Ugq_3AAEImQKhNkLAAAPtPyHU11rM3B1-XyfQw", BigDecimal.valueOf(0.140));
        sampEncMap.put("Ugq_2wAPKRYKKm5FAAARFXSeFgxpuIfIQHI_Vw", BigDecimal.valueOf(0.140));
        sampEncMap.put("Ugq_2wALgEcKKrwUAAAyWKfCQaiAvM_mTNmzLQ", BigDecimal.valueOf(0.110));
    }

    @Before
    public void setUp() {
        adxEncUtil = new AdXEncUtil(SAMP_ENCRYPTION_KEY, SAMP_INTEGRITY_KEY);
    }

    @Test
    public void testDecryptions() {
        for (Map.Entry<String, BigDecimal> entry : sampEncMap.entrySet()) {
            BigDecimal actualPrice = entry.getValue();
            String encPrice = entry.getKey();
            BigDecimal decryptedPrice = adxEncUtil.decodePrice(encPrice);
            String msg = encPrice + " --> " + decryptedPrice;
            Assert.assertTrue(msg + "not " + actualPrice, actualPrice.compareTo(decryptedPrice) == 0);
            //System.out.println(msg);
        }
    }
}
