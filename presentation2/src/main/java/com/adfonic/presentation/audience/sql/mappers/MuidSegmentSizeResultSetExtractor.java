package com.adfonic.presentation.audience.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractLongResultSetExtractor;

public class MuidSegmentSizeResultSetExtractor extends AbstractLongResultSetExtractor {

    @Override
    protected String getFieldResultSetColumnName() {
        return "segment_size";
    }

}
