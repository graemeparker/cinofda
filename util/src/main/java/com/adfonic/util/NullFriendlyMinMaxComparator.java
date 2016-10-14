package com.adfonic.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Provides a Comparator that effectively discards null values in collections
 * being evaluated by aggregate functions Collections.min() and
 * Collections.max().
 *
 * This class is designed only to work with the Collections.min() and
 * Collections.max() static methods. If an instance of this class is used for
 * any other purpose, such as sorting, its behaviour is unpredictable.
 */
public class NullFriendlyMinMaxComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(T lhs, T rhs) {
        if (lhs == null || rhs == null) {
            return 0; // pretend they're equal
        }
        return lhs.compareTo(rhs);
    }
}
