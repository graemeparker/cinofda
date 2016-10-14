package com.adfonic.retargeting.redis;

import com.adfonic.dmp.cache.OptOutType;

public class OptOutSerializer {

    public OptOutType parseOptOut(byte[] valBytes) {
        if (valBytes == null) {
            return OptOutType.noOptout;
        }

        String type = new String(valBytes);
        return OptOutType.valueOf(type);
    }

    public byte[] toByteArray(OptOutType optOutType) {
        return optOutType.toString().getBytes();
    }
}
