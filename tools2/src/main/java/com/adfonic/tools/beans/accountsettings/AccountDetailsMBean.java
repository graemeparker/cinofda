package com.adfonic.tools.beans.accountsettings;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.TaxUtils;
import com.adfonic.domain.VerificationCode;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.campaign.enums.TaxRegime;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.email.EmailAddressManager;
import com.adfonic.email.EmailAddressType;
import com.adfonic.email.EmailService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.currencyexchangerate.CurrencyExchangeRateService;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.beans.navigation.NavigationMBean;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.AdfonicTimeZone;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
        @URLMapping(id = "accountDetails", pattern = "/accountdetails", viewId = "/WEB-INF/jsf/accountsettings/accountdetails.jsf")
                        })
public class AccountDetailsMBean extends GenericAbstractBean implements Serializable {
     
    private static final long serialVersionUID = 4188436945608530254L;   
         
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDetailsMBean.class);

    private static final String COUNTRY_NAME_GB = "GB";
    private static final String COUNTRY_NAME_US = "US";
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailAddressManager emailAddressManager;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NavigationMBean navigationBean;
    
    @Autowired
    private CurrencyExchangeRateService currencyExchangeRateService;
    
    @Autowired
    private CompanyService companyService;
    
    private String email = null;
    private List<String> accountTypes = null;
    private String companyName = null;
    private String firstName = null; 
    private String lastname = null;
    private String alias = null;
    private CountryDto country = null;
    private String taxCode = null;
    private String phone = null;
    private String timezone = null;
    private boolean invoiceDateInGMT = false;
    private boolean business = false;
    private String oldEmail  = null;
    private String developerKey;
    
    // MAD-3303 - Default currency per client
    private Long defaultCurrencyId;
    private Map<Long, CurrencyExchangeRateDto> currenciesMap;
    
    @Override
    @URLActions(actions = {@URLAction(mappingId = "accountDetails")})
    public void init() throws Exception {
        LOGGER.debug("init()-->");
        LOGGER.debug("Loading user presentation entity");
        UserDTO userDTO = getUser();
        this.email = userDTO.getEmail();
        this.firstName = userDTO.getFirstName(); 
        this.lastname = userDTO.getLastName();
        this.country = userDTO.getCountry();
        this.phone = userDTO.getPhoneNumber();
        this.alias = userDTO.getAlias();
        this.developerKey = userDTO.getDeveloperKey();
        
        CompanyDto companyDTO = userDTO.getCompany();
        this.companyName = companyDTO.getName();
        this.taxCode = companyDTO.getTaxCode();
        this.timezone = companyDTO.getDefaultTimeZoneId();
        this.invoiceDateInGMT = companyDTO.isInvoiceDateInGMT();
        this.accountTypes = companyDTO.getAccountTypes();
        
        this.business = !StringUtils.isBlank(this.companyName);
        this.oldEmail = this.email;
        
        // MAD-3303 - Default currency per client
        CurrencyExchangeRateDto advertiserDefaultCurrencyExchangeRate = getUser().getAdvertiserDto().getDefaultCurrencyExchangeRate();
        if (advertiserDefaultCurrencyExchangeRate==null){
            this.defaultCurrencyId = 0L;  // default value
        }else{
            this.defaultCurrencyId = advertiserDefaultCurrencyExchangeRate.getId();
        }
        
        getAllCurrenciesAsOrderedMap();
        
        LOGGER.debug("<--init()");
    }
    
    private void getAllCurrenciesAsOrderedMap() {
        List<CurrencyExchangeRateDto> currencyExchangeRateDtos = this.currencyExchangeRateService.getAllCurrencyExchangeRate();
        if (currencyExchangeRateDtos!=null){
            this.currenciesMap = new LinkedHashMap<Long, CurrencyExchangeRateDto>(currencyExchangeRateDtos.size());
            for(CurrencyExchangeRateDto currencyExchangeRateDto : currencyExchangeRateDtos){
                this.currenciesMap.put(currencyExchangeRateDto.getId(), currencyExchangeRateDto);
            }
        } 
    }
    
    public String doSave() throws IOException{
        LOGGER.debug("doSave()-->");
        if (isValid()){
            // MAD-3303 - Default currency per client
            AdvertiserDto advertiserDto = companyService.setAdvertiserDefaultCurrency(getUser().getAdvertiserDto().getId(), 
                                                                                     (this.defaultCurrencyId==0L ? null : this.defaultCurrencyId));
            getUser().setAdvertiserDto(advertiserDto); // save advertiser information in user session object
            
            LOGGER.debug("Saving user information");
            UserDTO userDto = userService.saveUserSettingsDetails(this.oldEmail,
                                                                  this.email,
                                                                  this.firstName,
                                                                  this.lastname,
                                                                  this.country,
                                                                  this.phone,
                                                                  this.companyName,
                                                                  this.taxCode,
                                                                  this.timezone,
                                                                  this.invoiceDateInGMT, 
                                                                  this.accountTypes,
                                                                  emailHasChanged());
            
            
            if (emailHasChanged()){
                //Send mail to the old email
                oldAddressEmail(userDto);
                
                //Send mail to the new one
                newAddressEmail(userDto); 
                
                //If is not admin, logout and redirection to email sended confirmation page 
                if (!isAdminUserLoggedIn()){
                    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                    externalContext.redirect(getChgMailLogoutLink());
                }else{
                    addFacesMessage(FacesMessage.SEVERITY_INFO, "updateButton", null, "page.account.settings.accountdetails.success");
                }
            }else{
                addFacesMessage(FacesMessage.SEVERITY_INFO, "updateButton", null, "page.account.settings.accountdetails.success");
            }
            
            getUserSessionBean().updateSessionInfo(this.firstName,
                                                   this.lastname,
                                                   this.country,
                                                   this.phone,
                                                   this.companyName,
                                                   this.taxCode,
                                                   this.timezone,
                                                   this.invoiceDateInGMT, 
                                                   this.accountTypes);
        }
        LOGGER.debug("<--doSave()");
        return null;
    }

    private boolean isValid() {
        boolean isValid = true;
        
        if (!isEmailValid()){
            LOGGER.debug("Invalid tax code");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "emailInput", null, "page.account.settings.accountdetails.email.duplicate");
            isValid = false;
        }
        
        if (!isCompanyNameValid()){
            LOGGER.debug("Invalid company name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "companyInput", null, "page.account.settings.accountdetails.company.error");
            isValid = false;
        }

        if (!isVATValid()){
            LOGGER.debug("Invalid tax code");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "vatnumberInput", null, "page.account.settings.accountdetails.vatnumber.error");
            isValid = false;
        }
        
        return isValid;
    }

    private boolean isEmailValid() {
        boolean isValid = true;
        
        if (emailHasChanged()){
            UserDTO existedUser = userService.getUserByEmail(this.email);
            if (existedUser!=null){
                isValid = false;
            }
        }
        return isValid;
    }
    
    private boolean isCompanyNameValid() {
        boolean isValid = true;
        
        if (business && StringUtils.isBlank(this.companyName)){
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean isVATValid() {
        boolean isValid = true;
        
        if (StringUtils.isNotBlank(this.taxCode) &&
            (this.country.getTaxRegime() == TaxRegime.EU || this.country.getTaxRegime() == TaxRegime.UK) &&
            (!TaxUtils.isValidVatNumber(this.country.getIsoCode(), this.taxCode))) {
             isValid = false;
         }
        return isValid;
    }
    
    private boolean emailHasChanged(){
        return !(this.oldEmail.equals(this.email));
    }
    
    private boolean oldAddressEmail(UserDTO userDTO) {
        try {
            String to = this.oldEmail;
            String subject = "Changed username";
            
            Map<String,Object> values = new HashMap<String, Object>();
            values.put("user", userDTO);
            values.put("urlRoot", getURLRoot());
            values.put("oldEmail", this.oldEmail);
            values.put("companyName", getToolsApplicationBean().getCompanyName());

            String body = templateToString("/templates/change_email_old.html", values);

            emailService.sendEmail(emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY),
                                   to,
                                   subject,
                                   body,
                                   "text/html");

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send old address email to user id=" + userDTO.getId(), e);
        }
        return false;
    }

    private boolean newAddressEmail(UserDTO userDTO) {
        try {
            String to = userDTO.getFormattedEmail();
            String subject = "Changed username";
            
            VerificationCode vc = userService.getVerificationCode(userDTO);
            
            Map<String,Object> values = new HashMap<String,Object>();
            if (vc != null) {
                values.put("code", vc.getCode());
                values.put("user", userDTO);
                values.put("urlRoot", getURLRoot());
                values.put("ssoUrlRoot", navigationBean.getSsoBaseUrl());
                values.put("oldEmail", this.oldEmail);
                values.put("companyName", getToolsApplicationBean().getCompanyName());
            }
            
            String body = templateToString("/templates/change_email_new.html", values);

            emailService.sendEmail(emailAddressManager.getEmailAddress(EmailAddressType.NOREPLY),
                                   to,
                                   subject,
                                   body,
                                   "text/html");

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send new address email to user id=" + userDTO.getId(), e);
        }
        return false;
    }
    
    private String getChgMailLogoutLink() {
        StringBuffer logoutLink = new StringBuffer(navigationBean.getSsoBaseUrl());
        logoutLink.append("/");
        logoutLink.append(navigationBean.getChangeEmailLogoutLink());
        logoutLink.append("?service=");
        logoutLink.append(getURLRoot());
        return logoutLink.toString();
    }

    public AdfonicTimeZone getTimezone() {
        return AdfonicTimeZone.getAdfonicTimeZoneById(this.timezone);
    }

    public void setTimezone(AdfonicTimeZone timezone) {
        this.timezone = timezone.getId();
    }

    public Collection<CountryDto> getAllCountries() {
        return locationService.getAllCountries();
    }
    
    public CountryDto getGBCountry(){
        return locationService.getCountryByIsoCode(COUNTRY_NAME_GB);
    }
    
    public CountryDto getUSCountry(){
        return locationService.getCountryByIsoCode(COUNTRY_NAME_US);
    }
    
    public List<AdfonicTimeZone> getAllTimezones() {
        return new ArrayList<AdfonicTimeZone>(Arrays.asList(AdfonicTimeZone.values())); 
    }
    
    public AdfonicTimeZone getTimezoneGMT(){
        return AdfonicTimeZone.GMT;
    }
    
    public AccountType getAdvertiserAccountType(){
        return AccountType.ADVERTISER;
    }
    
    public AccountType getPublisherAccountType(){
        return AccountType.PUBLISHER;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(List<String> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public CountryDto getCountry() {
        return country;
    }

    public void setCountry(CountryDto country) {
        this.country = country;
    }
    
    public String getDeveloperKey() {
        return developerKey;
    }

        
    public String getAlias() {
        return alias;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isInvoiceDateInGMT() {
        return invoiceDateInGMT;
    }

    public void setInvoiceDateInGMT(boolean invoiceDateInGMT) {
        this.invoiceDateInGMT = invoiceDateInGMT;
    }
    
    public String getAdvertiserId(){ 
        return getUser().getAdvertiserDto().getExternalID();
    }
    
    public Long getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    public void setDefaultCurrencyId(Long defaultCurrencyId) {
        this.defaultCurrencyId = defaultCurrencyId;
    }
    
    public List<Long> getCurrencyIds(){
        return new ArrayList<Long>(this.currenciesMap.keySet());
    }
    
    public CurrencyExchangeRateDto getCurrency(Long id) {
        return this.currenciesMap.get(id);
    }
}
