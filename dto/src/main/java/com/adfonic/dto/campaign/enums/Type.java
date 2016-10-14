package com.adfonic.dto.campaign.enums;

public enum Type {

    MASSIVE(com.adfonic.domain.OperatorAlias.Type.MASSIVE), 
    QUOVA(com.adfonic.domain.OperatorAlias.Type.QUOVA);

    private com.adfonic.domain.OperatorAlias.Type type;

    private Type(com.adfonic.domain.OperatorAlias.Type type) {
        this.type = type;
    }

    protected com.adfonic.domain.OperatorAlias.Type getType() {
        return type;
    }
}
