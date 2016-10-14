package com.adfonic.adserver.rtb.open.v1;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public class RtbEnumDeserializer<T extends Enum<T>> extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<T> {

    private static final long serialVersionUID = 1L;

    private static final transient Logger LOG = Logger.getLogger(RtbEnumDeserializer.class.getName());

    private final T[] enumConstants;
    private final int length;
    private final String enumName;

    private final Set<Integer> unRecognizedTypes = Collections.synchronizedSet(new HashSet<Integer>());

    public RtbEnumDeserializer(Class<T> enumClass) {
        super(enumClass);
        enumConstants = enumClass.getEnumConstants();
        enumName = enumClass.getSimpleName();
        length = enumConstants.length;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        int idx = jsonParser.getValueAsInt();

        if (idx > 0 && idx < length) {
            return enumConstants[idx];
        }

        if (!unRecognizedTypes.contains(idx)) {
            unRecognizedTypes.add(idx);
            LOG.warning("Unrecognized " + enumName + " id: " + idx);
        }

        return null;
    }

}
