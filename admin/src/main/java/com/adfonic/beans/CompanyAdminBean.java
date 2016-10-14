package com.adfonic.beans;

import java.util.ArrayList;
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
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.BidSeat;
import com.adfonic.domain.BidSeat.BidSeatType;
import com.adfonic.domain.CurrencyExchangeRate;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.TargetPublisher;
import com.adfonic.domain.User;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.presentation.company.CompanyService;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;
import com.byyd.middleware.utils.TransactionalRunner;

@ManagedBean
@ViewScoped
public class CompanyAdminBean extends BaseBean {
    
    private static final String FORM_ID          = "companyForm";
    private static final String ADVERTISER_SEATS_ID = FORM_ID + ":advertiserLevelSeatID";
    
    private static final String ADVERTISER_SEATS_NOT_INVALID_KEY = "error.seatids.notvalid.required";

    private List<SelectItem> companyAdvertisers;
    private Advertiser advertiser;
    private List<SelectItem> adfonicUsers;
    private Publisher publisher;
    private String developerKey;
    
    private Map<Long, String> oldSeatIds = new HashMap<>();
    private List<BidSeat> bidSeats;
    private List<BidSeat> selectedBidSeats;
    private String seatIdInUseDialogMessage = "";
    
    private Long defaultCurrencyId;
    private Map<Long, CurrencyExchangeRate> defaultCurrenciesMap;

    private User user;

    @ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;

    @PostConstruct
    private void init() {
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
        
        getAllCurrenciesAsOrderedMap();
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

    public List<SelectItem> getAdfonicUsers() {
        return this.adfonicUsers;
    }

    public List<SelectItem> getCompanyAdvertisers() {
        return companyAdvertisers;
    }

    public void doRegenerateDeveloperKey() {
        setDeveloperKey(getUserManager().newDeveloperKey());
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(Advertiser advertiser) {
        this.advertiser = advertiser;
        if (advertiser!=null){
            // MAD-3168 - Loading bidseats for advertiser
            this.bidSeats = loadBidSeats();
            
            // MAD-3303 - Default currency per client
            loadDefaultCurrency();
        }
    }

    private List<BidSeat> loadBidSeats() {
        List<TargetPublisher> seatIdsTargetPublishers = getPublisherManager().getAllTargetPublishers(new TargetPublisherFilter().setRtbSeatIdAvailable(true));
        Set<BidSeat> advertiserRtbBidSeats = getAdvertiserManager().getAdvertiserRtbBidSeats(advertiser.getId());
        List<BidSeat> persistedbidSeats = new ArrayList<BidSeat>(seatIdsTargetPublishers.size());
        this.oldSeatIds.clear();
        for(TargetPublisher targetPublisher : seatIdsTargetPublishers){
            BidSeat bidSeat = getBidSeatFromAdvertiser(advertiserRtbBidSeats, targetPublisher);
            if (bidSeat==null){
                bidSeat = new BidSeat(null, null, BidSeatType.ADVERTISER, targetPublisher);
            }else{
                oldSeatIds.put(bidSeat.getTargetPublisher().getId(), bidSeat.getSeatId());
            }
            persistedbidSeats.add(bidSeat);
        }
        return persistedbidSeats;
    }

    private BidSeat getBidSeatFromAdvertiser(Set<BidSeat> advetiserRtbBidSeats, TargetPublisher targetPublisher) {
        for (BidSeat advetiserRtbBidSeat : advetiserRtbBidSeats){
            if (advetiserRtbBidSeat.getTargetPublisher().getId() == targetPublisher.getId()){
                return advetiserRtbBidSeat;
            }
        }
        return null;
    }
    
    private void loadDefaultCurrency() {
        CurrencyExchangeRate advertiserDefaultCurrencyExchangeRate = this.advertiser.getDefaultCurrencyExchangeRate();
        if (advertiserDefaultCurrencyExchangeRate==null){
            this.defaultCurrencyId = 0L;  // default value
        }else{
            this.defaultCurrencyId = advertiserDefaultCurrencyExchangeRate.getId();
        }
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public String getDeveloperKey() {
        return developerKey;
    }

    public void setDeveloperKey(String developerKey) {
        this.developerKey = developerKey;
    }

    public AdminAccountBean getAdminAccountBean() {
        return adminAccountBean;
    }

    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }

    public List<BidSeat> getBidSeats() {
        return bidSeats;
    }

    public void setBidSeats(List<BidSeat> bidSeats) {
        this.bidSeats = bidSeats;
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
            doSaveCompanyAdmin();
        }else{
            this.seatIdInUseDialogMessage = seatsInUse.toString();
            RequestContext.getCurrentInstance().execute("confirmSeatIdsInUse.show()");
        }
    }

    public void doSaveCompanyAdmin(){
        
        
        if(!validateForm()){
            return;
        }

        try {
            TransactionalRunner runner = getTransactionalRunner();
                  runner.callTransactional(
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            return updateCompanyAdmin(
                                    advertiser,
                                    publisher,
                                    developerKey
                                    );
                        }
                    }
            );
            
            // Reload bidseats
            this.bidSeats = loadBidSeats();
            
            setRequestFlag("didUpdateCompanyAdmin");
        } catch (Exception e) {
            CompanyService companyService = AdfonicBeanDispatcher.getBean(CompanyService.class);
            CompanyDto companyDto = companyService.getCompanyForAdvertiser(advertiser.getId());
            logger.log(
                    Level.SEVERE,
                    "Error saving company admin for company id=" +
                            companyDto.getId(),
                    e);
        }
    }

    private boolean validateForm() {
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
            FacesContext.getCurrentInstance().addMessage(ADVERTISER_SEATS_ID, messageForId(ADVERTISER_SEATS_NOT_INVALID_KEY, invalidTargetPublisherNames.toString()));
            return false;
        }
        
        return true;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        companyAdvertisers = loadCompanyAdvertisers();
        adfonicUsers = loadAdfonicUsers();
        publisher = getPublisherManager().getPublisherById(user.getCompany().getPublisher().getId());
        developerKey = user.getDeveloperKey();
    }

    public Void updateCompanyAdmin(Advertiser advertiser, Publisher publisher, String developerKey) {
        // MAD-3303 - Default currency per client
        CurrencyExchangeRate selectedCurrency = null;
        if (this.defaultCurrencyId!=0L){
            selectedCurrency = getCommonManager().getCurrencyExchangeRateById(this.defaultCurrencyId);
        }
        advertiser.setDefaultCurrencyExchangeRate(selectedCurrency);
        
        advertiser = getAdvertiserManager().update(advertiser);
        publisher = getPublisherManager().update(publisher);
        user.setDeveloperKey(developerKey);
        user = getUserManager().update(user);
        setRequestFlag("didUpdate");
        
        // MAD-3168 - Updating bid seats
        Set<BidSeat> advertiserRtbBidSeats = new HashSet<>();
        for(BidSeat bidSeat : this.bidSeats){
            if(StringUtils.isNotEmpty(bidSeat.getSeatId()) ){
                advertiserRtbBidSeats.add(bidSeat);
            }
        }
        advertiser = getAdvertiserManager().updateBidSeats(advertiser.getId(), advertiserRtbBidSeats);
        
        return null;
    }

    public List<SelectItem> loadCompanyAdvertisers() {
        if (companyAdvertisers == null) {
            List<SelectItem> items = new ArrayList<SelectItem>();
            for (Advertiser a : getAdvertiserManager().getAllAdvertisersForCompany(user.getCompany())) {
                items.add(new SelectItem(a, (StringUtils.isBlank(a.getName()) ? "[default]" : a.getName()) + "/" + a.getId()));
            }
            companyAdvertisers = items;
        }
        return companyAdvertisers;
    }

    public List<SelectItem> loadAdfonicUsers() {
        if (this.adfonicUsers == null) {
            List<AdfonicUser> adfonicUsers =
                    getUserManager().getAllAdfonicUsers(
                            new Sorting(new SortOrder(SortOrder.Direction.ASC, "firstName"),
                                    new SortOrder(SortOrder.Direction.ASC, "lastName")));
            List<SelectItem> admins = new ArrayList<SelectItem>();
            for (AdfonicUser au : adfonicUsers) {
                admins.add(new SelectItem(au, au.getFirstName() + " " + au.getLastName()));
            }
            this.adfonicUsers = admins;
        }
        return this.adfonicUsers;
    }
    
    public void autogenerateSeatIds(){
        if (CollectionUtils.isNotEmpty(this.selectedBidSeats)){
            getPublisherManager().generateBidSeats(this.selectedBidSeats);
        }
    }
}
