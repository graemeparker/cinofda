package com.adfonic.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.BidSeat.BidSeatType;
import com.adfonic.domain.Company;
import com.adfonic.domain.Company.AdvertiserCategory;
import com.adfonic.domain.CompanyDirectCost;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.domain.Role;
import com.adfonic.domain.TargetPublisher;
import com.adfonic.domain.User;
import com.adfonic.util.DateUtils;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class AccountSettingsBean extends BaseBean {
    
    private static final String POSTPAID_OPTION = "POSTPAID";
    private static final String PREPAID_OPTION = "PREPAID";
    private static final int DEFAULT_POSTPAY_TERM_DAYS = 14;
    private static final String REQUEST_FLAG_DID_UPDATE = "didUpdate";

    private static final String FORM_ID                     = "mainForm";
    private static final String MARGIN_ID                   = FORM_ID + ":margin";
    private static final String PAYMENT_OPTION_ID           = FORM_ID + ":paymentOption";
    private static final String POST_PAY_ACTIVATION_DATE_ID = FORM_ID + ":postPayActivationDate";
    private static final String ACCOUNT_FIXED_MARGIN_ID     = FORM_ID + ":accountFixedMargin";
    private static final String ACCOUNT_SEATS_ID            = FORM_ID + ":AgencyLevelSeatID";
    
    private static final String POST_PAY_DATE_INVALID_KEY     = "error.paymentOptions.postPayActivationDateInvalid";
    private static final String MARGINS_ZERO_KEY              = "error.paymentOptions.margin.required";
    private static final String PAYMENT_OPTIONS_REQUIRED_KEY  = "error.paymentOptions.required";
    private static final String ACCOUNT_FIXED_MARGIN_ZERO_KEY = "error.paymentOptions.accountfixedmargin.required";
    private static final String ACCOUNT_SEATS_NOT_INVALID_KEY = "error.seatids.notvalid.required";
    
    
	@ManagedProperty(value = "#{adminAccountBean}")
    AdminAccountBean adminAccountBean;
 
	private User user;
    private Company company;
    private Set<Role> roles = new HashSet<Role>();
    
    private boolean readOnly = false;
    private boolean editAgencyDiscount = false;
    private boolean showAgencyDiscount = false;
    private boolean thirdPartyTags = false;
    private boolean thirdPartyImpressionTrackers = false;
    private boolean betaTest = false;
    private boolean fixedMargin = false;
    private boolean adServingCPMFee = false;
    
    private Role dspReadOnlyRole;
    private Role editAgencyDiscountRole;
    private Role showAgencyDiscountRole;
    private Role thirdPartyTagsRole;
    private Role thirdPartyImpressionTrackersRole;
    private Role prepayRole;
    private Role betaTestRole;
    private Role fixedMarginRole;
    private Role adServingCPMFeeRole;
    
    private Set<OptimisationReportFields> optimisationReportFields = new HashSet<>();
    private BigDecimal margin;          // called Tech Fee in UI
    private BigDecimal companyDiscount = BigDecimal.ZERO; // called Default Agency Discount in UI
    private BigDecimal marginShareDsp;  // called Margin share to Adfonic
    private BigDecimal directCost = BigDecimal.ZERO;
    private BigDecimal oldDirectCost = directCost;
    private BigDecimal creditLimit;
    private Date postPayActivationDate;
    private Integer postPayTermDays;
    private boolean hasCompanySpend;
    private BigDecimal accountFixedMargin; 
        
    private String paymentOption;
    
    private boolean agencyLevelSeatID;
    private Map<Long, String> oldSeatIds = new HashMap<>();
    private List<BidSeat> bidSeats;
    private List<BidSeat> selectedBidSeats;
    private String seatIdInUseDialogMessage = "";
    
    private Long defaultCurrencyId;
    private Map<Long, CurrencyExchangeRate> defaultCurrenciesMap;

    public AccountSettingsBean(){}

    @PostConstruct
    public void init() {
    	if (adminAccountBean == null || adminAccountBean.getUser() == null) {
    	    // this shouldn't happen as the view triggers a check
    		throw new AdminGeneralException("no user loaded");
    	}
    	this.user = adminAccountBean.getUser();
    	
        TransactionalRunner runner = getTransactionalRunner();
        runner.runTransactional(
                    new Runnable() {
                        public void run() {
                            load();
                        }
                    }
                );
        getReferenceRoles();
        initControls();
        getAllCurrenciesAsOrderedMap();
        
        if (postPayTermDays == null) {
            postPayTermDays = DEFAULT_POSTPAY_TERM_DAYS;
        }
    }

    private void initControls() {
        readOnly = (roles.contains(dspReadOnlyRole) ? true : false);
        editAgencyDiscount = (roles.contains(editAgencyDiscountRole) ? true: false);
        showAgencyDiscount = (roles.contains(showAgencyDiscountRole) ? true: false);
        thirdPartyTags = (roles.contains(thirdPartyTagsRole) ? true : false);
        thirdPartyImpressionTrackers = (roles.contains(thirdPartyImpressionTrackersRole) ? true : false);
        paymentOption = (roles.contains(prepayRole) ? PREPAID_OPTION : POSTPAID_OPTION);
        betaTest = (roles.contains(betaTestRole) ? true : false);
        fixedMargin = (roles.contains(fixedMarginRole) ? true : false);
        adServingCPMFee = (roles.contains(adServingCPMFeeRole) ? true : false);
    }
    
    private void getReferenceRoles() {
        dspReadOnlyRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_DSP_READ_ONLY);
        editAgencyDiscountRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_EDIT_AGENCY_DISCOUNT);
        showAgencyDiscountRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_SHOW_AGENCY_DISCOUNT);
        thirdPartyTagsRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_THIRD_PARTY_TAGS);
        thirdPartyImpressionTrackersRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_THIRD_PARTY_IMPR_TRACKERS);
        prepayRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_PREPAY);
        betaTestRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_BETA_TEST);
        fixedMarginRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_FIXED_MARGIN);
        adServingCPMFeeRole = getUserManager().getRoleByName(Role.COMPANY_ROLE_AD_SERVING_CPM_FEE);
    }
    
    private void getAllCurrenciesAsOrderedMap() {
        List<CurrencyExchangeRate> currencyExchangeRates = getCommonManager().getAllCurrencyExchangeRate();
        if (currencyExchangeRates!=null){
            this.defaultCurrenciesMap = new LinkedHashMap<Long, CurrencyExchangeRate>(currencyExchangeRates.size());
            for(CurrencyExchangeRate currencyExchangeRate : currencyExchangeRates){
                this.defaultCurrenciesMap.put(currencyExchangeRate.getId(), currencyExchangeRate);
            }
        } 
    }
    
    public void autogenerateSeatIds(){
        if (CollectionUtils.isNotEmpty(this.selectedBidSeats)){
            getPublisherManager().generateBidSeats(this.selectedBidSeats);
        }
    }
    
    public void doChecks(){
        //Checks byydId
        List<String> seatsInUse = new ArrayList<>();

        for(BidSeat bidSeat : this.bidSeats){
            String oldSeatId = this.oldSeatIds.get(bidSeat.getTargetPublisher().getId());
            if ((oldSeatId!=null && !oldSeatId.equals(bidSeat.getSeatId())) ||
                (oldSeatId==null && StringUtils.isNotBlank(bidSeat.getSeatId()))){
                boolean isBidSeatAvailable = getPublisherManager().isBidSeatAvailabe(bidSeat);
                if (!isBidSeatAvailable){
                    seatsInUse.add(bidSeat.getTargetPublisher().getName());
                }
            }
        }
        
        if (seatsInUse.isEmpty()){
            doSave();
        }else{
            this.seatIdInUseDialogMessage = seatsInUse.toString();
            RequestContext.getCurrentInstance().execute("confirmSeatIdsInUse.show()");
        }
    }
    
    public void doSave() {
        // At least 1 one of Margin Share to Adfonic (currentMarginShareDSP)
        // or Tech Fee (currentMediaCostMargin) need to be greater than zero 
        // (otherwise Adfonic doesn't make any money!).        
        if (margin == null || marginShareDsp == null || 
                (margin.compareTo(BigDecimal.ZERO) <= 0 && marginShareDsp.compareTo(BigDecimal.ZERO) <= 0)) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(MARGIN_ID, messageForId(MARGINS_ZERO_KEY));
            return;
        }
        
        if ((accountFixedMargin==null)&&(isMustHaveAccountFixedMargin())){
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage(ACCOUNT_FIXED_MARGIN_ID, messageForId(ACCOUNT_FIXED_MARGIN_ZERO_KEY));
            return;
        }
        
        if (this.paymentOption.equals(POSTPAID_OPTION)) {
            FacesContext fc = FacesContext.getCurrentInstance();
            
            // validate required post-pay fields
            if (creditLimit == null || postPayActivationDate == null || postPayTermDays == null) {
                fc.addMessage(PAYMENT_OPTION_ID, messageForId(PAYMENT_OPTIONS_REQUIRED_KEY));
                return;
            }
            else {
                // we have post pay date, make sure it is start of day
                postPayActivationDate = DateUtils.getStartOfDay(postPayActivationDate, DEFAULT_TZ);
            }
            
            //  new/modified dates must not be earlier than next start of tomorrow if the company has spend
            if (company.getPostPayActivationDate() == null ||
                    !DateUtils.getStartOfDay(company.getPostPayActivationDate(), DEFAULT_TZ).equals(postPayActivationDate)) {
                if (hasCompanySpend) {
                    if (postPayActivationDate.before(DateUtils.getStartOfDayTomorrow(DateUtils.now(), DEFAULT_TZ))) {
                        fc.addMessage(POST_PAY_ACTIVATION_DATE_ID, messageForId(POST_PAY_DATE_INVALID_KEY));
                        return;
                    }
                }
            }
            // remove prepay role
            roles.remove(prepayRole);
        }
        else {
            creditLimit = null;
            postPayTermDays = null;
            postPayActivationDate = null;
            
            // add prepay role
            roles.add(prepayRole);
        }

        if (readOnly) {
            roles.add(dspReadOnlyRole);
        }
        else {
            roles.remove(dspReadOnlyRole);
        }
        if (editAgencyDiscount) {
            roles.add(editAgencyDiscountRole);
        } 
        else {
            roles.remove(editAgencyDiscountRole);
        }
        if (showAgencyDiscount) {
            roles.add(showAgencyDiscountRole);
        } 
        else {
            roles.remove(showAgencyDiscountRole);
        }
        if (thirdPartyTags) {
            roles.add(thirdPartyTagsRole);
        }
        else {
            roles.remove(thirdPartyTagsRole);
        }
        if (thirdPartyImpressionTrackers) {
            roles.add(thirdPartyImpressionTrackersRole);
        }
        else {
            roles.remove(thirdPartyImpressionTrackersRole);
        }
        if (betaTest) {
            roles.add(betaTestRole);
        }
        else {
            roles.remove(betaTestRole);
        }
        if (fixedMargin){
        	roles.add(fixedMarginRole);
        }else{
        	roles.remove(fixedMarginRole);
        }
        if (adServingCPMFee){
        	roles.add(adServingCPMFeeRole);
        }else{
        	roles.remove(adServingCPMFeeRole);
        }
        
        // MAD-3386 - Enable passing a separate agency seat - phase 2
        // BidSSeat validation
        List<String> invalidTargetPublisherNames = new ArrayList<String>();
        for(BidSeat bidSeat : bidSeats){
            String seatIdRegex = bidSeat.getTargetPublisher().getRtbSeatIdRegEx();
            if (seatIdRegex!=null && StringUtils.isNotEmpty(bidSeat.getSeatId())){
                Pattern p = Pattern.compile(seatIdRegex);
                Matcher m = p.matcher(bidSeat.getSeatId());
                if (!m.matches()){
                    invalidTargetPublisherNames.add(bidSeat.getTargetPublisher().getName());
                }
            }
        }
        
        if (!invalidTargetPublisherNames.isEmpty()){
            FacesContext.getCurrentInstance().addMessage(ACCOUNT_SEATS_ID, messageForId(ACCOUNT_SEATS_NOT_INVALID_KEY, invalidTargetPublisherNames.toString()));
            return;
        }
        
        try {
            TransactionalRunner runner = getTransactionalRunner();
            company = runner.callTransactional(
                        new Callable<Company>() {
                            public Company call() throws Exception {
                                return updateCompany(
                                        company,
                                        roles,
                                        margin,
                                        creditLimit,
                                        postPayActivationDate,
                                        postPayTermDays,
                                        companyDiscount,
                                        marginShareDsp,
                                        bidSeats,
                                        accountFixedMargin);
                            }
                        }
                    );
            // re-load roles and update form
            roles.clear();
            roles.addAll(company.getRoles());
            initControls();
            
            // Reload bidseats
            this.bidSeats = loadBidSeats();
            
            setRequestFlag(REQUEST_FLAG_DID_UPDATE);
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Error saving account settings for company item id=" +
                    company.getId(),
                    e);
        }            
    }
    
    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }
    
    public Set<OptimisationReportFields> getOptimisationReportFields() {
		return optimisationReportFields;
	}

	public void setOptimisationReportFields(
			Set<OptimisationReportFields> optimisationReportFields) {
		this.optimisationReportFields = optimisationReportFields;
	}

	//------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        company = getCompanyManager().getCompanyById(user.getCompany().getId());
        roles.addAll(company.getRoles());
        if (company.getCurrentMediaCostMargin() != null) {
            margin = company.getCurrentMediaCostMargin().getMediaCostMargin();
        }
        if (company.getCurrentMarginShareDSP() != null) {
            marginShareDsp = company.getCurrentMarginShareDSP().getMargin();
        }
        CompanyDirectCost companyDirectCost = company.getCompanyDirectCost();
        if (companyDirectCost != null) {
        	oldDirectCost = directCost = companyDirectCost.getDirectCost();
        }
        companyDiscount = company.getDiscount();
        postPayActivationDate = user.getCompany().getPostPayActivationDate();
        creditLimit = user.getCompany().getCreditLimit();
        postPayTermDays = user.getCompany().getPostPayTermDays();
        hasCompanySpend = getCompanyManager().hasSpendForCompany(user.getCompany());
        agencyLevelSeatID = company.getEnableRtbBidSeat();
        if (company.getCurrentAccountFixedMargin()!=null){
            accountFixedMargin = company.getCurrentAccountFixedMargin().getMargin();
        }
        
        OptimisationReportCompanyPreferences prefs = getCompanyManager().getOptimisationReportCompanyPreferencesForCompany(company);
        if(prefs != null) {
        	this.optimisationReportFields.addAll(prefs.getReportFields());
        }
        
        // MAD-3168 - Loading bidseats for campaign
        this.bidSeats = loadBidSeats();
        
        // MAD-3303 - Default currency per client
        this.defaultCurrencyId = company.getDefaultCurrencyExchangeRate().getId();
    }
    
    private List<BidSeat> loadBidSeats() {
        List<TargetPublisher> seatIdsTargetPublishers = getPublisherManager().getAllTargetPublishers(new TargetPublisherFilter().setRtbSeatIdAvailable(true));
        Set<BidSeat> companyRtbBidSeats = company.getCompanyRtbBidSeats();
        List<BidSeat> persistedbidSeats = new ArrayList<BidSeat>(seatIdsTargetPublishers.size());
        this.oldSeatIds.clear();
        for(TargetPublisher targetPublisher : seatIdsTargetPublishers){
            BidSeat bidSeat = getBidSeatFromCompany(companyRtbBidSeats, targetPublisher);
            if (bidSeat==null){
                bidSeat = new BidSeat(null, null, BidSeatType.COMPANY, targetPublisher);
            }else{
                oldSeatIds.put(bidSeat.getTargetPublisher().getId(), bidSeat.getSeatId());
            }
            persistedbidSeats.add(bidSeat);
        }
        return persistedbidSeats;
    }

    private BidSeat getBidSeatFromCompany(Set<BidSeat> companyRtbBidSeats, TargetPublisher targetPublisher) {
        for (BidSeat companyBidSeat : companyRtbBidSeats){
            if (companyBidSeat.getTargetPublisher().getId() == targetPublisher.getId()){
                return companyBidSeat;
            }
        }
        return null;
    }

    public Company updateCompany(
            Company company,
            Set<Role> newRoles,
            BigDecimal margin,
            BigDecimal creditLimit,
            Date postPayActivationDate,
            Integer postPayTermDays,
            BigDecimal companyDiscount,
            BigDecimal marginShareDsp,
            List<BidSeat> bidSeats,
            BigDecimal accountFixedMargin) {
        company.getRoles().clear();
        company.getRoles().addAll(newRoles);
        if (companyDiscount.compareTo(company.getDiscount()) != 0) {
            company.setDiscount(companyDiscount);
        }
        company.setCreditLimit(creditLimit);
        company.setPostPayActivationDate(postPayActivationDate);
        company.setPostPayTermDays(postPayTermDays);
        
        // Direct cost changed
        if (directCost != null && oldDirectCost.compareTo(directCost) != 0) {
        	company.setCompanyDirectCost(getCompanyManager().newCompanyDirectCost(company, directCost));
	        oldDirectCost = directCost;
        }
        company.setEnableRtbBidSeat(this.agencyLevelSeatID);
        
        // MAD-3303 - Default currency per client
        company.setDefaultCurrencyExchangeRate(this.defaultCurrenciesMap.get(this.defaultCurrencyId));
        company = getCompanyManager().update(company);

        boolean newMargin = false;
        if (company.getCurrentMediaCostMargin() == null) {
            newMargin = true;
        }
        else {
            // did existing change
            if (margin.compareTo(company.getCurrentMediaCostMargin().getMediaCostMargin()) != 0) {
                newMargin = true;
            }
        }
        if (newMargin) {
            company = getCompanyManager().newAdvertiserMediaCostMargin(company, margin);
            company = getCompanyManager().update(company);
        }

        boolean newMarginShareDsp = false;
        if (company.getCurrentMarginShareDSP() == null) {
            newMarginShareDsp = true;
        }else {
            // did existing change
            if (marginShareDsp.compareTo(company.getCurrentMarginShareDSP().getMargin()) != 0) {
                newMarginShareDsp = true;
            }
        }
        if (newMarginShareDsp) {
            company = getCompanyManager().newMarginShareDSP(company,  marginShareDsp);
            company = getCompanyManager().update(company);
        }
        
        // MAD-3348 - Account Minimum Fixed Margin
        if ((company.getCurrentAccountFixedMargin() == null) || (accountFixedMargin.compareTo(company.getCurrentAccountFixedMargin().getMargin()) != 0)) {
            company = getCompanyManager().newAccountFixedMargin(company,  accountFixedMargin);
        }
        
        OptimisationReportCompanyPreferences prefs = getCompanyManager().getOptimisationReportCompanyPreferencesForCompany(company);
        if(prefs == null) {
        	prefs = getCompanyManager().newOptimisationReportCompanyPreferences(company, optimisationReportFields);
        } else {
        	prefs.setReportFields(optimisationReportFields);
        	prefs = getCompanyManager().update(prefs);
        }
        
        // MAD-3168 - Updating bid seats
        Set<BidSeat> companyRtbBidSeats = new HashSet<>();
        for(BidSeat bidSeat : this.bidSeats){
            if(StringUtils.isNotEmpty(bidSeat.getSeatId()) ){
                companyRtbBidSeats.add(bidSeat);
            }
        }
        company = getCompanyManager().updateBidSeats(company.getId(), companyRtbBidSeats);
        
        return company;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isEditAgencyDiscount() {
        return editAgencyDiscount;
    }

    public void setEditAgencyDiscount(boolean editAgencyDiscount) {
        this.editAgencyDiscount = editAgencyDiscount;
    }

	public boolean isShowAgencyDiscount() {
		return showAgencyDiscount;
	}

	public void setShowAgencyDiscount(boolean showAgencyDiscount) {
		this.showAgencyDiscount = showAgencyDiscount;
	}

	public boolean isThirdPartyTags() {
        return thirdPartyTags;
    }

    public void setThirdPartyTags(boolean thirdPartyTags) {
        this.thirdPartyTags = thirdPartyTags;
    }

    public boolean isThirdPartyImpressionTrackers() {
		return thirdPartyImpressionTrackers;
	}

	public void setThirdPartyImpressionTrackers(boolean thirdPartyImpressionTrackers) {
		this.thirdPartyImpressionTrackers = thirdPartyImpressionTrackers;
	}

	public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getCompanyDiscount() {
        return companyDiscount;
    }

    public void setCompanyDiscount(BigDecimal companyDiscount) {
        this.companyDiscount = companyDiscount;
    }
    
    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }
    public Date getPostPayActivationDate() {
        return postPayActivationDate;
    }

    public void setPostPayActivationDate(Date postPayActivationDate) {
        this.postPayActivationDate = postPayActivationDate;
    }

    public Integer getPostPayTermDays() {
        return postPayTermDays;
    }

    public void setPostPayTermDays(Integer postPayTermDays) {
        this.postPayTermDays = postPayTermDays;
    }
    
    public boolean isHasCompanySpend() {
        return hasCompanySpend;
    }

    public void setHasCompanySpend(boolean hasCompanySpend) {
        this.hasCompanySpend = hasCompanySpend;
    }
    
    public Date getStartOfDayTomorrow() {
        return DateUtils.getStartOfDayTomorrow(DateUtils.now(), DEFAULT_TZ);
    }    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getMarginShareDsp() {
        return marginShareDsp;
    }

    public void setMarginShareDsp(BigDecimal marginShareDsp) {
        this.marginShareDsp = marginShareDsp;
    }
    
    public boolean isBetaTest() {
        return betaTest;
    }

    public void setBetaTest(boolean betaTest) {
        this.betaTest = betaTest;
    }
    
    public boolean isFixedMargin() {
        return fixedMargin;
    }

    public void setFixedMargin(boolean fixedMargin) {
        this.fixedMargin = fixedMargin;
    }

	public boolean isAdServingCPMFee() {
		return adServingCPMFee;
	}

	public void setAdServingCPMFee(boolean adServingCPMFee) {
		this.adServingCPMFee = adServingCPMFee;
	}

	public BigDecimal getDirectCost() {
		return directCost;
	}

	public void setDirectCost(BigDecimal directCost) {
		this.directCost = directCost;
	}

    public boolean isAgencyLevelSeatID() {
        return agencyLevelSeatID;
    }

    public void setAgencyLevelSeatID(boolean agencyLevelSeatID) {
        this.agencyLevelSeatID = agencyLevelSeatID;
    }

    public List<BidSeat> getBidSeats() {
        return bidSeats;
    }

    public void setBidSeats(List<BidSeat> bidSeats) {
        this.bidSeats = bidSeats;
    }

    public BigDecimal getAccountFixedMargin() {
        return accountFixedMargin;
    }

    public void setAccountFixedMargin(BigDecimal accountFixedMargin) {
        this.accountFixedMargin = accountFixedMargin;
    }

    public boolean isMustHaveAccountFixedMargin() {
        return (((company.getAdvertiserCategory()!=AdvertiserCategory.EXCHANGE) && 
                (company.getAdvertiserCategory()!=AdvertiserCategory.MADISON_LICENCE) &&
                (company.getAccountTypeFlags()!=AccountType.PUBLISHER.bitValue())) ||
                (company.getAdvertiserCategory()==AdvertiserCategory.MADISON_LICENCE && this.marginShareDsp.compareTo(new BigDecimal(0))==1));
    }

    public List<BidSeat> getSelectedBidSeats() {
        return selectedBidSeats;
    }

    public void setSelectedBidSeats(List<BidSeat> selectedBidSeats) {
        this.selectedBidSeats = selectedBidSeats;
    }

    public String getSeatIdInUseDialogMessage() {
        return seatIdInUseDialogMessage;
    }

    public Long getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    public void setDefaultCurrencyId(Long defaultCurrencyId) {
        this.defaultCurrencyId = defaultCurrencyId;
    }
    
    public List<Long> getDefaultCurrencyIds(){
        return new ArrayList<Long>(this.defaultCurrenciesMap.keySet());
    }
    
    public CurrencyExchangeRate getCurrency(Long id) {
        return this.defaultCurrenciesMap.get(id);
    }
    
}