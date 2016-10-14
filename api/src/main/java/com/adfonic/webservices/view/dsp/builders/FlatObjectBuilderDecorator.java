package com.adfonic.webservices.view.dsp.builders;

public abstract class FlatObjectBuilderDecorator<T> implements FlatObjectBuilder<T> {

    private FlatObjectBuilder<T> decorated;


    public FlatObjectBuilderDecorator(FlatObjectBuilder<T> decorated) {
        this.decorated = decorated;
    }


    @Override
    public FlatObjectBuilder<T> set(String name, String value) {
        decorated.set(name, value);
        return this;
    }


    @Override
    public T built() {
        return decorated.built();
    }

}
