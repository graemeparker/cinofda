package com.adfonic.tools.beans.campaign.bid;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;
import static com.adfonic.tools.util.BudgetUtils.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.Campaign.Status;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.campaign.campaignagencydiscount.CampaignAgencyDiscountDto;
import com.adfonic.dto.campaign.campaignbid.BidDeductionDto;
import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorDto;
import com.adfonic.dto.campaign.campaignbid.ThirdPartyVendorTypeDto;
import com.adfonic.dto.campaign.campaigntargetctr.CampaignTargetCTRDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.BiddingStrategyName;
import com.adfonic.dto.campaign.enums.DestinationType;
import com.adfonic.dto.campaign.enums.FrequencyCapPeriod;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.currencyexchangerate.CurrencyExchangeRateService;
import com.adfonic.presentation.devicetarget.DeviceTargetService;
import com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorService;
import com.adfonic.presentation.thirdartyvendor.service.ThirdPartyVendorTypeService;
import com.adfonic.tools.beans.application.ToolsApplicationBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.exception.BudgetValidatorException;

@Component
@Scope("view")
public class CampaignBidMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignBidMBean.class);
    
    private static final int INTEGER_10  = 10;
    private static final int INTEGER_100 = 100;
    
    private static final String OVERALL_DISTRIBUTION_ASAP = "ASAP";
    private static final String OVERALL_DISTRIBUTION_EVEN = "EVEN";
    private static final String OVERALL_DISTRIBUTION_EVEN_IN_DAY = "EVEN_DAY";
    
    private static final Set<Status> CAMPAIGN_NOT_LAUNCHED_STATUSES = new HashSet<>(Arrays.asList(Status.NEW, Status.NEW_REVIEW, Status.DELETED, Status.PENDING, Status.PENDING_PAUSED));
    
    private static final String JSF_COMPONENT_EXCHANGE_RATE = "exchange-rate";
    private static final String MESSAGE_KEY_EXCHANGE_RATE_ERROR = "page.campaign.bidbudget.exchangerate.error.thresholds";
    
    private static final String MESSAGE_KEY_VENDOR_UNIQUE_ERROR = "page.campaign.bidbudget.biddeductions.vendor.unique";
    private static final String MESSAGE_KEY_VENDOR_FREE_TEXT_UNIQUE_ERROR = "page.campaign.bidbudget.biddeductions.vendorname.unique";
    
    @Autowired
    private CampaignService campaignService;
    
    @Autowired
    private DeviceTargetService deviceTargetService;
    
    @Autowired
    private ThirdPartyVendorService thirdPartyVendorService;
    
    @Autowired
    private ThirdPartyVendorTypeService thirdPartyVendorTypeService;
    
    @Autowired
    private ToolsApplicationBean toolsApplicationBean;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private CurrencyExchangeRateService currencyExchangeRateService;
    
    @Autowired
    private CompanyService companyService;

    private CampaignDto campaignDto;

    private boolean renderWeekDay = false;

    private Integer frecuency = INTEGER_10;

    private String interval;

    private Double campaignDailyBudget = null;

    private String bidType;

    private String amountCpc = "";

    private String amountCpm = "";

    private String amountCpx = "";

    private String overallBudget = "";

    private String dailyBudget = "";

    private boolean dailyBudgetAlertEnabled;

    private boolean overallBudgetAlertEnabled;

    private Integer capImpressions;

    private Integer capPeriodSeconds;
    
    private boolean capPerCampaign;

    private List<SelectItem> cpxOptions = new ArrayList<SelectItem>();

    private BigDecimal targetCtr;

    private BigDecimal targetCpa;

    private String evenDistributionOverallBudget;

    private boolean evenDistributionDailyBudget;

    private BigDecimal fixedMargin;
    private boolean fixedMarginChange = false;

    private BigDecimal adServingCpmFee;

    private String budgetType;

    private List<SelectItem> lBudgetTypes = null;

    private BigDecimal agencyDiscount;
    
    private boolean mediaCostOptimisationEnabled;
    private boolean averageMaximumBidEnabled;
    private BigDecimal averageMaximumBidThreshold;
    
    // Vendor Pricing  [MAD-2711]
    private List<BidDeductionDto> bidDeductions = new ArrayList<BidDeductionDto>();
    private List<ThirdPartyVendorTypeDto> thirdPartyVendorTypes = null;
    private boolean bidDeductionVendorsAreUnique = true;
    private String bidDeductionDuplicatedVendorClientId;
    private String bidDeductionDuplicatedVendorErrorKey;
    private Integer bidDeductionIndexToRemove = null;
    
    // Currency & Exchange rate  [MAD-3267]
    private Map<Long, CurrencyExchangeRateDto> invoiceCurrenciesMap;
    private CurrencyExchangeRateDto defaultInvoiceCurrency;
    private Long oldInvoiceCurrencyId;
    private Long selectedInvoiceCurrencyId;
    private BigDecimal oldExchangeRate;
    private BigDecimal exchangeRate;
    private boolean exchangeRateAdminChange;
    
    // Account fixed margin [MAD-3348]
    private AccountFixedMarginDto accountFixedMarginDto;

    public void doSave(ActionEvent event) {
        LOGGER.debug("doSave-->");
        if (validBudgets()) {
            // save and continue campaign;
            campaignDto = campaignService.saveBid(prepareDto(campaignDto, event));
            // update campaignDto to controller bean
            updateCampaignBeans(campaignDto);
            getCNavigationBean().setConfirmationDisabled(false);
            getCNavigationBean().removeCampaignNavigation(campaignDto.getId());
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
            if (getCNavigationBean().isCampaignBlocked()) {
                getCNavigationBean().setCampaignBlocked(false);
            }
        }
        LOGGER.debug("doSave<--");
    }

    private boolean validBudgets() {
        try {
            validatePrices(campaignDto, bidType, amountCpc, amountCpm, amountCpx, dailyBudget, overallBudget, campaignDailyBudget,
            		averageMaximumBidEnabled, averageMaximumBidThreshold);
        } catch (BudgetValidatorException bve) {
            FacesUtils.addFacesMessage(FacesMessage.SEVERITY_ERROR, bve.getComponentId(), null, bve.getDetailKey(), bve.getParams());
            return false;
        }
        
        // Validate exchange rates thresholds
        BigDecimal minThreshold = this.invoiceCurrenciesMap.get(this.selectedInvoiceCurrencyId).getMinThreshold();
        BigDecimal maxThreshold = this.invoiceCurrenciesMap.get(this.selectedInvoiceCurrencyId).getMaxThreshold();
        if ((this.exchangeRate.compareTo(minThreshold)<0) || (this.exchangeRate.compareTo(maxThreshold)>0)){
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_EXCHANGE_RATE, null, MESSAGE_KEY_EXCHANGE_RATE_ERROR, minThreshold.toString(), maxThreshold.toString());
            return false;
        }
        
        // Validate unique bid deductions
        if (!bidDeductionVendorsAreUnique) {
        	addFacesMessage(FacesMessage.SEVERITY_ERROR, bidDeductionDuplicatedVendorClientId, null, bidDeductionDuplicatedVendorErrorKey);
        	return false;
        }
        
        // Fixed margin and MAD-3348 (Admin users are allowed to set negative fixed margins)
        if (fixedMarginChange){
            double margin = 0;
            if (fixedMargin != null) {
                margin = fixedMargin.doubleValue();
            }
            double minimunMinimun = (isAdminUserLoggedIn() ? -INTEGER_100 : 0);
            if (margin < minimunMinimun || margin > INTEGER_100) {
                LOGGER.debug("Fixed margin not btwn " + minimunMinimun + " and 100");
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "fixed-margin", null, "page.campaign.bidbudget.fixedmargin.value", String.valueOf(minimunMinimun));
                return false;
            }
        }
        
        return true;
    }

    public CampaignDto prepareDto(CampaignDto campaignDto, ActionEvent event) {
        LOGGER.debug("prepareDto-->");

        campaignDto.setAdvertiser(getUser().getAdvertiserDto());
        // prepare the amount chosen
        campaignDto.getCurrentBid().setBidType(this.bidType);
        
        writeCurrentBid(campaignDto, amountCpc, amountCpm, amountCpx);
        
        if (CPC.equals(this.bidType)) {
            campaignDto.setTargetCPA(this.targetCpa);
        } else if (CPM.equals(this.bidType)) {
            if (this.targetCtr != null) {
                if (campaignDto.getTargetCTR() == null) {
                    campaignDto.setTargetCTR(new CampaignTargetCTRDto());
                }
                campaignDto.getTargetCTR().setTargetCTR(this.targetCtr.divide(BIG_DECIMAL_100));
            }
        } else if (CPA.equals(this.bidType) || CPI.equals(this.bidType)) {
            campaignDto.setTargetCPA(null);
            campaignDto.setTargetCTR(null);
        }

        campaignDto = prepareBudgets();
        campaignDto.setOverallBudgetAlertEnabled(this.overallBudgetAlertEnabled);
        campaignDto.setCapImpressions(this.capImpressions);
        campaignDto.setCapPeriodSeconds(this.capPeriodSeconds);
        campaignDto.setCapPerCampaign(this.capPerCampaign);
        campaignDto.setDailyBudgetAlertEnabled(this.dailyBudgetAlertEnabled);

        if (!CollectionUtils.isEmpty(event.getComponent().getAttributes()) && event.getComponent().getAttributes().get("role") != null) {
            campaignDto.getCurrentBid().setMaximum(true);
        } else {
            // always maximum to false
            campaignDto.getCurrentBid().setMaximum(false);
        }

        // BL-275: Even distribution and AT-1102
        if (isNoCap(this.overallBudget)) {
            campaignDto.setEvenDistributionOverallBudget(false);
        } else if (this.evenDistributionOverallBudget.equals(OVERALL_DISTRIBUTION_ASAP)) {
            campaignDto.setEvenDistributionOverallBudget(false);
            campaignDto.setEvenDistributionDailyBudget(this.evenDistributionDailyBudget);
        } else if (this.evenDistributionOverallBudget.equals(OVERALL_DISTRIBUTION_EVEN)) {
            campaignDto.setEvenDistributionOverallBudget(true);
            campaignDto.setEvenDistributionDailyBudget(false);
        } else if (this.evenDistributionOverallBudget.equals(OVERALL_DISTRIBUTION_EVEN_IN_DAY)) {
            campaignDto.setEvenDistributionOverallBudget(true);
            campaignDto.setEvenDistributionDailyBudget(true);
        }
        if (!isNoCap(this.dailyBudget)) {
            campaignDto.setEvenDistributionDailyBudget(this.evenDistributionDailyBudget);
        } else if (isNoCap(this.overallBudget)) {
            campaignDto.setEvenDistributionDailyBudget(false);
        }

        // Bid deductions
        campaignDto.setCurrentBidDeductions(bidDeductions);
        
        // Variable markup
        writeFixedMargin(campaignDto, fixedMargin, accountFixedMarginDto);
        
        // AdServing Cpm Fee
        writeAdServingCpmFee(campaignDto, adServingCpmFee);
        
        if (this.agencyDiscount != null) {
            if (campaignDto.getCurrentAgencyDiscount() == null) {
                campaignDto.setCurrentAgencyDiscount(new CampaignAgencyDiscountDto());
                campaignDto.getCurrentAgencyDiscount().setStartDate(new Date());
            }
            campaignDto.getCurrentAgencyDiscount().setDiscount(this.agencyDiscount.divide(BIG_DECIMAL_100));
        } else {
            if (campaignDto.getCurrentAgencyDiscount() == null) {
                campaignDto.setCurrentAgencyDiscount(new CampaignAgencyDiscountDto());
                campaignDto.getCurrentAgencyDiscount().setStartDate(new Date());
            }
            campaignDto.getCurrentAgencyDiscount().setDiscount(BIG_DECIMAL_0);
        }
        
        // Bidding Strategies
        Set<BiddingStrategyName> biddingStrategies = new HashSet<BiddingStrategyName>();
        if (this.mediaCostOptimisationEnabled) {
            biddingStrategies.add(BiddingStrategyName.MEDIA_COST_OPTIMISATION);
        }
        if (this.averageMaximumBidEnabled) {
        	biddingStrategies.add(BiddingStrategyName.AVERAGE_MAXIMUM_BID);
        }
        campaignDto.setMaxBidThreshold(averageMaximumBidEnabled ? averageMaximumBidThreshold : null);
        campaignDto.setBiddingStrategies(biddingStrategies);
        
        // Currency & Exchange rate  [MAD-3267]
        campaignDto.setCurrencyExchangeRate(this.invoiceCurrenciesMap.get(selectedInvoiceCurrencyId));
        campaignDto.setExchangeRate(this.exchangeRate);
        campaignDto.setExchangeRateAdminChange(hasExchangeRateBeenChangedByAdmin());

        LOGGER.debug("prepareDto<--");
        return campaignDto;
    }

    public void loadCampaignDto(CampaignDto campaignDto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = campaignDto;
        if (campaignDto != null) {
            
            // Bid type
            this.bidType = readBidType(campaignDto);
            
            // Amount
            this.amountCpc = readAmountCpc(campaignDto);
            this.amountCpm = readAmountCpm(campaignDto);
            this.amountCpx = readAmountCpx(campaignDto);
            
            if (CPC.equals(this.bidType)) {
                this.targetCpa = campaignDto.getTargetCPA() != null && campaignDto.getTargetCPA() != BIG_DECIMAL_0 ? campaignDto
                        .getTargetCPA() : null;
                this.targetCtr = null;
            } else if (CPM.equals(this.bidType)) {
                this.targetCtr = campaignDto.getTargetCTR() != null && campaignDto.getTargetCTR().getTargetCTR() != BIG_DECIMAL_0 ? campaignDto
                        .getTargetCTR().getTargetCTR().multiply(BIG_DECIMAL_100)
                        : null;
                this.targetCpa = null;
            } else if (CPI.equals(this.bidType) || CPA.equals(this.bidType)) {
                this.targetCpa = null;
                this.targetCtr = null;
            }
            
            // Budget
            this.dailyBudget = readDailyBudget(campaignDto);
            this.overallBudget = readOverallBudget(campaignDto);
            this.overallBudgetAlertEnabled = campaignDto.getOverallBudgetAlertEnabled();
            this.dailyBudgetAlertEnabled = campaignDto.getDailyBudgetAlertEnabled();
            
            // Cap
            this.capImpressions = campaignDto.getCapImpressions();
            this.capPeriodSeconds = campaignDto.getCapPeriodSeconds();
            this.capPerCampaign = campaignDto.isCapPerCampaign();

            if (isNoCap(this.overallBudget)
                    || (!isNoCap(this.overallBudget) && !campaignDto.isEvenDistributionOverallBudget())) {
                this.evenDistributionOverallBudget = OVERALL_DISTRIBUTION_ASAP;
                this.evenDistributionDailyBudget = campaignDto.isEvenDistributionDailyBudget();
            } else if (campaignDto.isEvenDistributionOverallBudget() && !campaignDto.isEvenDistributionDailyBudget()) {
                this.evenDistributionOverallBudget = OVERALL_DISTRIBUTION_EVEN;
                // You can't set daily even distribution if there is a
                // campaign/account distribution
                this.evenDistributionDailyBudget = false;
                this.dailyBudget = "";
            } else if (campaignDto.isEvenDistributionOverallBudget() && campaignDto.isEvenDistributionDailyBudget()) {
                this.evenDistributionOverallBudget = OVERALL_DISTRIBUTION_EVEN_IN_DAY;
                // You can't set daily even distribution if there is a
                // campaign/account distribution
                this.evenDistributionDailyBudget = false;
                this.dailyBudget = "";
            } else {
                this.evenDistributionOverallBudget = OVERALL_DISTRIBUTION_ASAP;
                this.evenDistributionDailyBudget = campaignDto.isEvenDistributionDailyBudget();
            }

            // Bid deductions
            if (CollectionUtils.isEmpty(campaignDto.getCurrentBidDeductions())) {
                this.bidDeductions = new ArrayList<BidDeductionDto>();
            } else {
                this.bidDeductions = campaignDto.getCurrentBidDeductions();
                // Initialize third party vendor field if the free text search were stored instead
                for (BidDeductionDto bd : this.bidDeductions) {
					if (bd.getThirdPartyVendor() == null) {
						bd.setThirdPartyVendor(prepareNewThirdPartyVendor());
					}
				}
            }
            
            // Account fixed margin [MAD-3348]
            accountFixedMarginDto = companyService.getAccountFixedMargin(getUser().getCompany().getId());
            
            // Variable markup
            this.fixedMargin = readFixedMargin(campaignDto, accountFixedMarginDto);
            this.adServingCpmFee = readAdServingCpmFee(campaignDto);
            
            if (campaignDto.getCurrentAgencyDiscount() != null && campaignDto.getCurrentAgencyDiscount().getDiscount().doubleValue() != 0.0) {
                this.agencyDiscount = campaignDto.getCurrentAgencyDiscount().getDiscount().multiply(BIG_DECIMAL_100);
            } else {
                this.agencyDiscount = null;
            }
            this.budgetType = campaignDto.getBudType();
            this.lBudgetTypes = new ArrayList<SelectItem>();
            setListBudgets(null);
            
            // Bidding Strategies
            Set<BiddingStrategyName> biddingStrategies = campaignDto.getBiddingStrategies();
            if (biddingStrategies != null) {
                mediaCostOptimisationEnabled = biddingStrategies.contains(BiddingStrategyName.MEDIA_COST_OPTIMISATION);
                averageMaximumBidEnabled = biddingStrategies.contains(BiddingStrategyName.AVERAGE_MAXIMUM_BID);
                averageMaximumBidThreshold = campaignDto.getMaxBidThreshold();
            }
            
            // Currency & Exchange rate  [MAD-3267]
            getAllInvoiceCurrenciesAsOrderedMap(); 
            this.defaultInvoiceCurrency = getDefaultInvoiceCurrency();
            if (campaignDto.getCurrencyExchangeRate()==null){
                this.oldInvoiceCurrencyId = this.selectedInvoiceCurrencyId = defaultInvoiceCurrency.getId();
                this.oldExchangeRate = this.exchangeRate = defaultInvoiceCurrency.getCurrentExchangeRate();
                exchangeRateAdminChange = false;
            }else{
                this.oldInvoiceCurrencyId = this.selectedInvoiceCurrencyId = campaignDto.getCurrencyExchangeRate().getId();
                if (campaignDto.getExchangeRate()==null){
                    this.oldExchangeRate = this.exchangeRate = this.invoiceCurrenciesMap.get(this.selectedInvoiceCurrencyId).getCurrentExchangeRate();
                }else{
                    this.oldExchangeRate = this.exchangeRate = campaignDto.getExchangeRate();
                }
                exchangeRateAdminChange = campaignDto.isExchangeRateAdminChange();
            }
            
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public void cancel(/* ActionEvent event */) {
        LOGGER.debug("cancel-->");
        loadCampaignDto(this.campaignDto);
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        LOGGER.debug("cancel<--");
    }

    public void enableWeekEndBudget(/* ActionEvent event */) {
        // if click whe enable the layouts to filling the weekend thingy.
        renderWeekDay = true;
    }

    public void onBidTypeChangeEvent(ValueChangeEvent e) {
        String value = (String) e.getNewValue();
        if (value != null && !"".equals(value)) {
            setBidType(value);
            if (CPC.equals(value)) {
                setAmountCpm("");
                setAmountCpx("");
                setTargetCtr(null);
                setListBudgets(CPC);
            } else if (CPM.equals(value)) {
                setAmountCpc("");
                setAmountCpx("");
                setTargetCpa(null);
                setListBudgets(CPM);
            } else if (CPI.equals(value) || CPA.equals(value)) {
                setAmountCpc("");
                setAmountCpm("");
                setListBudgets("CPX");
            }
            budgetType = MONETARY;
        }// only update value of bidType when is not CPX as the cpx = cpa or cpi

    }

    public void changeOverallBudgetListener(/* AjaxBehaviorEvent event */) {
        if (isNoCap(this.overallBudget)) {
            this.evenDistributionOverallBudget = OVERALL_DISTRIBUTION_ASAP;
        }
    }

    public void changeDailyBudgetListener(/* AjaxBehaviorEvent event */) {
        if (isNoCap(this.dailyBudget)) {
            this.evenDistributionDailyBudget = false;
        }
    }

    public void addBidDeduction(/*ActionEvent event*/) {
        BidDeductionDto bidDeduction = new BidDeductionDto();
        bidDeduction.setPayerIsByyd(Boolean.TRUE);
        bidDeduction.setThirdPartyVendor(prepareNewThirdPartyVendor());
		bidDeductions.add(bidDeduction);
    }

	public void removeBidDeduction(/*ActionEvent event*/) {
        if (bidDeductionIndexToRemove != null) {
            bidDeductions.remove(bidDeductionIndexToRemove.intValue());
            
            // If the duplicated is going to be removed reset unique validation
            if (!bidDeductionVendorsAreUnique && bidDeductionDuplicatedVendorClientId.contains(":" + String.valueOf(bidDeductionIndexToRemove.intValue()) + ":")) {
            	bidDeductionVendorsAreUnique = true;
            }
            
            bidDeductionIndexToRemove = null;
        }
    }
    
    public boolean isBidDeductionAddable() {
        if (CollectionUtils.isEmpty(bidDeductions)) {
            return true;
        }
        BidDeductionDto last = bidDeductions.get(bidDeductions.size() - 1);
        if (last != null && bidDeductions.size() < toolsApplicationBean.getBidDeductionsLimit() && bidDeductionVendorsAreUnique) {
            return true;
        }
        return false;
    }
    
    public void changeBidDeductionVendor(AjaxBehaviorEvent event) {
    	bidDeductionVendorsAreUnique = true;
    	if (bidDeductions.size() > 1) {
	    	BidDeductionDto changedBidDeduction = getBidDeductionByIndex(getBidDeductionIndex(event));
	    	String clientId = event.getComponent().getClientId();
	    	
	    	if (changedBidDeduction.getPayerIsByyd()) {
	    		checkVendorUniqueness(clientId, changedBidDeduction.getThirdPartyVendor().getId());
	    	} else {
	    		checkVendorNameUniqueness(clientId, changedBidDeduction.getThirdPartyVendorFreeText());
	    	}
    	}
    }

	public void checkVendorNameUniqueness(String clientId, String changedVendorFreeText) {
		String changedUnifiedName = changedVendorFreeText.toLowerCase().trim();
		int numOfSameVendorNames = 0;
		for (BidDeductionDto bd : bidDeductions) {
			// the vendor free text was set if no Byyd
			if (!bd.getPayerIsByyd() && bd.getThirdPartyVendorFreeText() != null) {
				numOfSameVendorNames += (bd.getThirdPartyVendorFreeText().toLowerCase().trim().equals(changedUnifiedName)) ? 1 : 0; // +1 if same id exists
			}
		}
		if (numOfSameVendorNames > 1) {
			bidDeductionVendorsAreUnique = false;
			bidDeductionDuplicatedVendorClientId = clientId;
			bidDeductionDuplicatedVendorErrorKey = MESSAGE_KEY_VENDOR_FREE_TEXT_UNIQUE_ERROR;
			addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, MESSAGE_KEY_VENDOR_FREE_TEXT_UNIQUE_ERROR);
		}
	}

	public void checkVendorUniqueness(String clientId, Long changedVendorId) {
		int numOfSameVendorIds = 0;
		for (BidDeductionDto bd : bidDeductions) {
			// the vendor was set if Byyd
			if (bd.getPayerIsByyd() && bd.getThirdPartyVendor() != null) {
				numOfSameVendorIds += (bd.getThirdPartyVendor().getId().equals(changedVendorId)) ? 1 : 0; // +1 if same id exists
			}
		}
		if (numOfSameVendorIds > 1) {
			bidDeductionVendorsAreUnique = false;
			bidDeductionDuplicatedVendorClientId = clientId;
			bidDeductionDuplicatedVendorErrorKey = MESSAGE_KEY_VENDOR_UNIQUE_ERROR;
			addFacesMessage(FacesMessage.SEVERITY_ERROR, clientId, null, MESSAGE_KEY_VENDOR_UNIQUE_ERROR);
		}
	}
	
	public List<ThirdPartyVendorDto> getThirdPartyVendorsByTypeId(Long thirdPartyVendorTypeId) {
		return thirdPartyVendorService.getAllThirdPartyVendorsByTypeIds(Collections.singleton(thirdPartyVendorTypeId));
	}
	
    private BidDeductionDto getBidDeductionByIndex(int index) {
        return bidDeductions.get(index);
    }
    
    private int getBidDeductionIndex(FacesEvent event) {
        return ((Integer) event.getComponent().getAttributes().get(Constants.BID_DEDUCTION_INDEX)).intValue();
    }
    
    private ThirdPartyVendorDto prepareNewThirdPartyVendor() {
    	ThirdPartyVendorDto tpv = new ThirdPartyVendorDto();
        tpv.setId(0L);
		return tpv;
	}
    
    public boolean isCPC() {
        return (CPC.equals(campaignDto.getCurrentBid().getBidTypeStr())) ? true : false;
    }

    public boolean isCPM() {
        return (CPM.equals(campaignDto.getCurrentBid().getBidTypeStr())) ? true : false;
    }

    public boolean isTargeted() {
        if (campaignDto == null) {
            return false;
        }
        if (CPC.equals(campaignDto.getCurrentBid().getBidTypeStr())) {
            return (campaignDto.getTargetCPA() != null && campaignDto.getTargetCPA() != BIG_DECIMAL_0) ? true : false;
        } else if (CPM.equals(campaignDto.getCurrentBid().getBidTypeStr())) {
            return (campaignDto.getTargetCTR() != null && campaignDto.getTargetCTR().getTargetCTR() != BIG_DECIMAL_0) ? true : false;
        }
        return false;
    }

    public String getTargetLabel() {
        return (CPC.equals(campaignDto.getCurrentBid().getBidTypeStr())) ?
                FacesUtils.getBundleMessage("page.campaign.confirmation.bidding.cpagoal.label") : FacesUtils.getBundleMessage("page.campaign.confirmation.bidding.ctrgoal.label");
    }

    public String getFrecuencySummary() {
        if (campaignDto != null) {
            Integer capImpressions = campaignDto.getCapImpressions();
            if (capImpressions != null && capImpressions > 0) {
                return campaignDto.getCapImpressions() + " per " + getInterval();
            }
        }
        return FacesUtils.getBundleMessage("page.campaign.bidbudget.nocap.watermark");
    }

    public String getDailyBudgetSummary() {
        if (campaignDto != null) {
            DecimalFormat df = new DecimalFormat("#.00");
            if (CLICK.equals(campaignDto.getBudType()) && campaignDto.getDailyBudgetClicks() != null) {
                return campaignDto.getDailyBudgetClicks().toString() + " clicks";
            } else if (IMPRESSION.equals(campaignDto.getBudType()) && campaignDto.getDailyBudgetImpressions() != null) {
                return campaignDto.getDailyBudgetImpressions().toString() + " impressions";
            } else if (campaignDto.getDailyBudget() != null) {
                return "$" + df.format(campaignDto.getDailyBudget());
            }
        }
        return notSet();
    }

    public String getOverallBudgetSummary() {
        if (campaignDto != null) {
            DecimalFormat df = new DecimalFormat("#.00");
            if (CLICK.equals(campaignDto.getBudType()) && campaignDto.getOverallBudgetClicks() != null) {
                return campaignDto.getOverallBudgetClicks().toString() + " clicks";
            } else if (IMPRESSION.equals(campaignDto.getBudType()) && campaignDto.getOverallBudgetImpressions() != null) {
                return campaignDto.getOverallBudgetImpressions().toString() + " impressions";
            } else if (campaignDto.getOverallBudget() != null) {
                return "$" + df.format(campaignDto.getOverallBudget());
            }
        }
        return notSet();
    }
    
    public String getInvoiceCurrencySummary() {
        String invoiceCurrencySummary = notSet();
        if (campaignDto == null) {
            invoiceCurrencySummary = notSet();
        }else if (campaignDto.getCurrencyExchangeRate()==null){
            invoiceCurrencySummary = getDefaultInvoiceCurrency().getToCurrencyCode();
        }else{
            invoiceCurrencySummary = campaignDto.getCurrencyExchangeRate().getToCurrencyCode();
        }
        return invoiceCurrencySummary;
    }

    @Override
    protected void init() {
        // Empty
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public boolean getRenderWeekDay() {
        return renderWeekDay;
    }

    public void setRenderWeekDay(boolean renderWeekDay) {
        this.renderWeekDay = renderWeekDay;
    }

    public Integer getFrecuency() {
        return frecuency;
    }

    public void setFrecuency(Integer frecuency) {
        this.frecuency = frecuency;
    }

    public String getBidType() {
        return bidType;
    }

    public void setBidType(String bidType) {
        this.bidType = bidType;
    }

    public String getAmountCpc() {
        return amountCpc;
    }

    public void setAmountCpc(String amountCpc) {
        this.amountCpc = amountCpc;
    }

    public String getAmountCpm() {
        return amountCpm;
    }

    public void setAmountCpm(String amountCpm) {
        this.amountCpm = amountCpm;
    }

    public String getAmountCpx() {
        return amountCpx;
    }

    public void setAmountCpx(String amountCpx) {
        this.amountCpx = amountCpx;
    }

    public String getOverallBudget() {
        return overallBudget;
    }

    public void setOverallBudget(String overallBudget) {
        this.overallBudget = overallBudget;
    }

    public boolean isOverallBudgetAlertEnabled() {
        return overallBudgetAlertEnabled;
    }

    public void setOverallBudgetAlertEnabled(boolean overallBudgetAlertEnabled) {
        this.overallBudgetAlertEnabled = overallBudgetAlertEnabled;
    }

    public String getDailyBudget() {
        return this.dailyBudget;
    }

    public void setDailyBudget(String dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public boolean isDailyBudgetAlertEnabled() {
        return dailyBudgetAlertEnabled;
    }

    public void setDailyBudgetAlertEnabled(boolean dailyBudgetAlertEnabled) {
        this.dailyBudgetAlertEnabled = dailyBudgetAlertEnabled;
    }

    public Integer getCapImpressions() {
        return capImpressions;
    }

    public void setCapImpressions(Integer capImpressions) {
        this.capImpressions = capImpressions;
    }

    public Integer getCapPeriodSeconds() {
        return capPeriodSeconds;
    }

    public void setCapPeriodSeconds(Integer capPeriodSeconds) {
        this.capPeriodSeconds = capPeriodSeconds;
    }

    public boolean isCapPerCampaign() {
        return capPerCampaign;
    }

    public void setCapPerCampaign(boolean capPerCampaign) {
        this.capPerCampaign = capPerCampaign;
    }

    public List<SelectItem> getCpxOptions() {
        cpxOptions.clear();
        if (isCpiEnabled()) {
            cpxOptions.add(new SelectItem(CPI, CPI));
        } else if (isCpaEnabled()) {
            cpxOptions.add(new SelectItem(CPA, CPA));
        }
        return cpxOptions;
    }

    public void setCpxOptions(List<SelectItem> cpxOptions) {
        this.cpxOptions = cpxOptions;
    }

    public BigDecimal getTargetCtr() {
        return targetCtr;
    }

    public void setTargetCtr(BigDecimal targetCtr) {
        this.targetCtr = targetCtr;
    }

    public BigDecimal getTargetCpa() {
        return targetCpa;
    }

    public void setTargetCpa(BigDecimal targetCpa) {
        this.targetCpa = targetCpa;
    }

    public String getInterval() {
        if (capPeriodSeconds != null) {
            FrequencyCapPeriod f = FrequencyCapPeriod.getFrequencyCapPeriodBySeconds(capPeriodSeconds);
            if (f != null) {
                LOGGER.debug("No frecuency period found for " + capPeriodSeconds);
                return FacesUtils.getBundleMessage(f.getDescription());
            }
        }
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public FrequencyCapPeriod[] getIntervalList() {
        return FrequencyCapPeriod.values();
    }

    public Double getCampaignDailyBudget() {
        if (campaignDailyBudget == null) {
            campaignDailyBudget = getAccountDailyBudget(accountService);
        }
        return campaignDailyBudget;
    }

    public void setCampaignDailyBudget(Double dailyBudget) {
        this.campaignDailyBudget = dailyBudget;
    }

    public BigDecimal getBidMinCpx() {
        if (campaignDto.isPriceOverridden()) {
            return BigDecimal.valueOf(Constants.MIN_BID);
        } else if (isCpiEnabled()) {
            return BigDecimal.valueOf(Constants.MIN_CPI_BID);
        } else {
            return BigDecimal.valueOf(Constants.MIN_CPA_BID);
        }
    }

    public String getCpxLabel() {
        if (isCpiEnabled()) {
            return FacesUtils.getBundleMessage("page.campaign.bidbudget.cpiprice.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.bidbudget.cpaprice.label");
        }
    }

    public String getCpxTip() {
        if (isCpiEnabled()) {
            return FacesUtils.getBundleMessage("page.campaign.bidbudget.cpiprice.tip");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.bidbudget.cpaprice.tip");
        }
    }

    public boolean isCvrDisplayed() {
        return this.targetCpa == null;
    }

    public boolean isCtrDisplayed() {
        return this.targetCtr == null;
    }

    public void showCvr(/* ActionEvent event */) {
        this.targetCpa = BIG_DECIMAL_0;
    }

    public void showCtr(/* ActionEvent event */) {
        this.targetCtr = BIG_DECIMAL_0;
    }

    public BigDecimal getExternalCVR() {
        return campaignDto.getTargetCPA();
    }

    public BigDecimal getExternalCTR() {
        return campaignDto.getTargetCTR().getTargetCTR().multiply(BIG_DECIMAL_100);
    }

    public List<SelectItem> getlBudgetTypes() {
        if (lBudgetTypes == null) {
            lBudgetTypes = new ArrayList<SelectItem>();
            setListBudgets(null);
            budgetType = MONETARY;
        }
        return lBudgetTypes;
    }

    public void setlBudgetTypes(List<SelectItem> lBudgetTypes) {
        this.lBudgetTypes = lBudgetTypes;
    }

    /** PRIVATE METHODS **/
    private void setListBudgets(String bType) {
        if (bType == null) {
            bType = bidType;
        }
        lBudgetTypes.clear();
        lBudgetTypes.add(new SelectItem(MONETARY, FacesUtils.getBundleMessage("page.campaign.bidbudget.budgettype.monetary")));
        if (bType.equals(BidType.CPC.toString())) {
            lBudgetTypes.add(new SelectItem(CLICK, FacesUtils.getBundleMessage("page.campaign.bidbudget.budgettype.clicks")));
        } else if (bType.equals(BidType.CPM.toString())) {
            lBudgetTypes.add(new SelectItem(IMPRESSION, FacesUtils.getBundleMessage("page.campaign.bidbudget.budgettype.impressions")));
        }
    }

    private CampaignDto prepareBudgets() {
        writeOverallBudget(campaignDto, budgetType, overallBudget);
        writeDailyBudget(campaignDto, budgetType, dailyBudget);
        
        campaignDto.setBudType(budgetType);
        return campaignDto;
    }

    private boolean isCpiEnabled() {
        if (campaignDto.getInstallTrackingEnabled()) {
            if (deviceTargetService.isAndroidOnly(campaignDto) && allCreativesAndroidMarket()) {
                return true;
            } else if (deviceTargetService.isIOSOnly(campaignDto) && allCreativesAppStore()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCpaEnabled() {
        if (campaignDto.getConversionTrackingEnabled()) {
            return true;
        }
        return false;
    }

    private boolean allCreativesAndroidMarket() {
        boolean allAndroid = true;
        for (CreativeDto creative : getCampaignCreativeBean().getCampaignDto().getCreatives()) {
            if (creative.getDestination() != null && !creative.getDestination().getDestinationType().equals(DestinationType.ANDROID)) {
                LOGGER.debug("Creative " + creative.getName() + " not android market");
                allAndroid = false;
                break;
            }
        }
        return allAndroid;
    }

    private boolean allCreativesAppStore() {
        boolean allAppstore = true;
        for (CreativeDto creative : getCampaignCreativeBean().getCampaignDto().getCreatives()) {
            if (creative.getDestination() != null
                    && !creative.getDestination().getDestinationType().equals(DestinationType.IPHONE_APP_STORE)) {
                LOGGER.debug("Creative " + creative.getName() + " not appstore");
                allAppstore = false;
                break;
            }
        }
        return allAppstore;
    }
    
    private CurrencyExchangeRateDto getDefaultInvoiceCurrency() {
        if (this.defaultInvoiceCurrency==null){
            this.defaultInvoiceCurrency = companyService.getAdvertiserDefaultCurrency(getUser().getAdvertiserDto().getId());
        }
        if (this.defaultInvoiceCurrency==null){
            this.defaultInvoiceCurrency = this.currencyExchangeRateService.getDefaultCurrencyExchangeRate();
        }
        return this.defaultInvoiceCurrency;
    }

    private boolean hasExchangeRateBeenChangedByAdmin() {
        boolean hasBeenChanged = false;
        
        // Check if admin user is logged in
        if (isAdminUserLoggedIn()){
            //If the previous invoice currency is the same that selected by the user, we check previous values too
            if (this.oldInvoiceCurrencyId.longValue() == this.selectedInvoiceCurrencyId.longValue()){
                if (this.oldExchangeRate.compareTo(this.exchangeRate)!=0){
                    hasBeenChanged = true;
                }
            }else{  // In other case, we check if Admin has changed the currency exchange value returned by Autofeed
                BigDecimal currencyExchangeRate = this.invoiceCurrenciesMap.get(this.selectedInvoiceCurrencyId).getCurrentExchangeRate();
                if (currencyExchangeRate.compareTo(this.exchangeRate)!=0){
                    hasBeenChanged = true;
                }
            }
        }
        return hasBeenChanged;
    }

    public String getEvenDistributionOverallBudget() {
        return evenDistributionOverallBudget;
    }

    public boolean isEvenDistributionDailyBudget() {
        return evenDistributionDailyBudget;
    }

    public void setEvenDistributionOverallBudget(String evenDistributionOverallBudget) {
        this.evenDistributionOverallBudget = evenDistributionOverallBudget;
    }

    public void setEvenDistributionDailyBudget(boolean evenDistributionDailyBudget) {
        this.evenDistributionDailyBudget = evenDistributionDailyBudget;
    }

    // You can't set daily even distribution if there is a campaign/account
    // distribution
    public void onEvenDistributionOverallBudgetChangeEvent(ValueChangeEvent e) {
        LOGGER.debug("Even Distribution Overall Budget changed");
        String value = (String) e.getNewValue();

        if (!value.equals(OVERALL_DISTRIBUTION_ASAP)) {
            if (!isNoCap(this.dailyBudget)) {
                this.dailyBudget = "";
            }
            this.evenDistributionDailyBudget = false;
            this.dailyBudgetAlertEnabled = false;
        }
    }

    public BigDecimal getFixedMargin() {
        return fixedMargin;
    }

    public void setFixedMargin(BigDecimal fixedMargin) {
        // track fixedmargin changes
        if (((this.fixedMargin!=null) && (fixedMargin==null)) ||
            ((this.fixedMargin==null) && (fixedMargin!=null)) ||
            ((this.fixedMargin!=null) && (fixedMargin!=null) && (this.fixedMargin.compareTo(fixedMargin)!=0))){
            this.fixedMarginChange = true;
        }
        this.fixedMargin = fixedMargin;
    }

    public BigDecimal getAdServingCpmFee() {
        return adServingCpmFee;
    }

    public void setAdServingCpmFee(BigDecimal adServingCpmFee) {
        this.adServingCpmFee = adServingCpmFee;
    }

    public String getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }

    public BigDecimal getAgencyDiscount() {
        return agencyDiscount;
    }

    public void setAgencyDiscount(BigDecimal agencyDiscount) {
        this.agencyDiscount = agencyDiscount;
    }
    
    public boolean getMediaCostOptimisationEnabled() {
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

        if (campaignDto!=null){
            // if monetary budget is selected, mediacost optimisation checkbox is enabled too
            checkboxEnabled = budgetType.equals(MONETARY);
        }
        return checkboxEnabled;
    }

    public Integer getBidDeductionIndexToRemove() {
		return bidDeductionIndexToRemove;
	}

	public void setBidDeductionIndexToRemove(Integer bidDeductionIndexToRemove) {
		this.bidDeductionIndexToRemove = bidDeductionIndexToRemove;
	}

	public List<ThirdPartyVendorTypeDto> getThirdPartyVendorTypes() {
        if (thirdPartyVendorTypes == null) {
        	thirdPartyVendorTypes = thirdPartyVendorTypeService.getAllThirdPartyVendorTypes();
        }
        return thirdPartyVendorTypes;
    }

    public void setThirdPartyVendorTypes(List<ThirdPartyVendorTypeDto> thirdPartyVendorTypes) {
        this.thirdPartyVendorTypes = thirdPartyVendorTypes;
    }
    
	public List<BidDeductionDto> getBidDeductions() {
		return bidDeductions;
	}

	public void setBidDeductions(List<BidDeductionDto> bidDeductions) {
		this.bidDeductions = bidDeductions;
	}
	
	private void getAllInvoiceCurrenciesAsOrderedMap() {
        List<CurrencyExchangeRateDto> currencyExchangeRateDtos = this.currencyExchangeRateService.getAllCurrencyExchangeRate();
        if (currencyExchangeRateDtos!=null){
            this.invoiceCurrenciesMap = new LinkedHashMap<Long, CurrencyExchangeRateDto>(currencyExchangeRateDtos.size());
            for(CurrencyExchangeRateDto currencyExchangeRateDto : currencyExchangeRateDtos){
                this.invoiceCurrenciesMap.put(currencyExchangeRateDto.getId(), currencyExchangeRateDto);
            }
        } 
    }
    
    public List<Long> getInvoiceCurrencyIds(){
        return new ArrayList<Long>(invoiceCurrenciesMap.keySet());
    }

    public CurrencyExchangeRateDto getInvoiceCurrency(Long id) {
        return this.invoiceCurrenciesMap.get(id);
    }

    public Long getSelectedInvoiceCurrencyId() {
        return this.selectedInvoiceCurrencyId;
    }

    public void setSelectedInvoiceCurrencyId(Long selectedInvoiceCurrencyId) {
        this.selectedInvoiceCurrencyId = selectedInvoiceCurrencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate.setScale(6);
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
    public void updateExchangeRate(){
        this.exchangeRate = this.invoiceCurrenciesMap.get(selectedInvoiceCurrencyId).getCurrentExchangeRate();
    }

    public boolean isExchangeRateAdminChange() {
        return exchangeRateAdminChange;
    }
    
    public boolean isCampaignNotLaunched(){
        return CAMPAIGN_NOT_LAUNCHED_STATUSES.contains(this.campaignDto.getStatus());
    }
    
    // Account fixed margin [MAD-3348]
    public boolean enableFixedMarginPlaceholder(){
        return ((accountFixedMarginDto!=null) && 
                ( (campaignDto.getCurrentTradingDeskMargin()==null) || ((campaignDto.getCurrentTradingDeskMargin()!=null)&&(campaignDto.getCurrentTradingDeskMargin().getTradingDeskMargin().compareTo(accountFixedMarginDto.getMargin())==0))));
    }
}
