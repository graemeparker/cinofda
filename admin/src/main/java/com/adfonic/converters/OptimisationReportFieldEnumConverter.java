package com.adfonic.converters;

import javax.faces.convert.EnumConverter;
import javax.faces.convert.FacesConverter;

import com.adfonic.domain.OptimisationReportFields;

@FacesConverter(value="optimisationReportFieldConverter")
public class OptimisationReportFieldEnumConverter extends EnumConverter {

    public OptimisationReportFieldEnumConverter() {
        super(OptimisationReportFields.class);
    }
}
