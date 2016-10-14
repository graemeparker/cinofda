package com.adfonic.presentation.publication.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractNameIdDtoRowMapper;

public class PublicationAlgorithmStatusesNameIdDtoRowMapper extends AbstractNameIdDtoRowMapper {

    @Override
    protected String getNameColumnName() {
        return "publication_algo_status";
    }

}
