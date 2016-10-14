package com.adfonic.presentation.campaign;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.creative.CreativeFormatDto;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.trigger.CampaignTriggerDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.dto.language.LanguageDto;
import com.adfonic.dto.user.UserDTO;

public interface CampaignService {

    /**
     * Get's Campaigns acoording to the search criteria provided in the
     * CampaignSearchDto
     * 
     * @param dto
     * @return
     */
    public CampaignSearchDto getCampaigns(final CampaignSearchDto dto);
    
    public CampaignSearchDto getCampaignsThatHaveEverBeenActive(final CampaignSearchDto campaignSearchDto);
    public CampaignSearchDto getOptimisableCampaigns(final CampaignSearchDto campaignSearchDto);

    public CampaignDto getCampaignWithNameForAdvertiser(final CampaignSearchDto dto);
    
    public CampaignTypeAheadDto getCampaignWithName(final CampaignSearchDto dto);

    public CampaignDto getCampaignById(final CampaignSearchDto dto);
    
    public CampaignDto getCampaignByIdWithExternal(final Long id);

    public CampaignDto getCampaignById(final CampaignDto dto);

    public Campaign getCampaignEntityById(final CampaignDto dto);
    
    public CampaignDto getCampaignByExternalId(final CampaignDto dto);
    
    public CampaignTypeAheadDto getCampaignTypeAheadDtoById(final CampaignSearchDto dto);
    
    public List<CampaignDto> getCampaignsById(final String[] campaignsIds,long advertiserId);

    public CampaignDto saveSetUp(CampaignDto dto);
    
    public CampaignDto saveScheduling(CampaignDto dto, List<CampaignTriggerDto> campaignTriggers);

    public CampaignDto saveTargeting(CampaignDto dto, boolean saveLocationTarget);
    
    public CampaignDto saveInventoryTargeting(CampaignDto dto, PublicationListInfoDto publicationListDto, boolean includeUncathegorized);

    public CampaignDto saveBid(CampaignDto dto);

    public CampaignDto saveTracking(CampaignDto dto);
    
    public CampaignDto submit(CampaignCreativeDto dto,UserDTO userDto,CampaignStatus campaignSubmissionStatus);

    public boolean isPremiumCampaign(CampaignDto dto);

    public BigDecimal getBidMin(CampaignDto dto);
    
    public boolean isApplicationDestination(CampaignDto dto);
    
    public LanguageDto getLanguageByName(String name);
    
    public LanguageDto getLanguageById(Long id);
    
    public List<LanguageDto> getAllLanguages();
    public List<LanguageDto> getFirstLanguages();
    public List<LanguageDto> getSecondLanguages();
    
    public CampaignCreativeDto save(CampaignCreativeDto dto);
    
    public CampaignDto copyCampaign(long campaignId);
    public CampaignCreativeDto loadCreatives(long campaignId);
    public Collection<CreativeFormatDto> getAllCreativesForCampaignIds(Collection<Long> campaignIds);
    
    public CampaignCreativeDto copyCreatives(CampaignCreativeDto dto,long newCampaignId);
    public void changeCampaignStatus(List<Long> campaignIds , CampaignStatus campStatus);
    
    public PublicationListInfoDto loadPublicationList(long campaignId);
    
    public Map<String, Object> adOpsActivateNewCampaign(CampaignDto campaignDto,
                                                        String advertiserDomain,
                                                        SegmentSafetyLevel safetyLevel,
                                                        CategoryHierarchyDto campaignIabCategoryDto,
                                                        List<CategoryHierarchyDto> blackListedPublicationCategoryDtos,
                                                        AdfonicUser adfonicUser);
    
    public void adOpsUpdateExistingCampaign(CampaignDto campaignDto,
                                            String advertiserDomain,
                                            SegmentSafetyLevel safetyLevel,
                                            CategoryHierarchyDto campaignIabCategoryDto,
                                            List<CategoryHierarchyDto> blackListedPublicationCategoryDtos,
                                            boolean approveAllNewCreatives,
                                            AdfonicUser adfonicUser);

    public SegmentSafetyLevel getSafetyLevelForCampaign(CampaignDto campaignDto);
    
    public void addTimePeriod(CampaignTimePeriodDto timePeriod, Long campaignId);
    
    public void newCreativeHistory(CreativeDto creativeDto, String comment, AdfonicUser adfonicUser);
    
    public boolean isConversionTrackingUsed(Collection<Long> campaignIds, Long advertiserId);

    public boolean isGeotargetingUsed(Collection<Long> campaignIds, Long advertiserId);
    
    public List<CampaignTriggerDto> getCampaignTriggers(CampaignDto campaignDto);
    
    public List<CampaignAudienceDto> getCampaignAudiences(CampaignDto campaignDto);
    
    public boolean hasAllCreativeRejected(Long campaignId);
}
