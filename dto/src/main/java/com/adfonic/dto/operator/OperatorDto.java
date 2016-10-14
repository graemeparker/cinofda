package com.adfonic.dto.operator;

import java.util.Set;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.country.CountryDto;

public class OperatorDto extends OperatorAutocompleteDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "country")
    private CountryDto country;

    private OperatorGroupDto group; // nullable

    private Set<String> aliases;

    private Set<OperatorAliasDto> operatorAliases;

    public CountryDto getCountry() {
        return country;
    }

    public void setCountry(CountryDto country) {
        this.country = country;
    }

    public OperatorGroupDto getGroup() {
        return group;
    }

    public void setGroup(OperatorGroupDto group) {
        this.group = group;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public Set<OperatorAliasDto> getOperatorAliases() {
        return operatorAliases;
    }

    public void setOperatorAliases(Set<OperatorAliasDto> operatorAliases) {
        this.operatorAliases = operatorAliases;
    }

}
