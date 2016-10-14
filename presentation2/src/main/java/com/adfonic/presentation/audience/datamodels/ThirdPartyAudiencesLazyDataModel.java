package com.adfonic.presentation.audience.datamodels;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.audience.ThirdPartyAudienceDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.ThirdPartyAudiencesSortBy;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel;
import com.byyd.middleware.iface.dao.Pagination;

public class ThirdPartyAudiencesLazyDataModel extends AbstractLazyDataModel<ThirdPartyAudienceDto> {
	
	private AudienceService audienceService;
	
	private CompanyDto companyDto;
	
	private List<ThirdPartyAudienceDto> data;
	
	public ThirdPartyAudiencesLazyDataModel(
			CompanyDto companyDto, 
			AudienceService audienceService) {
		super();
		this.audienceService = audienceService;
		this.companyDto = companyDto;
		
	}

	@Override
	public String getRowKey(ThirdPartyAudienceDto t) {
		return t.getExternalId();
	}

	@Override
	public ThirdPartyAudienceDto getRowData(String rowKey) {
		if(!CollectionUtils.isEmpty(data)) {
			for(ThirdPartyAudienceDto dto : data) {
				if(dto.getExternalId().equals(rowKey)) {
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public List<ThirdPartyAudienceDto> loadPage(
			int firstRowIndex,
			int pageSize,
			String sortField,
			com.adfonic.presentation.datamodels.AbstractLazyDataModel.SortDirection sortDirection,
			Map<String, String> filters) {
		Pagination page = new Pagination(firstRowIndex, pageSize);
		
		String vendorPartialName = filters.get("vendorName");
		String audiencePartialName = filters.get("thirdPartyAudienceName");
		
		ThirdPartyAudiencesSortBy sortBy = null;
		if(!StringUtils.isEmpty(sortField)) {
			if(sortField.equals("thirdPartyAudienceName")) {
				sortBy = new ThirdPartyAudiencesSortBy(ThirdPartyAudiencesSortBy.Field.AUDIENCE_NAME, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("vendorName")) {
				sortBy = new ThirdPartyAudiencesSortBy(ThirdPartyAudiencesSortBy.Field.VENDOR_NAME, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("dataRetail")) {
				sortBy = new ThirdPartyAudiencesSortBy(ThirdPartyAudiencesSortBy.Field.AUDIENCE_DATARETAIL, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("population")) {
				sortBy = new ThirdPartyAudiencesSortBy(ThirdPartyAudiencesSortBy.Field.AUDIENCE_POPULATION, sortDirection.equals(SortDirection.ASC) ? true : false);
			} 
		}
		
        // If the filters changed, update the total count
        if (getCurrentFilters() == null || !getCurrentFilters().equals(filters)) {
    		Long count = audienceService.countThirdPartyAudiencesForCompany(
    				companyDto, 
    				vendorPartialName, 
    				audiencePartialName);
    		this.setTotalRowCount(count.intValue());
    		setCurrentFilters(filters);
        }
        
		data = audienceService.getThirdPartyAudiencesForCompany(
				companyDto, 
				vendorPartialName, 
				audiencePartialName, 
				sortBy, 
				page);
		return data;
	}
	
	

}
