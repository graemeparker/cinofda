package com.adfonic.presentation.publication.sql.mappers;

import com.adfonic.presentation.sql.mappers.AbstractNameIdDtoRowMapper;

public class PublicationExcludedCategoriesNameIdDtoRowMapper extends AbstractNameIdDtoRowMapper {

    @Override
    protected String getNameColumnName() {
        return "NAME";
    }

}
