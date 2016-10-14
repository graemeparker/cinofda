package com.adfonic.retargeting.redis;

import java.nio.ByteBuffer;

import org.joda.time.Instant;

public class InstantSerializer {

    private static final int BUFFER_CAPACITY = 8;

    public byte[] instantToByteArray(Instant instant) {
        long millis = instant.getMillis();
        ByteBuffer bb = ByteBuffer.allocate(BUFFER_CAPACITY);
        bb.putLong(millis);
        return bb.array();
    }

    public Instant parseInstant(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long millis = bb.getLong();
        return new Instant(millis);
    }

}
