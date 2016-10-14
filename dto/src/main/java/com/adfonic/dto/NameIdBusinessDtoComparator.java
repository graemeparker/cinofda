package com.adfonic.dto;

import java.io.Serializable;
import java.util.Comparator;

public class NameIdBusinessDtoComparator implements Comparator<NameIdBusinessDto>, Serializable {
    
    private static final long serialVersionUID = 1L;

    public enum Field {
        NAME, ID
    }

    private Field field;
    private boolean ascending;

    public NameIdBusinessDtoComparator(Field field) {
        this(field, true);
    }

    public NameIdBusinessDtoComparator(Field field, boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }

    @Override
    public int compare(NameIdBusinessDto o1, NameIdBusinessDto o2) {
        int result = 0;
        if (field.equals(Field.NAME)) {
            // new audiences may have null type
            if (o1.getName() == null && o2.getName() == null) {
                result = 0;
            } else if (o1.getName() == null && o2.getName() != null) {
                result = 1;
            } else if (o2.getName() == null && o1.getName() != null) {
                result = -1;
            } else {
                result = o1.getName().compareTo(o2.getName());
            }
        } else if (field.equals(Field.ID)) {
            result = Long.compare(o1.getId(), o2.getId());
        }
        if (ascending) {
            return result;
        } else {
            return (-1) * result;
        }
    }

}
