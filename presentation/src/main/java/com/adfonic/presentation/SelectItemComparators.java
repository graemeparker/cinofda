package com.adfonic.presentation;

import java.util.Comparator;

import javax.faces.model.SelectItem;

public final class SelectItemComparators {
    
    public static final Comparator<SelectItem> LABEL_COMPARATOR = new Comparator<SelectItem>() {
        public int compare(SelectItem lhs, SelectItem rhs) {
            if (lhs == null) {
                return (rhs == null) ? 0 : 1;
            } else if (rhs == null) {
                return -1;
            }
            String lname = lhs.getLabel();
            if (lname == null) {
                return -1;
            } else {
                return lname.compareToIgnoreCase(rhs.getLabel());
            }
        }
    };
    
    private SelectItemComparators() {
    }
}
