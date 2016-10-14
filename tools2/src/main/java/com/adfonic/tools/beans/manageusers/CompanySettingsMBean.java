package com.adfonic.tools.beans.manageusers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.currencyexchangerate.CurrencyExchangeRateService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "companySettings", pattern = "/agencyconsole/companysettings", viewId = "/WEB-INF/jsf/manageusers/companysettings.jsf") })
public class CompanySettingsMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private CurrencyExchangeRateService currencyExchangeRateService;
    
    // MAD-3303 - Default currency per client
    private Long oldCurrencyId;
    private Long defaultCurrencyId;
    private Map<Long, CurrencyExchangeRateDto> defaultCurrenciesMap;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "companySettings") })
    public void init() throws Exception {
        // Load company settings
        loadCompanySettings();
        
        // Load currencies rates
        getAllCurrenciesAsOrderedMap();
    }
    
    private void loadCompanySettings() {
        // MAD-3303 - Default currency per client
        // Get current default company currency, we also save the persisted status on oldCurrencyId for checking user changes
        this.oldCurrencyId = this.defaultCurrencyId = companyService.getCompanyDefaultCurrency(getUser().getCompany().getId()).getId();
    }
    
    public void doChecks() {
        // Check default currency changes
        if (this.oldCurrencyId.longValue() != this.defaultCurrencyId.longValue()){
            RequestContext.getCurrentInstance().execute("confirmationChanges.show()");
        }
    }
    
    public void doSave() {        
        // MAD-3303 - Default currency per client
        companyService.setCompanyDefaultCurrency(getUser().getCompany().getId(), this.defaultCurrencyId);
    }

    private void getAllCurrenciesAsOrderedMap() {
        List<CurrencyExchangeRateDto> currencyExchangeRates = currencyExchangeRateService.getAllCurrencyExchangeRate();
        if (currencyExchangeRates!=null){
            this.defaultCurrenciesMap = new LinkedHashMap<Long, CurrencyExchangeRateDto>(currencyExchangeRates.size());
            for(CurrencyExchangeRateDto currencyExchangeRate : currencyExchangeRates){
                this.defaultCurrenciesMap.put(currencyExchangeRate.getId(), currencyExchangeRate);
            }
        } 
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
    
    public CurrencyExchangeRateDto getCurrency(Long id) {
        return this.defaultCurrenciesMap.get(id);
    }
}
