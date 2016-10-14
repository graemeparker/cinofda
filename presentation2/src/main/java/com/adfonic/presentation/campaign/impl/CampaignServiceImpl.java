package com.adfonic.presentation.campaign.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.Browser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignTargetCTR;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.CampaignTrigger;
import com.adfonic.domain.CampaignTrigger.PluginType;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.Country;
import com.adfonic.domain.Country_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.Language;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Platform;
import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PrivateMarketPlaceDeal_;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.browser.BrowserDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.campaignbid.BidDeductionDto;
import com.adfonic.dto.campaign.creative.BeaconUrlDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.creative.CreativeFormatDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.BiddingStrategyName;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.dto.campaign.segment.SegmentPartialDto;
import com.adfonic.dto.campaign.trigger.CampaignTriggerDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.category.CategoryDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.dto.category.CategoryPartialDto;
import com.adfonic.dto.channel.ChannelDto;
import com.adfonic.dto.channel.ChannelPartialDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.geotarget.GeotargetDto;
import com.adfonic.dto.geotarget.GeotargetPartialDto;
import com.adfonic.dto.geotarget.LocationTargetDto;
import com.adfonic.dto.language.LanguageDto;
import com.adfonic.dto.model.ModelDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.publisher.PublisherDto;
import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.presentation.targetpublisher.TargetPublisherService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.audience.filter.CampaignAudienceFilter;
import com.byyd.middleware.audience.service.AudienceManager;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;
import com.byyd.middleware.integrations.service.IntegrationsManager;
import com.byyd.middleware.publication.service.PublicationManager;

@Service("campaignService")
public class CampaignServiceImpl extends GenericServiceImpl implements CampaignService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignServiceImpl.class);

    private static final int DEFAULT_MAX_AGE = 75;
    private static final int DEFAULT_MIN_AGE = 0;
    private static final int MAX_ELEMENTS_TO_QUERY = 1000;
    private static final int ENGLISH_LANGUAGE_ID = 38;
    private static final double SECONDS_IN_MILLS = 0.001;
    private static final int MINUTES_IN_A_DAY = 1439;
    private static final int SECONDS_IN_A_MINUTE = 60;
    private static final int MILLISECONDS_IN_A_SECOND = 1000;

    @Autowired
    private CommonManager commonManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private TargetingManager targetingManager;
    @Autowired
    private CampaignManager campaignManager;
    @Autowired
    FeeManager feeManager;
    @Autowired
    BiddingManager biddingManager;
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private PublisherManager publisherManager;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private AudienceManager audienceManager;
    @Autowired
    private IntegrationsManager integrationsManager;
    
    @Autowired
    private CreativeService cService;
    @Autowired
    private TargetPublisherService targetPublisherService;
    @Autowired
    private org.dozer.Mapper mapper;

    @Override
    public CampaignSearchDto getCampaigns(final CampaignSearchDto campaignSearchDto) {

        CampaignSearchDto result = new CampaignSearchDto();
        CampaignFilter filter = new CampaignFilter();

        if (!StringUtils.isEmpty(campaignSearchDto.getName())) {
            filter.setNameWithPreviousSpace(true);
            filter.setContainsName(campaignSearchDto.getName());
            LOGGER.debug("Setting Campaign Filter contain name = {}", campaignSearchDto.getName());
        }

        if (campaignSearchDto.getAdvertiser() != null && campaignSearchDto.getAdvertiser().getId() != null) {
            filter.setAdvertiser(advertiserManager.getAdvertiserById(campaignSearchDto.getAdvertiser().getId()));
        }

        Collection<Campaign> campaigns = campaignManager.getAllCampaigns(filter);
        Collection<CampaignTypeAheadDto> campaignsResult = getList(CampaignTypeAheadDto.class, campaigns);
        result.setCampaigns(campaignsResult);

        return result;
    }

    @Override
    public CampaignSearchDto getOptimisableCampaigns(final CampaignSearchDto campaignSearchDto) {

        CampaignSearchDto result = new CampaignSearchDto();
        CampaignFilter filter = new CampaignFilter();
        filter.setAdvertiser(advertiserManager.getAdvertiserById(campaignSearchDto.getAdvertiser().getId()));
        filter.setContainsName(campaignSearchDto.getName());
        filter.setNameWithPreviousSpace(true);
        List<Campaign.Status> statuses = new ArrayList<>();
        statuses.add(Campaign.Status.ACTIVE);
        statuses.add(Campaign.Status.PAUSED);
        statuses.add(Campaign.Status.COMPLETED);
        filter.setStatuses(statuses);

        Collection<Campaign> campaigns = campaignManager.getAllCampaigns(filter, new Sorting(SortOrder.asc("name")));
        Collection<CampaignTypeAheadDto> campaignsResult = getList(CampaignTypeAheadDto.class, campaigns);
        result.setCampaigns(campaignsResult);

        return result;
    }

    @Override
    public CampaignSearchDto getCampaignsThatHaveEverBeenActive(final CampaignSearchDto campaignSearchDto) {

        CampaignSearchDto result = new CampaignSearchDto();

        Collection<Campaign> campaigns = campaignManager.getAllCampaignsThatHaveEverBeenActiveForAdvertiser(
                advertiserManager.getAdvertiserById(campaignSearchDto.getAdvertiser().getId()), // Advertiser
                                                                                            // advertiser,
                campaignSearchDto.getName(), // String containsName,
                true); // boolean nameWithPreviousSpace;
        Collection<CampaignTypeAheadDto> campaignsResult = getList(CampaignTypeAheadDto.class, campaigns);
        result.setCampaigns(campaignsResult);

        return result;
    }

    @Override
    public CampaignDto getCampaignWithNameForAdvertiser(final CampaignSearchDto dto) {
        LOGGER.debug("Getting Campaign with name={} for Advertiser[{}]", dto.getName(), dto.getAdvertiser());

        List<Campaign> campaigns = campaignManager.getCampaignWithNameForAdvertiser(dto.getName(),
                advertiserManager.getAdvertiserById(dto.getAdvertiser().getId()), null);
        if (!CollectionUtils.isEmpty(campaigns) && campaigns.size() == 1) {
            CampaignTypeAheadDto typeDto = getObjectDto(CampaignTypeAheadDto.class, campaigns.get(0));
            return getDtoFromObject(CampaignDto.class, typeDto);
        } else {
            return new CampaignDto();
        }
    }

    @Override
    public CampaignTypeAheadDto getCampaignWithName(final CampaignSearchDto dto) {
        LOGGER.debug("Getting Campaign with name={} for Advertiser[{}]", dto.getName(), dto.getAdvertiser());

        List<Campaign> campaigns = campaignManager.getCampaignWithNameForAdvertiser(dto.getName(),
                advertiserManager.getAdvertiserById(dto.getAdvertiser().getId()), null);
        if (!CollectionUtils.isEmpty(campaigns) && campaigns.size() == 1) {
            return getObjectDto(CampaignTypeAheadDto.class, campaigns.get(0));
        } else {
            return new CampaignTypeAheadDto();
        }
    }

    @Override
    public CampaignDto getCampaignById(final CampaignSearchDto dto) {
        Campaign campaign = campaignManager.getCampaignById(dto.getId());
        if (campaign != null) {
            CampaignTypeAheadDto typeDto = getObjectDto(CampaignTypeAheadDto.class, campaign);
            return getDtoFromObject(CampaignDto.class, typeDto);
        } else {
            return null;
        }
    }

    @Override
    public CampaignDto getCampaignByIdWithExternal(final Long id) {
        Campaign campaign = campaignManager.getCampaignById(id);
        if (campaign != null) {
            CampaignTypeAheadDto typeDto = getObjectDto(CampaignTypeAheadDto.class, campaign);
            CampaignDto cDto = getDtoFromObject(CampaignDto.class, typeDto);
            cDto.setExternalID(campaign.getExternalID());
            return cDto;
        } else {
            return null;
        }
    }

    @Override
    public CampaignTypeAheadDto getCampaignTypeAheadDtoById(final CampaignSearchDto dto) {
        Campaign campaign = campaignManager.getCampaignById(dto.getId());
        if (campaign != null) {
            return getObjectDto(CampaignTypeAheadDto.class, campaign);
        } else {
            return null;
        }
    }

    @Override
    public List<CampaignDto> getCampaignsById(final String[] campaignsIds, long advertiserId) {
        FetchStrategy adv = new FetchStrategyBuilder().addLeft(Campaign_.advertiser).build();
        List<CampaignDto> result = new ArrayList<CampaignDto>(0);
        for (String camp : campaignsIds) {
            CampaignSearchDto dto = new CampaignSearchDto();
            dto.setId(Long.parseLong(camp.trim()));
            Campaign campaign = campaignManager.getCampaignById(dto.getId(), adv);
            if (campaign != null && advertiserId == campaign.getAdvertiser().getId()) {
                CampaignTypeAheadDto typeDto = getObjectDto(CampaignTypeAheadDto.class, campaign);
                result.add(getDtoFromObject(CampaignDto.class, typeDto));
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDto getCampaignById(final CampaignDto dto) {
        Campaign campaign = campaignManager.getCampaignById(dto.getId()/*
                                                                      * ,campaignFs
                                                                      */);

        return loadCampaign(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDto getCampaignByExternalId(final CampaignDto dto) {
        Campaign campaign = campaignManager.getCampaignByExternalId(dto.getExternalID());

        return loadCampaign(campaign);
    }

    private CampaignDto loadCampaign(Campaign campaign) {
        Segment segment = campaign.getSegments().get(0);
        Segment loaded = loadSegment(segment.getId());

        SegmentPartialDto segmentPartialDto = mapper.map(loaded, SegmentPartialDto.class);
        SegmentDto segmentDto = mapper.map(segmentPartialDto, SegmentDto.class);

        CampaignDto alreadyLoaded = mapper.map(campaign, CampaignDto.class);
        loadAllSegmentDetails(loaded, segmentDto);
        alreadyLoaded.getSegments().clear();
        alreadyLoaded.getSegments().add(segmentDto);
        getTargetPublisherDetails(alreadyLoaded);

        return alreadyLoaded;
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignEntityById(final CampaignDto dto) {
        return campaignManager.getCampaignById(dto.getId());
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveSetUp(CampaignDto dto) {
        CampaignDto localDto = dto;

        if (localDto.getId() != null && localDto.getId().longValue() > 0) {
            // update in setup!
            // eithe rname or reference has changed...
            Campaign campaign = campaignManager.getCampaignById(localDto.getId());
            campaign.setName(localDto.getName());
            campaign.setReference(localDto.getReference());
            campaign.setOpportunity(localDto.getOpportunity());

            campaign = campaignManager.update(campaign);

            campaign = campaignManager.getCampaignById(campaign.getId());

            Segment segment = campaign.getSegments().get(0);
            Segment loaded = loadSegment(segment.getId());
            SegmentPartialDto segmentPartialDto = mapper.map(loaded, SegmentPartialDto.class);
            SegmentDto segmentDto = mapper.map(segmentPartialDto, SegmentDto.class);

            localDto = mapper.map(campaign, CampaignDto.class);

            loadAllSegmentDetails(loaded, segmentDto);
            localDto.getSegments().clear();
            localDto.getSegments().add(segmentDto);

            getTargetPublisherDetails(localDto);

        } else {
            // save setup here
            Campaign entity = mapper.map(localDto, Campaign.class);

            Advertiser adv = advertiserManager.getAdvertiserById(localDto.getAdvertiser().getId());
            Language lang = commonManager.getLanguageById(Long.valueOf(ENGLISH_LANGUAGE_ID));

            if (entity.getCategory() == null) {
                entity.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));
            }

            if (StringUtils.isEmpty(entity.getName())) {
                entity.setName(StringUtils.EMPTY);
            }

            StopWatch stWatch = startWatch();
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newCampaign Begin [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);

            entity = campaignManager.newCampaign(entity.getName(), adv, entity.getCategory(), lang, entity.getDisableLanguageMatch());

            stWatch.stop();
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newCampaign End [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
            entity.setReference(localDto.getReference());
            entity.setOpportunity(localDto.getOpportunity());

            // set the segments here.mandatory segments!!
            if (CollectionUtils.isEmpty(localDto.getSegments())) {
                // adding default segment
                stWatch.reset();
                stWatch.start();
                LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newSegment Begin [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
                Segment newSegment = targetingManager.newSegment(adv, null);
                stWatch.stop();
                LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newSegment End [[{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
                newSegment.setMinAge(DEFAULT_MIN_AGE);
                newSegment.setMaxAge(DEFAULT_MAX_AGE);
                entity.getSegments().add(newSegment);
            } else {
                // Persist segments.
                List<SegmentDto> segments = localDto.getSegments();
                stWatch.reset();
                stWatch.start();
                LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newSegment Looping through all Segments - Begin [{}] seconds",
                        stWatch.getTime() * SECONDS_IN_MILLS);
                persistCampaignSegments(localDto, adv, segments);
                stWatch.stop();
                LOGGER.info("CampaignServiceImpl save(CampaignDto dto) newSegment Looping through all Segments - End [{}] seconds",
                        stWatch.getTime() * SECONDS_IN_MILLS);
            }

            // transation problems here if we convert entity with mapper.
            entity = campaignManager.update(entity);

            entity = campaignManager.getCampaignById(entity.getId());

            localDto = loadCampaignFullDetails(entity, localDto);

        }
        return localDto;
    }

    private void persistCampaignSegments(CampaignDto dto, Advertiser adv, List<SegmentDto> segments) {
        for (SegmentDto seg : segments) {
            Segment segEntity = targetingManager.newSegment(adv, seg.getName());
            SegmentDto temp = mapper.map(segEntity, SegmentDto.class);
            if (!dto.getSegments().contains(temp)) {
                dto.getSegments().add(temp);
            }
        }
    }

    private CampaignDto loadCampaignFullDetails(Campaign campaign, CampaignDto dto) {
        Segment segment = campaign.getSegments().get(0);
        Segment loaded = loadSegment(segment.getId());
        SegmentPartialDto segmentPartialDto = mapper.map(loaded, SegmentPartialDto.class);
        SegmentDto segmentDto = mapper.map(segmentPartialDto, SegmentDto.class);

        CampaignDto returnDto = dto;
        returnDto = mapper.map(campaign, CampaignDto.class);

        loadAllSegmentDetails(loaded, segmentDto);
        returnDto.getSegments().clear();
        returnDto.getSegments().add(segmentDto);

        getTargetPublisherDetails(returnDto);

        return returnDto;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveScheduling(CampaignDto dto, List<CampaignTriggerDto> campaignTriggers) {
        CampaignDto returnDto = dto;

        // Update campaign
        if (returnDto.getId() != null && returnDto.getId().longValue() > 0) {
            // already persisted
            Campaign campaign = campaignManager.getCampaignById(returnDto.getId());
            // set scheduling thingy.
            StopWatch stWatch = startWatch();
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saveTimePeriods Begin [{}] seconds", stWatch.getTime()
                    * SECONDS_IN_MILLS);
            campaign = saveTimePeriods(campaign, returnDto);
            stWatch.stop();
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saveTimePeriods End [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
            // campaign should have time periods here.
            stWatch.reset();
            stWatch.start();
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saveTime Begin [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
            campaign = saveTime(campaign, returnDto);
            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saveTime End [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);

            if (StringUtils.isEmpty(returnDto.getName())) {
                returnDto.setName(StringUtils.EMPTY);
            }
            campaign.setName(returnDto.getName());
            campaign.setReference(returnDto.getReference());
            campaign.setEvenDistributionOverallBudget(returnDto.isEvenDistributionOverallBudget());
            campaign.setEvenDistributionDailyBudget(returnDto.isEvenDistributionDailyBudget());
            // retrieve again to copy everything
            campaign = campaignManager.update(campaign);

            // Saving campaign triggers
            saveCampaignTriggers(campaign, campaignTriggers);

            // get campaign again with fetchstrategy to map back to dtos
            campaign = campaignManager.getCampaignById(campaign.getId());

            returnDto = loadCampaignFullDetails(campaign, returnDto);
        }

        return returnDto;
    }

    @Override
    @Transactional(readOnly = false)
    public void addTimePeriod(CampaignTimePeriodDto timePeriod, Long campaignId) {
        Campaign c = campaignManager.getCampaignById(campaignId);
        CampaignTimePeriod ctp = new CampaignTimePeriod(c, timePeriod.getStartDate(), timePeriod.getEndDate());
        c = targetingManager.addTimePeriodToCampaign(c, ctp);
        campaignManager.update(c);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveTargeting(CampaignDto dto, boolean saveLocationTarget) {
        FetchStrategy modifiedFs = new FetchStrategyBuilder().addLeft(Campaign_.advertiser).addLeft(Campaign_.segments)
                .addLeft(Campaign_.timePeriods).addLeft(Campaign_.transparentNetworks).addLeft(Campaign_.defaultLanguage)
                .addLeft(Campaign_.currentBid).addLeft(Campaign_.deviceIdentifierTypes)
                .addLeft(Campaign_.currentRichMediaAdServingFee).addLeft(Campaign_.currentTradingDeskMargin)
                .addLeft(Campaign_.currentDataFee).addLeft(Campaign_.privateMarketPlaceDeal).addLeft(Campaign_.campaignAudiences)
                .addLeft(PrivateMarketPlaceDeal_.publisher).addLeft(Advertiser_.company).addLeft(Country_.operators)
                .addLeft(Country_.region).build();

        CampaignDto returnDto = dto;

        // Update campaign
        StopWatch stWatch = startWatch();
        LOGGER.info("CampaignServiceImpl saveTargeting(CampaignDto dto) saveTargeting Begin [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
        if (returnDto.getId() != null && returnDto.getId().longValue() > 0) {
            // already persisted
            Campaign campaign = campaignManager.getCampaignById(dto.getId());

            campaign = saveTargeting(campaign, returnDto, saveLocationTarget);
            campaign = saveAudiences(campaign, returnDto);

            List<SegmentDto> segmentDtoList = returnDto.getSegments();

            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saving Targeting End [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
            // retrieve again to copy everything

            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saving Targeting starting update campaign Begin [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);

            campaign = campaignManager.update(campaign);

            LOGGER.info("CampaignServiceImpl save(CampaignDto dto) saving Targeting starting update campaign End [{}] seconds", stWatch.getTime() * SECONDS_IN_MILLS);
            // get campaign again with fetchstrategy to map back to dtos

            campaign = campaignManager.getCampaignById(campaign.getId(), modifiedFs);

            // we should have here segmentDto's
            returnDto = mapper.map(campaign, CampaignDto.class);

            // This next like added --Pierre
            returnDto.getSegments().clear();
            returnDto.getSegments().addAll(segmentDtoList);

            getTargetPublisherDetails(returnDto);
        }

        return returnDto;
    }

    private StopWatch startWatch() {
        StopWatch stWatch = new StopWatch();
        stWatch.start();
        return stWatch;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveInventoryTargeting(CampaignDto dto, PublicationListInfoDto publicationListDto, boolean includeUncathegorized) {
        if (dto.getId() != null && dto.getId().longValue() > 0) {
            Campaign campaign = campaignManager.getCampaignById(dto.getId());
            SegmentDto segmentDto = dto.getSegments().get(0);
            Segment segment = loadSegment(segmentDto);

            // Clean inventory information
            cleanInventoryTargeting(segment, campaign);

            // Setting safety level
            segment.setSafetyLevel(segmentDto.getSafetyLevel());

            // Saving segment information
            boolean savePubList = true;
            switch (dto.getInventoryType()) {
            case EXCHANGE_INVENTORY:
                saveInventorySources(segment, dto);
                break;
            case WHITELIST:
                targetingManager.update(segment);
                break;
            case CATEGORY:
                saveCategories(segment, segmentDto, includeUncathegorized);
                break;
            case PRIVATE_MARKET_PLACE:
                targetingManager.update(segment);
                campaign = savePrivateMarketPlace(campaign, dto);
                savePubList = false;
                break;
            default:
                break;
            }

            // Saving publication list
            if (savePubList) {
                campaign.setPublicationList(publicationManager.getPublicationListById(publicationListDto.getId()));
            }
            campaign.setInventoryTargetingType(dto.getInventoryType().getInventoryTargetingType());
            campaign = campaignManager.update(campaign);
            campaign = campaignManager.getCampaignById(dto.getId());
            loadAllSegmentDetails(segment, segmentDto);

            dto.getSegments().clear();
            dto.getSegments().add(segmentDto);
            dto.getRtb().clear();
            dto.getNonRtb().clear();
            getTargetPublisherDetails(dto);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveBid(CampaignDto dto) {
        CampaignDto returnDto = dto;

        if (returnDto.getId() != null && returnDto.getId().longValue() > 0) {
            // already persisted
            SegmentDto segmentDto = returnDto.getCurrentSegment();

            Campaign campaign = campaignManager.getCampaignById(returnDto.getId());

            campaign = saveBid(campaign, returnDto);

            if (campaign.getStatus() == null || campaign.getStatus().equals(Campaign.Status.NEW)) {
                campaign.setStatus(Campaign.Status.NEW_REVIEW);
            }

            campaign.setEvenDistributionOverallBudget(returnDto.isEvenDistributionOverallBudget());
            campaign.setEvenDistributionDailyBudget(returnDto.isEvenDistributionDailyBudget());
            campaign.setPriceOverridden(returnDto.isPriceOverridden());
            // retrieve again to copy everything
            campaign = campaignManager.update(campaign);

            // get campaign again with fetchstrategy to map back to dtos
            campaign = campaignManager.getCampaignById(campaign.getId());

            // fees handling
            BigDecimal newCampaignRichMediaAdServingFee = (returnDto.getCurrentRichMediaAdServingFee()!=null ? returnDto.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee() : null);
            feeManager.saveCampaignRichMediaAdServingFee(campaign.getId(), newCampaignRichMediaAdServingFee);
            
            BigDecimal newCampaignTradingDeskMargin = (returnDto.getCurrentTradingDeskMargin()!=null ? returnDto.getCurrentTradingDeskMargin().getTradingDeskMargin() : null);
            feeManager.saveCampaignTradingDeskMargin(campaign.getId(), newCampaignTradingDeskMargin);

            // retrieve again to copy everything
            campaign = campaignManager.update(campaign);

            returnDto = loadTargetingDtoInfo(segmentDto, campaign);
        }

        return returnDto;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto saveTracking(CampaignDto dto) {
        CampaignDto returnDto = dto;

        if (returnDto.getId() != null && returnDto.getId().longValue() > 0) {
            // already persisted
            SegmentDto segmentDto = returnDto.getCurrentSegment();

            Campaign campaign = campaignManager.getCampaignById(returnDto.getId());

            campaign = saveTracking(campaign, returnDto);

            // retrieve again to copy everything
            campaign = campaignManager.update(campaign);

            returnDto = loadTargetingDtoInfo(segmentDto, campaign);
        }
        return returnDto;
    }

    private CampaignDto loadTargetingDtoInfo(SegmentDto segmentDto, Campaign campaign) {
        // we should have here segmentDto's
        CampaignDto returnDto = mapper.map(campaignManager.getCampaignById(campaign.getId()), CampaignDto.class);

        Segment segment = loadSegment(segmentDto);
        loadAllSegmentDetails(segment, segmentDto);

        returnDto.getSegments().clear();
        returnDto.getSegments().add(segmentDto);

        getTargetPublisherDetails(returnDto);
        return returnDto;
    }

    private void loadAllSegmentDetails(Segment updated, SegmentDto dto) {
        loadChannels(updated, dto);
        loadBrowsers(updated, dto);
        loadDemographics(updated, dto);
        loadPlatforms(updated, dto);
        loadDeviceGroups(updated, dto);
        loadModels(updated, dto);
        loadExcludedModels(updated, dto);
        loadCountries(updated, dto);
        loadGeoTargets(updated, dto);
        loadOperators(updated, dto);
        loadConnectionType(updated, dto);
        loadCategories(updated, dto);
        loadIpAddresses(updated, dto);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto submit(CampaignCreativeDto dto, UserDTO userDto, CampaignStatus campaignSubmissionStatus) {
        CampaignDto campaignDto;

        if (dto.getId() != null && dto.getId().longValue() > 0) {
            // already persisted

            // campaign with creative fetch strategy
            Campaign campaign = campaignManager.getCampaignById(dto.getId()/*
                                                                          * ,
                                                                          * campaignCreativesFs
                                                                          */);

            // Creatives are submited when campaign is new, if not they are
            // submitted individually
            if (campaign.getStatus().equals(Campaign.Status.NEW) || campaign.getStatus().equals(Campaign.Status.NEW_REVIEW)) {
                campaign = submitCreatives(dto, campaignSubmissionStatus);
            }

            // retrieve again to copy everything
            campaign = campaignManager.update(campaign);

            campaign = campaignManager.getCampaignById(dto.getId()/* , campaignFs */);

            // campaign.getSegments().clear();
            // return campaignDto
            // we should have here segmentDto's
            campaignDto = mapper.map(campaign, CampaignDto.class);
            // get campaign again with fetchstrategy to map back to dtos

        } else {
            // get campaign again with fetchstrategy to map back to dtos
            Campaign campaign = campaignManager.getCampaignById(dto.getId());

            // we should have here segmentDto's
            campaignDto = mapper.map(campaign, CampaignDto.class);
        }
        return campaignDto;
    }

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> adOpsActivateNewCampaign(CampaignDto campaignDto, String advertiserDomain, SegmentSafetyLevel safetyLevel,
            CategoryHierarchyDto campaignIabCategoryDto, List<CategoryHierarchyDto> blackListedPublicationCategoryDtos,
            AdfonicUser adfonicUser) {
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        Category campaignIabCategory = commonManager.getCategoryById(campaignIabCategoryDto.getId());
        Set<Category> blackListedPublicationCategories = new HashSet<Category>();
        for (CategoryHierarchyDto dto : blackListedPublicationCategoryDtos) {
            Category category = commonManager.getCategoryById(dto.getId());
            if (category != null) {
                blackListedPublicationCategories.add(category);
            }
        }
        return campaignManager.adOpsActivateNewCampaign(campaign, advertiserDomain, safetyLevel, campaignIabCategory,
                blackListedPublicationCategories, adfonicUser);
    }

    @Override
    @Transactional(readOnly = false)
    public void adOpsUpdateExistingCampaign(CampaignDto campaignDto, String advertiserDomain, SegmentSafetyLevel safetyLevel,
            CategoryHierarchyDto campaignIabCategoryDto, List<CategoryHierarchyDto> blackListedPublicationCategoryDtos,
            boolean approveAllNewCreatives, AdfonicUser adfonicUser) {
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        Category campaignIabCategory = commonManager.getCategoryById(campaignIabCategoryDto.getId());
        Set<Category> blackListedPublicationCategories = new HashSet<Category>();
        for (CategoryHierarchyDto dto : blackListedPublicationCategoryDtos) {
            Category category = commonManager.getCategoryById(dto.getId());
            if (category != null) {
                blackListedPublicationCategories.add(category);
            }
        }
        campaignManager.adOpsUpdateExistingCampaign(campaign, advertiserDomain, safetyLevel, campaignIabCategory,
                blackListedPublicationCategories, approveAllNewCreatives, adfonicUser);
    }

    private Campaign submitCreatives(CampaignCreativeDto dto, CampaignStatus campaignSubmissionStatus) {
        Campaign campaign = campaignManager.getCampaignById(dto.getId());
        boolean hasPendingCreative = false;
        boolean hasActiveCreative = false;
        for (CreativeDto creative : dto.getCreatives()) {
            // Submit creatives
            if (creative.getStatus().equals(Creative.Status.NEW) ) {
                cService.submitCreative(creative, false);
                hasPendingCreative = true;
            } else if (creative.getStatus() != Creative.Status.ACTIVE) { // each
                                                                         // NEW
                                                                         // Creative
                hasActiveCreative = true;
            }
        }

        switch (campaign.getStatus()) {
        case NEW_REVIEW:
            if (hasPendingCreative) {
                if (campaignSubmissionStatus.equals(CampaignStatus.PENDING_PAUSED)) {
                    campaign.setStatus(Campaign.Status.PENDING_PAUSED);
                } else if (campaignSubmissionStatus.equals(CampaignStatus.PENDING)) {
                    campaign.setStatus(Campaign.Status.PENDING);
                }
            }
            LOGGER.debug("in new review");
            break;
        case PENDING:
            break;
        case ACTIVE:
        case PAUSED:
            if (!hasActiveCreative) {
                campaign.setStatus(Campaign.Status.PENDING);
            }
            break;
        default:
            break;
        }

        return campaign;
    }

    private Campaign saveAudiences(Campaign campaign, CampaignDto dto) {
        Set<CampaignAudience> campaignAudiences = new HashSet<CampaignAudience>();
        if (!CollectionUtils.isEmpty(dto.getCampaignAudiences())) {
            for (CampaignAudienceDto audienceDto : dto.getCampaignAudiences()) {
                CampaignAudience ca = new CampaignAudience();
                if (audienceDto.getId() != null) {
                    ca.setId(audienceDto.getId());
                }
                ca.setAudience(audienceManager.getAudienceById(audienceDto.getAudience().getId()));
                ca.setInclude(audienceDto.isInclude());

                // Audience Recency
                ca.setRecencyDateFrom(audienceDto.getRecencyDateFrom());
                ca.setRecencyDateTo(audienceDto.getRecencyDateTo());
                ca.setRecencyDaysFrom(audienceDto.getRecencyDaysFrom());
                ca.setRecencyDaysTo(audienceDto.getRecencyDaysTo());

                campaignAudiences.add(ca);
            }
        }

        return audienceManager.updateCampaignAudiences(campaign, campaignAudiences);
    }

    private Campaign saveTimePeriods(Campaign entity, CampaignDto dto) {
        // ensure campaign is persisted
        Set<CampaignTimePeriod> set = new HashSet<CampaignTimePeriod>(0);

        Campaign localEntity = entity;

        if (!CollectionUtils.isEmpty(dto.getTimePeriods())) {
            List<CampaignTimePeriodDto> timePeriodsList = dto.getTimePeriods();
            for (CampaignTimePeriodDto ct : timePeriodsList) {
                CampaignTimePeriod timePeriod = mapper.map(ct, CampaignTimePeriod.class);
                saveTimePeriod(localEntity, ct, timePeriod);

                set.add(timePeriod);
            }
            // validate time range

            targetingManager.deleteCampaignTimePeriods(targetingManager.getAllCampaignTimePeriodsForCampaign(localEntity));
            localEntity.getTimePeriods().clear();

            localEntity = targetingManager.addTimePeriodsToCampaign(localEntity, set);
        }

        return localEntity;
    }

    private void saveTimePeriod(Campaign entity, CampaignTimePeriodDto ct, CampaignTimePeriod timePeriod) {
        timePeriod.setCampaign(entity);

        if (timePeriod.getStartDate() != null) {
            if (ct.getStartTimeOffset() != null) {
                timePeriod.setStartDate(getTimezoneDate(timePeriod.getStartDate(), ct.getStartTimeOffset(), entity.getAdvertiser()
                        .getCompany().getDefaultTimeZone()));
            } else {
                // default is at 00:00
                timePeriod
                        .setStartDate(getTimezoneDate(timePeriod.getStartDate(), entity.getAdvertiser().getCompany().getDefaultTimeZone()));
            }
        }

        if (timePeriod.getEndDate() != null) {
            if (ct.getEndTimeOffset() != null) {
                timePeriod.setEndDate(getTimezoneDate(timePeriod.getEndDate(), ct.getEndTimeOffset(), entity.getAdvertiser().getCompany()
                        .getDefaultTimeZone()));
            } else {
                // defaut is at 23:59 which is 1439 minutes
                timePeriod.setEndDate(getTimezoneDate(timePeriod.getEndDate(), MINUTES_IN_A_DAY - 1, entity.getAdvertiser().getCompany()
                        .getDefaultTimeZone()));
            }
        }
    }

    private Campaign saveTime(Campaign entity, CampaignDto dto) {
        if (!CollectionUtils.isEmpty(dto.getSegments())) {
            SegmentDto segmentDto = dto.getSegments().get(0);
            Segment segment = targetingManager.getSegmentById(segmentDto.getId());

            segment.setDaysOfWeekAsArray(ArrayUtils.toPrimitive(dto.getScheduleDto().getDaysOfWeek()));
            segment.setHoursOfDayAsArray(ArrayUtils.toPrimitive(dto.getScheduleDto().getHoursOfDay()));
            segment.setHoursOfDayWeekendAsArray(ArrayUtils.toPrimitive(dto.getScheduleDto().getHoursOfDayWeekend()));

            targetingManager.update(segment);

        }

        return entity;
    }

    private void loadChannels(Segment updated, SegmentDto dto) {

        dto.setChannelEnabled(updated.isChannelEnabled());

        Collection<ChannelPartialDto> partialChannelsDtoList = getDtoList(ChannelPartialDto.class, updated.getChannels());
        Collection<ChannelDto> finalChannelsDtoList = getDtoList(ChannelDto.class, partialChannelsDtoList);

        dto.getChannels().clear();
        dto.getChannels().addAll(finalChannelsDtoList);
    }

    private void saveCategories(Segment segment, SegmentDto dto, boolean includeUncathegorized) {

        if (!CollectionUtils.isEmpty(dto.getIncludedCategories())) {
            segment.getIncludedCategories().clear();
            Iterator<CategoryDto> it = dto.getIncludedCategories().iterator();
            while (it.hasNext()) {
                CategoryDto catDto = it.next();
                Category temp = commonManager.getCategoryById(catDto.getId());
                segment.getIncludedCategories().add(temp);
            }
            if (includeUncathegorized) {
                Category temp = commonManager.getCategoryByIabId("IAB24");
                if (temp != null) {
                    segment.getIncludedCategories().add(temp);
                }
            }
        } else {
            segment.getIncludedCategories().clear();
        }

        Segment updated = targetingManager.update(segment);
        loadCategories(updated, dto);

    }

    private Campaign savePrivateMarketPlace(Campaign campaign, CampaignDto dto) {
        Publisher publisher = publisherManager.getPublisherById(dto.getPrivateMarketPlaceDeal().getPublisher().getId());
        PrivateMarketPlaceDeal pmd = publisherManager.getPrivateMarketPlaceDealByPublisherAndDealId(publisher, dto.getPrivateMarketPlaceDeal()
                .getDealId());
        if (pmd == null) {
            pmd = publisherManager.newPrivateMarketPlaceDeal(publisher, dto.getPrivateMarketPlaceDeal().getDealId(), null, null);
        }
        campaign.setPrivateMarketPlaceDeal(pmd);
        return campaign;
    }

    private void loadCategories(Segment updated, SegmentDto dto) {

        Collection<CategoryPartialDto> partialCategoriesDtoList = getDtoList(CategoryPartialDto.class, updated.getIncludedCategories());
        Collection<CategoryDto> finalCategoriesDtoList = getDtoList(CategoryDto.class, partialCategoriesDtoList);

        dto.getIncludedCategories().clear();
        dto.getIncludedCategories().addAll(finalCategoriesDtoList);
    }

    private void saveBrowsers(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getBrowsers())) {
            segment.getBrowsers().clear();
            Iterator<BrowserDto> it = dto.getBrowsers().iterator();
            while (it.hasNext()) {
                BrowserDto brDto = it.next();
                Browser br = deviceManager.getBrowserById(brDto.getId());
                if (!segment.getBrowsers().contains(br)) {
                    segment.getBrowsers().add(br);
                }
            }
        } else {
            segment.getBrowsers().clear();
        }
        Segment updated = targetingManager.update(segment);
        loadBrowsers(updated, dto);
    }

    private void loadBrowsers(Segment segment, SegmentDto dto) {
        dto.getBrowsers().clear();
        Collection<BrowserDto> browsers = getDtoList(BrowserDto.class, segment.getBrowsers());
        dto.getBrowsers().addAll(browsers);
    }

    private void saveConnectionType(Segment segment, SegmentDto dto) {
        if (!StringUtils.isEmpty(dto.getConnectionType())) {

            String connecionType = dto.getConnectionType();
            if ("OPERATOR".equals(connecionType)) {
                segment.setConnectionType(ConnectionType.OPERATOR);
            } else if ("WIFI".equals(connecionType)) {
                segment.setConnectionType(ConnectionType.WIFI);
            } else if ("BOTH".equals(connecionType)) {
                segment.setConnectionType(ConnectionType.BOTH);
            }
        } else {
            segment.setConnectionType(ConnectionType.BOTH);
        }

        Segment updated = targetingManager.update(segment);
        loadConnectionType(updated, dto);

    }

    private void loadConnectionType(Segment updated, SegmentDto dto) {
        if (updated.getConnectionType().equals(ConnectionType.OPERATOR)) {
            dto.setConnectionType("OPERATOR");
        } else if (updated.getConnectionType().equals(ConnectionType.WIFI)) {
            dto.setConnectionType("WIFI");
        } else if (updated.getConnectionType().equals(ConnectionType.BOTH)) {
            dto.setConnectionType("BOTH");
        }
    }

    private void saveCountries(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getCountries())) {
            segment.getCountries().clear();
            Iterator<CountryDto> itec = dto.getCountries().iterator();
            while (itec.hasNext()) {
                CountryDto cdto = itec.next();
                Country c = commonManager.getCountryById(cdto.getId());
                segment.getCountries().add(c);
            }
            // country whitelist is saved in the dto property
        } else {
            // no country selected then clear
            segment.getCountries().clear();
        }
        segment.setCountryListIsWhitelist(dto.getCountryListIsWhitelist());
        Segment updated = targetingManager.update(segment);

        loadCountries(updated, dto);

    }

    private void loadCountries(Segment updated, SegmentDto dto) {
        dto.setCountryListIsWhitelist(updated.getCountryListIsWhitelist());
        dto.getCountries().clear();

        Collection<CountryDto> countryListDto = getDtoList(CountryDto.class, updated.getCountries());
        dto.getCountries().addAll(countryListDto);
    }

    private void saveGeotargets(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getGeotargets())) {
            segment.getGeotargets().clear();
            Iterator<GeotargetDto> geoSet = dto.getGeotargets().iterator();
            while (geoSet.hasNext()) {
                GeotargetDto geoTargetDto = geoSet.next();
                Geotarget geoTarget = targetingManager.getGeotargetById(geoTargetDto.getId());
                segment.getGeotargets().add(geoTarget);
            }
        } else {
            segment.getGeotargets().clear();
        }
        GeotargetType type = null;
        if (dto.getGeotargetType() != null) {
            type = targetingManager.getGeotargetTypeById(dto.getGeotargetType().getId());
        }
        segment.setGeotargetType(type);
        Segment udpated = targetingManager.update(segment);

        loadGeoTargets(udpated, dto);

    }

    private void loadGeoTargets(Segment updated, SegmentDto dto) {
        Collection<GeotargetPartialDto> partialGeotargetList = getDtoList(GeotargetPartialDto.class, updated.getGeotargets());
        Collection<GeotargetDto> geotargetList = getDtoList(GeotargetDto.class, partialGeotargetList);
        dto.getGeotargets().clear();

        dto.getGeotargets().addAll(geotargetList);
    }

    private void saveLocationTargets(Segment segment, SegmentDto dto, boolean saveLocationTarget) {
        if (saveLocationTarget || (segment.getGeotargetType()!=null && !GeotargetType.COORDINATES.equals(segment.getGeotargetType().getName()))){
            if (!CollectionUtils.isEmpty(dto.getLocationTargets())) {
                updateLocationTargets(segment, dto);
            } else {
                segment.getLocationTargets().clear();
            }
        }
        segment.setExplicitGPSEnabled(dto.isExplicitGPSEnabled());
        Segment udpated = targetingManager.update(segment);

        loadGeoTargets(udpated, dto);

    }

    private void updateLocationTargets(Segment segment, SegmentDto dto) {
        Set<LocationTarget> oldLocationTargets = new HashSet<LocationTarget>(segment.getLocationTargets());
        Set<LocationTarget> newLocationTargets = new HashSet<LocationTarget>();

        List<Long> ids = new ArrayList<>();
        Iterator<LocationTargetDto> locSet = dto.getLocationTargets().iterator();
        int cnt = 0;
        while (locSet.hasNext()) {

            LocationTargetDto locationTargetDto = locSet.next();
            ids.add(locationTargetDto.getId());
            cnt++;

            if ((cnt % MAX_ELEMENTS_TO_QUERY == 0) || (cnt == dto.getLocationTargets().size())) {
                List<LocationTarget> locationTargets = targetingManager.getLocationTargetByIds(ids);
                if (locationTargets != null && !locationTargets.isEmpty()) {
                    newLocationTargets.addAll(locationTargets);
                    oldLocationTargets.removeAll(locationTargets);
                }
                ids.clear();
            }
        }

        segment.setLocationTargets(newLocationTargets);
    }

    private void saveModels(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getModels())) {
            Iterator<ModelDto> it = dto.getModels().iterator();
            segment.getModels().clear();
            while (it.hasNext()) {
                ModelDto modDto = it.next();
                Model model = deviceManager.getModelById(modDto.getId());
                segment.getModels().add(model);

            }
        } else {
            segment.getModels().clear();
        }
        Segment update = targetingManager.update(segment);
        loadModels(update, dto);

    }

    private void loadModels(Segment updated, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(updated.getModels())) {
            FetchStrategy modelFetch = new FetchStrategyBuilder().addLeft(Model_.vendor).addLeft(Model_.platforms).build();
            // go for models, then get also vendor
            Iterator<Model> itModel = updated.getModels().iterator();
            dto.getModels().clear();
            while (itModel.hasNext()) {
                Model mod = itModel.next();
                mod = deviceManager.getModelById(mod.getId(), modelFetch);
                ModelDto modDto = getDtoObject(ModelDto.class, mod);
                dto.getModels().add(modDto);
            }
        }

    }

    private void saveExcludedModels(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getExcludedModels())) {
            Iterator<ModelDto> it = dto.getExcludedModels().iterator();
            segment.getExcludedModels().clear();
            while (it.hasNext()) {
                ModelDto modDto = it.next();
                Model model = deviceManager.getModelById(modDto.getId());
                segment.getExcludedModels().add(model);

            }
        } else {
            segment.getExcludedModels().clear();
        }
        Segment udpated = targetingManager.update(segment);
        loadExcludedModels(udpated, dto);

    }

    private void loadExcludedModels(Segment updated, SegmentDto dto) {
        Set<Model> setOfModels = updated.getExcludedModels();
        if (!CollectionUtils.isEmpty(setOfModels)) {
            FetchStrategy modelsFs = new FetchStrategyBuilder().addLeft(Model_.platforms).addLeft(Model_.vendor).build();

            Iterator<Model> modIt = setOfModels.iterator();
            dto.getExcludedModels().clear();
            while (modIt.hasNext()) {
                Model mod = modIt.next();
                Model modReloaded = campaignManager.getObjectById(Model.class, mod.getId(), modelsFs);
                ModelDto modDto = getDtoObject(ModelDto.class, modReloaded);
                dto.getExcludedModels().add(modDto);
            }
        }
    }

    private void savePlatforms(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getPlatforms())) {
            Iterator<PlatformDto> plat = dto.getPlatforms().iterator();
            segment.getPlatforms().clear();
            while (plat.hasNext()) {
                PlatformDto platDto = plat.next();
                Platform pla = deviceManager.getPlatformById(platDto.getId());
                segment.getPlatforms().add(pla);
            }
        } else {
            segment.getPlatforms().clear();
        }
        Segment updated = targetingManager.update(segment);

        loadPlatforms(updated, dto);
    }

    private void loadPlatforms(Segment updated, SegmentDto dto) {
        Collection<PlatformDto> platformsList = getDtoList(PlatformDto.class, updated.getPlatforms());
        dto.getPlatforms().clear();
        dto.getPlatforms().addAll(platformsList);
    }

    private void saveDeviceGroups(Segment segment, SegmentDto dto) {
        if (!CollectionUtils.isEmpty(dto.getDeviceGroups())) {
            segment.getDeviceGroups().clear();
            Iterator<DeviceGroupDto> iter = dto.getDeviceGroups().iterator();
            while (iter.hasNext()) {
                DeviceGroupDto dgdto = iter.next();
                segment.getDeviceGroups().add(deviceManager.getDeviceGroupById(dgdto.getId()));
            }
        } else {
            segment.getDeviceGroups().clear();
        }
        Segment updated = targetingManager.update(segment);
        loadDeviceGroups(updated, dto);
    }

    private void loadDeviceGroups(Segment updated, SegmentDto dto) {
        Collection<DeviceGroupDto> deviceGroups = getDtoList(DeviceGroupDto.class, updated.getDeviceGroups());
        dto.getDeviceGroups().clear();
        dto.getDeviceGroups().addAll(deviceGroups);
    }

    private void saveOperators(Segment segment, SegmentDto dto) {
        if(!CollectionUtils.isEmpty(dto.getMobileOperators()) || !CollectionUtils.isEmpty(dto.getIspOperators())){
            segment.getOperators().clear();
            if (!CollectionUtils.isEmpty(dto.getMobileOperators())) {
                Iterator<OperatorAutocompleteDto> plat = dto.getMobileOperators().iterator();
                while (plat.hasNext()) {
                    OperatorAutocompleteDto platDto = plat.next();
                    Operator pla = deviceManager.getOperatorById(platDto.getId());
                    segment.getOperators().add(pla);
                }
            }
            if (!CollectionUtils.isEmpty(dto.getIspOperators())) {
                Iterator<OperatorAutocompleteDto> plat = dto.getIspOperators().iterator();
                while (plat.hasNext()) {
                    OperatorAutocompleteDto platDto = plat.next();
                    Operator pla = deviceManager.getOperatorById(platDto.getId());
                    segment.getOperators().add(pla);
                }
            }
        }
        else {
            segment.getOperators().clear();
        }

        segment.setMobileOperatorListIsWhitelist(dto.getMobileOperatorListIsWhitelist());
        segment.setIspOperatorListIsWhitelist(dto.getIspOperatorListIsWhitelist());

        Segment updated = targetingManager.update(segment);

        loadOperators(updated, dto);

    }

    private void loadOperators(Segment updated, SegmentDto dto) {
        List<Operator> mobileOperators = new ArrayList<Operator>();
        List<Operator>ispOperators = new ArrayList<Operator>();
        if(!CollectionUtils.isEmpty(updated.getOperators())){
            for(Operator operator : updated.getOperators()){
                if(operator.isMobileOperator()){
                    mobileOperators.add(operator);
                }
                else{
                    ispOperators.add(operator);
                }
            }
        }
        Collection<OperatorAutocompleteDto> mobileOperatorList = getDtoList(OperatorAutocompleteDto.class, mobileOperators);
        Collection<OperatorAutocompleteDto> ispOperatorList = getDtoList(OperatorAutocompleteDto.class, ispOperators);
        
        dto.getMobileOperators().clear();
        dto.getIspOperators().clear();
        dto.setMobileOperatorListIsWhitelist(updated.getMobileOperatorListIsWhitelist());
        dto.setIspOperatorListIsWhitelist(updated.getIspOperatorListIsWhitelist());
        dto.getMobileOperators().addAll(mobileOperatorList);
        dto.getIspOperators().addAll(ispOperatorList);
    }

    private void saveInventorySources(Segment segment, CampaignDto dto) {
        segment.setIncludeAdfonicNetwork(dto.getCurrentSegment().isIncludeAdfonicNetwork());
        // rtb and non-rtbdetails
        segment.getTargettedPublishers().clear();

        if (!CollectionUtils.isEmpty(dto.getNonRtb())) {
            List<TargetPublisherDto> nrtbList = dto.getNonRtb();
            for (TargetPublisherDto list : nrtbList) {
                Publisher pub = publisherManager.getPublisherById(list.getPublisher().getId());
                segment.getTargettedPublishers().add(pub);
            }
        }

        if (!CollectionUtils.isEmpty(dto.getRtb())) {
            List<TargetPublisherDto> rtbList = dto.getRtb();
            for (TargetPublisherDto list : rtbList) {
                Publisher pub = publisherManager.getPublisherById(list.getPublisher().getId());
                segment.getTargettedPublishers().add(pub);
            }
        }

        Segment updated = targetingManager.update(segment);
        Set<Publisher> publishersDto = updated.getTargettedPublishers();
        Collection<PublisherDto> publisherDtoList = getDtoList(PublisherDto.class, publishersDto);
        dto.getCurrentSegment().getTargettedPublishers().clear();
        dto.getCurrentSegment().getTargettedPublishers().addAll(publisherDtoList);
    }

    private void saveDemographics(Segment segment, SegmentDto dto) {

        if (segment.getGenderMix().compareTo(dto.getGenderMix()) != 0) {
            segment.setGenderMix(dto.getGenderMix());
        }
        segment.setMaxAge(dto.getMaxAge());
        segment.setMinAge(dto.getMinAge());
        segment.setMedium(dto.getMedium());

        Segment updated = targetingManager.update(segment);

        loadDemographics(updated, dto);

    }

    private void loadDemographics(Segment segment, SegmentDto dto) {
        dto.setGenderMix(segment.getGenderMix());
        dto.setMaxAge(segment.getMaxAge());
        dto.setMinAge(segment.getMinAge());
        dto.setMedium(segment.getMedium());
    }

    private Segment loadSegment(SegmentDto dto) {
        return targetingManager.getSegmentById(dto.getId());
    }

    private Segment loadSegment(Long id) {
        return targetingManager.getSegmentById(id);
    }
    
    private void saveIpAddresses(Segment segment, SegmentDto dto) {
        segment.getIpAddresses().clear();
        segment.getIpAddresses().addAll(dto.getIpAddresses());
        segment.setIpAddressesListWhitelist(dto.isIpAddressesListWhitelist());
        
        Segment updated = targetingManager.update(segment);
        loadIpAddresses(updated, dto);
    }
    
    private void loadIpAddresses(Segment segment, SegmentDto dto) {
        dto.getIpAddresses().clear();
        dto.getIpAddresses().addAll(segment.getIpAddresses());
        dto.setIpAddressesListWhitelist(segment.isIpAddressesListWhitelist());
    }

    private Campaign saveTargeting(Campaign entity, CampaignDto dto, boolean saveLocationTarget) {
        if (!CollectionUtils.isEmpty(dto.getSegments())) {

            SegmentDto segmentDto = dto.getSegments().get(0);

            Segment segment = loadSegment(segmentDto);

            // Save first the channels
            saveIpAddresses(segment, dto.getCurrentSegment());
            
            saveBrowsers(segment, dto.getCurrentSegment());

            saveConnectionType(segment, dto.getCurrentSegment());

            saveCountries(segment, dto.getCurrentSegment());

            saveGeotargets(segment, dto.getCurrentSegment());

            saveLocationTargets(segment, dto.getCurrentSegment(), saveLocationTarget);

            saveModels(segment, dto.getCurrentSegment());

            saveExcludedModels(segment, dto.getCurrentSegment());

            savePlatforms(segment, dto.getCurrentSegment());

            saveOperators(segment, dto.getCurrentSegment());

            saveDemographics(segment, dto.getCurrentSegment());

            saveDeviceGroups(segment, dto.getCurrentSegment());

            // everymethod since here updates segment. NO need to do it again here.
            dto.getSegments().clear();
            dto.getSegments().add(segmentDto);
        }

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPremiumCampaign(CampaignDto dto) {
        Campaign campaign = campaignManager.getCampaignById(dto.getId());
        return !CollectionUtils.isEmpty(campaign.getTransparentNetworks());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBidMin(CampaignDto dto) {

        if (dto != null && dto.getId() != null && !StringUtils.isEmpty(dto.getCurrentBid().getBidType())) {

            Campaign campaign = campaignManager.getCampaignById(dto.getId());

            if (CollectionUtils.isEmpty(campaign.getTransparentNetworks())) {
                return calculateMinBidWithRateCards(dto, campaign);
            } else {
                return calculateMinBidWithNetwork(dto, campaign);
            }
        } else {
            return new BigDecimal("1");
        }
    }

    private BigDecimal calculateMinBidWithRateCards(CampaignDto dto, Campaign campaign) {
        // empty: minBid calculated with ratecards
        // Otherwise minimums are dictated by geographical targeting
        com.adfonic.domain.BidType bidType = com.adfonic.domain.BidType.valueOf(dto.getCurrentBid().getBidType());

        RateCard rateCard = publicationManager.getRateCardByBidType(bidType);

        BigDecimal minBid = rateCard.getDefaultMinimum();

        for (Country c : campaign.getSegments().get(0).getCountries()) {
            minBid = minBid.max(rateCard.getMinimumBid(c));

        }
        return minBid;
    }

    private BigDecimal calculateMinBidWithNetwork(CampaignDto dto, Campaign campaign) {
        // minBid calculated with minimun rate setup in the network.
        Iterator<TransparentNetwork> it = campaign.getTransparentNetworks().iterator();
        boolean goOn = true;
        BigDecimal result = new BigDecimal(0);
        while (it.hasNext() && goOn) {
            TransparentNetwork network = it.next();
            if (TransparentNetwork.PERFORMANCE_NETWORK_NAME.equals(network.getName())) {
                com.adfonic.domain.BidType bidType = com.adfonic.domain.BidType.valueOf(dto.getCurrentBid().getBidType());
                BigDecimal bg = network.getRateCard(bidType).getDefaultMinimum();
                result = bg;
                goOn = false;
            }
        }
        return result;
    }

    private Campaign saveBid(Campaign entity, CampaignDto dto) {
        Campaign localEntity = entity;

        // save bidtype
        if (localEntity.getCurrentBid() == null
                || !localEntity.getCurrentBid().getBidType().getName().equals(dto.getCurrentBid().getBidType())
                || localEntity.getCurrentBid().getAmount().compareTo(dto.getCurrentBid().getAmount()) != 0) {
            // only creates new one when it changes (type or amount)
            com.adfonic.domain.BidType bidType = com.adfonic.domain.BidType.valueOf(dto.getCurrentBid().getBidType());
            localEntity = biddingManager.newCampaignBid(localEntity, bidType, dto.getCurrentBid().getAmount(), dto.getCurrentBid()
                    .isMaximum());
        }

        // AgencyDiscount
        if (hasAgencyDiscountChanged(dto, localEntity)) {
            localEntity = feeManager.newCampaignAgencyDiscount(localEntity, dto.getCurrentAgencyDiscount().getDiscount());
        }

        localEntity.setOverallBudget(dto.getOverallBudget());
        localEntity.setOverallBudgetAlertEnabled(dto.getOverallBudgetAlertEnabled());
        localEntity.setOverallBudgetClicks(dto.getOverallBudgetClicks());
        localEntity.setOverallBudgetImpressions(dto.getOverallBudgetImpressions());
        localEntity.setBudgetType(dto.getBudgetType());

        if (dto.getDailyBudgetWeekend() != null) {
            localEntity.setDailyBudgetWeekday(dto.getDailyBudget());
            localEntity.setDailyBudgetWeekend(dto.getDailyBudgetWeekend());
            localEntity.setDailyBudget(null);
        } else {
            localEntity.setDailyBudget(dto.getDailyBudget());
            localEntity.setDailyBudgetClicks(dto.getDailyBudgetClicks());
            localEntity.setDailyBudgetImpressions(dto.getDailyBudgetImpressions());
            localEntity.setDailyBudgetWeekday(null);
            localEntity.setDailyBudgetWeekend(null);
        }
        // Setting target ctr or cvr if required
        CampaignTargetCTR ctr;
        if (dto.getCurrentBid().getBidType().equals(BidType.CPC.toString())) {
            if (dto.getTargetCPA() != null && dto.getTargetCPA().compareTo(new BigDecimal(0)) != 0) {
                localEntity.setTargetCPA(dto.getTargetCPA());
            } else {
                localEntity.setTargetCPA(null);
            }
        } else if (dto.getCurrentBid().getBidType().equals(BidType.CPM.toString()) && dto.getTargetCTR() != null
                && dto.getTargetCTR().getTargetCTR().compareTo(new BigDecimal(0)) != 0) {
            if (localEntity.getTargetCTR() == null) {
                localEntity.setTargetCTR(biddingManager.newCampaignTargetCTR(localEntity, dto.getTargetCTR().getTargetCTR()));
            } else {
                ctr = biddingManager.getCampaignTargetCTRById(localEntity.getTargetCTR().getId());
                ctr.setTargetCTR(dto.getTargetCTR().getTargetCTR());
                biddingManager.update(ctr);
                localEntity.setTargetCTR(ctr);
            }
            if (dto.getTargetCPA() != null) {
                localEntity.setTargetCPA(null);
            }
        } else if (dto.getTargetCTR() != null) {
            ctr = biddingManager.getCampaignTargetCTRById(dto.getTargetCTR().getId());
            biddingManager.delete(ctr);
            localEntity.setTargetCTR(null);
        }
        localEntity.setDailyBudgetAlertEnabled(dto.getDailyBudgetAlertEnabled());
        localEntity.setCapImpressions(dto.getCapImpressions());
        localEntity.setCapPeriodSeconds(dto.getCapPeriodSeconds());
        localEntity.setCapPerCampaign(dto.isCapPerCampaign());
        
        // Bidding Strategy
        saveBiddingStrategies(localEntity, dto.getBiddingStrategies(), dto.getMaxBidThreshold());
        
        // Currency & Exchange Rate [MAD-3267]
        CurrencyExchangeRate currencyExchangeRate = commonManager.getCurrencyExchangeRateById(dto.getCurrencyExchangeRate().getId());
        localEntity.setCurrencyExchangeRate(currencyExchangeRate);
        localEntity.setExchangeRate(dto.getExchangeRate());
        boolean changedByAdmin = localEntity.isExchangeRateAdminChange() || dto.isExchangeRateAdminChange();
        localEntity.setExchangeRateAdminChange(changedByAdmin);
        
		// Bid Deductions
        Set<BidDeduction> bidDeductions = new HashSet<BidDeduction>();
        BidDeduction bd;
        boolean payerIsByyd = false;
        for (BidDeductionDto bidDeductionDto : dto.getCurrentBidDeductions()) {
        	
        	// BidDeduction
        	bd = new BidDeduction();  
        	if (bidDeductionDto.getId() != null) {       		
	        	bd.setId(bidDeductionDto.getId());
        	}
        	bd.setAmount(bidDeductionDto.getAmount());
        	payerIsByyd = bidDeductionDto.getPayerIsByyd();
        	bd.setPayerIsByyd(payerIsByyd);
        	bd.setThirdPartyVendor(payerIsByyd ? commonManager.getThirdPartyVendorById(bidDeductionDto.getThirdPartyVendor().getId()) : null);
        	bd.setThirdPartyVendorFreeText(payerIsByyd ? null : bidDeductionDto.getThirdPartyVendorFreeText().trim());
        	bd.setCampaign(localEntity);
        	
			bidDeductions.add(bd);
        }
        localEntity = biddingManager.updateBidDeductions(localEntity, bidDeductions);
        
        return localEntity;
    }

    private void saveBiddingStrategies(Campaign localEntity, Set<BiddingStrategyName> biddingStrategiesDto, BigDecimal maxBidThreshold) {
        Set<BiddingStrategy> biddingStrategies = new HashSet<>();
        if (!CollectionUtils.isEmpty(biddingStrategiesDto)) {
            for(BiddingStrategyName biddingStrategyName : biddingStrategiesDto){
                biddingStrategies.add(biddingStrategyName.getBiddingStrategy());
            }
        }
        localEntity.setBiddingStrategies(biddingStrategies);
        
        // Set Average Maximum Bid Threshold
        localEntity.setMaxBidThreshold(biddingStrategiesDto.contains(BiddingStrategyName.AVERAGE_MAXIMUM_BID) ? maxBidThreshold : null);
    }

    private boolean hasAgencyDiscountChanged(CampaignDto dto, Campaign localEntity) {
        return (dto.getCurrentAgencyDiscount() != null)
                && ((localEntity.getCurrentAgencyDiscount() == null) || (localEntity.getCurrentAgencyDiscount() != null && localEntity
                        .getCurrentAgencyDiscount().getDiscount().compareTo(dto.getCurrentAgencyDiscount().getDiscount()) != 0));
    }

    private Campaign saveTracking(Campaign entity, CampaignDto dto) {
        entity.setConversionTrackingEnabled(dto.getConversionTrackingEnabled());
        entity.setInstallTrackingEnabled(dto.getInstallTrackingEnabled());

        // null in case its blank or null
        entity.setApplicationID(StringUtils.isEmpty(dto.getApplicationID()) ? null : dto.getApplicationID());

        // no Adx setting on tools2 anymore: MAD-1109
        entity.setInstallTrackingAdXEnabled(false);
        if (!CollectionUtils.isEmpty(dto.getDeviceIdentifierTypes())) {
            entity.getDeviceIdentifierTypes().clear();
            Iterator<DeviceIdentifierTypeDto> a = dto.getDeviceIdentifierTypes().iterator();
            while (a.hasNext()) {
                DeviceIdentifierTypeDto diType = a.next();
                DeviceIdentifierType di = deviceManager.getDeviceIdentifierTypeBySystemName(diType.getSystemName());
                entity.getDeviceIdentifierTypes().add(di);
            }
        } else {
            entity.getDeviceIdentifierTypes().clear();
        }
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplicationDestination(CampaignDto dto) {
        Campaign c = campaignManager.getCampaignById(dto.getId());
        return isApplicationDestination(c.getCreatives());
    }

    private boolean isApplicationDestination(List<Creative> creatives) {
        for (Creative c : creatives) {
            if (c.getDestination() == null) {
                continue;
            } else if (c.getDestination().getDestinationType().equals(DestinationType.IPHONE_APP_STORE)
                    || c.getDestination().getDestinationType().equals(DestinationType.ANDROID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LanguageDto getLanguageByName(String name) {
        Language language = commonManager.getLanguageByName(name);
        return getObjectDto(LanguageDto.class, language);
    }

    @Override
    public LanguageDto getLanguageById(Long id) {
        Language language = commonManager.getLanguageById(id);
        return getObjectDto(LanguageDto.class, language);
    }

    @Override
    public List<LanguageDto> getAllLanguages() {
        List<LanguageDto> res = new ArrayList<LanguageDto>();

        for (Language l : commonManager.getAllLanguages()) {
            res.add(getObjectDto(LanguageDto.class, l));
        }
        return res;
    }

    @Override
    public List<LanguageDto> getFirstLanguages() {
        List<LanguageDto> res = new ArrayList<LanguageDto>();

        Language l = commonManager.getLanguageByName("English");
        res.add(getObjectDto(LanguageDto.class, l));
        l = commonManager.getLanguageByName("Spanish");
        res.add(getObjectDto(LanguageDto.class, l));
        l = commonManager.getLanguageByName("German");
        res.add(getObjectDto(LanguageDto.class, l));
        l = commonManager.getLanguageByName("French");
        res.add(getObjectDto(LanguageDto.class, l));
        return res;
    }

    @Override
    public List<LanguageDto> getSecondLanguages() {
        List<LanguageDto> res = new ArrayList<LanguageDto>();

        for (Language l : commonManager.getAllLanguages()) {
            if (!"English".equals(l.getName()) && !"Spanish".equals(l.getName()) && !"German".equals(l.getName())
                    && !"French".equals(l.getName())) {
                res.add(getObjectDto(LanguageDto.class, l));
            }
        }
        return res;
    }

    @Override
    public CampaignCreativeDto save(CampaignCreativeDto dto) {
        if (dto.getId() == null || dto.getId() < 1) {
            throw new NullPointerException();
        }

        List<CreativeDto> lCreatives = new ArrayList<CreativeDto>();

        for (CreativeDto c : dto.getCreatives()) {
            lCreatives.add(cService.save(c, dto, true));
        }

        CampaignCreativeDto res = new CampaignCreativeDto();

        res.setAdvertiser(dto.getAdvertiser());
        res.setId(dto.getId());
        res.setCreatives(lCreatives);

        return res;
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDto copyCampaign(long campaignId) {

        Campaign campaign = campaignManager.getCampaignById(campaignId);

        Campaign newCampaign = campaignManager.copyCampaignWithTimePeriods(campaign);
        if (newCampaign.getCurrentBid() != null) {
            newCampaign.setStatus(Campaign.Status.NEW_REVIEW);
            newCampaign = campaignManager.update(newCampaign);
        }
        newCampaign = campaignManager.getCampaignById(newCampaign.getId());

        CampaignDto dto = new CampaignDto();
        dto = loadCampaignFullDetails(newCampaign, dto);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignCreativeDto loadCreatives(long campaignId) {
        Campaign entity = campaignManager.getCampaignById(campaignId);

        CampaignCreativeDto campaignCreativeDto = null;
        List<CreativeDto> lCreatives = new ArrayList<CreativeDto>();
        
        if (entity != null){
            List<Creative> creatives = removeDuplicated(entity.getCreatives());
            entity.getCreatives().clear();
            entity.getCreatives().addAll(creatives);
    
            for (Creative c : entity.getCreatives()) {
                CreativeDto creative = cService.getCreativeById(c.getId());
                creative.setHiddenClass("none");
                Destination destination = c.getDestination();
                if(destination != null && CollectionUtils.isEmpty(destination.getBeaconUrls())){
                    creative.getDestination().getBeaconUrls().add(new BeaconUrlDto());
                }
                lCreatives.add(creative);
            }
            
            // Mapper mapper = new DozerBeanMapper();
            campaignCreativeDto = mapper.map(entity, CampaignCreativeDto.class);
        }else{
            campaignCreativeDto = new CampaignCreativeDto();
        }
        
        campaignCreativeDto.getCreatives().clear();
        campaignCreativeDto.setCreatives(lCreatives);

        return campaignCreativeDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CreativeFormatDto> getAllCreativesForCampaignIds(Collection<Long> campaignIds) {
        if (campaignIds == null || campaignIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            return getList(CreativeFormatDto.class, creativeManager.getAllCreativesForCampaignIds(campaignIds));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PublicationListInfoDto loadPublicationList(long campaignId) {
        Campaign entity = campaignManager.getCampaignById(campaignId);
        PublicationListInfoDto publicationListInfoDto = null;
        if (entity.getPublicationList() != null) {
            publicationListInfoDto = mapper.map(entity.getPublicationList(), PublicationListInfoDto.class);
        }

        return publicationListInfoDto;
    }

    private List<Creative> removeDuplicated(List<Creative> creatives) {
        List<Long> ids = new ArrayList<Long>();
        List<Creative> result = new ArrayList<Creative>();
        for (Creative c : creatives) {
            if (!ids.contains(c.getId())) {
                ids.add(c.getId());
                result.add(c);
            }
        }

        return result;
    }

    @Override
    public CampaignCreativeDto copyCreatives(CampaignCreativeDto dto, long newCampaignId) {
        CampaignCreativeDto campaign = new CampaignCreativeDto();
        campaign.setId(newCampaignId);

        for (CreativeDto c : dto.getCreatives()) {
            campaign.getCreatives().add(cService.copyCreative(c, dto));
        }

        return campaign;
    }

    private void getTargetPublisherDetails(CampaignDto dto) {
        if (!CollectionUtils.isEmpty(dto.getCurrentSegment().getTargettedPublishers())) {
            Iterator<PublisherDto> it = dto.getCurrentSegment().getTargettedPublishers().iterator();
            while (it.hasNext()) {
                PublisherDto pubdto = it.next();
                getTargetPublisherInfo(dto, pubdto);
            }
        }
    }

    private void getTargetPublisherInfo(CampaignDto dto, PublisherDto pubdto) {
        TargetPublisherDto tPubDto = targetPublisherService.getTargetPublisherByPublisherId(pubdto.getId());
        if (tPubDto != null) {
            if (tPubDto.isRtb() && !tPubDto.isHidden()) {
                dto.getRtb().add(tPubDto);
            } else {
                if (!tPubDto.isHidden()) {
                    dto.getNonRtb().add(tPubDto);
                }
            }
        }
    }

    private void cleanInventoryTargeting(Segment segment, Campaign campaign) {
        // Exchange inventory
        segment.setIncludeAdfonicNetwork(false);
        segment.getTargettedPublishers().clear();
        // Publication list
        campaign.setPublicationList(null);
        // IAB Category
        segment.getIncludedCategories().clear();
        // Private MarketPlace
        campaign.setPrivateMarketPlaceDeal(null);
    }

    // get the date at the given offset
    private Date getTimezoneDate(Date date, Integer offset, TimeZone timezone) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (offset != null) {
            cal.add(Calendar.MILLISECOND, offset * SECONDS_IN_A_MINUTE * MILLISECONDS_IN_A_SECOND);// offsset is in minutes
        }
        cal.add(Calendar.MILLISECOND, -1 * timezone.getOffset(new Date().getTime()));
        return cal.getTime();
    }

    private Date getTimezoneDate(Date date, TimeZone timezone) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, -1 * timezone.getOffset(new Date().getTime()));
        return cal.getTime();
    }

    @Override
    @Transactional(readOnly = false)
    public void changeCampaignStatus(List<Long> campaignIds, CampaignStatus campStatus) {
        if (!CollectionUtils.isEmpty(campaignIds)) {
            for (Long id : campaignIds) {
                Campaign camp = campaignManager.getCampaignById(id);

                if (camp.transitionStatus(campStatus.getStatus())) {
                    campaignManager.update(camp);
                }
            }
        }

    }

    @Override
    @Transactional(readOnly = true)
    public SegmentSafetyLevel getSafetyLevelForCampaign(CampaignDto campaignDto) {
        if (campaignDto == null || campaignDto.getId() == null || campaignDto.getId().equals(0L)) {
            return SegmentSafetyLevel.OFF;
        }
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        return campaign.getSegments().get(0).getSafetyLevel();
    }

    @Override
    @Transactional(readOnly = false)
    public void newCreativeHistory(CreativeDto creativeDto, String comment, AdfonicUser adfonicUser) {
        creativeManager.newCreativeHistory(creativeManager.getCreativeById(creativeDto.getId()), comment, adfonicUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isConversionTrackingUsed(Collection<Long> campaignIds, Long advertiserId) {
        List<Campaign> allCampaigns = getAllOrSelectedCampaignsForAdvertiser(campaignIds, advertiserId);
        for (Campaign campaign : allCampaigns) {
            if (campaign != null
                    && (campaign.isInstallTrackingEnabled() || campaign.isConversionTrackingEnabled() || campaign
                            .isInstallTrackingAdXEnabled())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGeotargetingUsed(Collection<Long> campaignIds, Long advertiserId) {
        List<Campaign> allCampaigns = getAllOrSelectedCampaignsForAdvertiser(campaignIds, advertiserId);
        for (Campaign campaign : allCampaigns) {
            if (!campaign.getSegments().get(0).getGeotargets().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private List<Campaign> getAllOrSelectedCampaignsForAdvertiser(Collection<Long> campaignIds, Long advertiserId) {
        List<Campaign> allCampaigns = null;
        if (CollectionUtils.isEmpty(campaignIds)) {
            allCampaigns = campaignManager.getAllCampaignsForAdvertiser(advertiserManager.getAdvertiserById(advertiserId));
        } else {
            CampaignFilter campaignFilter = new CampaignFilter();
            campaignFilter.setCampaignIds(campaignIds);
            allCampaigns = campaignManager.getAllCampaigns(campaignFilter);
        }
        return allCampaigns;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignTriggerDto> getCampaignTriggers(CampaignDto campaignDto) {
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        return getDtoList(CampaignTriggerDto.class, integrationsManager.getCampaignTriggers(new CampaignTriggerFilter().setCampaign(campaign).setDeleted(false)));
    }

    private void saveCampaignTriggers(Campaign campaign, Collection<CampaignTriggerDto> newCampaignTriggerDtos) {
        Set<CampaignTrigger> newCampaignTriggers = new HashSet<CampaignTrigger>();
        if (!CollectionUtils.isEmpty(newCampaignTriggerDtos)) {
            for (CampaignTriggerDto campaignTriggerDto : newCampaignTriggerDtos) {
                CampaignTrigger ct = new CampaignTrigger();
                ct.setPluginVendor(integrationsManager.getPluginVendorById(campaignTriggerDto.getPluginVendor().getId()));
                ct.setPluginType(PluginType.valueOf(campaignTriggerDto.getPluginType().name()));
                newCampaignTriggers.add(ct);
            }
        }
        integrationsManager.updateCampaignTriggers(campaign, newCampaignTriggers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignAudienceDto> getCampaignAudiences(CampaignDto campaignDto) {
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        CampaignAudienceFilter filter = new CampaignAudienceFilter();
        filter.setCampaign(campaign);
        List<CampaignAudience> campaignAudiences = audienceManager.getCampaignAudiences(filter);
        return getDtoList(CampaignAudienceDto.class, campaignAudiences);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasAllCreativeRejected(Long campaignId){
        CampaignCreativeDto campaignCreativesDto = loadCreatives(campaignId);
        List<CreativeDto> creatives = campaignCreativesDto.getCreatives();
        boolean isAnyRejected = false;
        if (creatives!=null && creatives.size()>0){
            isAnyRejected = true;
            for(CreativeDto creative : creatives){
                isAnyRejected &= (creative.getStatus() == Creative.Status.REJECTED);
            }
        }
        return isAnyRejected;
    }
}
