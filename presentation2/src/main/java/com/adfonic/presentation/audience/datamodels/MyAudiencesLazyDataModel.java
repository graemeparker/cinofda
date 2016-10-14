package com.adfonic.presentation.audience.datamodels;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.MyAudienceDto;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.MyAudiencesSortBy;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel;
import com.byyd.middleware.iface.dao.Pagination;

public class MyAudiencesLazyDataModel extends AbstractLazyDataModel<MyAudienceDto> {

	private AudienceService audienceService;
	
	private AdvertiserDto advertiserDto;
	
	private List<MyAudienceDto> data;
	
	public MyAudiencesLazyDataModel(AdvertiserDto advertiserDto, AudienceService audienceService) {
		super();
		this.advertiserDto = advertiserDto;
		this.audienceService = audienceService;
	}

	@Override
	public String getRowKey(MyAudienceDto t) {
		return t.getExternalId();
	}

	@Override
	public MyAudienceDto getRowData(String rowKey) {
		if(!CollectionUtils.isEmpty(data)) {
			for(MyAudienceDto dto : data) {
				if(dto.getExternalId().equals(rowKey)) {
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public List<MyAudienceDto> loadPage(
			int firstRowIndex,
			int pageSize,
			String sortField,
			com.adfonic.presentation.datamodels.AbstractLazyDataModel.SortDirection sortDirection,
			Map<String, String> filters) {
		Pagination page = new Pagination(firstRowIndex, pageSize);
		
		String audiencePartialName = filters.get("name");
		MyAudiencesSortBy sortBy = null;
		if(!StringUtils.isEmpty(sortField)) {
			if(sortField.equals("name")) {
				sortBy = new MyAudiencesSortBy(MyAudiencesSortBy.Field.AUDIENCE_NAME, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("status")) {
				sortBy = new MyAudiencesSortBy(MyAudiencesSortBy.Field.AUDIENCE_STATUS, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("population")) {
				sortBy = new MyAudiencesSortBy(MyAudiencesSortBy.Field.AUDIENCE_POPULATION, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("type")) {
				sortBy = new MyAudiencesSortBy(MyAudiencesSortBy.Field.AUDIENCE_TYPE, sortDirection.equals(SortDirection.ASC) ? true : false);
			} 
		}
        // If the filters changed, update the total count
        if (getCurrentFilters() == null || !getCurrentFilters().equals(filters)) {
        	Long count = audienceService.countMyAudiences(
    				advertiserDto, 
    				audiencePartialName);
    		this.setTotalRowCount(count.intValue());    		
    		setCurrentFilters(filters);
        }

		data = audienceService.getMyAudiences(advertiserDto, audiencePartialName, sortBy, page);
		return data;
	}
}
