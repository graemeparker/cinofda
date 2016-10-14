package com.adfonic.presentation.publication.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractFieldResultSetExtractor;

public class PublicationAdOpsStatusResultSetExtractor extends AbstractFieldResultSetExtractor<String> {

    @Override
    protected String getFieldResultSetColumnName() {
        return "AD_OPS_STATUS";
    }

}
