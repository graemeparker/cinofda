package com.adfonic.presentation.sql.mappers;

public abstract class AbstractLongResultSetExtractor extends AbstractFieldResultSetExtractor<Long> {

    protected abstract String getFieldResultSetColumnName();

    @Override
    protected Long getFieldResultSetDefaultValue() {
        return Long.valueOf(0L);
    }

}
