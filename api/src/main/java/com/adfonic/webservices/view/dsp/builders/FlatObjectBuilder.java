package com.adfonic.webservices.view.dsp.builders;

public interface FlatObjectBuilder<T> {
    
    FlatObjectBuilder<T> set(String name, String value);
    
    T built();

}
