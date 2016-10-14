package com.adfonic.reporting;

import mondrian.olap.Result;

public class Locator {
    private int[] location;
    
    public Locator(int dimensions) {
        location = new int[dimensions];
    }
    
    public Object nextValue(Result result, int dimension) {
        Object out = result.getCell(location).getValue();
        ++location[dimension];
        return out;
    }

    public Object nextValue(Result result, int dimension, Metric metric) {
        Object out = nextValue(result, dimension);
        return (out == null) ? metric.getEmptyValue() : out;
    }

    public void reset(int dimension, int value) {
        location[dimension++] = value;
        while (dimension < location.length) {
            location[dimension++] = 0;
        }
    }
}
