package com.byyd.middleware.auditlog.listener;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Browser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.InventoryTargetingType;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative.Status;
import com.adfonic.domain.Format;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.Language;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Model;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Platform;
import com.adfonic.domain.PrivateMarketPlaceDeal;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.publication.filter.PublicationListFilter;
import com.byyd.middleware.publication.service.PublicationManager;
import com.ibm.icu.text.SimpleDateFormat;

@Service("auditLogTestService")
public class AuditLogTestService {

    private static final String CAMPAIGN_NAME = "TEST_AUDIT_LOG_CAMPAIGN";
    private static final String CATEGORY_NAME_AUTOMOTIVE = "Automotive";
    private static final String DEFAULT_LANGUAGE_NAME_BRETON = "Breton";

    @Autowired
    PublisherManager publisherManager = null;
    @Autowired
    PublicationManager publicationManager = null;
    @Autowired
    AdvertiserManager advertiserManager = null;
    @Autowired
    UserManager userManager = null;
    @Autowired
    CampaignManager campaignManager = null;
    @Autowired
    TargetingManager targetingManager = null;
    @Autowired
    FeeManager feeManager = null;
    @Autowired
    BiddingManager biddingManager = null;
    @Autowired
    CommonManager commonManager = null;
    @Autowired
    DeviceManager devicesManager = null;

    private CreativeManager creativeManager;
    
    public AuditLogTestService(){
        
    }
    
    @Transactional(readOnly=true)
    public AdfonicUser getAdfonicUser(){
        return userManager.getAdfonicUserById(3L);
    }

    @Transactional(readOnly=false)
    public Campaign getCampaignInstance(Advertiser advertiser) {
        Category category = commonManager.getCategoryByName(CATEGORY_NAME_AUTOMOTIVE);
        assertNotNull(category);
        Language defaultLanguage = commonManager.getLanguageByName(DEFAULT_LANGUAGE_NAME_BRETON);
        assertNotNull(defaultLanguage);
        Campaign campaign = campaignManager.newCampaign(CAMPAIGN_NAME, advertiser, category, defaultLanguage, true);
        assertNotNull(campaign);
        campaign.setStatus(Campaign.Status.STOPPED);

        // CAMPAIGN entity audited properties
        // startDate
        campaign.setStartDate(new Date());
        // endDate
        campaign.setEndDate(new Date());
        // inventoryTargetingType
        campaign.setInventoryTargetingType(InventoryTargetingType.RUN_OF_NETWORK);
        // publicationList.publications.name
        PublicationListFilter filter = new PublicationListFilter();
        filter.setAdvertiser(advertiser);
        List<PublicationList> publicationLists = publicationManager.getAllPublicationLists(filter);
        assertNotNull(publicationLists);
        assertTrue(publicationLists.size() > 0);
        campaign.setPublicationList(publicationLists.get(0));
        // privateMarketPlaceDeal.publisher.id
        // privateMarketPlaceDeal.dealId
        PrivateMarketPlaceDeal privateMarketPlaceDeal = publisherManager.getPrivateMarketPlaceDealById(1L);
        campaign.setPrivateMarketPlaceDeal(privateMarketPlaceDeal);
        // overallBudget
        campaign.setOverallBudget(new BigDecimal(300));
        // dailyBudget
        campaign.setDailyBudget(new BigDecimal(40));
        // evenDistributionOverallBudget
        campaign.setEvenDistributionOverallBudget(true);
        // evenDistributionDailyBudget
        campaign.setEvenDistributionDailyBudget(true);
        // capImpressions
        campaign.setCapImpressions(500);
        // capPeriodSeconds
        campaign.setCapPeriodSeconds(600);
        // currentBid.amount
        // currentBid.bidType
        campaign = biddingManager.newCampaignBid(campaign, BidType.CPC, new BigDecimal(200), false);
        // currentTradingDeskMargin.tradingDeskMargin and currentRichMediaAdServingFee.richMediaAdServingFee
        campaign = feeManager.saveCampaignRichMediaAdServingFee(campaign.getId(), new BigDecimal(900));
        campaign = feeManager.saveCampaignTradingDeskMargin(campaign.getId(), new BigDecimal(0));

        campaign = campaignManager.update(campaign);

        return campaign;
    }

    @Transactional(readOnly=false)
    public Segment getSegmentInstance(Advertiser advertiser) {

        Segment segment = targetingManager.newSegment(advertiser, null);

        // hoursOfDay
        segment.setHoursOfDay(15);

        // hoursOfDayWeekend
        segment.setHoursOfDayWeekend(20);

        // daysOfWeek
        segment.setDaysOfWeek(8);

        // geotargetType.name
        GeotargetType geotargetType = targetingManager.getGeotargetTypeByNameAndType("STATE", "STATE");
        assertNotNull(geotargetType);
        segment.setGeotargetType(geotargetType);

        // geotargets*name
        List<Country> countries = commonManager.getAllCountries();
        assertNotNull(countries);
        assertTrue(countries.size() >= 2);
        Country country1 = countries.get(0);
        Country country2 = countries.get(1);
        String dateStr = new SimpleDateFormat().format(new Date()) + Math.random();

        Geotarget geotarget1 = targetingManager.newGeotarget("GEOTARGET 1" + dateStr, country1, geotargetType, 1d, 1d);
        assertNotNull(geotarget1);
        Geotarget geotarget2 = targetingManager.newGeotarget("GEOTARGET 2" + dateStr, country2, geotargetType, 2d, 2d);
        assertNotNull(geotarget2);
        Set<Geotarget> geotargets = segment.getGeotargets();
        geotargets.add(geotarget1);
        geotargets.add(geotarget2);
        // locationTargets*name
        // locationTargets*latitude
        // locationTargets*longitude
        // locationTargets*radiusMiles
        LocationTarget locationTarget1 = targetingManager.newLocationTarget(advertiser, "LOCATION TARGET 1", new BigDecimal(10), new BigDecimal(10), new BigDecimal(10));
        assertNotNull(locationTarget1);
        LocationTarget locationTarget2 = targetingManager.newLocationTarget(advertiser, "LOCATION TARGET 2", new BigDecimal(20), new BigDecimal(20), new BigDecimal(20));
        assertNotNull(locationTarget2);
        Set<LocationTarget> locationTargets = segment.getLocationTargets();
        locationTargets.add(locationTarget1);
        locationTargets.add(locationTarget2);
        // explicitGPSEnabled
        segment.setExplicitGPSEnabled(true);

        // platforms.name
        List<Platform> platforms = devicesManager.getAllPlatforms();
        assertNotNull(platforms);
        Set<Platform> segmentPlatforms = segment.getPlatforms();
        segmentPlatforms.addAll(platforms);

        // models*deviceGroup.systemName
        // models*name
        List<Model> models = devicesManager.getModelsByName("One Touch C", LikeSpec.STARTS_WITH, false);
        assertNotNull(models);
        Set<Model> segmentModels = segment.getModels();
        segmentModels.addAll(models);

        // excludedModels*name
        List<Model> modelsToExclude = devicesManager.getModelsByName("Vandroid", LikeSpec.STARTS_WITH, false);
        assertNotNull(modelsToExclude);
        Set<Model> segmentModelsToExclude = segment.getExcludedModels();
        segmentModelsToExclude.addAll(modelsToExclude);

        // connectionType
        segment.setConnectionType(ConnectionType.WIFI);

        // mobileOperatorListIsWhitelist
        segment.setMobileOperatorListIsWhitelist(true);
        // ispOperatorListIsWhitelist
        segment.setIspOperatorListIsWhitelist(true);

        // mobileOperators
        Operator opeartor1 = devicesManager.getOperatorByName("Mobiland");
        assertNotNull(opeartor1);
        Operator opeartor2 = devicesManager.getOperatorByName("O2 UK");
        assertNotNull(opeartor2);
        Set<Operator> operators = segment.getOperators();
        operators.add(opeartor1);
        operators.add(opeartor2);

        // browsers*name
        Browser browser1 = devicesManager.getBrowserByName("Android OS 2.0 or above");
        assertNotNull(browser1);
        Browser browser2 = devicesManager.getBrowserByName("Android OS 2.2");
        assertNotNull(browser2);
        Set<Browser> browsers = segment.getBrowsers();
        browsers.add(browser1);
        browsers.add(browser2);

        // channels*name
        List<Channel> channels = commonManager.getAllChannels();
        assertNotNull(channels);
        Set<Channel> segmentChannel = segment.getChannels();
        segmentChannel.addAll(channels);

        // genderMix
        segment.setGenderMix(new BigDecimal("1.0"));

        // minAge
        segment.setMinAge(18);

        // maxAge
        segment.setMaxAge(95);

        // medium
        segment.setMedium(Medium.SITE);

        // includeAdfonicNetwork
        segment.setIncludeAdfonicNetwork(true);

        // targettedPublishers*id
        Publisher publisher1 = publisherManager.getPublisherByExternalId("7998ade3-2286-4057-92ff-045cb648797d");
        assertNotNull(publisher1);
        Publisher publisher2 = publisherManager.getPublisherByExternalId("beb169ca-3147-431c-9747-1e8b813b05e7");
        assertNotNull(publisher2);
        Publisher publisher3 = publisherManager.getPublisherByExternalId("b44c90e5-c4d4-40ad-afe7-b407d8c34c1e");
        assertNotNull(publisher3);
        Set<Publisher> targettedPublishers = segment.getTargettedPublishers();
        targettedPublishers.add(publisher1);
        targettedPublishers.add(publisher2);
        targettedPublishers.add(publisher3);

        segment = targetingManager.update(segment);

        return segment;
    }

    @Transactional(readOnly=false)
    public  Advertiser getAdvertiserInstance() {
        Advertiser advertiser = advertiserManager.getAdvertiserById(215L);
        assertNotNull(advertiser);

        // dailyBudget
        advertiser.setDailyBudget(new BigDecimal(400));

        advertiser = advertiserManager.update(advertiser);

        return advertiser;
    }
    
    @Transactional(readOnly=false)
    public  Creative getCreativeInstance(Campaign campaign, Segment segment) {
        Format format = commonManager.getFormatByName("Text Link");
        Creative creative = creativeManager.newCreative(campaign, segment, format, "Creative 1");

        // status
        creative.setStatus(Status.ACTIVE);

        creative = creativeManager.update(creative);

        return creative;
    }

    /**
     * Delete newly created campaign and the following entities:
     *  - creatives
     *  - segment (with geo targets and geo location)
     *  - campaign daily spend / campaign overall spend table collection records
     *  - campaign (with campaign bid, trading desk margin and richmedia adserving fee)
     */
    @Transactional(readOnly=false)
    public void deleteNewlyCreatedTestEntities(Campaign campaign, 
                                                  Segment segment,
                                                  List<Creative> creatives) {
        // delete creatives
        if (creatives!=null){
            creativeManager.deleteCreatives(creatives);
        }

        if (segment!=null){
            if (segment.getLocationTargets()!=null){
                Set<LocationTarget> copyLoc = new HashSet<LocationTarget>(segment.getLocationTargets());
                segment.getLocationTargets().clear();
                targetingManager.update(segment);
                for (LocationTarget loc : copyLoc) {
                    // delete geo location targets
                    targetingManager.delete(loc);
                }
            }
            
            if (segment.getGeotargets()!=null){
                for (Geotarget geo : segment.getGeotargets()) {
                    // delete geo targets
                    targetingManager.delete(geo);
                }
                segment.getGeotargets().clear();
            }
            
            // delete segments
            targetingManager.delete(segment);
        }

        if (campaign!=null){
            // delete campaign
            // delete campaign bid
            // delete campaign trading desk margin
            // delete campaign rich media adserving fee
            campaignManager.delete(campaign);
        }
    }
}
