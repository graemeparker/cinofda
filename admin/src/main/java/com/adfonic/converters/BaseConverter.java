package com.adfonic.converters;

import javax.faces.convert.Converter;

public abstract class BaseConverter implements Converter {

    protected String cleanupValue(String value) {
        if(value == null) {
            return null;
        }
        int index = value.indexOf('[');
        if(index == -1) {
            return value;
        }
        return value.substring(0, index);
    }

}
