package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Operator;

public class OperatorConverter extends BaseReferenceEntityConverter<Operator> {

    public OperatorConverter() {
        super(Operator.class, "name");
    }

    @Override
    public Operator resolveEntity(String name) {
        return getDeviceManager().getOperatorByName(name);
    }
}
