package com.adfonic.presentation.operator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Country;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Operator_;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.dto.operator.OperatorDto;
import com.adfonic.presentation.operator.OperatorService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.device.filter.OperatorFilter;

@Service("operatorService")
public class OperatorServiceImpl extends GenericServiceImpl implements
		OperatorService {

	@Autowired
	private CommonManager commonManager;
	@Autowired
    private DeviceManager deviceManager;
    @Autowired
    private org.dozer.Mapper mapper;

	public Collection<OperatorAutocompleteDto> doQuery(String search,List<CountryDto> country, boolean mobileOperator) {
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
	    .addLeft(Operator_.country)
	    .build();
		Collection<Operator> col;
		if(!CollectionUtils.isEmpty(country)){
		    List<Country> countries = new ArrayList<Country>();
		    for(CountryDto c : country){
		        countries.add(commonManager.getCountryById(c.getId()));
		    }
		    OperatorFilter filter = new OperatorFilter(search, LikeSpec.STARTS_WITH, false, false, mobileOperator, countries);
		    col = deviceManager.getOperators(filter, fetchStrategy);
		}
		else{
		    OperatorFilter filter = new OperatorFilter(search, LikeSpec.STARTS_WITH, false, false, mobileOperator, null);
    		col = deviceManager.getOperators(filter, fetchStrategy);
		}
		Collection<OperatorAutocompleteDto> colDto = getDtoList(OperatorAutocompleteDto.class, col);
		return colDto;
	}

	public OperatorAutocompleteDto getOperatorByName(String name) {
		FetchStrategy fetchStrategy = new FetchStrategyBuilder()
				.addLeft(Operator_.aliases).addLeft(Operator_.operatorAliases)
				.addLeft(Operator_.group).build();
		Operator op = deviceManager.getOperatorByName(name, fetchStrategy);
		OperatorAutocompleteDto opDto = getDtoObject(OperatorAutocompleteDto.class, op);
		return opDto;
	}
	
	public OperatorAutocompleteDto getOperatorAutocompleteById(Long id) {
		FetchStrategy fetchStrategy = new FetchStrategyBuilder()
				.addLeft(Operator_.aliases).addLeft(Operator_.operatorAliases)
				.addLeft(Operator_.group).build();
		Operator op = deviceManager.getOperatorById(id, fetchStrategy);
		OperatorAutocompleteDto opDto = getDtoObject(OperatorAutocompleteDto.class, op);
		return opDto;
	}
	
	public OperatorDto getOperatorById(Long id){
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
              .addLeft(Operator_.aliases)
              .addLeft(Operator_.operatorAliases)
              .addLeft(Operator_.group)
              .addLeft(Operator_.country)
              .build();
	    Operator o = deviceManager.getOperatorById(id, fetchStrategy);
	    return mapper.map(o, OperatorDto.class);
//	    return getObjectDto(OperatorDto.class, o);
	}
}
