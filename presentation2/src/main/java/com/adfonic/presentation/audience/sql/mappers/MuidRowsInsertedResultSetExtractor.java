package com.adfonic.presentation.audience.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractLongResultSetExtractor;

public class MuidRowsInsertedResultSetExtractor extends AbstractLongResultSetExtractor {

    @Override
    protected String getFieldResultSetColumnName() {
        return "rows_inserted";
    }

}
