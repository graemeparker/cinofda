package com.adfonic.presentation.sql.mappers;

public class RecordCountResultSetExtractor extends AbstractLongResultSetExtractor {

    @Override
    protected String getFieldResultSetColumnName() {
        return "rec_count";
    }
}
