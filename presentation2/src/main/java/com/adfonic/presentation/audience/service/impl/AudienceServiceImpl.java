package com.adfonic.presentation.audience.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Audience;
import com.adfonic.domain.Audience.Status;
import com.adfonic.domain.AudiencePrices;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.DMPAttribute;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPVendor;
import com.adfonic.domain.DMPVendor_;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.domain.FirstPartyAudience.Type;
import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory;
import com.adfonic.domain.FirstPartyAudienceDeviceIdsUploadHistory_;
import com.adfonic.dto.NameIdBusinessDtoComparator;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.CampaignUsingAudienceDto;
import com.adfonic.dto.audience.DMPAttributeDto;
import com.adfonic.dto.audience.DMPAttributeDto.DMPAttributeSortBy;
import com.adfonic.dto.audience.DMPAudienceDto;
import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.dto.audience.DMPSelectorDto.DMPSelectorSortBy;
import com.adfonic.dto.audience.DMPSelectorForDMPAudienceDto;
import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.dto.audience.FirstPartyAudienceCampaignDto;
import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.audience.MyAudienceDto;
import com.adfonic.dto.audience.ThirdPartyAudienceDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.resultwrapper.DeviceIdsValidated;
import com.adfonic.presentation.audience.dao.MuidDao;
import com.adfonic.presentation.audience.dao.MuidSizeDao;
import com.adfonic.presentation.audience.datamodels.FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel;
import com.adfonic.presentation.audience.datamodels.MyAudiencesLazyDataModel;
import com.adfonic.presentation.audience.datamodels.ThirdPartyAudiencesLazyDataModel;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.FirstPartyAudienceDeviceIdsUploadHistorySortBy;
import com.adfonic.presentation.audience.sort.MyAudiencesSortBy;
import com.adfonic.presentation.audience.sort.ThirdPartyAudiencesSortBy;
import com.adfonic.presentation.audience.sql.procedures.MuidLinkDeviceToSegmentStoredProc;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.audience.filter.AudienceFilter;
import com.byyd.middleware.audience.filter.DMPAttributeFilter;
import com.byyd.middleware.audience.filter.DMPSelectorFilter;
import com.byyd.middleware.audience.filter.DMPVendorFilter;
import com.byyd.middleware.audience.filter.FirstPartyAudienceDeviceIdsUploadHistoryFilter;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("audienceService")
public class AudienceServiceImpl extends GenericServiceImpl implements AudienceService {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceServiceImpl.class);
    private static final String DISPLAY_ORDER = "displayOrder";
    private static final String GENERIC_DMP_ATTRIBUTE_NAME = "Generic";
    
    public static final Long NO_AUDIENCE_SIZE = -1L;
    public static final List<String> VENDORS_WITH_NO_AUDIENCE_SIZE = Arrays.asList("Adsquare"); 

    @Autowired
    private CompanyManager companyManager;
    @Autowired
    private AudienceManager audienceManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private CampaignManager campaignManager;
    @Autowired
    private FeeManager feeManager;

    @Autowired
    private MuidDao muidDao;
    @Autowired
    private MuidSizeDao muidSizeDao;

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DMPVendorDto getDMPVendorByName(String name, boolean includeHiddenSelectors) {
        DMPVendor dmpVendor = audienceManager.getDMPVendorByName(name);
        if (dmpVendor == null) {
            return null;
        }
        return this.getDMPVendorDto(dmpVendor, includeHiddenSelectors);
    }

    @Override
    @Transactional(readOnly = true)
    public DMPVendorDto getDMPVendorById(Long id, boolean includeHiddenSelectors) {
        DMPVendor dmpVendor = audienceManager.getDMPVendorById(id);
        if (dmpVendor == null) {
            return null;
        }
        return this.getDMPVendorDto(dmpVendor, includeHiddenSelectors);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<DMPVendorDto> getAllDMPVendors(){
        List<DMPVendor> vendors = audienceManager.getDMPVendors();
        return this.getList(DMPVendorDto.class, vendors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DMPVendorDto> getDMPVendorsForCompany(CompanyDto companyDto, boolean includeHiddenSelectors) {
        return this.getDMPVendorsForCompany(companyDto, false, includeHiddenSelectors, DMPAttributeSortBy.DISPLAY_ORDER, DMPSelectorSortBy.DISPLAY_ORDER);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DMPVendorDto> getDMPVendorsForCompanyForAdmins(CompanyDto companyDto, boolean includeHiddenSelectors) {
        return this.getDMPVendorsForCompany(companyDto, true, includeHiddenSelectors, DMPAttributeSortBy.DISPLAY_ORDER, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private DMPVendorDto getDMPVendorDto(DMPVendor vendor, Boolean includeHiddenSelectors) {
        return this.getDMPVendorDto(vendor, includeHiddenSelectors, DMPAttributeSortBy.DISPLAY_ORDER, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private DMPVendorDto getDMPVendorDto(DMPVendor vendor, Boolean includeHiddenSelectors, DMPAttributeSortBy sortDMPAttributesBy, DMPSelectorSortBy sortDMPSelectorsBy) {
        DMPVendorDto vendorDto = this.getObjectDto(DMPVendorDto.class, vendor);
        vendorDto.setDMPAttributes(this.getDMPAttributesForDMPVendor(vendorDto, includeHiddenSelectors, sortDMPAttributesBy, sortDMPSelectorsBy));
        return vendorDto;
    }

    private List<DMPVendorDto> getDMPVendorDtos(List<DMPVendor> vendors, Boolean includeHiddenSelectors, DMPAttributeSortBy sortDMPAttributesBy, DMPSelectorSortBy sortDMPSelectorsBy) {
        List<DMPVendorDto> list = new ArrayList<DMPVendorDto>();
        if (!CollectionUtils.isEmpty(vendors)) {
            for (DMPVendor vendor : vendors) {
                list.add(this.getDMPVendorDto(vendor, includeHiddenSelectors, sortDMPAttributesBy, sortDMPSelectorsBy));
            }
        }
        return list;
    }

    private List<DMPVendorDto> getDMPVendorsForCompany(CompanyDto companyDto, boolean showAdminVendors, Boolean includeHiddenSelectors, DMPAttributeSortBy sortDMPAttributesBy, DMPSelectorSortBy sortDMPSelectorsBy) {
        DMPVendorFilter filter = new DMPVendorFilter();
        filter.setRestricted(false);
        filter.setAdminOnly(showAdminVendors);
        FetchStrategy publisherFs = new FetchStrategyBuilder().addLeft(DMPVendor_.publishers).build();
        List<DMPVendor> vendors = audienceManager.getDMPVendors(filter, publisherFs);
        FetchStrategy companyFs = new FetchStrategyBuilder().addLeft(Company_.restrictedDMPVendors).build();
        Company company = companyManager.getCompanyById(companyDto.getId(), companyFs);
        if (!CollectionUtils.isEmpty(company.getRestrictedDMPVendors())) {
            for (DMPVendor companyRestrictedVendor : company.getRestrictedDMPVendors()) {
                if (!vendors.contains(companyRestrictedVendor)) {
                    vendors.add(audienceManager.getDMPVendorById(companyRestrictedVendor.getId(), publisherFs));
                }
            }
        }
        return this.getDMPVendorDtos(vendors, includeHiddenSelectors, sortDMPAttributesBy, sortDMPSelectorsBy);
    }

    @Override
    public List<DMPAttributeDto> getDMPAttributesForDMPVendor(DMPVendorDto vendorDto, boolean includeHiddenSelectors) {
        return getDMPAttributesForDMPVendor(vendorDto, includeHiddenSelectors, DMPAttributeSortBy.DISPLAY_ORDER, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private DMPAttributeDto getDMPAttributeDto(DMPAttribute attribute, Boolean includeHiddenSelectors, DMPSelectorSortBy sortDMPSelectorsBy) {
        DMPAttributeDto attributeDto = this.getObjectDto(DMPAttributeDto.class, attribute);
        attributeDto.setDMPSelectors(this.getDMPSelectorsForDMPAttribute(attributeDto, includeHiddenSelectors, sortDMPSelectorsBy));
        return attributeDto;
    }

    private List<DMPAttributeDto> getDMPAttributeDtos(List<DMPAttribute> attributes, Boolean includeHiddenSelectors, DMPSelectorSortBy sortDMPSelectorsBy) {
        List<DMPAttributeDto> list = new ArrayList<DMPAttributeDto>();
        if (!CollectionUtils.isEmpty(attributes)) {
            for (DMPAttribute attribute : attributes) {
                list.add(this.getDMPAttributeDto(attribute, includeHiddenSelectors, sortDMPSelectorsBy));
            }
        }
        return list;
    }

    private List<DMPAttributeDto> getDMPAttributesForDMPVendor(DMPVendorDto vendorDto, Boolean includeHiddenSelectors, DMPAttributeSortBy sortBy, DMPSelectorSortBy sortDMPSelectorsBy) {
        DMPVendor vendor = audienceManager.getDMPVendorById(vendorDto.getId());
        DMPAttributeFilter filter = new DMPAttributeFilter();
        filter.setDMPVendor(vendor);
        Sorting sort = null;
        if (sortBy == DMPAttributeSortBy.NAME) {
            sort = new Sorting(SortOrder.asc(DMPAttribute.class, "name"));
        } else if (sortBy == DMPAttributeSortBy.DISPLAY_ORDER) {
            sort = new Sorting(SortOrder.asc(DMPAttribute.class, DISPLAY_ORDER));
        }
        List<DMPAttribute> attributes = audienceManager.getDMPAttributes(filter, sort);
        return this.getDMPAttributeDtos(attributes, includeHiddenSelectors, sortDMPSelectorsBy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DMPSelectorDto> getDMPSelectorsForDMPAttribute(DMPAttributeDto attributeDto, boolean includeHiddenSelectors) {
        return this.getDMPSelectorsForDMPAttribute(attributeDto, includeHiddenSelectors, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private List<DMPSelectorDto> getDMPSelectorsForDMPAttribute(DMPAttributeDto attributeDto, Boolean includeHiddenSelectors, DMPSelectorSortBy sortBy) {
        DMPAttribute attribute = audienceManager.getDMPAttributeById(attributeDto.getId());
        DMPSelectorFilter filter = new DMPSelectorFilter();
        filter.setDMPAttribute(attribute);
        filter.setHidden(includeHiddenSelectors==true ? null : false);
        Sorting sort = null;
        if (sortBy == DMPSelectorSortBy.NAME) {
            sort = new Sorting(SortOrder.asc(DMPSelector.class, "name"));
        } else if (sortBy == DMPSelectorSortBy.DISPLAY_ORDER) {
            sort = new Sorting(SortOrder.asc(DMPSelector.class, DISPLAY_ORDER));
        }
        List<DMPSelector> selectors = audienceManager.getDMPSelectors(filter, sort);
        return this.makeListFromCollection(getList(DMPSelectorDto.class, selectors));
    }

    private List<DMPSelectorForDMPAudienceDto> getDMPSelectorsForDMPAudience(DMPAudienceDto audienceDto, DMPSelectorSortBy sortBy) {
        DMPAudience audience = audienceManager.getDMPAudienceById(audienceDto.getId());
        Sorting sort = null;
        if (sortBy == DMPSelectorSortBy.NAME) {
            sort = new Sorting(SortOrder.asc(DMPSelector.class, "name"));
        } else if (sortBy == DMPSelectorSortBy.DISPLAY_ORDER) {
            sort = new Sorting(SortOrder.asc(DMPSelector.class, DISPLAY_ORDER));
        }
        List<DMPSelector> selectors = audienceManager.getDMPSelectorsForDMPAudience(audience, sort);
        return this.makeListFromCollection(getList(DMPSelectorForDMPAudienceDto.class, selectors));
    }

    @Override
    @Transactional(readOnly = true)
    public DMPSelectorDto getDMPSelectorById(Long id) {
        DMPSelector selector = audienceManager.getDMPSelectorById(id);
        if (selector != null) {
            return this.getObjectDto(DMPSelectorDto.class, selector);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DMPSelectorDto getDMPSelectorByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId) {
        DMPSelector selector = audienceManager.getDMPSelectorByExternalIdAndDmpVendorId(externalId, dmpVendorId);
        if (selector != null) {
            return this.getObjectDto(DMPSelectorDto.class, selector);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DMPSelectorForDMPAudienceDto getDMPSelectorForDMPAudienceDtoByExternalIdAndDmpVendorId(String externalId, Long dmpVendorId) {
        DMPSelector selector = audienceManager.getDMPSelectorByExternalIdAndDmpVendorId(externalId, dmpVendorId);
        if (selector != null) {
            return this.getObjectDto(DMPSelectorForDMPAudienceDto.class, selector);
        } else {
            return null;
        }
    }

    /*
     * do a find by external id but also verify it's not restricted for the company
     */
    @Override
    @Transactional(readOnly = true)
    public DMPSelectorDto getDMPSelectorByExternalIdAndDmpVendorIdForCompany(String externalId, Long dmpVendorId, CompanyDto companyDto) {
        DMPSelector selector = audienceManager.getDMPSelectorByExternalIdAndDmpVendorId(externalId, dmpVendorId);
        Company company = companyManager.getCompanyById(companyDto.getId());
        if (selector != null) {
            // check restrictions
            if (CollectionUtils.isNotEmpty(company.getRestrictedDMPVendors()) && company.getRestrictedDMPVendors().contains(selector.getDmpAttribute().getDmpVendor())) {
                return null;
            }
            return this.getObjectDto(DMPSelectorDto.class, selector);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DMPSelectorDto> searchDMPSelectorByExternalIdForCompanyAndVendor(String externalId, boolean hidden, CompanyDto companyDto, DMPVendorDto vendor) {
        DMPSelectorFilter filter = new DMPSelectorFilter();
        filter.setExternalId(externalId, LikeSpec.STARTS_WITH, true);
        filter.setDmpVendorId(vendor.getId());
        filter.setHidden(hidden);
        List<DMPSelector> selectors = audienceManager.getDMPSelectors(filter);
        Company company = companyManager.getCompanyById(companyDto.getId());
        List<DMPSelectorDto> result = new ArrayList<DMPSelectorDto>();
        if (!CollectionUtils.isEmpty(selectors)) {
            for (DMPSelector selector : selectors) {
                // check restrictions
                if (selector.getDmpAttribute().getDmpVendor().getId() == vendor.getId()
                        && (!selector.getDmpAttribute().getDmpVendor().getRestricted() || (CollectionUtils.isNotEmpty(company.getRestrictedDMPVendors()) && company
                                .getRestrictedDMPVendors().contains(selector.getDmpAttribute().getDmpVendor())))) {
                    result.add(this.getObjectDto(DMPSelectorDto.class, selector));
                }
            }
        }
        return result;
    }
    
    @Override
    @Transactional(readOnly = false)
    public DMPSelectorDto updateDMPSelector(DMPSelectorDto dmpSelectorDto){
        DMPSelector dmpSelector = audienceManager.getDMPSelectorById(dmpSelectorDto.getId());
        
        dmpSelector.setExternalID(dmpSelectorDto.getExternalID());
        dmpSelector.setName(dmpSelectorDto.getName());
        dmpSelector.setPublisher((dmpSelectorDto.getPublisher() != null) ? publisherManager.getPublisherById(dmpSelectorDto.getPublisher().getId()) : null);
        dmpSelector.setHidden(dmpSelectorDto.getHidden());
        
        AudiencePrices audiencePrices = dmpSelector.getAudiencePrices();
        audiencePrices.setDataWholesale(dmpSelectorDto.getDataWholesale());
        audiencePrices.setDataRetail(dmpSelectorDto.getDataRetail());
        
        return getObjectDto(DMPSelectorDto.class, audienceManager.update(dmpSelector));
    }
    
	@Override
	public DMPSelectorDto createFactualDMPSelector(String externalId, DMPVendorDto dmpVendor, Long publisherId) {
		DMPSelector dmpSelector = new DMPSelector();
		
		// New Factual selectors will use the default prices from its vendor
		dmpSelector.setDataRetail(dmpVendor.getAudiencePrices().getDataRetail());
		dmpSelector.setDataWholesale(dmpVendor.getAudiencePrices().getDataWholesale());
		
		dmpSelector.setDisplayOrder(0);
		dmpSelector.setDmpAttribute(audienceManager.getDMPAttributeByName(audienceManager.getDMPVendorById(dmpVendor.getId()), GENERIC_DMP_ATTRIBUTE_NAME));
		dmpSelector.setDmpVendorId(dmpVendor.getId());
		dmpSelector.setExternalID(externalId);
		dmpSelector.setHidden(true);
		dmpSelector.setMuidSegmentId(null);
		
		// New Factual selector name will also be the external id 
		dmpSelector.setName(externalId);
		
		// New Factual selector was based on Audience
		if (publisherId != null) {
			dmpSelector.setPublisher(publisherManager.getPublisherById(publisherId));
		}
		
		return getObjectDto(DMPSelectorDto.class, audienceManager.create(dmpSelector));
	}
	
	@Override
	public DMPSelectorDto createFactualDMPSelector(String externalId, DMPVendorDto dmpVendor) {
		return createFactualDMPSelector(externalId, dmpVendor, null);
	}

	@Override
	public DMPSelectorDto updateFactualDMPSelector(DMPSelectorDto dmpSelectorDto) {
		DMPSelector dmpSelector = audienceManager.getDMPSelectorById(dmpSelectorDto.getId());
		
		dmpSelector.setExternalID(dmpSelectorDto.getExternalID());
		dmpSelector.setName(dmpSelectorDto.getExternalID());
		dmpSelector.setPublisher((dmpSelectorDto.getPublisher() != null) ? publisherManager.getPublisherById(dmpSelectorDto.getPublisher().getId()) : null);
		
		return getObjectDto(DMPSelectorDto.class, audienceManager.update(dmpSelector));
	}

    private DMPAudienceDto getDMPAudienceDto(DMPAudience audience, DMPSelectorSortBy sortDMPSelectorsBy) {
        DMPAudienceDto audienceDto = this.getObjectDto(DMPAudienceDto.class, audience);
        audienceDto.setDmpSelectors(this.getDMPSelectorsForDMPAudience(audienceDto, sortDMPSelectorsBy));
        return audienceDto;
    }

    private FirstPartyAudienceDto getFirstPartyAudienceDto(FirstPartyAudience firstPartyAudience) {
        return this.getObjectDto(FirstPartyAudienceDto.class, firstPartyAudience);
    }

    @Override
    @Transactional(readOnly = true)
    public AudienceDto getAudienceDtoById(long id) {
        Audience audience = audienceManager.getAudienceById(id);
        return this.getAudienceDto(audience);
    }

    @Override
    @Transactional(readOnly = true)
    public AudienceDto getAudienceByExternalId(String externalId) {
        Audience audience = audienceManager.getAudienceByExternalId(externalId);
        return this.getAudienceDto(audience);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienceDto> getAudiencesForAdvertiser(Long advertiserId) {
        AudienceFilter filter = new AudienceFilter();
        filter.setAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        filter.setStatusesNotIncluded(Status.DELETED);
        List<Audience> results = audienceManager.getAudiences(filter);
        List<AudienceDto> audiences = new ArrayList<AudienceDto>();
        for (Audience a : results) {
            audiences.add(this.getAudienceDto(a));
        }
        return audiences;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienceDto> getAudiencesForAdvertiser(Long advertiserId, Status status) {
        AudienceFilter filter = new AudienceFilter();
        filter.setAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        filter.setStatusesIncluded(status);
        List<Audience> results = audienceManager.getAudiences(filter);
        List<AudienceDto> audiences = new ArrayList<AudienceDto>();
        for (Audience a : results) {
            AudienceDto audienceDto = this.getAudienceDto(a);
            audienceDto.setAudienceType(audienceDto.resolveAudienceType(audienceDto));
            audiences.add(audienceDto);
        }
        return audiences;
    }

    @Override
    @Transactional(readOnly = true)
    public AudienceDto getAudienceByNameForAdvertiser(String name, Long advertiserId) {
        AudienceFilter filter = new AudienceFilter();
        filter.setAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        filter.setName(name, false);
        filter.setStatusesNotIncluded(Status.DELETED);
        List<Audience> results = audienceManager.getAudiences(filter);
        AudienceDto audienceDto = null;
        if (CollectionUtils.isNotEmpty(results)) {
            audienceDto = this.getAudienceDto(results.get(0));
        }
        return audienceDto;
    }

    @Override
    @Transactional(readOnly = false)
    public AudienceDto createAudience(AudienceDto audienceDto) {
        return this.createAudience(audienceDto, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    @Override
    @Transactional(readOnly = false)
    public AudienceDto updateAudience(AudienceDto audienceDto) {
        return updateAudience(audienceDto, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private AudienceDto getAudienceDto(Audience audience) {
        return this.getAudienceDto(audience, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private AudienceDto getAudienceDto(Audience audience, DMPSelectorSortBy sortDMPSelectorsBy) {
        AudienceDto audienceDto = this.getObjectDto(AudienceDto.class, audience);
        if (audience.getDmpAudience() != null) {
            audienceDto.setDmpAudience(this.getDMPAudienceDto(audience.getDmpAudience(), sortDMPSelectorsBy));
        }
        if (audience.getFirstPartyAudience() != null) {
            audienceDto.setFirstPartyAudience(this.getFirstPartyAudienceDto(audience.getFirstPartyAudience()));
        }
        return audienceDto;
    }

    private AudienceDto createAudience(AudienceDto audienceDto, DMPSelectorSortBy sortDMPSelectorsBy) {
        Advertiser advertiser = advertiserManager.getAdvertiserById(audienceDto.getAdvertiser().getId());
        Audience audience = new Audience();

        DMPAudience dmpAudience = null;
        FirstPartyAudience firstPartyAudience = null;

        DMPAudienceDto dmpAudienceDto = audienceDto.getDmpAudience();
        if (dmpAudienceDto != null) {
            dmpAudience = createDMPAudienceEntity(dmpAudienceDto, audience);
        } else {
            FirstPartyAudienceDto firstPartyAudienceDto = audienceDto.getFirstPartyAudience();
            if (firstPartyAudienceDto != null) {
                firstPartyAudience = createFirstPartyAudienceEntity(firstPartyAudienceDto, audience);
            }
        }

        audience.setAdvertiser(advertiser);
        audience.setCreationTime(new Date());
        audience.setName(audienceDto.getName());
        audience = audienceManager.create(audience, dmpAudience, firstPartyAudience);

        return this.getAudienceDto(audienceManager.getAudienceById(audience.getId()), sortDMPSelectorsBy);
    }

    /*
     * Advertiser cannot be updated after creation of an Audience
     */
    private AudienceDto updateAudience(AudienceDto audienceDto, DMPSelectorSortBy sortDMPSelectorsBy) {
        Audience audience = audienceManager.getAudienceById(audienceDto.getId());

        createChildEntitiesAudiences(audienceDto, audience);

        audience.setName(audienceDto.getName());
        audience.setStatus(audienceDto.getStatus());
        audience = audienceManager.update(audience);

        return this.getAudienceDto(audience, sortDMPSelectorsBy);
    }

    private void createChildEntitiesAudiences(AudienceDto audienceDto, Audience audience) {
        DMPAudience dmpAudience = null;
        FirstPartyAudience firstPartyAudience = null;

        DMPAudienceDto dmpAudienceDto = audienceDto.getDmpAudience();
        if (dmpAudienceDto != null) {
            dmpAudience = createDMPAudienceEntity(dmpAudienceDto, audience);
            audience.setDmpAudience(dmpAudience);
        } else {
            FirstPartyAudienceDto firstPartyAudienceDto = audienceDto.getFirstPartyAudience();
            if (firstPartyAudienceDto != null) {
                firstPartyAudience = createFirstPartyAudienceEntity(firstPartyAudienceDto, audience);
                audience.setFirstPartyAudience(firstPartyAudience);
            }
        }
    }

    private DMPAudience createDMPAudienceEntity(DMPAudienceDto dmpAudienceDto, Audience audience) {
        DMPAudience dmpAudience = null;

        if (!dmpAudienceDto.persisted()) {
            dmpAudience = new DMPAudience();
            dmpAudience.setAudience(audience);
        } else {
            dmpAudience = audienceManager.getDMPAudienceById(dmpAudienceDto.getId());
            dmpAudience.getDmpSelectors().clear();
        }
        
        // Save the changed DMP vendor as well
        DMPVendor dmpVendor = audienceManager.getDMPVendorById(dmpAudienceDto.getDmpVendor().getId());
        dmpAudience.setDmpVendor(dmpVendor);

        dmpAudience.setUserEnteredDMPSelectorExternalId(dmpAudienceDto.getUserEnteredDMPSelectorExternalId());
        if (!CollectionUtils.isEmpty(dmpAudienceDto.getDmpSelectors())) {
            for (DMPSelectorDto dmpSelectorDto : dmpAudienceDto.getDmpSelectors()) {
                dmpAudience.getDmpSelectors().add(audienceManager.getDMPSelectorById(dmpSelectorDto.getId()));
            }
        }

        return dmpAudience;
    }

    private FirstPartyAudience createFirstPartyAudienceEntity(FirstPartyAudienceDto firstPartyAudienceDto, Audience audience) {
        FirstPartyAudience firstPartyAudience = null;

        Type firstPartyAudienceDtoType = firstPartyAudienceDto.getType();
        if (!firstPartyAudienceDto.persisted()) {
            firstPartyAudience = new FirstPartyAudience();
            firstPartyAudience.setAudience(audience);
            // MAX-2341 - Prevent proc_create_segment call in case of Location Audiences
            if (!FirstPartyAudience.Type.LOCATION.equals(firstPartyAudienceDtoType)) {
                firstPartyAudience.setMuidSegmentId(this.createMuidSegment(audience.getId(), firstPartyAudienceDtoType.toString()));
            }
        } else {
            // Audience, muidSegmentId and external ID cannot be updated after creation of a FirstPartyAudience
            firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
            if (!CollectionUtils.isEmpty(firstPartyAudienceDto.getCampaigns())) {
                firstPartyAudience.getCampaigns().clear();
            }
        }

        firstPartyAudience.setActive(firstPartyAudienceDto.isActive());
        firstPartyAudience.setType(firstPartyAudienceDtoType);

        if (!CollectionUtils.isEmpty(firstPartyAudienceDto.getCampaigns())) {
            for (FirstPartyAudienceCampaignDto campaignDto : firstPartyAudienceDto.getCampaigns()) {
                firstPartyAudience.getCampaigns().add(campaignManager.getCampaignById(campaignDto.getId()));
            }
        }

        return firstPartyAudience;
    }

    private void deleteAudience(Audience audience) {
        if (audience != null) {
            if (audience.getFirstPartyAudience() != null) {
                audience.getFirstPartyAudience().setActive(false);
                audienceManager.update(audience.getFirstPartyAudience());
            }
            audienceManager.delete(audience);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<CampaignUsingAudienceDto> getAllCampaignsUsingAudience(AudienceDto audienceDto) {
        Audience audience = audienceManager.getAudienceById(audienceDto.getId());
        List<Campaign> campaigns = audienceManager.getCampaignsLinkedToAudience(audience);
        return this.makeListFromCollection(this.getList(CampaignUsingAudienceDto.class, campaigns));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignUsingAudienceDto> getAllCampaignsUsingAudiencesAsSingleList(Collection<MyAudienceDto> audienceDtos) {
        Map<MyAudienceDto, List<CampaignUsingAudienceDto>> map = getAllCampaignsUsingAudiences(audienceDtos);
        Set<CampaignUsingAudienceDto> set = new HashSet<>();
        for (List<CampaignUsingAudienceDto> list : map.values()) {
            set.addAll(list);
        }
        List<CampaignUsingAudienceDto> list = new ArrayList<>();
        list.addAll(set);
        Collections.sort(list, new NameIdBusinessDtoComparator(NameIdBusinessDtoComparator.Field.NAME));
        return list;
    }

    private List<CampaignUsingAudienceDto> getAllCampaignsUsingAudience(MyAudienceDto audienceDto) {
        Audience audience = audienceManager.getAudienceByExternalId(audienceDto.getExternalId());
        List<Campaign> campaigns = audienceManager.getCampaignsLinkedToAudience(audience);
        List<CampaignUsingAudienceDto> list = this.makeListFromCollection(this.getList(CampaignUsingAudienceDto.class, campaigns));
        Collections.sort(list, new NameIdBusinessDtoComparator(NameIdBusinessDtoComparator.Field.NAME));
        return list;
    }

    private Map<MyAudienceDto, List<CampaignUsingAudienceDto>> getAllCampaignsUsingAudiences(Collection<MyAudienceDto> audienceDtos) {
        Map<MyAudienceDto, List<CampaignUsingAudienceDto>> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(audienceDtos)) {
            for (MyAudienceDto audienceDto : audienceDtos) {
                List<CampaignUsingAudienceDto> list = getAllCampaignsUsingAudience(audienceDto);
                map.put(audienceDto, list);
            }
        }
        return map;
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countThirdPartyAudiencesForCompany(CompanyDto companyDto, String vendorPartialName, String audiencePartialName) {
        List<ThirdPartyAudienceDto> list = this.getRawThirdPartyAudiences(companyDto, vendorPartialName, audiencePartialName, false);
        return Long.valueOf(list.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThirdPartyAudienceDto> getThirdPartyAudiencesForCompany(CompanyDto companyDto, String vendorPartialName, String audiencePartialName,
            ThirdPartyAudiencesSortBy sortBy, Pagination page) {
        List<ThirdPartyAudienceDto> list = this.getRawThirdPartyAudiences(companyDto, vendorPartialName, audiencePartialName, true);
        Collections.sort(list, new ThirdPartyAudiencesDtoComparator(sortBy));
        if (page == null || (page.getOffet() == 0 && page.getLimit() == 0) || CollectionUtils.isEmpty(list)) {
            return list;
        } else {
            int toIndex;
            if (page.getOffet() + page.getLimit() > list.size()) {
                toIndex = list.size();
            } else {
                toIndex = page.getOffet() + page.getLimit();
            }
            return list.subList(page.getOffet(), toIndex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ThirdPartyAudiencesLazyDataModel createThirdPartyAudiencesLazyDataModel(CompanyDto companyDto) {
        return new ThirdPartyAudiencesLazyDataModel(companyDto, this);
    }

    private List<ThirdPartyAudienceDto> getRawThirdPartyAudiences(CompanyDto companyDto, String vendorPartialName, String audiencePartialName, boolean gatherSizes) {
        DecimalFormat moneyFormat = new DecimalFormat();
        moneyFormat.applyPattern("$###,##0.00");
        DecimalFormat populationFormat = new DecimalFormat();
        populationFormat.applyPattern("###,###,##0");

        List<ThirdPartyAudienceDto> list = new ArrayList<ThirdPartyAudienceDto>();

        List<DMPVendorDto> vendors = this.getDMPVendorsForCompany(companyDto, false);
        for (DMPVendorDto vendor : vendors) {
            if (StringUtils.isEmpty(vendorPartialName) || StringUtils.containsIgnoreCase(vendor.getName(), vendorPartialName)) {
                String vendorName = vendor.getName();
                for (DMPAttributeDto attribute : vendor.getDMPAttributes()) {
                    String attributeName = attribute.getName();
                    for (DMPSelectorDto selector : attribute.getDMPSelectors()) {
                        String selectorName = selector.getName();
                        String thirdPartyAudienceName = attributeName + ": " + selectorName;
                        if (StringUtils.isEmpty(audiencePartialName) || StringUtils.containsIgnoreCase(thirdPartyAudienceName, audiencePartialName)) {
                            ThirdPartyAudienceDto dto = new ThirdPartyAudienceDto();
                            dto.setExternalId(selector.getExternalID());
                            dto.setVendorName(vendorName);
                            dto.setAttributeName(attributeName);
                            dto.setSelectorName(selectorName);
                            dto.setThirdPartyAudienceName(thirdPartyAudienceName);
                            dto.setDataRetail(selector.getDataRetail().doubleValue());
                            if (gatherSizes) {
                                dto.setPopulation((VENDORS_WITH_NO_AUDIENCE_SIZE.contains(vendor.getName())) ? NO_AUDIENCE_SIZE : getMuidSegmentSize(selector.getMuidSegmentId()));
                            }
                            list.add(dto);
                        }
                    }
                }
            }
        }

        return list;
    }

    protected static class ThirdPartyAudiencesDtoComparator implements Comparator<ThirdPartyAudienceDto> {

        private final ThirdPartyAudiencesSortBy sortBy;

        public ThirdPartyAudiencesDtoComparator(ThirdPartyAudiencesSortBy sortBy) {
            this.sortBy = sortBy;
        }

        @Override
        public int compare(ThirdPartyAudienceDto o1, ThirdPartyAudienceDto o2) {
            if (sortBy == null) {
                return 0;
            }
            int result = 0;
            if (sortBy.getField().equals(ThirdPartyAudiencesSortBy.Field.AUDIENCE_NAME)) {
                result = o1.getThirdPartyAudienceName().compareTo(o2.getThirdPartyAudienceName());
            } else if (sortBy.getField().equals(ThirdPartyAudiencesSortBy.Field.VENDOR_NAME)) {
                result = o1.getVendorName().compareTo(o2.getVendorName());
            } else if (sortBy.getField().equals(ThirdPartyAudiencesSortBy.Field.AUDIENCE_DATARETAIL)) {
                result = Double.compare(o1.getDataRetail(), o2.getDataRetail());
            } else if (sortBy.getField().equals(ThirdPartyAudiencesSortBy.Field.AUDIENCE_POPULATION)) {
                result = Long.compare(o1.getPopulation(), o2.getPopulation());
            }
            if (sortBy.isAscending()) {
                return result;
            } else {
                return (-1) * result;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countMyAudiences(AdvertiserDto advertiserDto, String audiencePartialName) {
        return Long.valueOf(this.getRawMyAudiences(advertiserDto, audiencePartialName).size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyAudienceDto> getMyAudiences(AdvertiserDto advertiserDto, String audiencePartialName, MyAudiencesSortBy sortBy, Pagination page) {
        List<MyAudienceDto> list = this.getRawMyAudiences(advertiserDto, audiencePartialName);
        Collections.sort(list, new MyAudiencesDtoComparator(sortBy));
        if (page == null || (page.getOffet() == 0 && page.getLimit() == 0) || CollectionUtils.isEmpty(list)) {
            return list;
        } else {
            int toIndex;
            if (page.getOffet() + page.getLimit() > list.size()) {
                toIndex = list.size();
            } else {
                toIndex = page.getOffet() + page.getLimit();
            }
            return list.subList(page.getOffet(), toIndex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MyAudiencesLazyDataModel createMyAudiencesLazyDataModel(AdvertiserDto advertiserDto) {
        return new MyAudiencesLazyDataModel(advertiserDto, this);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAudience(MyAudienceDto audienceDto) {
        Audience audience = audienceManager.getAudienceByExternalId(audienceDto.getExternalId());
        deleteAudience(audience);
    }

    @Override
    @Transactional(readOnly = false)
    public void toggleFirstPartyAudienceCollection(MyAudienceDto myAudienceDto, boolean collect) {
        Audience audience = audienceManager.getAudienceByExternalId(myAudienceDto.getExternalId());
        FirstPartyAudience fpa = audience.getFirstPartyAudience();
        if (fpa != null && fpa.isActive() != collect) {
            fpa.setActive(collect);
            audienceManager.update(fpa);
        }
    }

    private List<MyAudienceDto> getRawMyAudiences(AdvertiserDto advertiserDto, String audiencePartialName) {
        DecimalFormat populationFormat = new DecimalFormat();
        populationFormat.applyPattern("###,###,##0");

        List<MyAudienceDto> list = new ArrayList<MyAudienceDto>();

        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
        AudienceFilter filter = new AudienceFilter().setAdvertiser(advertiser).setName(audiencePartialName, LikeSpec.CONTAINS, false);
        filter.setStatusesNotIncluded(Status.DELETED);
        List<Audience> audiences = audienceManager.getAudiences(filter);
        for (Audience audience : audiences) {
            if (StringUtils.isEmpty(audiencePartialName) || StringUtils.containsIgnoreCase(audience.getName(), audiencePartialName)) {
                MyAudienceDto dto = new MyAudienceDto();
                dto.setExternalId(audience.getExternalID());
                dto.setName(audience.getName());
                if (audience.getDmpAudience() == null && audience.getFirstPartyAudience() == null) {
                    // Half-baked
                    dto.setStatus(MyAudienceDto.Status.NEW);
                } else {
                    if (audience.getDmpAudience() != null) {
                        // DMPAudience - static
                        dto.setStatus(MyAudienceDto.Status.STATIC);
                        dto.setType(MyAudienceDto.Type.DMP);
                    } else if (audience.getFirstPartyAudience() != null) {
                        FirstPartyAudience fpa = audience.getFirstPartyAudience();
                        if (fpa.getType().equals(FirstPartyAudience.Type.UPLOAD) || fpa.getType().equals(FirstPartyAudience.Type.LOCATION)) {
                            if (audience.getStatus().equals(Audience.Status.NEW) || audience.getStatus().equals(Audience.Status.NEW_REVIEW)) {
                                dto.setStatus(MyAudienceDto.Status.NEW);
                                // Device IDs uploads - static once active
                            } else {
                                dto.setStatus(MyAudienceDto.Status.STATIC);
                            }
                            dto.setType(fpa.getType().equals(FirstPartyAudience.Type.UPLOAD) ? MyAudienceDto.Type.DEVICE_IDS : MyAudienceDto.Type.LOCATION);
                        } else {
                            // These are the campaign events or visitor collection ones, the only ones which could be paused
                            if (audience.getStatus().equals(Audience.Status.NEW) || audience.getStatus().equals(Audience.Status.NEW_REVIEW)) {
                                dto.setStatus(MyAudienceDto.Status.NEW);
                            } else {
                                // Audience is active, is the FPA active as well?
                                if (audience.getFirstPartyAudience().isActive()) {
                                    dto.setStatus(MyAudienceDto.Status.ACTIVE);
                                } else {
                                    dto.setStatus(MyAudienceDto.Status.PAUSED);
                                }
                            }
                            if (fpa.getType().equals(FirstPartyAudience.Type.CLICK)) {
                                dto.setType(MyAudienceDto.Type.CLICKERS);
                            } else if (fpa.getType().equals(FirstPartyAudience.Type.CONVERSION)) {
                                dto.setType(MyAudienceDto.Type.CONVERTERS);
                            } else if (fpa.getType().equals(FirstPartyAudience.Type.INSTALL)) {
                                dto.setType(MyAudienceDto.Type.INSTALLERS);
                            } else if (fpa.getType().equals(FirstPartyAudience.Type.COLLECT)) {
                                dto.setType(MyAudienceDto.Type.SITE_APP_VISITOR);
                            }
                        }
                    }
                }
                list.add(dto);
            }
        }
        return list;
    }

    protected static class MyAudiencesDtoComparator implements Comparator<MyAudienceDto> {
        private final MyAudiencesSortBy sortBy;

        public MyAudiencesDtoComparator(MyAudiencesSortBy sortBy) {
            this.sortBy = sortBy;
        }

        @Override
        public int compare(MyAudienceDto o1, MyAudienceDto o2) {
            if (sortBy == null) {
                return 0;
            }
            int result = 0;
            if (sortBy.getField().equals(MyAudiencesSortBy.Field.AUDIENCE_NAME)) {
                result = o1.getName().compareTo(o2.getName());
            } else if (sortBy.getField().equals(MyAudiencesSortBy.Field.AUDIENCE_STATUS)) {
                result = o1.getStatus().compareTo(o2.getStatus());
            } else if (sortBy.getField().equals(MyAudiencesSortBy.Field.AUDIENCE_TYPE)) {
                // new audiences may have null type
                if (o1.getType() == null && o2.getType() == null) {
                    result = 0;
                } else if (o1.getType() == null && o2.getType() != null) {
                    result = 1;
                } else if (o2.getType() == null && o1.getType() != null) {
                    result = -1;
                } else {
                    result = o1.getType().compareTo(o2.getType());
                }
            } else if (sortBy.getField().equals(MyAudiencesSortBy.Field.AUDIENCE_POPULATION)) {
                result = Long.compare(o1.getPopulation(), o2.getPopulation());
            }
            if (sortBy.isAscending()) {
                return result;
            } else {
                return (-1) * result;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DMPAttributeDto> getAttributesAndSelectorsForDMPAudience(DMPAudienceDto audienceDto) {
        return getAttributesAndSelectorsForDMPAudience(audienceDto, DMPAttributeSortBy.DISPLAY_ORDER, DMPSelectorSortBy.DISPLAY_ORDER);
    }

    private List<DMPAttributeDto> getAttributesAndSelectorsForDMPAudience(DMPAudienceDto audienceDto, DMPAttributeSortBy sortDMPAttributesBy, DMPSelectorSortBy sortDMPSelectorsBy) {
        List<DMPAttributeDto> attributes = new ArrayList<DMPAttributeDto>();

        Map<DMPAttributeDto, DMPAttributeDto> attributesMap = new HashMap<DMPAttributeDto, DMPAttributeDto>();
        List<DMPSelectorForDMPAudienceDto> selectors = this.getDMPSelectorsForDMPAudience(audienceDto, sortDMPSelectorsBy);
        for (DMPSelectorForDMPAudienceDto selector : selectors) {
            DMPAttributeDto attribute = attributesMap.get(selector.getDmpAttribute());
            if (attribute == null) {
                attribute = selector.getDmpAttribute();
                attributesMap.put(attribute, attribute);
            }
            attribute.getDMPSelectors().add(selector);
        }
        attributes.addAll(attributesMap.values());
        Collections.sort(attributes, new DMPAttributeDtoComparator(sortDMPAttributesBy));
        return attributes;
    }

    protected static class DMPAttributeDtoComparator implements Comparator<DMPAttributeDto>, Serializable {
        private static final long serialVersionUID = 1L;

        private final DMPAttributeSortBy sortBy;

        public DMPAttributeDtoComparator(DMPAttributeSortBy sortBy) {
            this.sortBy = sortBy;
        }

        @Override
        public int compare(DMPAttributeDto o1, DMPAttributeDto o2) {
            if (sortBy.equals(DMPAttributeSortBy.DISPLAY_ORDER)) {
                return o1.getDisplayOrder().compareTo(o2.getDisplayOrder());
            } else if (sortBy.equals(DMPAttributeSortBy.NAME)) {
                return o1.getName().compareTo(o2.getName());
            }
            return 0;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long getMuidSegmentSize(Long segmentId) {
        return muidSizeDao.getMuidSegmentSize(segmentId);
    }

    private Long createMuidSegment(Long segmentReference, String segmentType) {
        return muidDao.createMuidSegment(segmentReference, segmentType);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private FirstPartyAudienceDeviceIdsUploadHistoryDto createFirstPartyAudienceDeviceIdsUploadHistory(FirstPartyAudienceDto firstPartyAudienceDto, String filename,
            DeviceIdentifierTypeDto deviceIdentifierTypeDto, Long totalNumRecords, Long numValidatedRecords, Long numInsertedRecords) {
        FirstPartyAudience firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
        DeviceIdentifierType deviceIdentifierType = deviceManager.getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeDto.getSystemName());
        FirstPartyAudienceDeviceIdsUploadHistory uploadHistory = audienceManager.newFirstPartyAudienceDeviceIdsUploadHistory(firstPartyAudience, filename, deviceIdentifierType,
                totalNumRecords, numValidatedRecords, numInsertedRecords);
        return this.getObjectDto(FirstPartyAudienceDeviceIdsUploadHistoryDto.class, uploadHistory);
    }

    // ---------------------------------------------------------------------------------------------------------------

    protected boolean isDeviceIdentifierValid(String deviceIdentifier, DeviceIdentifierType deviceIdentifierType) {
        if (StringUtils.isNotEmpty(deviceIdentifier)) {
            return deviceIdentifier.matches(deviceIdentifierType.getValidationRegex());
        }
        return false;
    }

    protected String makeDeviceIdentifierSecure(String deviceIdentifier, DeviceIdentifierType deviceIdentifierType) {
        if (!deviceIdentifierType.isSecure()) {
            return new String(Hex.encodeHex(DigestUtils.sha1(deviceIdentifier)));
        }
        return deviceIdentifier;
    }

    static class DeviceIdsFileUploadReader {

        private final String contentType;

        private Iterator<Row> hssfSheetRowIterator = null;
        private Iterator<Row> xssfSheetRowIterator = null;
        private CSVReader csvReader = null;

        public DeviceIdsFileUploadReader(InputStream inputStream, String contentType) throws IOException {
            this.contentType = contentType;
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLS)) {
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                hssfSheetRowIterator = sheet.iterator();
            }
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLSX)) {
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                xssfSheetRowIterator = sheet.iterator();
            }
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_CSV)) {
                csvReader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, "UTF-8")));
            }
        }

        public String readNext() throws IOException {
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLS) && hssfSheetRowIterator.hasNext()) {
                Row row = hssfSheetRowIterator.next();
                Cell cell = row.getCell(0);
                if (cell != null) {
                    return cell.getStringCellValue();
                }
            }
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLSX) && xssfSheetRowIterator.hasNext()) {
                Row row = xssfSheetRowIterator.next();
                Cell cell = row.getCell(0);
                if (cell != null) {
                    return cell.getStringCellValue();
                }
            }
            if (contentType.equals(DEVICE_IDS_UPLOAD_CONTENT_TYPE_CSV)) {
                String[] nextLine = csvReader.readNext();
                if (nextLine != null && nextLine.length > 0) {
                    return nextLine[0];
                }
            }
            return null;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public DeviceIdsValidated validateDeviceIdsFileUpload(String audienceName, DeviceIdentifierTypeDto deviceIdentifierTypeDto, String fileName, String contentType,
            InputStream inputStream) throws Exception {

        DeviceIdsValidated result = new DeviceIdsValidated();
        result.setIdsValidated(new ArrayList<String>());

        DeviceIdentifierType deviceIdentifierType = deviceManager.getDeviceIdentifierTypeBySystemName(deviceIdentifierTypeDto.getSystemName());
        DeviceIdentifierType securedDeviceIdentifierType = deviceManager.getDeviceIdentifierTypeForPromotion(deviceIdentifierType);

        long totalNumRecords = 0;
        long numValidatedRecords = 0;

        DeviceIdsFileUploadReader iterator = new DeviceIdsFileUploadReader(inputStream, contentType);

        String deviceIdentifier = null;
        while ((deviceIdentifier = iterator.readNext()) != null) {
            LOGGER.debug("Read identifier \"{}\" for audience {} and file {}", new Object[] { deviceIdentifier, audienceName, fileName });
            // Ignoring the header
            if (!StringUtils.isEmpty(deviceIdentifier) && !"device id".equalsIgnoreCase(deviceIdentifier)) {
                totalNumRecords++;
                if (isDeviceIdentifierValid(deviceIdentifier, deviceIdentifierType)) {
                    numValidatedRecords++;
                    String securedDeviceIdentifier = makeDeviceIdentifierSecure(deviceIdentifier, deviceIdentifierType);
                    LOGGER.debug("Identifier \"{}\" is valid for audience {} and file {}. Secured identifier \"{}\"", new Object[] { deviceIdentifier, audienceName, fileName,
                            securedDeviceIdentifier });
                    String thisIdentifier = securedDeviceIdentifier + "~" + securedDeviceIdentifierType.getId();
                    result.getIdsValidated().add(thisIdentifier);

                }
            }
        }

        LOGGER.info("Device upload for audience {} and file {}. Result after validating device Ids: [read={}] [valid={}] ", new Object[] { audienceName, fileName, totalNumRecords,
                numValidatedRecords });

        result.setDevicesValidated(numValidatedRecords);
        result.setDevicesRead(totalNumRecords);

        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public Map<String, String> processDeviceIdsFileUpload(String audienceName, FirstPartyAudienceDto firstPartyAudienceDto, List<String> deviceIds, long totalNumRecords,
            long numValidatedRecords, String fileName, DeviceIdentifierTypeDto deviceIdentifierTypeDto) {
        Map<String, String> rc = new HashMap<String, String>();
        try {

            long numInsertedRecords = 0;

            StringBuilder sb = new StringBuilder();

            int chunk = 1;
            int appendedIds = 0;
            for (String id : deviceIds) {
                String thisIdentifier = (sb.length() > 0 ? "|" : "") + id;
                if (sb.length() + thisIdentifier.length() < MuidLinkDeviceToSegmentStoredProc.MAX_DEVICE_IDS_STRING_LENGTH) {
                    sb.append(thisIdentifier);
                    appendedIds++;
                } else {
                    Long thisCall = muidDao.linkDevicesToSegment(firstPartyAudienceDto.getMuidSegmentId(), sb.toString());
                    numInsertedRecords += thisCall;
                    LOGGER.info("Device upload for audience {} and file {}. Saving device Ids chunk {} into muid segment {}: [passed={}] [inserted={}]", new Object[] {
                            audienceName, fileName, chunk, firstPartyAudienceDto.getMuidSegmentId(), appendedIds, thisCall });
                    chunk++;
                    sb = new StringBuilder();
                    // To make sure we dont start out the next one with a '|'
                    sb.append(id);
                    appendedIds = 1;
                }
            }
            if (sb.length() > 0) {
                Long thisCall = muidDao.linkDevicesToSegment(firstPartyAudienceDto.getMuidSegmentId(), sb.toString());
                numInsertedRecords += thisCall;
                LOGGER.info("Device upload for audience {} and file {}. Saving device Ids chunk {} into muid segment {}: [passed={}] [inserted={}]", new Object[] { audienceName,
                        fileName, chunk, firstPartyAudienceDto.getMuidSegmentId(), appendedIds, thisCall });
            }

            LOGGER.info("Device upload for audience {} and file {}. Saving device Ids into muid segment {} has finished: [total inserted={}]", new Object[] { audienceName,
                    fileName, firstPartyAudienceDto.getMuidSegmentId(), numInsertedRecords });

            createFirstPartyAudienceDeviceIdsUploadHistory(firstPartyAudienceDto, fileName, deviceIdentifierTypeDto, totalNumRecords, numValidatedRecords, numInsertedRecords);

            FirstPartyAudience firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
            // MAD-912 Push recently updated size to db cache
            muidSizeDao.buildSingleSegmentSize(firstPartyAudience.getMuidSegmentId());

            rc.put(DEVICE_IDS_UPLOAD_STATUS, DEVICE_IDS_UPLOAD_STATUS_SUCCESS);
            rc.put(DEVICE_IDS_UPLOAD_NUM_INSERTED_RECORDS, Long.toString(numInsertedRecords));
        } catch (Exception e) {
            rc.put(DEVICE_IDS_UPLOAD_STATUS, DEVICE_IDS_UPLOAD_STATUS_FAILURE);
            rc.put(DEVICE_IDS_UPLOAD_STATUS_ERROR_MESSAGE, e.getMessage());
            rc.put(DEVICE_IDS_UPLOAD_STATUS_STACK_TRACE, ExceptionUtils.getStackTrace(e));
            LOGGER.error("Device upload for audience {} and file {}. Exception {}", new Object[] { audienceName, fileName, e.getMessage() }, e);
        }

        return rc;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto) {
        FirstPartyAudience firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
        FirstPartyAudienceDeviceIdsUploadHistoryFilter filter = new FirstPartyAudienceDeviceIdsUploadHistoryFilter().setFirstPartyAudience(firstPartyAudience);
        return audienceManager.countAllFirstPartyAudienceDeviceIdsUploadHistories(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FirstPartyAudienceDeviceIdsUploadHistoryDto> getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto,
            FirstPartyAudienceDeviceIdsUploadHistorySortBy sortBy) {
        FirstPartyAudience firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
        FirstPartyAudienceDeviceIdsUploadHistoryFilter filter = new FirstPartyAudienceDeviceIdsUploadHistoryFilter().setFirstPartyAudience(firstPartyAudience);
        FetchStrategy fs = new FetchStrategyBuilder().addInner(FirstPartyAudienceDeviceIdsUploadHistory_.deviceIdentifierType).build();
        List<FirstPartyAudienceDeviceIdsUploadHistory> list = audienceManager.getAllFirstPartyAudienceDeviceIdsUploadHistories(filter,
                (sortBy != null ? sortBy.getSorting() : null), fs);
        return this.makeListFromCollection(getList(FirstPartyAudienceDeviceIdsUploadHistoryDto.class, list));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FirstPartyAudienceDeviceIdsUploadHistoryDto> getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(FirstPartyAudienceDto firstPartyAudienceDto,
            FirstPartyAudienceDeviceIdsUploadHistorySortBy sortBy, Pagination page) {
        FirstPartyAudience firstPartyAudience = audienceManager.getFirstPartyAudienceById(firstPartyAudienceDto.getId());
        FirstPartyAudienceDeviceIdsUploadHistoryFilter filter = new FirstPartyAudienceDeviceIdsUploadHistoryFilter().setFirstPartyAudience(firstPartyAudience);
        Pagination pagination = null;
        if (sortBy != null) {
            pagination = new Pagination(page, sortBy.getSorting());
        } else {
            pagination = page;
        }
        FetchStrategy fs = new FetchStrategyBuilder().addInner(FirstPartyAudienceDeviceIdsUploadHistory_.deviceIdentifierType).build();
        List<FirstPartyAudienceDeviceIdsUploadHistory> list = audienceManager.getAllFirstPartyAudienceDeviceIdsUploadHistories(filter, pagination, fs);
        return this.makeListFromCollection(getList(FirstPartyAudienceDeviceIdsUploadHistoryDto.class, list));
    }

    @Override
    public FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel createFirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel(FirstPartyAudienceDto firstPartyAudience) {
        return new FirstPartyAudienceDeviceIdsUploadHistoryLazyDataModel(firstPartyAudience, this);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateAudienceDataFee(AudienceDto audienceDto) {
        Audience audience = null;

        if (audienceDto.persisted()) {
            audience = audienceManager.getAudienceById(audienceDto.getId());
        } else {
            audience = getDtoFromObject(Audience.class, audienceDto);
            createChildEntitiesAudiences(audienceDto, audience);
        }

        AudiencePrices audiencePrices = feeManager.calculateAudienceDataFee(audience);
        return audiencePrices.getDataRetail();
    }
}
