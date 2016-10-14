package com.adfonic.webservices.view.dsp.builders;

public class NameTransformingObjectBuilder<T> extends FlatObjectBuilderDecorator<T> {

    private ValueNameTransformer transformer;


    public NameTransformingObjectBuilder(FlatObjectBuilder<T> decorated, ValueNameTransformer transformer) {
        super(decorated);
        this.transformer = transformer;
    }


    @Override
    public FlatObjectBuilder<T> set(String name, String value) {
        return super.set(transformer.convert(name), value);
    }

}
