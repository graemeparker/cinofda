package com.adfonic.util;

/**
 * Utility methods for bitmask manipulation. We could use the BitField class
 * from commons-lang, but that requires an instance. That's silly. I don't want
 * to create excess garbage just to muck with bitmasks. So static methods it is.
 */
public final class BitMaskUtils {
    
    private BitMaskUtils() {
    }

    public static int set(int bitmask, int index, boolean onOff) {
        if (onOff) {
            return bitmask | (1 << index);
        } else {
            return bitmask;
        }
    }

    public static boolean isSet(int bitmask, int index) {
        return (bitmask & (1 << index)) != 0;
    }
}
