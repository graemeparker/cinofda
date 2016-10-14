package com.adfonic.dto.geotarget;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.country.CountryDto;

public class GeotargetDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "country")
    private CountryDto country;
    @Source(value = "type")
    private String type;

    public CountryDto getCountry() {
        return country;
    }

    public void setCountry(CountryDto country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
