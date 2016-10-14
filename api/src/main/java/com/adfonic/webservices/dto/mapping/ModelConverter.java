package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Model;

public class ModelConverter extends BaseReferenceEntityConverter<Model> {

    public ModelConverter() {
        super(Model.class, "name");
    }

    @Override
    public Model resolveEntity(String name) {
        return getDeviceManager().getModelByName(name);
    }
}
