package com.adfonic.util;

import java.util.Comparator;

public interface Weighted {

    /** Simple low-to-high weight comparator */
    Comparator<Weighted> COMPARATOR = new Comparator<Weighted>() {
        @Override
        public int compare(Weighted lhs, Weighted rhs) {
            if (lhs == null) {
                return (rhs == null) ? 0 : -1;
            }
            if (rhs == null) {
                return 1;
            }
            double lweight = lhs.getWeight();
            double rweight = rhs.getWeight();
            if (lweight < rweight) {
                return -1;
            } else if (rweight < lweight) {
                return 1;
            } else {
                return 0;
            }
        }
    };
    
    double getWeight();
}
