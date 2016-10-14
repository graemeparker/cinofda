package com.adfonic.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NeverNullConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
    static final long serialVersionUID = 0L;

    private Class<V> vClass;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NeverNullConcurrentHashMap(Class vClass) {
        this.vClass = vClass;
    }

    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        synchronized (this) {
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

    @Override
    public V put(K key, V value) {
        synchronized (this) {
            return super.put(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (this) {
            super.putAll(m);
        }
    }

    @Override
    public V remove(Object key) {
        synchronized (this) {
            return super.remove(key);
        }
    }
}
