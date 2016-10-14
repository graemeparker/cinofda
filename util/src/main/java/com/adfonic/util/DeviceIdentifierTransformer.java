package com.adfonic.util;

import org.apache.commons.codec.digest.DigestUtils;

public enum DeviceIdentifierTransformer {
    AS_IS {
        @Override
        public String transform(String deviceIdentifier) {
            return deviceIdentifier;
        }
    },
    SHA1 {
        @Override
        public String transform(String deviceIdentifier) {
            return DigestUtils.shaHex(deviceIdentifier);
        }
    },
    ;

    public abstract String transform(String deviceIdentifier);
}
