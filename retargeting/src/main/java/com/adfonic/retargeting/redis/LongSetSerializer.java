package com.adfonic.retargeting.redis;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongSetSerializer {
    private static final int BYTES_IN_INT = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(LongSetSerializer.class);

    public Set<Long> parseSet(byte[] bytes) {
        if (bytes == null) {
            return Collections.<Long> emptySet();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        Set<Long> set = Collections.emptySet();
        try {
            set = new HashSet<>();
            do {
                int int1 = bb.getInt();
                set.add(Long.valueOf(int1));
            } while (true);
        } catch (BufferUnderflowException e) {
            // buffer read
        } catch (Exception e) {
            LOGGER.debug("deserialize failed", e);
        }
        return set;
    }

    public byte[] longSetToByteArray(Set<Long> set) {

        if (set == null || set.isEmpty()) {
            return new byte[] {};
        }

        ByteBuffer bb = ByteBuffer.allocate(set.size() * BYTES_IN_INT);
        for (Long entry : set) {
            Integer asInt = entry.intValue();
            bb.putInt(asInt);
        }
        return bb.array();
    }

}
