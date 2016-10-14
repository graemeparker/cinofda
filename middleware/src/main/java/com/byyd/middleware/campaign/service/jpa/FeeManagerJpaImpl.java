package com.byyd.middleware.campaign.service.jpa;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AccountFixedMargin;
import com.adfonic.domain.Audience;
import com.adfonic.domain.AudiencePrices;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAgencyDiscount;
import com.adfonic.domain.CampaignAudience;
import com.adfonic.domain.CampaignDataFee;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.adfonic.domain.DMPAudience;
import com.adfonic.domain.DMPSelector;
import com.adfonic.domain.DMPVendor;
import com.byyd.middleware.campaign.dao.CampaignAgencyDiscountDao;
import com.byyd.middleware.campaign.dao.CampaignDataFeeDao;
import com.byyd.middleware.campaign.dao.CampaignRichMediaAdServingFeeDao;
import com.byyd.middleware.campaign.dao.CampaignTradingDeskMarginDao;
import com.byyd.middleware.campaign.filter.CampaignDataFeeFilter;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.FeeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("feeManager")
public class FeeManagerJpaImpl extends BaseJpaManagerImpl implements FeeManager {
    
    @Autowired(required = false)
    private CampaignTradingDeskMarginDao campaignTradingDeskMarginDao;
    
    @Autowired(required = false)
    private CampaignRichMediaAdServingFeeDao campaignRichMediaAdServingFeeDao;

    @Autowired(required = false)
    private CampaignDataFeeDao campaignDataFeeDao;
    
    @Autowired(required=false)
    private CampaignAgencyDiscountDao campaignAgencyDiscountDao;
    
    @Autowired
    private CampaignManager campaignManager;
    

    //------------------------------------------------------------------------------------------
    // CampaignTradingDeskMargin
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CampaignTradingDeskMargin getCampaignTradingDeskMarginById(Long id, FetchStrategy... fetchStrategy) {
        return campaignTradingDeskMarginDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignTradingDeskMargin getCampaignTradingDeskMarginById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignTradingDeskMarginById(makeLong(id), fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignTradingDeskMargin create(CampaignTradingDeskMargin campaignTradingDeskMargin) {
        return campaignTradingDeskMarginDao.create(campaignTradingDeskMargin);
    }

    @Transactional(readOnly = false)
    public CampaignTradingDeskMargin update(CampaignTradingDeskMargin campaignTradingDeskMargin) {
        return campaignTradingDeskMarginDao.update(campaignTradingDeskMargin);
    }

    @Transactional(readOnly = false)
    public void delete(CampaignTradingDeskMargin campaignTradingDeskMargin) {
        campaignTradingDeskMarginDao.delete(campaignTradingDeskMargin);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteCampaignTradingDeskMargins(List<CampaignTradingDeskMargin> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(CampaignTradingDeskMargin entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignTradingDeskMarginsForCampaign(Campaign campaign) {
        return campaignTradingDeskMarginDao.countAllForCampaign(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return campaignTradingDeskMarginDao.getAllForCampaign(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return campaignTradingDeskMarginDao.getAllForCampaign(campaign, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignTradingDeskMargin> getAllCampaignTradingDeskMarginsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return campaignTradingDeskMarginDao.getAllForCampaign(campaign, sort, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Campaign saveCampaignTradingDeskMargin(Long campaignId, BigDecimal newCampaignTradingDeskMargin){
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        
        if (campaign!=null){

            if (newCampaignTradingDeskMargin != null) {
                if ((campaign.getCurrentTradingDeskMargin() == null) || 
                    (newCampaignTradingDeskMargin.compareTo(campaign.getCurrentTradingDeskMargin().getTradingDeskMargin()) != 0)) {
                    campaign = newCampaignTradingDeskMargin(campaign, newCampaignTradingDeskMargin);
                }
            } 
        }

        return campaign;
    }
    
    private Campaign newCampaignTradingDeskMargin(Campaign campaign, BigDecimal amount) {
        Date now = new Date();
        
        // Old fee data
        CampaignTradingDeskMargin currentTDMargin = campaign.getCurrentTradingDeskMargin();
        if(currentTDMargin != null) {
            currentTDMargin.setEndDate(now);
            update(currentTDMargin);
        }

        // New fee data
        BigDecimal newMargin = amount;
        if (newMargin==null){
            // Account fixed margin [MAD-3348]
            AccountFixedMargin accountFixedMargin = campaign.getAdvertiser().getCompany().getCurrentAccountFixedMargin();
            if (accountFixedMargin!=null){
                newMargin = accountFixedMargin.getMargin();
            }else{
                newMargin = new BigDecimal(0);
            }
        }
        
        CampaignTradingDeskMargin ctdm = campaign.createNewTradingDeskMargin(newMargin, now);
        create(ctdm);
        return campaignManager.update(campaign);
    }

    //------------------------------------------------------------------------------------------
    // CampaignRichMediaAdServingFee
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CampaignRichMediaAdServingFee getCampaignRichMediaAdServingFeeById(Long id, FetchStrategy... fetchStrategy) {
        return campaignRichMediaAdServingFeeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignRichMediaAdServingFee getCampaignRichMediaAdServingFeeById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignRichMediaAdServingFeeById(makeLong(id), fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignRichMediaAdServingFee create(CampaignRichMediaAdServingFee campaignRichMediaAdServingFee) {
        return campaignRichMediaAdServingFeeDao.create(campaignRichMediaAdServingFee);
    }

    @Transactional(readOnly = false)
    public CampaignRichMediaAdServingFee update(CampaignRichMediaAdServingFee campaignRichMediaAdServingFee) {
        return campaignRichMediaAdServingFeeDao.update(campaignRichMediaAdServingFee);
    }

    @Transactional(readOnly = false)
    public void delete(CampaignRichMediaAdServingFee campaignRichMediaAdServingFee) {
        campaignRichMediaAdServingFeeDao.delete(campaignRichMediaAdServingFee);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteCampaignRichMediaAdServingFees(List<CampaignRichMediaAdServingFee> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(CampaignRichMediaAdServingFee entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign) {
        return campaignRichMediaAdServingFeeDao.countAllForCampaign(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return campaignRichMediaAdServingFeeDao.getAllForCampaign(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return campaignRichMediaAdServingFeeDao.getAllForCampaign(campaign, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignRichMediaAdServingFee> getAllCampaignRichMediaAdServingFeesForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return campaignRichMediaAdServingFeeDao.getAllForCampaign(campaign, sort, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly = false)
    public Campaign saveCampaignRichMediaAdServingFee(Long campaignId, BigDecimal newCampaignRichMediaAdServingFee){
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        
        if (campaign!=null){ 
            // RichMediaAdServingFee
            if (newCampaignRichMediaAdServingFee != null) {
                if ((campaign.getCurrentRichMediaAdServingFee() == null) || 
                    (newCampaignRichMediaAdServingFee.compareTo(campaign.getCurrentRichMediaAdServingFee().getRichMediaAdServingFee()) != 0)) {
                    campaign = newCampaignRichMediaAdServingFee(campaign, newCampaignRichMediaAdServingFee);
                }
            }
        }

        return campaign;
    }
    
    private Campaign newCampaignRichMediaAdServingFee(Campaign campaign, BigDecimal amount) {
        Date now = new Date();
        CampaignRichMediaAdServingFee currentRMASFee = campaign.getCurrentRichMediaAdServingFee();
        if(currentRMASFee != null) {
            currentRMASFee.setEndDate(now);
            update(currentRMASFee);
        }

        CampaignRichMediaAdServingFee crm = campaign.createNewRichMediaAdServingFee(amount, now);
        create(crm);
        return campaignManager.update(campaign);
    }


    //------------------------------------------------------------------------------------------
    // CampaignDataFee
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CampaignDataFee getCampaignDataFeeById(Long id, FetchStrategy... fetchStrategy) {
        return campaignDataFeeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignDataFee getCampaignDataFeeById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignDataFeeById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDataFee create(CampaignDataFee campaignDataFee) {
        return campaignDataFeeDao.create(campaignDataFee);
    }

    @Override
    @Transactional(readOnly = false)
    public CampaignDataFee update(CampaignDataFee campaignDataFee) {
        return campaignDataFeeDao.update(campaignDataFee);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(CampaignDataFee campaignDataFee) {
        campaignDataFeeDao.delete(campaignDataFee);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteCampaignDataFees(List<CampaignDataFee> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(CampaignDataFee entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignDataFees(CampaignDataFeeFilter filter) {
        return campaignDataFeeDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, FetchStrategy ... fetchStrategy) {
        return campaignDataFeeDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return campaignDataFeeDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignDataFee> getAllCampaignDataFees(CampaignDataFeeFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return campaignDataFeeDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCampaignDataFee(Campaign campaign){
        return calculateCampaignAudienceDataFee (campaign.getCampaignAudiences());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCampaignAudienceDataFee(Set<CampaignAudience> campaignAudiences){
        return calculateCampaignAudiencesDataFee(campaignAudiences).getDataFee();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateAudicencesDataFee(Set<Audience> audiences){
        return calculateDataFee(audiences).getDataFee();
    }

    @Override
    @Transactional(readOnly = true)
    public DataFeeCalculationResult calculateCampaignAudiencesDataFee(Set<CampaignAudience> campaignAudiences){
        Set<Audience> audiences = new HashSet<Audience>(campaignAudiences.size());
        for (CampaignAudience campaignAudience : campaignAudiences){
            audiences.add(campaignAudience.getAudience());
        }
        return calculateDataFee(audiences);
    }

    private DataFeeCalculationResult calculateDataFee(Set<Audience> audiences){
        BigDecimal dataFee = BigDecimal.ZERO;

        // Map to save max datafee per vendor
        Map<DMPVendor, BigDecimal> dmpVendors = new HashMap<DMPVendor, BigDecimal>();

        for (Audience audience : audiences){
            DMPAudience dmpAudience = audience.getDmpAudience();
            // Calculating Data Fee for DMPAudience only
            if (dmpAudience!=null){
                dataFee = calculateDataFeeForDMPAudience(audience, dmpAudience, dmpVendors, dataFee);
            }
        }

        DataFeeCalculationResult result = new DataFeeCalculationResult();
        result.setDataFee(dataFee);
        result.setDataFeePerVendor(dmpVendors);
        return result;
    }

    private BigDecimal calculateDataFeeForDMPAudience(Audience audience, DMPAudience dmpAudience, Map<DMPVendor, BigDecimal> dmpVendors, BigDecimal dataFee) {
        //Calculate DataFee
        BigDecimal audienceDataFee = calculateAudienceDataFee(audience).getDataRetail();

        // If we have handled other audience for this vendor before, use the max data fee value
        DMPVendor audienceDmpVendor = dmpAudience.getDmpVendor();
        if (dmpVendors.containsKey(audienceDmpVendor)){
            BigDecimal currentMaxDatafee = dmpVendors.get(audienceDmpVendor);
            if (currentMaxDatafee.compareTo(audienceDataFee)==-1){
                dmpVendors.put(audienceDmpVendor, audienceDataFee);
                dataFee = dataFee.add(audienceDataFee).subtract(currentMaxDatafee);
            }
        }else{
            dataFee = dataFee.add(audienceDataFee);
            dmpVendors.put(audienceDmpVendor, audienceDataFee);
        }
        return dataFee;
    }

    @Override
    @Transactional(readOnly = true)
    public AudiencePrices calculateAudienceDataFee(Audience audience){
        BigDecimal dataRetail = BigDecimal.ZERO;
        BigDecimal dataWholesale = BigDecimal.ZERO;

        DMPAudience dmpAudience = audience.getDmpAudience();
        if (dmpAudience!=null){
            BigDecimal defaultDataRetail = dmpAudience.getDmpVendor().getDefaultAudiencePrices().getDataRetail();
            BigDecimal defaultDataWholesale = dmpAudience.getDmpVendor().getDefaultAudiencePrices().getDataWholesale();
            for (DMPSelector dmpSelector : dmpAudience.getDmpSelectors()){
                BigDecimal currentDataRetail = dmpSelector.getDataRetail();
                BigDecimal currentDataWholesale = dmpSelector.getDataWholesale();
                if (currentDataRetail==null){
                    currentDataRetail = defaultDataRetail;
                    currentDataWholesale = defaultDataWholesale;
                }

                if (dataRetail.compareTo(currentDataRetail)==-1){
                    dataRetail = currentDataRetail;
                    dataWholesale = currentDataWholesale;
                }
            }
        }

        return new AudiencePrices(dataRetail, dataWholesale);
    }

    public static class DataFeeCalculationResult {
        private BigDecimal dataFee;
        private Map<DMPVendor, BigDecimal> dataFeePerVendor;

        public BigDecimal getDataFee() {
            return dataFee;
        }
        public void setDataFee(BigDecimal dataFee) {
            this.dataFee = dataFee;
        }
        public Map<DMPVendor, BigDecimal> getDataFeePerVendor() {
            return dataFeePerVendor;
        }
        public void setDataFeePerVendor(Map<DMPVendor, BigDecimal> dataFeePerVendor) {
            this.dataFeePerVendor = dataFeePerVendor;
        }
    }
    
    // ------------------------------------------------------------------------------------------
    // CampaignAgencyDiscount
    // ------------------------------------------------------------------------------------------
    /**
     * NOTE: currentAgencyDiscount and historicalAgencyDiscounts MUST be hydrated on Campaign for this method to work
     */
    @Override
    @Transactional(readOnly = false)
    public Campaign newCampaignAgencyDiscount(Campaign campaign, BigDecimal discount) {
        Date now = new Date();
        CampaignAgencyDiscount currentAgencyDiscount = campaign.getCurrentAgencyDiscount();
        if(currentAgencyDiscount != null) {
            currentAgencyDiscount.setEndDate(now);
            update(currentAgencyDiscount);
        }

        CampaignAgencyDiscount cad = campaign.createNewAgencyDiscount(discount, now);
        create(cad);
        
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        return campaignManager.update(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignAgencyDiscount getCampaignAgencyDiscountById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignAgencyDiscountById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignAgencyDiscount getCampaignAgencyDiscountById(Long id, FetchStrategy... fetchStrategy) {
        return campaignAgencyDiscountDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public CampaignAgencyDiscount create(CampaignAgencyDiscount campaignAgencyDiscount) {
        return campaignAgencyDiscountDao.create(campaignAgencyDiscount);
    }

    @Transactional(readOnly = false)
    public CampaignAgencyDiscount update(CampaignAgencyDiscount campaignAgencyDiscount) {
        return campaignAgencyDiscountDao.update(campaignAgencyDiscount);
    }

    @Transactional(readOnly = false)
    public void delete(CampaignAgencyDiscount campaignAgencyDiscount) {
        campaignAgencyDiscountDao.delete(campaignAgencyDiscount);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteCampaignAgencyDiscounts(List<CampaignAgencyDiscount> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CampaignAgencyDiscount entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCampaignAgencyDiscountsForCampaign(Campaign campaign) {
        return campaignAgencyDiscountDao.countAllForCampaign(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, FetchStrategy ... fetchStrategy) {
        return campaignAgencyDiscountDao.getAllForCampaign(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, Pagination page, FetchStrategy ... fetchStrategy) {
        return campaignAgencyDiscountDao.getAllForCampaign(campaign, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignAgencyDiscount> getAllCampaignAgencyDiscountsForCampaign(Campaign campaign, Sorting sort, FetchStrategy ... fetchStrategy) {
        return campaignAgencyDiscountDao.getAllForCampaign(campaign, sort, fetchStrategy);
    }

}
