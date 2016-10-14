package com.byyd.middleware.iface.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.byyd.middleware.iface.dao.SortOrder.Direction;

/**
 * Sort option for queries.
 */
public class Sorting implements Iterable<SortOrder>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    private List<SortOrder> sortOrders;


    public Sorting(SortOrder... sortOrders) {
        this(Arrays.asList(sortOrders));
    }


    public Sorting(List<SortOrder> sortOrders) {
        if (null == sortOrders || sortOrders.isEmpty()) {
            throw new IllegalArgumentException("No Sort Order provided");
        }
        this.sortOrders = sortOrders;
    }


    public Sorting(String... fieldNames) {
        this(DEFAULT_DIRECTION, fieldNames);
    }


    public Sorting(Direction direction, String... fieldNames) {
        this(direction, fieldNames == null ? new ArrayList<String>() : Arrays.asList(fieldNames));
    }


    public Sorting(Direction direction, List<String> fieldNames) {

        if (fieldNames == null || fieldNames.isEmpty()) {
            throw new IllegalArgumentException("No Field Name provided");
        }

        this.sortOrders = new ArrayList<SortOrder>(fieldNames.size());

        for (String property : fieldNames) {
            this.sortOrders.add(new SortOrder(direction, property));
        }
    }

    public SortOrder getSortOrderForFieldName(String fieldName) {
        for (SortOrder sortOrder : sortOrders) {
            if (sortOrder.getFieldName().equals(fieldName)) {
                return sortOrder;
            }
        }
        return null;
    }


    @Override
    public Iterator<com.byyd.middleware.iface.dao.SortOrder> iterator() {
        return this.sortOrders.iterator();
    }

    public List<SortOrder> getSortOrders() {
        return this.sortOrders;
    }

    @Override
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean translateVariableNames) {
        StringBuilder buffer = new StringBuilder();
        for(int i = 0;i < sortOrders.size();i++) {
            if(i > 0) {
                buffer.append(", ");
            }
            SortOrder sortOrder = sortOrders.get(i);
            buffer.append(sortOrder.toString(translateVariableNames));
        }
        return buffer.toString();
    }
    
    public int size() {
        if(sortOrders == null) {
            return 0;
        }
        return sortOrders.size();
    }
    
    public boolean isEmpty() {
        if(sortOrders == null) {
            return true;
        }
        return sortOrders.isEmpty();
    }
    
    public Set<Class<?>> getClazzes() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        if(sortOrders != null) {
            for(SortOrder sortOrder : sortOrders) {
                set.add(sortOrder.getClazz());
            }
        }
        return set;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Sorting)) {
            return false;
        }
        Sorting that = (Sorting) obj;
        return this.sortOrders.equals(that.sortOrders);
    }


    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + sortOrders.hashCode();
        return result;
    }
}

