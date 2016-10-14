package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestBitMaskUtils {
    private static final int RANGE = 17;

    @Test
    public void testSet() throws Exception {
        for (int pos = 0; pos <= RANGE; ++pos) {
            int bitmask = 0;
            bitmask = BitMaskUtils.set(bitmask, pos, true);
            assertEquals("position " + pos, 1 << pos, bitmask);
        }
    }

    @Test
    public void testIsSet() throws Exception {
        for (int k = 0; k < 32; ++k) {
            assertTrue(BitMaskUtils.isSet(0xFFFFFFFF, k));
        }

        for (int k = 0; k < 32; ++k) {
            assertFalse(BitMaskUtils.isSet(0, k));
        }

        String binaryString = "1010101010";
        int bitmask = Integer.parseInt(binaryString, 2);
        for (int k = 0; k < binaryString.length(); ++k) {
            if (binaryString.charAt(binaryString.length() - k - 1) == '1') {
                assertTrue(BitMaskUtils.isSet(bitmask, k));
            } else {
                assertFalse(BitMaskUtils.isSet(bitmask, k));
            }
        }

        for (int pos = 0; pos <= RANGE; ++pos) {
            bitmask = 1 << pos;
            assertTrue(BitMaskUtils.isSet(bitmask, pos));
            for (int p2 = 0; p2 <= RANGE; ++p2) {
                if (p2 == pos) {
                    continue;
                }
                assertFalse("pos=" + pos + ", p2=" + p2, BitMaskUtils.isSet(bitmask, p2));
            }
        }
    }
}
