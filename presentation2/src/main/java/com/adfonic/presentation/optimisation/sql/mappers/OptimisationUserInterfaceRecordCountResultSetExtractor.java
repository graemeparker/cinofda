package com.adfonic.presentation.optimisation.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractLongResultSetExtractor;

/**
 * Mapper for a count stored procedure
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceRecordCountResultSetExtractor extends AbstractLongResultSetExtractor {

    @Override
    protected String getFieldResultSetColumnName() {
        return "result_set_record_count";
    }
}