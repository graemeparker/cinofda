package com.adfonic.util;

import java.util.HashMap;

public class NeverNullHashMap<K, V> extends HashMap<K, V> {
    static final long serialVersionUID = 0L;

    private final Class<V> vClass;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NeverNullHashMap(Class vClass) {
        this.vClass = vClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            try {
                value = vClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            super.put((K) key, value);
        }
        return value;
    }
}
