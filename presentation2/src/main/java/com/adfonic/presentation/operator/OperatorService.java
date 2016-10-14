package com.adfonic.presentation.operator;

import java.util.Collection;
import java.util.List;

import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.dto.operator.OperatorDto;

public interface OperatorService {
	public Collection<OperatorAutocompleteDto> doQuery(String search,List<CountryDto> countries, boolean mobileOperator) ;
	public OperatorAutocompleteDto getOperatorByName(String name);
	public OperatorDto getOperatorById(Long id);
	public OperatorAutocompleteDto getOperatorAutocompleteById(Long id);
}
