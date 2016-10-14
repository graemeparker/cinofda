package com.adfonic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface CodedEnum<C, E extends Enum<E>> {

    public C getCode();

    // unfortunately interface fields are public
    static final Map<Class<? extends CodedEnum>, Map<?, ? extends CodedEnum>> cache = new ConcurrentHashMap<Class<? extends CodedEnum>, Map<?, ? extends CodedEnum>>();

    public static <C, E extends CodedEnum<C, ?>> E valueOf(C code, Class<E> klass) {
        Map<C, E> enumValues = (Map<C, E>) cache.get(klass);
        if (enumValues == null) {
            enumValues = new HashMap<C, E>();
            cache.put(klass, enumValues);
            E[] constants = klass.getEnumConstants();
            for (E constant : constants) {
                C c = constant.getCode();
                if (enumValues.get(c) != null) {
                    throw new IllegalStateException("Repeated code " + c + " for enum " + klass.getSimpleName() + " values '" + constant + "' and '" + enumValues.get(c) + "'");
                }
                enumValues.put(c, constant);
            }
        }
        return enumValues.get(code);
    }

}
