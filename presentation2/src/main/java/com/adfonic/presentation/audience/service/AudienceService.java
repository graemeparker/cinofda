package com.adfonic.presentation.audience.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.Audience.Status;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.CampaignUsingAudienceDto;
import com.adfonic.dto.audience.DMPAttributeDto;
import com.adfonic.dto.audience.DMPAudienceDto;
import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.dto.audience.DMPSelectorForDMPAudienceDto;
import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.audience.MyAudienceDto;
import com.adfonic.dto.audience.ThirdPartyAudienceDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.resultwrapper.DeviceIdsValidated;
import com.adfonic.presentation.audience.datamodels.FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel;
import com.adfonic.presentation.audience.datamodels.MyAudiencesLazyDataModel;
import com.adfonic.presentation.audience.datamodels.ThirdPartyAudiencesLazyDataModel;
import com.adfonic.presentation.audience.sort.FirstPartyAudienceDeviceIdsUploadHistorySortBy;
import com.adfonic.presentation.audience.sort.MyAudiencesSortBy;
import com.adfonic.presentation.audience.sort.ThirdPartyAudiencesSortBy;
import com.byyd.middleware.iface.dao.Pagination;

public interface AudienceService {

	//-----------------------------------------------------------------------------------------------------------------------------
	
	DMPVendorDto getDMPVendorByName(String name, boolean includeHiddenSelectors);
    DMPVendorDto getDMPVendorById(Long id, boolean includeHiddenSelectors);
    List<DMPVendorDto> getAllDMPVendors();
	List<DMPVendorDto> getDMPVendorsForCompany(CompanyDto companyDto, boolean includeHiddenSelectors);
	List<DMPVendorDto> getDMPVendorsForCompanyForAdmins(CompanyDto companyDto, boolean includeHiddenSelectors);

	List<DMPAttributeDto> getDMPAttributesForDMPVendor(DMPVendorDto vendorDto, boolean includeHiddenSelectors);
	
	List<DMPSelectorDto> getDMPSelectorsForDMPAttribute(DMPAttributeDto attributeDto, boolean includeHiddenSelectors);

	DMPSelectorDto getDMPSelectorById(Long id);
	DMPSelectorDto getDMPSelectorByExternalIdAndDmpVendorId(String externalId, Long vendorId);
	DMPSelectorForDMPAudienceDto getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(String externalId, Long vendorId);
    DMPSelectorDto getDMPSelectorByExternalIdAndDmpVendorIdForCompany(String externalId, Long vendorId, CompanyDto companyDto);
    List<DMPSelectorDto> searchDMPSelectorByExternalIdForCompanyAndVendor(String externalId, boolean hidden, CompanyDto companyDto, DMPVendorDto vendor);
    DMPSelectorDto updateDMPSelector(DMPSelectorDto dmpSelectorDto);
    DMPSelectorDto createFactualDMPSelector(String externalId, DMPVendorDto dmpVendor, Long publisherId);
    DMPSelectorDto createFactualDMPSelector(String externalId, DMPVendorDto dmpVendor);
    DMPSelectorDto updateFactualDMPSelector(DMPSelectorDto dmpSelectorDto);

	AudienceDto getAudienceDtoById(long id);
	AudienceDto getAudienceByExternalId(String externalId);
	List<AudienceDto> getAudiencesForAdvertiser(Long advertiserId);
	List<AudienceDto> getAudiencesForAdvertiser(Long advertiserId,Status status);
	AudienceDto getAudienceByNameForAdvertiser(String name, Long advertiserId);
	AudienceDto createAudience(AudienceDto audienceDto);
	AudienceDto updateAudience(AudienceDto audienceDto);
	
	List<CampaignUsingAudienceDto> getAllCampaignsUsingAudience(AudienceDto audienceDto);
	List<CampaignUsingAudienceDto> getAllCampaignsUsingAudiencesAsSingleList(Collection<MyAudienceDto> audienceDtos);

	//-----------------------------------------------------------------------------------------------------------------------------
	
	Long countThirdPartyAudiencesForCompany(CompanyDto companyDto, String vendorPartialName, String audiencePartialName);
	List<ThirdPartyAudienceDto> getThirdPartyAudiencesForCompany(CompanyDto companyDto, String vendorPartialName, String audiencePartialName, ThirdPartyAudiencesSortBy sortBy, Pagination page);

	ThirdPartyAudiencesLazyDataModel createThirdPartyAudiencesLazyDataModel(CompanyDto companyDto);
	
	//-----------------------------------------------------------------------------------------------------------------------------

	Long countMyAudiences(AdvertiserDto advertiserDto, String audiencePartialName);
	List<MyAudienceDto> getMyAudiences(AdvertiserDto advertiserDto, String audiencePartialName, MyAudiencesSortBy sortBy, Pagination page);
	
	MyAudiencesLazyDataModel createMyAudiencesLazyDataModel(AdvertiserDto advertiserDto);
	
	void deleteAudience(MyAudienceDto audienceDto);
    void toggleFirstPartyAudienceCollection(MyAudienceDto myAudienceDto, boolean collect);

	//-----------------------------------------------------------------------------------------------------------------------------

	List<DMPAttributeDto> getAttributesAndSelectorsForDMPAudience(DMPAudienceDto audienceDto);

	//-----------------------------------------------------------------------------------------------------------------------------
	
	Long getMuidSegmentSize(Long segmentId);

	//-----------------------------------------------------------------------------------------------------------------------------
	
	static final String DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLS = "application/vnd.ms-excel";
	static final String DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	static final String DEVICE_IDS_UPLOAD_CONTENT_TYPE_CSV = "text/csv";
	
	static final String DEVICE_IDS_UPLOAD_NUM_INSERTED_RECORDS = "DEVICE_IDS_UPLOAD_NUM_INSERTED_RECORDS";
	
	static final String DEVICE_IDS_UPLOAD_STATUS = "DEVICE_IDS_UPLOAD_STATUS";
	static final String DEVICE_IDS_UPLOAD_STATUS_SUCCESS = "DEVICE_IDS_UPLOAD_STATUS_SUCCESS";
	static final String DEVICE_IDS_UPLOAD_STATUS_FAILURE = "DEVICE_IDS_UPLOAD_STATUS_FAILURE";
	static final String DEVICE_IDS_UPLOAD_STATUS_ERROR_MESSAGE = "DEVICE_IDS_UPLOAD_STATUS_ERROR_MESSAGE";
	static final String DEVICE_IDS_UPLOAD_STATUS_STACK_TRACE = "DEVICE_IDS_UPLOAD_STATUS_STACK_TRACE";
	
	DeviceIdsValidated validateDeviceIdsFileUpload(String audienceName,
                                                   DeviceIdentifierTypeDto deviceIdentifierTypeDto,
                                                   String fileName,
                                                   String contentType,
                                                   InputStream inputStream) throws Exception;
	
	 Map<String, String> processDeviceIdsFileUpload(String audienceName,
                                    	            FirstPartyAudienceDto firstPartyAudienceDto,
                                    	            List<String> deviceIds,
                                    	            long totalNumRecords,
                                    	            long numValidatedRecords,
                                    	            String filename,
                                    	            DeviceIdentifierTypeDto deviceIdentifierTypeDto);
	
	Long countFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto);
	List<FirstPartyAudienceDeviceIdsUploadHistoryDto> getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto, FirstPartyAudienceDeviceIdsUploadHistorySortBy sortBy);
	List<FirstPartyAudienceDeviceIdsUploadHistoryDto> getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto, FirstPartyAudienceDeviceIdsUploadHistorySortBy sortBy, Pagination page);
	
	FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel createFirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel(FirstPartyAudienceDto firstPartyAudience);
	
	//-----------------------------------------------------------------------------------------------------------------------------
	
	BigDecimal calculateAudienceDataFee(AudienceDto audienceDto);
}
