package com.adfonic.presentation.audience.datamodels;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.FirstPartyAudienceDeviceIdsUploadHistorySortBy;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel;
import com.byyd.middleware.iface.dao.Pagination;

public class FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel extends AbstractLazyDataModel<FirstPartyAudienceDeviceIdsUploadHistoryDto> {
	
	@Autowired
	private AudienceService audienceService;

	private FirstPartyAudienceDto firstPartyAudienceDto;
	
	private List<FirstPartyAudienceDeviceIdsUploadHistoryDto> data;
	
	public FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel(
			FirstPartyAudienceDto firstPartyAudienceDto,
			AudienceService audienceService) {
		super();
		this.firstPartyAudienceDto = firstPartyAudienceDto;
		this.audienceService = audienceService;
	}
	
	@Override
	public String getRowKey(FirstPartyAudienceDeviceIdsUploadHistoryDto t) {
		return t.getId().toString();
	}

	@Override
	public FirstPartyAudienceDeviceIdsUploadHistoryDto getRowData(String rowKey) {
		if(!CollectionUtils.isEmpty(data)) {
			for(FirstPartyAudienceDeviceIdsUploadHistoryDto dto : data) {
				if(dto.getId().toString().equals(rowKey)) {
					return dto;
				}
			}
		}
		return null;
	}

	@Override
	public List<FirstPartyAudienceDeviceIdsUploadHistoryDto> loadPage(
			int firstRowIndex,
			int pageSize,
			String sortField,
			com.adfonic.presentation.datamodels.AbstractLazyDataModel.SortDirection sortDirection,
			Map<String, String> filters) {
		Pagination page = new Pagination(firstRowIndex, pageSize);
		
		FirstPartyAudienceDeviceIdsUploadHistorySortBy sortBy = null;
		if(!StringUtils.isEmpty(sortField)) {
			if(sortField.equals("filename")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.FILENAME, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("dateTimeUploaded")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.DATE_TIME_UPLOAD, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("deviceIdentifierType")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.DEVICE_IDENTIFIER_TYPE, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("totalNumRecords")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.TOTAL_NUM_RECORDS, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("numValidatedRecords")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.NUM_VALIDATED_RECORDS, sortDirection.equals(SortDirection.ASC) ? true : false);
			} else if(sortField.equals("numInsertedRecords")) {
				sortBy = new FirstPartyAudienceDeviceIdsUploadHistorySortBy(FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.NUM_INSERTED_RECORDS, sortDirection.equals(SortDirection.ASC) ? true : false);
			} 
		}
		
        // If the filters changed, update the total count
        if (getCurrentFilters() == null || !getCurrentFilters().equals(filters)) {
         	Long count = audienceService.countFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(firstPartyAudienceDto);
    		this.setTotalRowCount(count.intValue());    		
    		setCurrentFilters(filters);
        }

		data = audienceService.getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(firstPartyAudienceDto, sortBy, page);
		return data;
	}


}
