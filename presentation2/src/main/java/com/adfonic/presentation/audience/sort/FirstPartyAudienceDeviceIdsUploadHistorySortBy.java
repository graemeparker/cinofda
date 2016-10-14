package com.adfonic.presentation.audience.sort;

import java.util.Comparator;

import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

public class FirstPartyAudienceDeviceIdsUploadHistorySortBy implements Comparator<FirstPartyAudienceDeviceIdsUploadHistoryDto> {

	public enum Field {
		FILENAME, DATE_TIME_UPLOAD, DEVICE_IDENTIFIER_TYPE, TOTAL_NUM_RECORDS, NUM_VALIDATED_RECORDS, NUM_INSERTED_RECORDS
	}

	private Field field;
	private boolean ascending;
	
	public FirstPartyAudienceDeviceIdsUploadHistorySortBy(Field field) {
		this(field, true);
	}
	
	public FirstPartyAudienceDeviceIdsUploadHistorySortBy(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public Field getField() {
		return field;
	}

	public boolean isAscending() {
		return ascending;
	}
	
	protected String getFieldName() {
		switch(field) {
		case FILENAME: return "filename";
		case DATE_TIME_UPLOAD: return "dateTimeUploaded";
		case DEVICE_IDENTIFIER_TYPE: return "deviceIdentifierType.name";
		case TOTAL_NUM_RECORDS: return "totalNumRecords";
		case NUM_VALIDATED_RECORDS: return "numValidatedRecords";
		case NUM_INSERTED_RECORDS: return "numInsertedRecords";
		}
		return "dateTimeUploaded";
	}
	
	public Sorting getSorting() {
		SortOrder sortOrder = (ascending 
				? SortOrder.asc(FirstPartyAudienceDeviceIdsUploadHistory.class, getFieldName()) 
				: SortOrder.desc(FirstPartyAudienceDeviceIdsUploadHistory.class, getFieldName()));
		return new Sorting(sortOrder);
	}

	//-----------------------------------------------------------------------------------------------------
	
	@Override
	public int compare(FirstPartyAudienceDeviceIdsUploadHistoryDto o1,
			FirstPartyAudienceDeviceIdsUploadHistoryDto o2) {
		int result = 0;
		switch(field) {
		case FILENAME: 
			result = o1.getFilename().compareTo(o2.getFilename());
			break;
		case DATE_TIME_UPLOAD: 
			result = o1.getDateTimeUploaded().compareTo(o2.getDateTimeUploaded());
			break;
		case DEVICE_IDENTIFIER_TYPE: 
			result = o1.getDeviceIdentifierType().getName().compareTo(o2.getDeviceIdentifierType().getName());
			break;
		case TOTAL_NUM_RECORDS: 
			result = o1.getTotalNumRecords().compareTo(o2.getTotalNumRecords());
			break;
		case NUM_VALIDATED_RECORDS: 
			result = o1.getNumValidatedRecords().compareTo(o2.getNumValidatedRecords());
			break;
		case NUM_INSERTED_RECORDS: 
			result = o1.getNumInsertedRecords().compareTo(o2.getNumInsertedRecords());
			break;
		
		}
		if(isAscending()) {
			return result;
		} else {
			return (-1) * result;
		}
		
	}
	

}
