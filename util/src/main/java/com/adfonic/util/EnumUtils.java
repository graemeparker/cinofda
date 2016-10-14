package com.adfonic.util;

import java.util.Comparator;

public abstract class EnumUtils {

    /*
     * An instance of the described comparator that can be reused
     */
    public static final Comparator<Described> DESCRIPTION_COMPARATOR = new Comparator<Described>() {
        @Override
        public int compare(Described o1, Described o2) {
            return o1.getDescription().compareTo(o2.getDescription());
        }
    };
    
    private EnumUtils(){
    }
}
