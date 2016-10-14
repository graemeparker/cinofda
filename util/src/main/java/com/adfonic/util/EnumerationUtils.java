package com.adfonic.util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationUtils {
    
    private EnumerationUtils(){
    }
    
    /**
     * Make an Enumeration iterable so you can use shorthand like: for (Object
     * obj : makeIterable(e)) { ... }
     */
    public static <T> Iterable<T> makeIterable(Enumeration<T> e) {
        return new IterableEnumeration<T>(e);
    }

    static class EnumerationIterator<T> implements Iterator<T> {
        private final Enumeration<T> e;

        EnumerationIterator(Enumeration<T> e) {
            this.e = e;
        }

        @Override
        public boolean hasNext() {
            return e.hasMoreElements();
        }

        @Override
        public T next() {
            return e.nextElement();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class IterableEnumeration<T> implements Iterable<T> {
        private final Enumeration<T> e;

        public IterableEnumeration(Enumeration<T> e) {
            this.e = e;
        }

        @Override
        public Iterator<T> iterator() {
            return new EnumerationIterator<T>(e);
        }
    }
}
