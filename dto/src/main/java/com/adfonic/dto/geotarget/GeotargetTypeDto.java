package com.adfonic.dto.geotarget;

import java.util.Set;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.country.CountryDto;

public class GeotargetTypeDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "type")
    private String type;

    @Source(value = "name")
    private String name;

    @DTOCascade
    @Source(value = "countries")
    private Set<CountryDto> countries;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Set<CountryDto> getCountries() {
        return countries;
    }

    public void setCountries(Set<CountryDto> countries) {
        this.countries = countries;
    }
}
