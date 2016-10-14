package com.adfonic.domain;

import java.util.Collection;

public class NamedUtils {
    public static boolean contains(Collection<? extends Named> collection, String name) {
        for (Named element : collection) {
            if (element.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public static String namedCollectionToString(Collection<? extends Named> collection) {
        if (collection == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean sawFirst = false;
        for (Named n : collection) {
            if (sawFirst) {
                sb.append(',');
            } else {
                sawFirst = true;
            }
            sb.append(n.getName());
        }
        return sb.toString();
    }
}
