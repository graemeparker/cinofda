package com.adfonic.beans;

import static com.adfonic.beans.CategoryQueryBean.CATEGORY_SEPARATOR;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

import com.adfonic.beans.approval.creative.dto.PublisherAuditedInfoDto;
import com.adfonic.beans.approval.publication.dto.PublicationDto;
import com.adfonic.beans.datamodel.LazyCreativeDataModel;
import com.adfonic.beans.datamodel.LazyPublicationListPublicationDataModel;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.BeaconUrl;
import com.adfonic.domain.BidDeduction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Browser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Campaign.Status;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Company;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.CreativeHistory;
import com.adfonic.domain.CreativeHistory_;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Operator;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.User;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.presentation.learnings.service.LearningAlgorithmService;
import com.adfonic.presentation.validator.ValidationResult;
import com.adfonic.presentation.validator.ValidationUtils;
import com.adfonic.util.AdXUtils;
import com.adfonic.util.CreativeEmailUtils;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;
import com.byyd.middleware.campaign.filter.CampaignFilter;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;
import com.byyd.middleware.utils.TransactionalRunner;

@SessionScoped
@ManagedBean
public class CreativeAdminBean extends BaseBean {

    private static final transient Logger LOG = Logger.getLogger(CreativeAdminBean.class.getName());
    private static final String MOBCLIX_EMAIL = "admin@mobclix.com";
    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+\\.[a-z0-9_]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final List<MediaType> NON_EDITABLE_TEMPLATES_MEDIATYPES = new ArrayList<MediaType>(Arrays.asList(new MediaType[]{MediaType.VAST_XML_2_0}));
    
    private static final Set<Status> CAMPAIGN_NOT_LAUNCHED_STATUSES = new HashSet<>(Arrays.asList(Status.NEW, Status.NEW_REVIEW, Status.DELETED, Status.PENDING, Status.PENDING_PAUSED));
    
    private static AuditedPublishersBean auditedPublishersBean = AdfonicBeanDispatcher.getBean(AuditedPublishersBean.class);
    
    @ManagedProperty(value="#{optimizationDefaultMaxRemoval}")
    private BigDecimal optimizationDefaultMaxRemoval;

    @ManagedProperty(value="#{optimizationDefaultMultiplier}")
    private BigDecimal optimizationDefaultMultiplier;

    @ManagedProperty(value="#{creativeEmailUtils}",name="creativeEmailUtils")
    private CreativeEmailUtils creativeEmailUtils;
    
    
    private LearningAlgorithmService learningAlgorithmService = AdfonicBeanDispatcher.getBean(LearningAlgorithmService.class);
    private CampaignService campaignService = AdfonicBeanDispatcher.getBean(CampaignService.class);

    private Creative creative;
    private String externalID;
    private List<AssetBean> assets = new ArrayList<AssetBean>();
    private BigDecimal newBidAmount;
    private boolean onMobclix = false;
    private List<Browser> browsers = new ArrayList<Browser>();
    private List<Channel> channels = new ArrayList<Channel>();
    private boolean removeIPTargeting = false;
    private Date creativeEndDate;
    private ExtendedCreativeType extendedCreativeType;
    private List<ContentForm> validContentFormsForExtendedCreativeType;
    private Map<ContentForm, String> originalTemplateValuesForContentForm;
    private boolean templatesEditable = true;

    // Added as gettable bean instance variables for the JSF
    private Campaign campaign;
    private Advertiser advertiser;
    private Company company;
    private Segment segment;
    private String destinationData;
    private boolean dataIsFinalDestination;
    private String finalDestination;
    private List<BeaconUrl> beacons;
    Boolean isPremium;
    private Map<Category,String> excludedCategoryLabelMap;
    private BidType bidType;
    private List<BidDeduction> bidDeductions;
    private Long creativesCount;
    private BigDecimal adServingCpmFee;

    /* lazy load, displayed for non-key advertisers */
    private Long campaignsCount;
    private String newCreativeName;
    private String newApplicationId;
    private String newCampaignName;

    private List<CampaignAudience> campaignAudiences;
    
    // Used to live in CampaignAdminBean
    private List<BidType> availableBidTypes;
    
    private Long maxModelsForDetailDisplay = 5L;
    private Long modelsCount = 0L;
    // This is the backing of the view's checkbox
    private boolean displayModelsList = false;
    // This is for the view's conditional rendering of the list
    private boolean mustDisplayModelsList = false;
    
    private Long maxExcludedModelsForDetailDisplay = 5L;
    private Long excludedModelsCount = 0L;
    // This is the backing of the view's checkbox
    private boolean displayExcludedModelsList = false;
    // This is for the view's conditional rendering of the list
    private boolean mustDisplayExcludedModelsList = false;
    private String excludedModelsListAsString = null;
    private String deviceTypes = null;
    
    private boolean algorithmStatus = false;
    private boolean oldAlgorithmStatus = false;
    
    private LazyDataModel<PublicationDto> lazyPublicationListPublicationDataModel;
    
    // VAST info metadata
    private MobileAdVastMetadataDto vastMetaData = null;
    
    //Publisher audited creative information for Adx and AppNexus
    private List<PublisherAuditedInfoDto> publishersAuditedInfo = null;
    
    // Media Cost Optimisation bidding strategy
    private boolean mediaCostOptimisationEnabled = false;
    
    // Average Maximum Bid bidding strategy
    private boolean averageMaximumBidEnabled = false;
    
    // Average Maximum Bid bidding strategy threshold
    private BigDecimal averageMaximumBidThreshold;
    
    // MAD-2109 - Mobile and  ISP Operators
    private Set<Operator> ispOperators;
    private Set<Operator> mobileOperators;
    
    // MAD-3552 - Enable/Disable Internal LLD
    private boolean internalLLDStatus;
    private boolean oldInternalLLDStatus;
    
    //========================================================================
    // Approval form related stuff
    //========================================================================
    private static final boolean DEFAULT_NOTIFY_WATCHERS = false;
    private static final boolean DEFAULT_NOTIFY_ADVERTISER = false;

    private Long creativeId;
    private List<CreativeHistory> history;
    // We work with a status variable independent of the Creative itself.
    // The reason is because we don't just want to update Creative.status,
    // we need to invoke updateCreativeStatus for business logic reasons.
    private Creative.Status newStatus;
    private AdfonicUser originalAssignedTo;
    private String comment;
    private boolean notifyWatchers = DEFAULT_NOTIFY_WATCHERS;
    private boolean notifyAdvertiser = DEFAULT_NOTIFY_ADVERTISER;
    private Set<AdfonicUser> watchers;
    private String adserverKey;
    
    //========================================================================

    public Long getCreativeId() {
        return creativeId;
    }
    public void setCreativeId(Long creativeId) {
        if (!ObjectUtils.equals(creativeId, this.creativeId)) {
            this.externalID = null;
            this.creativeId = creativeId;
        }
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        if (!StringUtils.equals(externalID, this.externalID)) {
            this.creativeId = null;
            this.externalID = externalID;
        }
    }
    
    private static final FetchStrategy CREATIVE_HISTORY_FS = new FetchStrategyBuilder()
															    .addLeft(CreativeHistory_.adfonicUser)
															    .addLeft(CreativeHistory_.assignedTo)
															    .build();

    private static final SelectItem[] STATUS_OPTIONS = FacesUtils.makeEnumSelectItems(Creative.Status.values(), false).toArray(new SelectItem[0]);
    
    private void hydrateCreative(Creative creative) {
        // Trigger all the loads the FS used to handle
        creative.getCampaign().getName();
        creative.getSegment().getName();
        creative.getAssetBundleMap().size();
        creative.getFormat().getName();
        creative.getLanguage().getName();
        
        if(creative.getDestination() != null) {
        	creative.getDestination().getData();
        	if(creative.getDestination().getBeaconUrls()!=null){
        	    for(BeaconUrl beacon : creative.getDestination().getBeaconUrls()){
        	        beacon.getUrl();
        	    }
        	}
        }
        if(creative.getAssignedTo() != null) {
        	creative.getAssignedTo().getFirstName();
        }
        if(creative.getExtendedCreativeType() != null) {
        	creative.getExtendedCreativeType().getName();
        }
        
        creative.getPublishersAuditedCreative();
    }
    
    private void hydrateCampaign(Campaign campaign) {
        // Trigger all the loads the FS used to handle
    	if(campaign.getSegments() != null) {
    		campaign.getSegments().size();
    	}
        campaign.getAdvertiser().getName();
        campaign.getDefaultLanguage().getName();
        if(campaign.getTimePeriods() != null) {
        	campaign.getTimePeriods().size();
        }
        campaign.getCategory().getName();
        if(campaign.getCurrentBid() != null) {
        	campaign.getCurrentBid().getAmount();
        }
        if(campaign.getCurrentBidDeductions() != null) {
        	for(BidDeduction bd : campaign.getCurrentBidDeductions()){
        		if (bd.getThirdPartyVendor() != null) {
        			bd.getThirdPartyVendor().getName();
        			bd.getThirdPartyVendor().getThirdPartyVendorType().getName();
        		}
        	}
        }
        if(campaign.getTransparentNetworks() != null) {
        	campaign.getTransparentNetworks().size();
        	for(TransparentNetwork tn : campaign.getTransparentNetworks()){
        	    tn.isDefaultRateCard();
        	}
        }
        if(campaign.getHistoricalBids() != null) {
        	campaign.getHistoricalBids().size();
        }
        if(campaign.getWatchers() != null) {
        	campaign.getWatchers().size();
        }
        if (campaign.getCampaignAudiences() != null) {
            for (CampaignAudience ca : campaign.getCampaignAudiences()) {
                ca.getAudience().getName();
            }
        }
        if (campaign.getCurrentRichMediaAdServingFee() != null) {
            campaign.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee();
        }
        if (campaign.getCurrentTradingDeskMargin() != null) {
            campaign.getCurrentTradingDeskMargin().getTradingDeskMargin();
        }
        
        if (campaign.getPublicationList() != null) {
            campaign.getPublicationList().getId();
        }
        
        if (campaign.getPrivateMarketPlaceDeal() != null) {
            campaign.getPrivateMarketPlaceDeal().getPublisher().getId();
        }

        if(campaign.getCampaignTriggers() != null) {
            campaign.getCampaignTriggers().size();
        }

    }
    
    private void hydrateExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
        // Trigger all the loads the FS used to handle
    	if(extendedCreativeType.getTemplateMap() != null) {
    		extendedCreativeType.getTemplateMap().size();
    	}
    	if(extendedCreativeType.getFeatures() != null) {
    		extendedCreativeType.getFeatures().size();
    	}
    }
    
    private void hydrateAdvertiser(Advertiser advertiser) {
        // Trigger all the loads the FS used to handle
        advertiser.getCompany().getName();
        advertiser.getAccount().getBalance();
        if(advertiser.getDestinations() != null) {
        	advertiser.getDestinations().size();
        }
    }
    
    private void hydrateCompany(Company company) {
        // Trigger all the loads the FS used to handle
    	if(company.getPublisher() != null) {
    		company.getPublisher().getName();
    	}
        company.getAccountManager().getFirstName();
        if (company.getRoles() != null) {
            company.getRoles().size();
        }
    }
    
    public void hydrateExcludedModelsInSegmentListener(AjaxBehaviorEvent event) {
        TransactionalRunner runner = getTransactionalRunner();
        try {
	        runner.runTransactionalReadOnly(
	                    new Runnable() {
	                        public void run() {
	                        	hydrateExcludedModelsInSegment();
	                        }
	                    }
	                );
       } catch(Exception e) {
       	LOG.severe("While running hydrateExcludedModelsInSegment():\n" +
       			   ExceptionUtils.getFullStackTrace(e));
       }
    }
    
    private void hydrateExcludedModelsInSegment() {
    	LOG.fine("Hydrating excluded models");
    	excludedModelsCount = getTargetingManager().countExcludedModelsForSegment(segment);
    	if(excludedModelsCount > 0 && (excludedModelsCount < maxExcludedModelsForDetailDisplay || this.displayExcludedModelsList)) {
    		mustDisplayExcludedModelsList = true;
    		if(excludedModelsListAsString == null) {
	    		FetchStrategy fs = new FetchStrategyBuilder()
	    		                   .addLeft(Segment_.excludedModels)
	    		                   .addLeft(Model_.vendor)
	    		                   .build();
	    		Segment s = getTargetingManager().getSegmentById(segment.getId(), fs);
	    		StringBuffer buffer = new StringBuffer();
	    		int i = 0;
	    		for(Model model : s.getExcludedModels()) {
	    			if(i > 0) {
	    				buffer.append(", ");
	    			}
	    			buffer.append(model.getVendor().getName() + " " + model.getName());
	    			i++;
	    		}
	    		excludedModelsListAsString = buffer.toString();
    		}
    	} else {
    		mustDisplayExcludedModelsList = false;
    	}
    	LOG.fine("Done hydrating excluded models. mustDisplayExcludedModelsList: " + mustDisplayExcludedModelsList);
    }
    
    private void hydrateSegment() {
        // Trigger all the loads the FS used to handle
    	if(segment.getIpAddresses() != null) {
    		segment.getIpAddresses().size();
    	}
    	if(segment.getGeotargets() != null) {
    		segment.getGeotargets().size();
    	}
    	
    	if(segment.getCountries() != null) {
    		segment.getCountries().size();
    	}
    	
    	if(segment.getLocationTargets() != null) {
    		segment.getLocationTargets().size();
    	}
    	
    	hydrateExcludedModelsInSegment();
    	
    	if (segment.getIncludedCategories() != null) {
    	    segment.getIncludedCategories().size();
    	}
    	if(segment.getExcludedCategories() != null) {
    		segment.getExcludedCategories().size();
    	}
    	if(segment.getModels() != null && segment.getModels().size() > 0) {
    		FetchStrategy fs = new FetchStrategyBuilder()
					            .addLeft(Segment_.models)
					            .addLeft(Model_.vendor)
					            .build();
			Segment s = getTargetingManager().getSegmentById(segment.getId(), fs);
    		// Must buffer before clearing, as Hibernate returns as s the same object as segment. Damn caching...
    		Set<Model> models = new HashSet<Model>();
    		models.addAll(s.getModels());
			segment.getModels().clear();
			segment.getModels().addAll(models);
    		//segment.getModels().size();
	        //for(Model model : segment.getModels()) {
	        //	model.getVendor().getName();
	        //}
    	}
    	if(segment.getTargettedPublishers() != null) {
    		segment.getTargettedPublishers().size();
    	}
    }

    public void loadCreative() {
        TransactionalRunner runner = getTransactionalRunner();
         try {
	        runner.runTransactionalReadOnly(
	                    new Runnable() {
	                        public void run() {
	                        	loadCreativeTransactional();
	                        }
	                    }
	                );
        } catch(Exception e) {
        	LOG.severe("While running loadCreativeTransactional() for campaign " + (campaign != null ? campaign.getId() : "null") + 
        			   (this.creative != null ? " and creative + " +  (creativeId != null ? creativeId : externalID ) : "") + ":\n" +
        			   ExceptionUtils.getFullStackTrace(e));
        }
    }
    
    public void loadCreativeTransactional() {
        reinit();
        //Check
        if (creativeId != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.info("Loading Creative id=" + creativeId);
            }
            creative = getCreativeManager().getCreativeById(creativeId/*, CREATIVE_FS*/);
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.info("Loading Creative externalID=" + externalID);
            }
            creative = getCreativeManager().getCreativeByExternalId(externalID/*, CREATIVE_FS*/);
        }
        if (creative == null || creative.getCampaign() == null) {
            // the view should check these conditions and display appropriate messsages
            return;
        }
        else if(!creativeAllowedForUser(creative)){
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }
        hydrateCreative(creative);
        creativeId = creative.getId();
        externalID = creative.getExternalID();
        setCreative(creative);
        Destination destination = creative.getDestination();
        if (destination != null) {
            destinationData = destination.getData();
            dataIsFinalDestination = destination.isDataIsFinalDestination();
            finalDestination = destination.getFinalDestination();
            for(BeaconUrl beacon : destination.getBeaconUrls()){
                beacons.add(new BeaconUrl(beacon.getUrl()));
            }
            if(destination.getBeaconUrls().size()==0){
                beacons.add(new BeaconUrl(null));
            }
        }
        creativeEndDate = creative.getEndDate();
        
        campaign = getCampaignManager().getCampaignById(creative.getCampaign().getId()/*, CAMPAIGN_FS*/);
        hydrateCampaign(campaign);
        
        // Loading bidding strategies
        Set<BiddingStrategy> biddingStrategies = campaign.getBiddingStrategies();
        if (biddingStrategies!=null){
        	
        	// Optimise Media Cost
            mediaCostOptimisationEnabled = biddingStrategies.contains(BiddingStrategy.MEDIA_COST_OPTIMISATION);
            
            // Average Maximum Bid
            averageMaximumBidEnabled = biddingStrategies.contains(BiddingStrategy.AVERAGE_MAXIMUM_BID);
            
            // Average Maximum Bid threshold
            averageMaximumBidThreshold = campaign.getMaxBidThreshold();
        }
        
        this.algorithmStatus = this.oldAlgorithmStatus = this.learningAlgorithmService.isCampaignAddedToLearningAlgorithm(campaign.getId());
        
        // MAD-3552 - Enable/Disable Internal LLD
        this.internalLLDStatus = this.oldInternalLLDStatus = getCampaignManager().isCampaignInternalLLDEnabled(campaign.getId());
        
        campaignAudiences = new ArrayList<CampaignAudience>(campaign.getCampaignAudiences());
        if (creative.getExtendedCreativeType() != null) {
            extendedCreativeType = getExtendedCreativeManager().getExtendedCreativeTypeById(creative.getExtendedCreativeType().getId()/*, EXTENDED_CREATIVE_FS*/);
            hydrateExtendedCreativeType(extendedCreativeType);
            Map<ContentForm,String> templateMap = extendedCreativeType.getTemplateMap();
            validContentFormsForExtendedCreativeType = new ArrayList<ContentForm>();
            originalTemplateValuesForContentForm = new HashMap<>();
            Map<ContentForm, ExtendedCreativeTemplate> data = getExtendedCreativeManager().getExtendedCreativeTemplatesMapForCreative(creative);
            for(ContentForm contentForm : templateMap.keySet()) {
            	validContentFormsForExtendedCreativeType.add(contentForm);
            	ExtendedCreativeTemplate template = data.get(contentForm);
            	if(template != null) {
            		originalTemplateValuesForContentForm.put(contentForm, template.getTemplateOriginal());
                    if (extendedCreativeType.getMediaType() == MediaType.VAST_XML_2_0){
                        loadVastMetadata(template.getTemplateOriginal());
                    }
            	}
            }
            templatesEditable = areCreativeTemplatesEditable(extendedCreativeType.getMediaType());
        }
        creativesCount = getCreativeManager().countAllCreatives(new CreativeFilter().setCampaign(campaign));
        
        advertiser = getAdvertiserManager().getAdvertiserById(campaign.getAdvertiser().getId()/*, ADVERTISER_FS*/);
        hydrateAdvertiser(advertiser);
        
        company = getCompanyManager().getCompanyById(advertiser.getCompany().getId()/*, COMPANY_FS*/);
        hydrateCompany(company);
        
        segment = getTargetingManager().getSegmentById(creative.getSegment().getId()/*, SEGMENT_FS*/);
        hydrateSegment();
        
        // Loading mobile and isp operators
        if (segment.getOperators()!=null || segment.getOperators().size()>0){
            this.ispOperators = new HashSet<Operator>();
            this.mobileOperators = new HashSet<Operator>();
            for (Operator operator : segment.getOperators()){
                if (operator.isMobileOperator()){
                    this.mobileOperators.add(operator);
                }else{
                    this.ispOperators.add(operator);
                }
            }
        }
        this.deviceTypes = segment.getDeviceGroupHumanReadable();
        
        // House ads don't have a BidType, others should but null check anyway AO-120
        if (!campaign.isHouseAd() && campaign.getCurrentBid() != null) {
            bidType = campaign.getCurrentBid().getBidType();
        }
        
        // Bid Deductions
        bidDeductions = new ArrayList<BidDeduction>(campaign.getCurrentBidDeductions());
        
        // 3rd Party Vendor Cost (CPM)
        CampaignRichMediaAdServingFee rmAdFee = campaign.getCurrentRichMediaAdServingFee();
        adServingCpmFee = (rmAdFee != null) ? rmAdFee.getRichMediaAdServingFee() : null;
        
        // Approval-related items
        history = getCreativeManager().getCreativeHistory(creative, CREATIVE_HISTORY_FS);
        
        newStatus = creative.getStatus();
        originalAssignedTo = creative.getAssignedTo();
        watchers = new HashSet<AdfonicUser>(campaign.getWatchers());

        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================

        // Don't forget to "initialize" your variables here.


        // And don't forget to update reinit cuz that pattern sucks butt.


        // @see CampaignAdminBean.State for a way around this lameness


        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================

        if (campaign.getTransparentNetworks().contains(getPublicationManager().getTransparentNetworkByName(TransparentNetwork.PERFORMANCE_NETWORK_NAME))){
            isPremium = true;
        }else{
            isPremium = false;
        }

        if ( !CollectionUtils.isEmpty(campaign.getSegments()) && campaign.getSegments().get(0)!=null
                    && !CollectionUtils.isEmpty(campaign.getSegments().get(0).getChannels())){

                channels.addAll(campaign.getSegments().get(0).getChannels());
        }

        this.browsers.addAll(segment.getBrowsers());

         for (DisplayType dt : creative.getAssetBundleMap().keySet()) {
            AssetBundle bundle = creative.getAssetBundle(dt);
            // This one should be fine without a FS, since we are in a transactional stack and the assetMap is used in getAsset()
            bundle = getAssetManager().getAssetBundleById(bundle.getId()/*, ASSET_BUNDLE_FS*/);
            for (Component c : bundle.getAssetMap().keySet()) {
                assets.add(new AssetBean(dt.getName(), c.getName(), bundle.getAsset(c)));
            }
        }

        // Already loaded 
        if (campaign.getCurrentBid() != null) {
            if (campaign.getCurrentBid().getAmount() != null){
                newBidAmount = campaign.getCurrentBid().getAmount();
            }
        }

        // Is the company back-fill enabled
        if (advertiser.getCompany().isBackfill()) {
        	// In a transactional stack, so whatever the FS used to load will be loaded when accessed
            User mobclixUser = getUserManager().getUserByEmail(MOBCLIX_EMAIL/*, USER_FS*/);
            if (mobclixUser != null) {
                if (mobclixUser.getCompany().getPublisher().getApprovedCreatives().contains(creative)) {
                    onMobclix = true;
                } else {
                    onMobclix = false;
                }
            }
        }

        newCreativeName = creative.getName();

        newCampaignName = campaign.getName();

        String oldApplicationId = campaign.getApplicationID();
        if (StringUtils.isNotBlank(oldApplicationId)){
            newApplicationId = oldApplicationId;
        }
        
        adserverKey = DigestUtils.sha1Hex(this.advertiser.getExternalID().toLowerCase());
        
        if (campaign.getPublicationList() != null) {
            lazyPublicationListPublicationDataModel = new 
                    LazyPublicationListPublicationDataModel(
                            getPublicationManager(), campaign.getPublicationList());       
        }
        
        // Load publisher audited info
        loadPublisherAuditedCreativeInfo();
    }

    private boolean creativeAllowedForUser(Creative creative){
        if(isRestrictedUser()){
            Advertiser advertiser = creative.getCampaign().getAdvertiser();
            if(getAdvertisersForAdfonicUser().contains(advertiser)){
                return true;
            }
            return false;
        }
        else{
            return true;
        }
    }

    public BigDecimal getAdvertiserBalance() {
        return this.advertiser.getAccount().getBalance();
    }

    public BigDecimal getBudgetSpendYesterday() {
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DATE, -1);
        return getCampaignManager().getBudgetSpendAmountForCampaign(campaign, new Date(today.getTimeInMillis()));
    }

    public BigDecimal getBudgetSpendToday() {
        Date now = new Date(System.currentTimeMillis());
        return getCampaignManager().getBudgetSpendAmountForCampaign(campaign, now);
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public Creative getCreative() {
        return creative;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public Segment getSegment() {
        return segment;
    }

    protected void setCreative(Creative cre) {
        this.creative = cre;
    }

    public List<AssetBean> getAssets() {
        return assets;
    }

    /*
     * status check on campaign and creative
     */
    public boolean isCreativeEndDateEditable() {
        return (creative.getStatus() == Creative.Status.ACTIVE || creative.getStatus() == Creative.Status.PENDING) &&
               (campaign.getStatus() == Campaign.Status.ACTIVE || campaign.getStatus() == Campaign.Status.PENDING);
    }

    /*
     * you may only update if creative.status and campaign.status and
     * creative status are pending or active
     *
     * you may not update the end date to null or a date > than campaign
     * end date if it has one
     *
     * you may not update this field to a date in the past
     */
    public boolean isValidCreativeEndDate() {
        FacesContext fc = FacesContext.getCurrentInstance();

        // end of day for in advertiser tz
        if (creativeEndDate != null) {
            creativeEndDate = DateUtils.getEndOfDay(creativeEndDate, company.getDefaultTimeZone());
        }

        // is it changing?
        if (creativeEndDate == null && creative.getEndDate() == null) {
            // null == null, ok
            return true;
        }
        else if (creativeEndDate != null && creative.getEndDate() != null &&
            creativeEndDate.equals(DateUtils.getEndOfDay(creative.getEndDate(),company.getDefaultTimeZone()))) {
            // same, ok
            return true;
        }

        // disabled in UI but check again
        if (!isCreativeEndDateEditable()) {
            fc.addMessage("mainForm:creativeEndDate",
                    messageForId("error.creativeAdmin.creativeEndDate.campaignCreativeStatus"));
            return false;
        }

        // now that we've verified it's changing and the statuses allow a change, check date
        if (creativeEndDate == null) {
            // open-ended, ok
            return true;
        }

        // the creative end date be after any campaign end date
        if (campaign.getEndDate() != null && creativeEndDate.after(campaign.getEndDate())) {
            fc.addMessage("mainForm:creativeEndDate",
                    messageForId("error.creativeAdmin.creativeEndDate.afterCampaignEndDate"));
            return false;
        }

        // the creative end date may not be before today
        if (creativeEndDate.before(DateUtils.getEndOfDay(new Date(), company.getDefaultTimeZone()))) {
            fc.addMessage("mainForm:creativeEndDate",
                    messageForId("error.creativeAdmin.creativeEndDate.past"));
            return false;
        }

        return true;
    }

    public boolean isValidAdvertiserDomain() {
        boolean isValid = false;
        if (StringUtils.isNotBlank(campaign.getAdvertiserDomain())){
            Matcher matcher = DOMAIN_NAME_PATTERN.matcher(campaign.getAdvertiserDomain());
            if (matcher.matches()) {
                isValid = true;
            }
        }

        if (!isValid) {
            FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:advertiserDomain",
                        messageForId("error.creativeAdmin.advertiserDomain"));
        }
        return isValid;
    }

    public boolean isValidUrls() {        
        // Validate destination data
        boolean destinationValid = validateUrlFieldWithMessage(destinationData, "mainForm:destinationData");
        
        // Validate final destination if selected
        boolean finalDestinationValid = (!dataIsFinalDestination) ? validateUrlFieldWithMessage(finalDestination, "mainForm:finalDestination") : destinationValid;
        
        return destinationValid && finalDestinationValid;
    }
    
    /**
     * Validate an URL specified with an element id and put the related error message next to the element if the URL is not valid.
     *  
     * @param url the URL to be validated
     * @param id the element id in html which contains the URL text
     * 
     * @return true only if the URL is valid
     */
    private boolean validateUrlFieldWithMessage(String url, String id) {
        ValidationResult validation = ValidationUtils.validateUrl(url);
        if (validation.isFailed()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(id, messageForId(validation.getMessageKey()));
            return false;
        }
        return true;
    }
    
    public boolean isValidCreativeName(){
        boolean isValid = false;

        if (StringUtils.isNotBlank(newCreativeName)){
            if (!creative.getName().equals(newCreativeName)){
                // Check for uniqueness
                if (getCreativeManager().isCreativeNameUnique(newCreativeName, creative.getCampaign(), creative)){
                    creative.setName(newCreativeName);
                    isValid = true;
                }else{
                    // Duplicate
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage("mainForm:newCreativeName",
                            messageForId("error.creativeAdmin.newCreativeName.unavailable"));
                    return false;
                }
            }else{
                // No change.
                isValid = true;
            }
        }

        if (!isValid) {
            FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:newCreativeName",
                        messageForId("error.creativeAdmin.newCreativeName.invalid"));
        }
        return isValid;
    }

    public boolean isValidApplicationId(){
        boolean isValid = false;

        if (!campaign.isInstallTrackingAdXEnabled() && !campaign.isInstallTrackingEnabled()) {
            // UI doesn't present the option. Just bail without change.
            return true;
        }
        else if (campaign.isInstallTrackingAdXEnabled()) {
            if (AdXUtils.isValidAdXApplicationID(newApplicationId)) {
                isValid = true;
            }
        }
        else if (StringUtils.isNotBlank(newApplicationId)) {
            // it's non blank, fine
            isValid = true;
        }

        if (!isValid) {
            FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:newApplicationId",
                        messageForId("error.creativeAdmin.newApplicationId.invalid"));
        }
        return isValid;
    }

    public boolean isValidCampaignName(){
        boolean isValid = false;

        if (StringUtils.isNotBlank(newCampaignName)){
            if (!campaign.getName().equals(newCampaignName)){
                // Check for uniqueness
                if (getCampaignManager().isCampaignNameUnique(newCampaignName, creative.getCampaign().getAdvertiser(), creative.getCampaign())){
                    campaign.setName(newCampaignName);
                    isValid = true;
                }else{
                    // Duplicate
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage("mainForm:newCampaignName",
                            messageForId("error.creativeAdmin.newCampaignName.unavailable"));
                    return false;
                }
            }else{
                // No change.
                isValid = true;
            }
        }

        if (!isValid) {
            FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:newCampaignName",
                        messageForId("error.creativeAdmin.newCampaignName.invalid"));
        }
        return isValid;
    }
    
    public boolean isValidExchangeRate(){
        boolean isValid = true;
        
        // Validate exchange rates thresholds
        BigDecimal minThreshold = campaign.getCurrencyExchangeRate().getMinThreshold();
        BigDecimal maxThreshold = campaign.getCurrencyExchangeRate().getMaxThreshold();
        if ((campaign.getExchangeRate().compareTo(minThreshold)<0) || (campaign.getExchangeRate().compareTo(maxThreshold)>0)){
            FacesMessage message = messageForId("error.creativeAdmin.exchangerates.thresholds.invalids", minThreshold.setScale(6).toString(), maxThreshold.setScale(6).toString());
            FacesContext.getCurrentInstance().addMessage("mainForm:exchangeRate", message);
            isValid = false;
        }
        
        return isValid;
    }
    
    public boolean isCampaignNotLaunched(){
        return CAMPAIGN_NOT_LAUNCHED_STATUSES.contains(campaign.getStatus());
    }
    
    public boolean isValidExtendedCreativeTemplateData() {
    	boolean someContentPresent = false;
    	for(int i = 0;i < this.validContentFormsForExtendedCreativeType.size();i++) {
    		ContentForm contentForm = this.validContentFormsForExtendedCreativeType.get(i);
    		String content = originalTemplateValuesForContentForm.get(contentForm);
        	if(StringUtils.isEmpty(content)) {
        		continue;
        	}
        	someContentPresent = true;
        	if(content.length() > 30000) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:contentFormLoop:" + i + ":templateOriginalValue",
                        messageForId("error.creativeAdmin.extendedCreativeTemplate.contentTooLong"));
                return false;
        	}
    	}
        //AO-418 content only required if isUseDynamicTemplates
        if(this.extendedCreativeType.isUseDynamicTemplates() && !someContentPresent) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("mainForm:contentFormLoop:0:templateOriginalValue",
                    messageForId("error.creativeAdmin.extendedCreativeTemplate.noContent"));
            return false;
    	}
    	
    	return true;
    }
    
    

    private void reinit() {
        //session scoped, so need to clear out
        newBidAmount = null;
        onMobclix = false;
        removeIPTargeting = false;
        creativeEndDate = null;
        creative = null;
        campaign = null;
        company = null;
        segment = null;
        advertiser = null;
        destinationData = null;
        finalDestination = null;
        dataIsFinalDestination = true;
        beacons = new ArrayList<BeaconUrl>();
        assets.clear();
        browsers.clear();
        isPremium = null;
        excludedCategoryLabelMap = null;
        channels = new ArrayList<Channel>();
        bidType = null;
        bidDeductions = null;
        campaignsCount = null;
        creativesCount = null;
        newCreativeName = null;
        newApplicationId = null;
        newCampaignName = null;
        campaignAudiences = null;
        extendedCreativeType = null;
        
        // Approval related items
        history = null;
        newStatus = null;
        originalAssignedTo = null;
        comment = null;
        notifyWatchers = DEFAULT_NOTIFY_WATCHERS;
        notifyAdvertiser = DEFAULT_NOTIFY_ADVERTISER;
        watchers = null;
        
        modelsCount = 0L;
        displayModelsList = false;
        mustDisplayModelsList = false;
        
        excludedModelsCount = 0L;
        displayExcludedModelsList = false;
        mustDisplayExcludedModelsList = false;
        excludedModelsListAsString = null;

        availableBidTypes = null;
        
        lazyPublicationListPublicationDataModel = null;
        
        vastMetaData = null;
        
        publishersAuditedInfo = null;
        
        mediaCostOptimisationEnabled = false;
        averageMaximumBidEnabled = false;
        averageMaximumBidThreshold = null;
        
        ispOperators = null;
        mobileOperators = null;
        
        deviceTypes = null;
        
        algorithmStatus = false;
        oldAlgorithmStatus = false;
        
        // MAD-3552 - Enable/Disable Internal LLD
        internalLLDStatus = false;
        oldInternalLLDStatus = false;
        
        // The reinit pattern wins the "Shitty Pattern of the Year" award.
        // This keeps biting us in the ass because developers keep forgetting
        // to use it.
        //
        // @see CampaignAdminBean.State for a way around this lameness
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================




        // HEY YOU!  This block should stand out nicely.


        // @see CampaignAdminBean.State for a way around this lameness


        // Don't forget to consider this horrible pattern when you add
        // new attributes to this bean.

        // @see CampaignAdminBean.State for a way around this lameness



        // And don't forget to look at CampaignAdminBean, which has a
        // one-line state reset that I wrote a while back, but nobody
        // has spent the time to adopt.

        // @see CampaignAdminBean.State for a way around this lameness


        // Whatever.


        // @see CampaignAdminBean.State for a way around this lameness


        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================
        //=====================================================================

    }

    public String doSubmitNew() {
        // Save creative information
        TransactionalRunner runner = getTransactionalRunner();
        String rc = null;
        try {
	        rc = runner.callTransactional(
	                    new Callable<String>() {
	                        public String call() throws Exception {
	                            return doSubmitNewTransactional();
	                        }
	                    }
	                );
	        
	        // Execute algorithm
	        if (rc!=null){
                try{
                    if (this.oldAlgorithmStatus!=this.algorithmStatus){
                        if (this.algorithmStatus){
                            LOG.fine("Activating status per campaign");
                            this.learningAlgorithmService.includeCampaignToLearningAlgorithm(campaign.getId(), adfonicUser().getId());
                        }else{
                            LOG.fine("Removing status per campaign");
                            this.learningAlgorithmService.excludeCampaignFromLearningAlgorithm(campaign.getId(), adfonicUser().getId());
                        }
                    }
                }catch(Exception e){
                    String errorMessage = "Cannot " + (algorithmStatus? "include" : "remove") + " campaign to learning algorithm";  
                    LOG.log(Level.SEVERE, errorMessage, e);
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage("mainForm:status",  new FacesMessage((errorMessage)));
                    rc = null;
                }
            }
        } catch(Exception e) {
        	LOG.severe("While running doSubmitNewTransactional() for campaign " + campaign.getId() + 
        			   (this.creative != null ? " and creative + " +  this.creative.getId() : "") + ":\n" +
        			   ExceptionUtils.getFullStackTrace(e));
        }
        return rc;
    }
    /**
     * This is the new action method supporting creative approvals
     */
    public String doSubmitNewTransactional() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creative id=" + creative.getId() + " is being updated by " + adfonicUser().getFullName());
        }
        
        // Disregard the view returned from doSubmit...we only care if it
        // returned null in the case of an error
        if (doSubmit() == null) {
            return null;
        }

        // Reconcile any changes in watchers, auto-adding as applicable
        updateWatchers();

        // Update the creative status as applicable, and do all the usual
        // business logic associated with the activation, i.e. transitioning
        // the campaign to a live status if necessary, creating notification
        // flags, company messages, etc.  That's all encapsulated in the call
        // to middleware logic below.  But first let's see if the creative had
        // been approved before, sometime in the past (i.e. resubmission).
        boolean hadBeenApprovedBefore = creative.getApprovedDate() != null;
        boolean statusChanged = false;
        if (!creative.getStatus().equals(newStatus)) {
            Map<String, Object> rcMap = getCreativeManager().updateCreativeStatusAsMap(creative, newStatus);
            statusChanged = (Boolean) rcMap.get(CreativeManager.CREATIVE_STATUS_UPDATE_STATUS);
            if (!statusChanged) {
                // Failed. Look for the error message
                String errorMessage = (String)rcMap.get(CreativeManager.CREATIVE_STATUS_UPDATE_ERROR_MESSAGE);
                FacesContext fc = FacesContext.getCurrentInstance();
                if (StringUtils.isEmpty(errorMessage)) {
                    fc.addMessage("mainForm:status", messageForId("error.creativeAdmin.updateStatus.unknown"));                    
                }
                else {
                    fc.addMessage("mainForm:status", new FacesMessage((errorMessage)));
                }
                // redisplay form with error
                return null;
            }            
        }        
        
        if (statusChanged) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Creative id=" + creative.getId() + " is now " + creative.getStatus());
            }
        }

        // Only bother creating a history entry (and maybe notifying watchers)
        // if at least one of the following occcurred:
        // a) creative.status changed
        // b) creative.assignedTo changed
        // c) the admin user entered a comment
        if (statusChanged || !ObjectUtils.equals(creative.getAssignedTo(), originalAssignedTo) || StringUtils.isNotBlank(comment)) {
            // Create a new history entry to keep a paper trail
            getCreativeManager().newCreativeHistory(creative, comment, adfonicUser());
            
            if (notifyWatchers) {
                sendEmailToWatchers();
            }

            // Only notify the advertiser if the checkbox was selected, and
            // then only if the status changed or a comment was entered.
            if (notifyAdvertiser && (statusChanged || StringUtils.isNotBlank(comment))) {
                sendEmailToAdvertiser(hadBeenApprovedBefore);
            }
        }

        // This is our approvals dashboard specific view
        return "pretty:admin/approval/creative";
    }

    void updateWatchers() {
        campaign.getWatchers().clear();
        campaign.getWatchers().addAll(watchers);

        // Make sure the user submitting the form, and the assignee (if there
        // is one) are automatically added as watchers
        campaign.getWatchers().add(adfonicUser());
        if (creative.getAssignedTo() != null) {
            campaign.getWatchers().add(creative.getAssignedTo());
        }
        
        getCampaignManager().update(campaign);

        // "Refresh" our collection with what's now current
        watchers.clear();
        watchers.addAll(campaign.getWatchers());
    }

    public String doSubmit() {
        TransactionalRunner runner = getTransactionalRunner();
        String rc = null;
        try {
	        rc = runner.callTransactional(
	                    new Callable<String>() {
	                        public String call() throws Exception {
	                            return doSubmitTransactional();
	                        }
	                    }
	                );
        } catch(Exception e) {
        	LOG.severe("While running doSubmitTransactional() for campaign " + campaign.getId() + 
        			   (this.creative != null ? " and creative + " +  this.creative.getId() : "") + ":\n" +
        			   ExceptionUtils.getFullStackTrace(e));
        }
        return rc;
    }

    public String doSubmitTransactional() {
        // session collision check
        if (!creative.getExternalID().equals(this.externalID)) {
            reinit();
            setRequestFlag("sessionCollision");
            return null;
        }

        // Bid price check
        Double averageMaximumBid = (averageMaximumBidThreshold != null) ? averageMaximumBidThreshold.doubleValue() : null;
		if (isAverageMaximumBidEnabled() && averageMaximumBid != null && newBidAmount.doubleValue() > averageMaximumBid.doubleValue()) {
        	FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("mainForm:newBidAmount", messageForId("page.creative.approval.bidbudget.greaterthanthreshold", averageMaximumBid.toString()));
        	return null;
        }
        
        Destination destination = creative.getDestination();
        if (destination != null && ObjectUtils.notEqual(DestinationType.CALL, destination.getDestinationType()) && !isValidUrls()) {
            return null;
        }
        
        // Not validate IAB and Campaign domain during rejected state change MAD-1184
        if(!Creative.Status.REJECTED.equals(newStatus)) {
            if(!isValidIABCategory()){
                return null;
            }
    
            if (!isValidAdvertiserDomain()) {
                return null;
            }
        }

        if (!isValidCreativeName()) {
            return null;
        }
        
        if(creative.getExtendedCreativeType() != null && !isValidExtendedCreativeTemplateData()) {
        	return null;
        }

        boolean applicationIdChanged = false;
        if (!isValidApplicationId()){
            return null;
        } else {
            if (ObjectUtils.notEqual(campaign.getApplicationID(), newApplicationId)) {
            	applicationIdChanged = true;
                campaign.setApplicationID(newApplicationId);
            }

        }

        if (!isValidCampaignName()){
            return null;
        }
        
        if (!isValidExchangeRate()){
            return null;
        }

        if (!isValidCreativeEndDate()) {
            // messages should be in faces context already
            return null;
        }
        else {
            creative.setEndDate(creativeEndDate);
        }

        segment.getBrowsers().clear();
        segment.getBrowsers().addAll(browsers);

        segment.getChannels().clear();
        segment.getChannels().addAll(channels);

        if(channels == null || channels.size() == 0){
            if(segment.isChannelEnabled()){//we can blindly set to false..donno much JPA.. it may merge the object and updates only modified fields.
                segment.setChannelEnabled(false);
            }
        }else{
            if(segment.isChannelEnabled() == false){
                segment.setChannelEnabled(true);
            }
        }

        //check for changes in the bid.
        //BidType may have changed as well !!
        // No need to check for house ads as the getCurrentBidType will return null
        if (campaign.getCurrentBid() != null && campaign.getCurrentBid().getAmount() != null) {

            //we'll check a change of bid OR change of bidType
            if ( (newBidAmount != null && campaign.getCurrentBid().getAmount().compareTo(newBidAmount) != 0)
                || (campaign.getCurrentBid().getBidType()!=bidType)
                    ) {

                // if the bid type is changing set BudgetType to monetary as not all types support imp/click etc.
                if ((campaign.getCurrentBid().getBidType()!=bidType) && 
                        campaign.getBudgetType() != BudgetType.MONETARY) {
                    campaign.setBudgetType(BudgetType.MONETARY);
                }
                
                //BidType bidType = campaign.getCurrentBid().getBidType();
                campaign = getBiddingManager().newCampaignBid(campaign, bidType, newBidAmount);
                
                
            }//NO change of bidAmout or bidType so no newCampaignBid

            BigDecimal bidAmount = campaign.getCurrentBid().getAmount();

             if( bidType.equals(BidType.CPA) || bidType.equals(BidType.CPI) ){
                //if not null means has value and TRUE: set autommatically this one.
                campaign.setPriceOverridden(true);
             }//will be null in other cases

             if (!campaign.isPriceOverridden()) {
                 // auto set to priceOverridden if 
                 // - fractional price
                 if (isFractionalPrice(bidAmount)) {
                     campaign.setPriceOverridden(true);
                 }
             }
        }
        
        // Bidding Strategies
        Set<BiddingStrategy> biddingStrategies = new HashSet<BiddingStrategy>();
        if (mediaCostOptimisationEnabled) {
            biddingStrategies.add(BiddingStrategy.MEDIA_COST_OPTIMISATION);
        }
        if (averageMaximumBidEnabled) {
            biddingStrategies.add(BiddingStrategy.AVERAGE_MAXIMUM_BID);
        }
        campaign.setMaxBidThreshold(averageMaximumBidEnabled ? averageMaximumBidThreshold : null);
        campaign.setBiddingStrategies(biddingStrategies);
        
        // Ad Serving CPM fee
        if (adServingCpmFee.compareTo(campaign.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee()) != 0){
        	campaign = getFeeManager().saveCampaignRichMediaAdServingFee(campaign.getId(), adServingCpmFee);
        }
        
        // Campaign Bid Deduction changes
        if (bidDeductions != null && !bidDeductions.isEmpty()) {
        	// Refresh current bid deductions
        	campaign.setCurrentBidDeductions(getCampaignManager().getCampaignById(campaign.getId()).getCurrentBidDeductions());
        	campaign = getBiddingManager().updateBidDeductions(campaign, new HashSet<BidDeduction>(bidDeductions));
        }

        // Is the company back-fill enabled
        if (advertiser.getCompany().isBackfill()) {

            // Lets get the mobclix account
        	// In a transactional stack, so no need for FS'es
            User mobclixUser = getUserManager().getUserByEmail(MOBCLIX_EMAIL/*, USER_FS */);

            // Do we want to add it to mobclix or remove it from mobclix?
            if (mobclixUser != null) {
                if (onMobclix) {
                    // If the creative is already on mobclix, don't add it again
                    if (!mobclixUser.getCompany().getPublisher().getApprovedCreatives().contains(creative)) {
                        mobclixUser.getCompany().getPublisher().getApprovedCreatives().add(creative);
                    }
                } else {
                    mobclixUser.getCompany().getPublisher().getApprovedCreatives().remove(creative);
                }
                getPublisherManager().update(mobclixUser.getCompany().getPublisher());
            }
        }

        if (removeIPTargeting && !segment.getIpAddresses().isEmpty() ) {
            segment.setIpAddressesListWhitelist(true);
            segment.getIpAddresses().clear();
        }

        boolean destinationChanged = false;
        if (StringUtils.isNotBlank(destinationData)) {
//            // beacon is optional, and defined as a string, so the form returns an empty string to represent null
//            if (StringUtils.isBlank(beacon)) {
//                beacon = null;
//            }
            beacons = removeEmptyBeacons(beacons);
            if(destination != null && (ObjectUtils.notEqual(destination.getData(), destinationData) || 
                    !equalListBeacons(beacons, destination.getBeaconUrls()) ||
                    ObjectUtils.notEqual(destination.getFinalDestination(), finalDestination) ||
                    destination.isDataIsFinalDestination() != dataIsFinalDestination)) {
                if (dataIsFinalDestination) {
                    finalDestination = null;
                }

                DestinationType destinationType = destination.getDestinationType();
                Destination dest = 
                        getCreativeManager().getDestinationForAdvertiserAndDestinationTypeAndData(
                                advertiser, 
                                destinationType, 
                                destinationData, 
                                true, 
                                removeRepeatedBeacons(beacons),
                                dataIsFinalDestination,
                                finalDestination);
                creative.setDestination(dest);
                destinationChanged = true;
            }
        }

        creative = getCreativeManager().update(creative);
        campaign = getCampaignManager().update(campaign);
        segment = getTargetingManager().update(segment);
        
        if(this.extendedCreativeType != null) {
            Map<ContentForm, ExtendedCreativeTemplate> data = getExtendedCreativeManager().getExtendedCreativeTemplatesMapForCreative(creative);
        	for(ContentForm contentForm : this.validContentFormsForExtendedCreativeType) {
        		String templateOriginalContent = this.originalTemplateValuesForContentForm.get(contentForm);
        		if(StringUtils.isEmpty(templateOriginalContent)) {
        			// No data. See if we had some, and if so, kill it
        			ExtendedCreativeTemplate oldData = data.get(contentForm);
        			if(oldData != null) {
        			    getExtendedCreativeManager().delete(oldData);
        			}
        		} else {
        			// See if there is data for it already, if so, update it, if not, create a new entry
        			ExtendedCreativeTemplate oldData = data.get(contentForm);
        			if(oldData != null) {
        				oldData.setTemplateOriginal(templateOriginalContent);
        				getExtendedCreativeManager().update(oldData);
        			} else {
        			    getExtendedCreativeManager().newExtendedCreativeTemplate(creative, contentForm, templateOriginalContent);
        			}
        		}
        	}
        }

        if(destinationChanged) {
        	List<Creative> updatedCreatives = getCreativeManager().updateCreativeStatusForAdXReprovisioning(creative);
			submitCreatives(updatedCreatives);
        }

		if(applicationIdChanged) {
			if(campaign.isInstallTrackingAdXEnabled()) {
				List<Creative> creatives = getCreativeManager().getCreativesEligibleForAdXReprovisioning(campaign/*, creativeFs*/);
				List<Creative> updatedCreatives = getCreativeManager().updateCreativeStatusForAdXReprovisioning(creatives);
				submitCreatives(updatedCreatives);
			}
		}
		
        creative = getCreativeManager().getCreativeById(creative.getId()/*, CREATIVE_FS*/);
        hydrateCreative(creative);
        
        campaign = getCampaignManager().getCampaignById(campaign.getId()/*, CAMPAIGN_FS*/);
        hydrateCampaign(campaign);
        
        segment = getTargetingManager().getSegmentById(segment.getId()/*, SEGMENT_FS*/);
        hydrateSegment();
        
        
        // MAD-3552 - Enable/Disable Internal LLD
        try{
            if (this.oldInternalLLDStatus!=this.internalLLDStatus){
                if (this.internalLLDStatus){
                    LOG.fine("Activating internal LLD for campaign " + campaign.getId());
                    getCampaignManager().enableCampaignInternalLLD(campaign.getId());
                }else{
                    LOG.fine("Disabling internal LLD for campaign " + campaign.getId());
                    getCampaignManager().disableCampaignInternalLLD(campaign.getId());
                }
            }
        }catch(Exception e){
            String errorMessage = "Cannot " + (internalLLDStatus? "enable" : "disable") + " internal LLD for this campaign.";  
            LOG.log(Level.SEVERE, errorMessage, e);
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("mainForm:status",  new FacesMessage((errorMessage)));
            return null;
        }
        

        return "pretty:admin/creative";
    }
    
    protected void submitCreatives(List<Creative> creatives) {
		for(Creative creative : creatives) {
			try {
			    getCreativeManager().submitCreative(creative);
	        } catch (Exception e) {
	            logger.log(
	                    Level.SEVERE,
	                    "Error generating ticket campaign item id=" +
	                    campaign.getId() +
	                    " for user id=" +
	                    getUserId(),
	                    e);
	        }
		}
    }
    
    // Remove campaign learnings
    public void resetCampaignLearnings(ActionEvent event) {
        if (campaign!=null){
           this.learningAlgorithmService.removeCampaignLearnings(this.campaign.getId(), adfonicUser().getId());
        }
    }

    public void cancelBrowserDialog(ActionEvent event) {
        // reset browsers
        this.browsers.clear();
        this.browsers.addAll(segment.getBrowsers());
    }
    
    //public CampaignAdminBean getCampaignAdminBean() {
    //    return campaignAdminBean;
    //}

    public BigDecimal getNewBidAmount() {
        return newBidAmount;
    }

    public void setNewBidAmount(BigDecimal newBidAmount) {
        this.newBidAmount = newBidAmount;
    }

    public void setOnMobclix(boolean onMobclix) {
        this.onMobclix = onMobclix;
    }

    public boolean getOnMobclix() {
        return this.onMobclix;
    }

    public List<Browser> getBrowsers() {
        return browsers;
    }

    public void setBrowsers(List<Browser> browsers) {
        this.browsers = browsers;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void setRemoveIPTargeting(boolean removeIPTargeting) {
        this.removeIPTargeting = removeIPTargeting;
    }

    public boolean getRemoveIPTargeting() {
        return this.removeIPTargeting;
    }

    public Date getCreativeEndDate() {
        return this.creativeEndDate;
    }

    public void setCreativeEndDate(Date creativeEndDate) {
        this.creativeEndDate = creativeEndDate;
    }

    public boolean isDataIsFinalDestination() {
        return dataIsFinalDestination;
    }
    public void setDataIsFinalDestination(boolean dataIsFinalDestination) {
        this.dataIsFinalDestination = dataIsFinalDestination;
    }
    public String getFinalDestination() {
        return finalDestination;
    }
    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
    }
    public void setOptimizationDefaultMaxRemoval(
            BigDecimal optimizationDefaultMaxRemoval) {
        this.optimizationDefaultMaxRemoval = optimizationDefaultMaxRemoval;
    }

    public BigDecimal getOptimizationDefaultMaxRemoval() {
        return optimizationDefaultMaxRemoval;
    }

    public void setOptimizationDefaultMultiplier(
            BigDecimal optimizationDefaultMultiplier) {
        this.optimizationDefaultMultiplier = optimizationDefaultMultiplier;
    }

    public BigDecimal getOptimizationDefaultMultiplier() {
        return optimizationDefaultMultiplier;
    }

    public String getDestinationData() {
        if (this.destinationData == null) {
            Destination destination = this.creative.getDestination();
            this.destinationData = (destination != null) ? destination.getData() : StringUtils.EMPTY;
        }
        return this.destinationData;
    }

    public void setDestinationData(String destinationData) {
        this.destinationData = destinationData;
    }
    
    public List<BeaconUrl> getBeacons() {
        if (CollectionUtils.isEmpty(this.beacons)) {
            this.beacons.add(new BeaconUrl(null));
        }
        return this.beacons;
    }

    public void setBeacons(List<BeaconUrl> beacons) {
        this.beacons = beacons;
    }    

    /***
     * Validates that the Category chosen is different from the Adfonic not Categorized one (default one)
     * @return true if the category is diferent from {@link com.adfonic.domain.Category.NOT_CATEGORIZED_NAME}
     *
     * */
    private boolean isValidIABCategory() {
        Category cat = getCampaign().getCategory();
        FacesContext fc = FacesContext.getCurrentInstance();
        if(cat!=null) {
            if(Category.NOT_CATEGORIZED_NAME.equals(cat.getName())){
                //has to select another one before saving.
                fc.addMessage("mainForm:campaignCategory",
                         messageForId("error.pubCatSearch.changenotcategorizedcategory"));
                return false;
            }else{
                //different one is selected
                return true;
            }
        }else{
                fc.addMessage("mainForm:campaignCategorySearch",
                         messageForId("error.pubCatSearch.categorynotnull"));
            return false;
        }

    }

    public String getCampaignCategoryHierarchyName() {
        if (this.campaign != null &&
                this.campaign.getCategory() != null) {
            return categoryHierarchyService.getHierarchicalName(
                    this.campaign.getCategory(),
                    CATEGORY_SEPARATOR);
        }
        return StringUtils.EMPTY;
    }

    // read-only list, hierarchy sorted
    public List<Category> getExcludedCategories() {
        if (segment != null &&
                CollectionUtils.isNotEmpty(segment.getExcludedCategories())) {
            List<Category> excluded =
                new ArrayList<Category>(segment.getExcludedCategories());
            categoryHierarchyService.sortCategoriesByHierarchicalName(excluded,
                    false);
            Map<Category,String> labelMap = new HashMap<Category,String>();
            for (Category c : excluded) {
                labelMap.put(c, categoryHierarchyService.getHierarchicalName(c, CATEGORY_SEPARATOR));
            }
            this.excludedCategoryLabelMap = labelMap;
            return excluded;
        }
        return Collections.emptyList();
    }

    public Map<Category, String> getExcludedCategoryLabelMap() {
        return this.excludedCategoryLabelMap;
    }

    public void doRemoveExcludedCategory(Category category) {
        if (category != null && segment != null &&
                CollectionUtils.isNotEmpty(segment.getExcludedCategories())) {
            segment.getExcludedCategories().remove(category);
        }
    }

    public void handleSelectedExcludedCategory(SelectEvent event) {
        Category c = (Category)event.getObject();
        if (c != null && segment != null) {
            segment.getExcludedCategories().add(c);
        }
    }

    public void handleSelectedCategory(SelectEvent event) {
        Category c = (Category)event.getObject();
        if (c != null && campaign != null) {
            campaign.setCategory(c);
        }
    }

    public List<BidType> getBidTypes() {
        List<BidType> bidTypes = new ArrayList<BidType>();
            bidTypes.add(BidType.CPC);
            bidTypes.add(BidType.CPM);
                if(getCampaign().isInstallTrackingEnabled() || getCampaign().isInstallTrackingAdXEnabled()){
                    bidTypes.add(BidType.CPI);
                }
                if(getCampaign().isConversionTrackingEnabled()){
                    bidTypes.add(BidType.CPA);
                }
        return bidTypes;
    }
    
    public BidType getBidType() {
        return bidType;
    }

    public void setBidType(BidType bidType) {
        this.bidType = bidType;
    }

    /* lazy load, displayed for non-key advertisers */
    public Long getCampaignsCount() {
        if (campaignsCount == null && advertiser != null) {
            campaignsCount = getCampaignManager().countAllCampaigns(new CampaignFilter().setAdvertiser(advertiser));
        }
        return campaignsCount;
    }

    public Long getCreativesCount() {
        return creativesCount;
    }

    public String getNewCreativeName(){
        return newCreativeName;
    }

    public void setNewCreativeName(String newCreativeName){
        this.newCreativeName = newCreativeName;
    }

    public String getNewApplicationId(){
        return newApplicationId;
    }

    public void setNewApplicationId(String newApplicationId){
        this.newApplicationId = newApplicationId;
    }

    public String getNewCampaignName(){
        return newCampaignName;
    }

    public void setNewCampaignName(String newCampaignName){
        this.newCampaignName = newCampaignName;
    }

    public List<CreativeHistory> getCreativeHistory() {
        return history;
    }

    public Creative.Status getNewStatus() {
        return newStatus;
    }
    public void setNewStatus(Creative.Status newStatus) {
        this.newStatus = newStatus;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNotifyWatchers() {
        return notifyWatchers;
    }
    public void setNotifyWatchers(boolean notifyWatchers) {
        this.notifyWatchers = notifyWatchers;
    }

    public boolean isNotifyAdvertiser() {
        return notifyAdvertiser;
    }
    public void setNotifyAdvertiser(boolean notifyAdvertiser) {
        this.notifyAdvertiser = notifyAdvertiser;
    }

    public Set<AdfonicUser> getWatchers() {
        return watchers;
    }
    public void setWatchers(Set<AdfonicUser> watchers) {
        this.watchers = watchers;
    }

    public SelectItem[] getStatusOptions() {
        return STATUS_OPTIONS;
    }

    public void setExtendedCreativeType(ExtendedCreativeType extendedCreativeType) {
        this.extendedCreativeType = extendedCreativeType;
    }
    public ExtendedCreativeType getExtendedCreativeType() {
        return this.extendedCreativeType;
    }
    
    public SelectItem[] getAssignToOptions() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        selectItems.add(new SelectItem(null, "-- Unassign"));
        for (AdfonicUser adfonicUser : getUserManager().getAllAdfonicUsers(new Sorting("firstName", "lastName"))) {
            selectItems.add(new SelectItem(adfonicUser, adfonicUser.getFullName()));
        }
        return selectItems.toArray(new SelectItem[0]);
    }

    public SelectItem[] getAdfonicUserOptions() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (AdfonicUser adfonicUser : getUserManager().getAllAdfonicUsers(new Sorting("firstName", "lastName"))) {
            selectItems.add(new SelectItem(adfonicUser, adfonicUser.getFullName()));
        }
        return selectItems.toArray(new SelectItem[0]);
    }

    /**
     * Send an email to the advertiser for a creative status change and/or
     * comment made by the admin user
     * @param hadBeenApprovedBefore whether or not the creative had been
     * approved sometime in the past, i.e. when this was a resubmission
     */
    void sendEmailToAdvertiser(boolean hadBeenApprovedBefore) {
        switch (creative.getStatus()) {
            case ACTIVE:
                sendActivationEmailToAdvertiser(hadBeenApprovedBefore);
                break;
            case REJECTED:
                sendRejectionEmailToAdvertiser();
                break;
            default:
                break;
        }
        
        // if comment is non-blank
        if (StringUtils.isNotBlank(comment)) {
            sendCommentEmailToAdvertiser();
        }
    }
    
    /**
     * Send an activation email to the advertiser
     * @param hadBeenApprovedBefore whether or not the creative had been
     * approved sometime in the past, i.e. when this was a resubmission
     */
    void sendActivationEmailToAdvertiser(boolean hadBeenApprovedBefore) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending activation email for Creative id=" + creative.getId());
        }
        try {
            creativeEmailUtils.sendCreativeApprovalEmail(creative, hadBeenApprovedBefore, FacesContext.getCurrentInstance());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send approval email for Creative id=" + creative.getId(), e);
        }
    }

    /**
     * Send an rejection email to the advertiser
     */
    void sendRejectionEmailToAdvertiser() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending rejection email for Creative id=" + creative.getId());
        }
        try {
            creativeEmailUtils.sendCreativeRejectionEmail(creative, comment, FacesContext.getCurrentInstance());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send rejection email for Creative id=" + creative.getId(), e);
        }
    }

    /**
     * Send a comment email to the advertiser (i.e. requesting more info)
     */
    void sendCommentEmailToAdvertiser() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending comment email for Creative id=" + creative.getId() + ", comment=" + comment);
        }
        try {
            creativeEmailUtils.sendCreativeCommentEmail(creative, comment, FacesContext.getCurrentInstance());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send comment email for Creative id=" + creative.getId(), e);
        }
    }

    /**
     * Send an email to all of the campaign's watchers
     */
    void sendEmailToWatchers() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending update email to watchers for Creative id=" + creative.getId());
        }
        try {
            creativeEmailUtils.sendUpdateEmailToWatchers(creative, FacesContext.getCurrentInstance());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send update email to watchers for Creative id=" + creative.getId(), e);
        }
    }

    public void setCreativeEmailUtils(CreativeEmailUtils creativeEmailUtils) {
        this.creativeEmailUtils = creativeEmailUtils;
    }
    
    public List<CampaignAudience> getCampaignAudiences() {
        return this.campaignAudiences;
    }
    
    public List<BidDeduction> getBidDeductions() {
		return bidDeductions;
	}
    
	public BigDecimal getAdServingCpmFee() {
		return adServingCpmFee;
	}
	
	public void setAdServingCpmFee(BigDecimal adServingCpmFee) {
		this.adServingCpmFee = adServingCpmFee;
	}
	
	public boolean isRichMedia() {
        if (extendedCreativeType != null) {
            return (CollectionUtils.isNotEmpty(extendedCreativeType.getFeatures()) &&
                    extendedCreativeType.getFeatures().contains(Feature.RICH_MEDIA));
        }
        return false;
    }
	public List<ContentForm> getValidContentFormsForExtendedCreativeType() {
		return validContentFormsForExtendedCreativeType;
	}
	public Map<ContentForm, String> getOriginalTemplateValuesForContentForm() {
		return originalTemplateValuesForContentForm;
	}

	public String getAdserverKey() {
        return adserverKey;
    }
    public void setAdserverKey(String adserverKey) {
        this.adserverKey = adserverKey;
    }
	public Long getMaxModelsForDetailDisplay() {
		return maxModelsForDetailDisplay;
	}
	
	
	public void setMaxModelsForDetailDisplay(Long maxModelsForDetailDisplay) {
		this.maxModelsForDetailDisplay = maxModelsForDetailDisplay;
	}
	public Long getModelsCount() {
		return modelsCount;
	}
	public void setModelsCount(Long modelsCount) {
		this.modelsCount = modelsCount;
	}
	public boolean isDisplayModelsList() {
		return displayModelsList;
	}
	public void setDisplayModelsList(boolean displayModelsList) {
		this.displayModelsList = displayModelsList;
	}
	public Long getMaxExcludedModelsForDetailDisplay() {
		return maxExcludedModelsForDetailDisplay;
	}
	public void setMaxExcludedModelsForDetailDisplay(
			Long maxExcludedModelsForDetailDisplay) {
		this.maxExcludedModelsForDetailDisplay = maxExcludedModelsForDetailDisplay;
	}
	public Long getExcludedModelsCount() {
		return excludedModelsCount;
	}
	public void setExcludedModelsCount(Long excludedModelsCount) {
		this.excludedModelsCount = excludedModelsCount;
	}
	public boolean isDisplayExcludedModelsList() {
		return displayExcludedModelsList;
	}
	public void setDisplayExcludedModelsList(boolean displayExcludedModelsList) {
		this.displayExcludedModelsList = displayExcludedModelsList;
	}
	public boolean isMustDisplayModelsList() {
		return mustDisplayModelsList;
	}
	public void setMustDisplayModelsList(boolean mustDisplayModelsList) {
		this.mustDisplayModelsList = mustDisplayModelsList;
	}
	public boolean isMustDisplayExcludedModelsList() {
		return mustDisplayExcludedModelsList;
	}
	public void setMustDisplayExcludedModelsList(
			boolean mustDisplayExcludedModelsList) {
		this.mustDisplayExcludedModelsList = mustDisplayExcludedModelsList;
	}
	public String getExcludedModelsListAsString() {
		return excludedModelsListAsString;
	}
	    
    public LazyDataModel<PublicationDto> getLazyPublicationListPublicationDataModel() {
        return lazyPublicationListPublicationDataModel;
    }
    
    public BigDecimal getExchangeRate(){
        BigDecimal exchangeRate = null;
        if (campaign!=null){
            exchangeRate = this.campaign.getExchangeRate().setScale(6);
        }
        return exchangeRate;
    }
    
    public void setExchangeRate(BigDecimal exchangeRate){
        BigDecimal currentExchangeRate = getExchangeRate();
        if (currentExchangeRate.compareTo(exchangeRate)!=0){
            this.campaign.setExchangeRate(exchangeRate);
            this.campaign.setExchangeRateAdminChange(true);
        }
    }
    
    public Double getExchangeRateMinThreshold(){
        return campaign.getCurrencyExchangeRate().getMinThreshold().setScale(6).doubleValue();
    }
    
    public Double getExchangeRateMaxThreshold(){
        return campaign.getCurrencyExchangeRate().getMaxThreshold().setScale(6).doubleValue();
    }

    public boolean isMediaCostOptimisationEnabled() {
        return mediaCostOptimisationEnabled;
    }
    
    public void setMediaCostOptimisationEnabled(boolean mediaCostOptimisationEnabled) {
        this.mediaCostOptimisationEnabled = mediaCostOptimisationEnabled;
    }
    
    public boolean isAverageMaximumBidEnabled() {
		return averageMaximumBidEnabled;
	}
    
	public void setAverageMaximumBidEnabled(boolean averageMaximumBidEnabled) {
		this.averageMaximumBidEnabled = averageMaximumBidEnabled;
	}
	
	public BigDecimal getAverageMaximumBidThreshold() {
		return averageMaximumBidThreshold;
	}
	
	public void setAverageMaximumBidThreshold(BigDecimal averageMaximumBidThreshold) {
		this.averageMaximumBidThreshold = averageMaximumBidThreshold;
	}
	
	public boolean enableMediaCostOptimisationCheckbox(){
        boolean checkboxEnabled = false;

        if (campaign!=null){
            // if monetary budget is selected, mediacost optimisation checkbox is enabled too
            checkboxEnabled = (BudgetType.MONETARY == campaign.getBudgetType());
        }
        return checkboxEnabled;
    }
    
    public Set<Operator> getIspOperators() {
        return ispOperators;
    }
    
    public Set<Operator> getMobileOperators() {
        return mobileOperators;
    }
    
    public String getDeviceTypes() {
        return deviceTypes;
    }
    
    public boolean isInternalLLDStatus() {
        return internalLLDStatus;
    }
    public void setInternalLLDStatus(boolean internalLLDStatus) {
        this.internalLLDStatus = internalLLDStatus;
    }
    

    //==========================================================================================================
    // Code previously living in CampaignAdminBean, and modified to avoid repeated loads of Segment
    //==========================================================================================================

    public enum FrequencyCapPeriod {
        HOUR (60*60, "hour"),
        DAY  (60*60*24, "day"),
        WEEK (60*60*24*7, "week"),
        MONTH(60*60*24*30, "month");

        private int seconds;
        private String description;

        public int getSeconds() {
            return this.seconds;
        }
        public String getDescription() {
            return this.description;
        }
        private FrequencyCapPeriod(int seconds, String description) {
            this.seconds = seconds;
            this.description = description;
        }

        public FrequencyCapPeriod getFrequencyCapPeriodBySeconds(int seconds) {
            for (FrequencyCapPeriod f : FrequencyCapPeriod.values()) {
                if (f.seconds == seconds) {
                    return f;
                }
            }
            return null;
        }
    }

    public String getVerboseWeekdays() {
        boolean[] days = segment.getDaysOfWeekAsArray();
        List<Range<Segment.DayOfWeek>> list = new ArrayList<Range<Segment.DayOfWeek>>();
        for (int i = Segment.DayOfWeek.Monday.ordinal(); i <= Segment.DayOfWeek.Friday.ordinal(); i++) {
            if (days[i]) {
                Range.combine(list, Segment.DayOfWeek.values()[i]);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) { sb.append(", "); }
            Range<Segment.DayOfWeek> r = list.get(i);
            sb.append(r.toString());
        }
        return sb.toString();
    }

    public String getVerboseWeekdayHours() {
         if (segment.isAllHoursWeekdays()) {
            return "All day";
        }
        boolean[] hours = segment.getHoursOfDayAsArray();
        List<Range<Integer>> list = new ArrayList<Range<Integer>>();
        for (int i = 0; i < 24; i++) {
            if (hours[i]) {
                Range.combine(list, i);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) { sb.append(", "); }
            Range<Integer> r = list.get(i);
            sb.append(r.getStart())
            .append(":00")
            .append(" to ");
            if (r.getEnd() == 23) {
                sb.append("00");
            } else {
                sb.append(r.getEnd()+1);
            }
            sb.append(":00");
        }
        return sb.toString();
    }

    public String getVerboseWeekends() {
         boolean[] days = segment.getDaysOfWeekAsArray();
        StringBuilder sb = new StringBuilder();
        if (days[Segment.DayOfWeek.Saturday.ordinal()]) {
            sb.append(Segment.DayOfWeek.Saturday.toString());
        }
        if (days[Segment.DayOfWeek.Sunday.ordinal()]) {
            if (sb.length() > 0) { sb.append(", "); }
            sb.append(Segment.DayOfWeek.Sunday.toString());
        }
        return sb.toString();
    }

    public String getVerboseWeekendHours() {
        if (segment.isAllHoursWeekends()) {
            return "All day";
        }
        boolean[] hours = segment.getHoursOfDayWeekendAsArray();
        List<Range<Integer>> list = new ArrayList<Range<Integer>>();
        for (int i = 0; i < 24; i++) {
            if (hours[i]) {
                Range.combine(list, i);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) { sb.append(", "); }
            Range<Integer> r = list.get(i);
            sb.append(r.getStart())
            .append(":00")
            .append(" to ");
            if (r.getEnd() == 23) {
                sb.append("00");
            } else {
                sb.append(r.getEnd()+1);
            }
            sb.append(":00");
        }
        return sb.toString();
    }
    
    // expose to page technologies that don't do j5 enum
    public FrequencyCapPeriod[] getFrequencyCapPeriodValues() {
        return FrequencyCapPeriod.values();
    }

    public boolean isAppleOnly() {
        boolean isAppleOnly = false;
        if (segment!=null){
            getDeviceManager().isAppleOnly(segment);
        }
        return isAppleOnly;
    }

    public boolean isAndroidOnly() {
        boolean isAndroidOnly = false;
        if (this.segment!=null){
            isAndroidOnly = getDeviceManager().isAndroidOnly(segment);
        }
        return isAndroidOnly; 
    }
    
    public BigDecimal getBidMin() {
        return getBidMin(campaign, segment);
    }

    protected BigDecimal getBidMin(Campaign campaign, Segment segment) {
        if (campaign.isTransparent()) {
            // If it's a premium campaign, the minimum is dictated by the networks chosen.
            return maxMinBid(campaign.getTransparentNetworks(), campaign.getCurrentBid().getBidType());
        } else {
            // Otherwise minimums are dictated by geographical targeting
            //RateCardHelper rateCardHelper = new RateCardHelper(pm());
            //RateCard rateCard = rateCardHelper.getDefaultRateCard(campaign.getCurrentBid().getBidType());

            RateCard rateCard = getPublicationManager().getRateCardByBidType(campaign.getCurrentBid().getBidType());
            BigDecimal minBid = rateCard.getDefaultMinimum();
            for (Country c : segment.getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }
            return minBid;
        }
    }
    
    // find the highest min bid for a set of transparent networks
    protected BigDecimal maxMinBid(Set<TransparentNetwork> transparentNetworks, BidType bidType) {
        BigDecimal maxMinBid = BigDecimal.ZERO;

        for (TransparentNetwork tn : transparentNetworks) {
            if (getMinBidMap(tn).containsKey(bidType)) {
                maxMinBid = maxMinBid.max(getMinBidMap(tn).get(bidType));
            }
        }
        return maxMinBid;
    }

    /*
     * determine min bid for a transparent network respecting segment country targeting
     * getAvailableBidTypes should already exclude bid types that aren't valid for
     * the network
     */
    protected Map<BidType,BigDecimal> getMinBidMap(TransparentNetwork network) {
        return getMinBidMap(network, getAvailableBidTypes(), segment);
    }

    protected Map<BidType,BigDecimal> getMinBidMap(TransparentNetwork network, List<BidType> availableBidTypes, Segment segment) {
        Map<BidType,BigDecimal> minBidMap = new HashMap<BidType,BigDecimal>();

        //RateCardHelper rateCardHelper = new RateCardHelper(pm());

        for (BidType bidType : availableBidTypes) {
            RateCard rateCard = null;

            if (!network.isDefaultRateCard() && network.getRateCard(bidType) != null) {
                rateCard = network.getRateCard(bidType);
            }
            else {
                rateCard = getPublicationManager().getRateCardByBidType(bidType);
            }

            BigDecimal minBid = rateCard.getDefaultMinimum();

            for (Country c : segment.getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }

            minBidMap.put(bidType, minBid);
        }
        return minBidMap;
    }

    protected List<BidType> getAvailableBidTypes() {
        if (availableBidTypes == null) {
            List<BidType> allowed = new ArrayList<BidType>();
            CollectionUtils.addAll(allowed, BidType.values());

            if (!campaign.isTransparent()) {
                availableBidTypes = allowed;
            } else {
                // Transparent networks may not allow all bid types
                // All targeted networks must support the bid type
                for (TransparentNetwork p : campaign.getTransparentNetworks()) {
                    // for default don't remove any of the bid types
                    if (!p.isDefaultRateCard()) {
                        allowed.retainAll(p.getRateCardMap().keySet());
                    }
                }
                availableBidTypes = allowed;
            }
        }
        return availableBidTypes;
    }

    private boolean isFractionalPrice(BigDecimal bidAmount) {
        return ((bidAmount.multiply(new BigDecimal(1000)).intValue()) % 10) != 0;        
    }
    
    public int getMaxUrlLength() {
        return ValidationUtils.getUrlMaxLength();
    }
    
    public BigDecimal getTargetCPA() {
        return campaign.getTargetCPA();
    }
    public void setTargetCPA(BigDecimal targetCPA) {
        this.campaign.setTargetCPA(getTransformZeroValue(targetCPA));
    }
    public BigDecimal getOptimizationMaxRemoval() {
        return campaign.getOptimizationMaxRemoval();
    }
    public void setOptimizationMaxRemoval(BigDecimal optimizationMaxRemoval) {
        this.campaign.setOptimizationMaxRemoval(getTransformZeroValue(optimizationMaxRemoval));
    }
    public BigDecimal getOptimizationMultiplier() {
        return campaign.getOptimizationMultiplier();
    }
    public void setOptimizationMultiplier(BigDecimal optimizationMultiplier) {
        this.campaign.setOptimizationMultiplier(getTransformZeroValue(optimizationMultiplier));
    }
    public boolean isAlgorithmStatus() {
        return algorithmStatus;
    }
    public void setAlgorithmStatus(boolean algorithmStatus) {
        this.algorithmStatus = algorithmStatus;
    }
    
    private BigDecimal getTransformZeroValue(BigDecimal value){
        if ((value!=null) && (value.compareTo(new BigDecimal(0.0d))==0)){
            return null;
        }
        return value;
    }
    
    public boolean isTemplatesEditable() {
        return templatesEditable;
    }
    private boolean areCreativeTemplatesEditable(MediaType mediaType) {
        return NON_EDITABLE_TEMPLATES_MEDIATYPES.contains(mediaType);
    }
    
    public MobileAdVastMetadataDto getVastMetaData() {
        return vastMetaData;
    }
    
    private void loadVastMetadata(String templateOriginal) {
        CreativeService creativeService = AdfonicBeanDispatcher.getBean(CreativeService.class);
        if (creativeService!=null){
            this.vastMetaData = creativeService.processVastTag(templateOriginal);
        }
    }
    
    private boolean equalListBeacons(List<BeaconUrl> beacons1, List<BeaconUrl> beacons2){
        if(beacons1.size()!=beacons2.size()){
            return false;
        }
        boolean foundAllBeacons = true;
        for(BeaconUrl b1 : beacons1){
            boolean beaconFound=false;
            for(BeaconUrl b2 : beacons2){
                if(b1.getUrl().equals(b2.getUrl())){
                    beaconFound=true;
                    break;
                }
            }
            if(!beaconFound){
                foundAllBeacons = false;
            }
        }
        return foundAllBeacons;
    }
    
    private List<BeaconUrl> removeEmptyBeacons(List<BeaconUrl> beacons){
        List<BeaconUrl> result = new ArrayList<BeaconUrl>();
        for(BeaconUrl beacon : beacons){
            if(StringUtils.isNotEmpty(beacon.getUrl())){
                result.add(beacon);
            }
        }
        return result;
    }
    
    private List<BeaconUrl> removeRepeatedBeacons(List<BeaconUrl> beacons){
        List<String> urls = new ArrayList<String>();
        List<BeaconUrl> result = new ArrayList<BeaconUrl>();
        if(CollectionUtils.isNotEmpty(beacons)){
            for(BeaconUrl beacon : beacons){
                if(!urls.contains(beacon.getUrl())){
                    result.add(beacon);
                    urls.add(beacon.getUrl());
                }
            }
        }
        return result;
    }
    
    
    public List<PublisherAuditedInfoDto> getPublishersAuditedInfo() {
        return publishersAuditedInfo;
    }
    private void loadPublisherAuditedCreativeInfo() {
        publishersAuditedInfo = new ArrayList<PublisherAuditedInfoDto>();
        
        // Adx
        PublisherAuditedInfoDto adxPublisherAuditedInfo = LazyCreativeDataModel.getAdxPublisherAuditedInfo(this.creative.getPublishersAuditedCreative(), auditedPublishersBean.getAdxPublishers(), this.creative.getId());
        if (adxPublisherAuditedInfo!=null){
            publishersAuditedInfo.add(adxPublisherAuditedInfo);
        }
        
        // AppNexus
        PublisherAuditedInfoDto apnPublisherAuditedInfo = LazyCreativeDataModel.getApnPublisherAuditedInfo(this.creative.getPublishersAuditedCreative(), auditedPublishersBean.getApnPublishers(), this.creative.getId());
        if (apnPublisherAuditedInfo!=null){
            publishersAuditedInfo.add(apnPublisherAuditedInfo);
        }
    }
    
    public static class AssetBean {
        String displayType;
        String component;
        String url;
        String text;
        Asset asset;

        public AssetBean(String displayType, String component, Asset asset) {
            this.displayType = displayType;
            this.component = component;

            AssetManager assetManager = AdfonicBeanDispatcher.getBean(AssetManager.class);
            this.asset = assetManager.getAssetById(asset.getId());

            if (this.asset.isText()) {
                text = this.asset.getDataAsString();
            } else {
                url = getURLRoot().concat("/showAsset/").concat(this.asset.getExternalID());
            }
        }

        public String getDisplayType() {
            return displayType;
        }

        public String getComponent() {
            return component;
        }

        public String getURL() {
            return url;
        }

        public String getText() {
            return text;
        }

        public Asset getAsset() {
            return asset;
        }
    }
}
 
