package com.adfonic.adserver.web.jsf.managed.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;

import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.data.cache.ecpm.api.EcpmData;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.service.WeightageServices;
import com.adfonic.util.DateUtils;

public class AdServerCacheBean implements Serializable {

    private static final long serialVersionUID = 1L;
    AdserverDomainCacheManager adserverDomainCacheManager;
    DomainCacheManager domainCacheManager;
    private AdSpaceDto selectedAdSpace;
    private CreativeDto selectedCreative;

    private AdserverDataCacheManager dataCacheUpdate;

    Long adspaceIdForEcpm = 0L;
    Long creativeIdForEcpm = 0L;

    AdSpaceDto adspace;
    CreativeDto creative;
    PlatformDto platform;
    List<PlatformDto> allPlatforms;
    CountryDto selectedCountry;
    List<CountryDto> allCountries;
    Double enteredBidFloorPrice;
    Double tradingDeskMarginMediaCostOptimisation = null;

    //Move it to EcpmData
    Double adfonicCtrBuffer;

    Logger logger = Logger.getLogger(this.getClass().getName());

    private EcpmData ecpmDataFromDataCache;
    private Double currencyConversionRate;

    public AdserverDomainCacheManager getAdserverDomainCacheManager() {
        return adserverDomainCacheManager;
    }

    public void setAdserverDomainCacheManager(AdserverDomainCacheManager adserverDomainCacheManager) {
        this.adserverDomainCacheManager = adserverDomainCacheManager;
    }

    public AdSpaceDto[] getAllAdspaces() {
        return adserverDomainCacheManager.getCache().getAllAdSpaces();
    }

    public AdSpaceDto getSelectedAdSpace() {
        return selectedAdSpace;
    }

    public void setSelectedAdSpace(AdSpaceDto selectedAdSpace) {
        this.selectedAdSpace = selectedAdSpace;
    }

    /**
     * @return
     */
    public List<CreativeDto> getSelectedAdSapceEligibleCreatives() {
        if (selectedAdSpace == null) {
            return new ArrayList<CreativeDto>();
        }
        AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
        AdspaceWeightedCreative[] adspaceWeightedCreatives = adserverDomainCache.getEligibleCreatives(selectedAdSpace.getId());
        List<CreativeDto> creativeList = new ArrayList<CreativeDto>();
        for (AdspaceWeightedCreative oneAdspaceWeightedCreative : adspaceWeightedCreatives) {
            for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
                creativeList.add(adserverDomainCache.getCreativeById(oneCreativeId));
            }
        }
        return creativeList;

    }

    public CreativeDto getSelectedCreative() {
        return selectedCreative;
    }

    public void setSelectedCreative(CreativeDto selectedCreative) {
        this.selectedCreative = selectedCreative;
    }

    public boolean isRtbEnabled() {
        return adserverDomainCacheManager.getCache().isRtbEnabled();
    }

    public String getStringForDeviceIdentifierTypeIds(Set<Long> deviceIdenitfierList) {
        return StringUtils.join(deviceIdenitfierList, ',');
    }

    public Long getAdspaceIdForEcpm() {
        return adspaceIdForEcpm;
    }

    public void setAdspaceIdForEcpm(Long adspaceIdForEcpm) {
        this.adspaceIdForEcpm = adspaceIdForEcpm;
    }

    public Long getCreativeIdForEcpm() {
        return creativeIdForEcpm;
    }

    public void setCreativeIdForEcpm(Long creativeIdForEcpm) {
        this.creativeIdForEcpm = creativeIdForEcpm;
    }

    public void clear(ActionEvent event) {
        adspaceIdForEcpm = null;
        creativeIdForEcpm = null;
        adspace = null;
        creative = null;
        platform = null;
        selectedCountry = null;
        enteredBidFloorPrice = null;
        ecpmDataFromDataCache = null;
    }

    public void calculateEcpm(ActionEvent event) {
        logger.fine("Calculating ECPM for following combination");
        try {
            AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
            WeightageServices ecpmDataCache = dataCacheUpdate.getEcpmDataCacheAsWS();
            adspace = adserverDomainCache.getAdSpaceById(adspaceIdForEcpm);
            creative = adserverDomainCache.getCreativeById(creativeIdForEcpm);
            logger.fine("Adspace Id=" + adspaceIdForEcpm);
            logger.fine("creative Id=" + creativeIdForEcpm);

            if (adspace == null) {
                FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Adspace not found", "Adspace not found"));
            }
            if (creative == null) {
                FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Creative not found", "Creative not found"));
            }
            if (!FacesContext.getCurrentInstance().isValidationFailed() && adspace != null && creative != null) {
                logger.info("Start ecpm calculations");

                if (ecpmDataCache != null) {
                    ecpmDataFromDataCache = calculateEcpmInternal(ecpmDataCache);
                    logger.info("Elapsed time for data cache:" + ecpmDataFromDataCache.getElapsedTime());
                } else
                    logger.info("Datacache disabled!");

                adfonicCtrBuffer = dataCacheUpdate.getCache().getDefaultDoubleValue("adfonic_ctr_dsp_buffer", 0.0);

            }
            String fromCurrency = "USD";
            String toCurrency = "USD";
            if (adspace != null && adspace.getPublication().getPublisher().getRtbConfig() != null) {
                toCurrency = adspace.getPublication().getPublisher().getRtbConfig().getBidCurrency();
            }
            String gmtTimeId = String.valueOf(DateUtils.getTimeID(new Date(), TimeZone.getDefault()));
            currencyConversionRate = dataCacheUpdate.getCache().getCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId).doubleValue();
        } catch (Exception ex) {
            logger.info("Exception " + ex.getClass().getName() + " with message " + ex.getMessage());

            ex.printStackTrace();
        }

    }

    private EcpmData calculateEcpmInternal(WeightageServices dataCache) {
        long start = System.nanoTime();

        long countryId = 0;
        if (selectedCountry != null) {
            countryId = selectedCountry.getId();
        }

        BigDecimal ecpmFloor = null;
        if (enteredBidFloorPrice != null) {
            ecpmFloor = new BigDecimal(enteredBidFloorPrice);
        }
        if (adspace.getPublication().isUseSoftFloor() && ecpmFloor != null) {
            double softBidMultiplier = dataCache.getDefaultDoubleValue("soft_floor_multiplier", 0.0);
            if (softBidMultiplier > 0.0) {
                ecpmFloor = ecpmFloor.multiply(new BigDecimal(1 - softBidMultiplier));
            }
        }

        EcpmData result = new EcpmData();

        //dataCache.computeEcpmInfo(adspace, creative, platform, countryId, ecpmFloor, result.getEcpmInfo());
        dataCache.computeEcpmInfo(adspace, creative, platform, countryId, ecpmFloor, result.getEcpmInfo());

        result.setCampaignCvr(dataCache.getCampaignCvr(creative.getId()));
        result.setCreativeCvr(dataCache.getCreativeCvr(creative.getId()));
        result.setAdspaceCtr(dataCache.getAdspaceCtr(adspace.getId()));
        result.setCreativeWeightedCtrIndex(dataCache.getCreativeWeightedCtrIndex(creative, platform));
        result.setPublicationWeightedCvrIndex(dataCache.getPublicationWeightedCvrIndex(adspace.getPublication(), platform, creative));
        ExpectedStatsDto expectedStatsDto = dataCache.getExpectedStats(adspace.getId(), creative.getId());
        if (expectedStatsDto == null) {
            result.setExpectedStatsRgr(0.0);
            result.setExpectedStatsCvr(0.0);
            result.setExpectedStatsCtr(0.0);
        } else {
            result.setExpectedStatsRgr(expectedStatsDto.getExpectedRgr());
            result.setExpectedStatsCvr(expectedStatsDto.getExpectedCvr());
            result.setExpectedStatsCtr(expectedStatsDto.getExpectedCtr());
        }
        if (adspace.getPublication().getPublisher().getRtbConfig() != null
                && adspace.getPublication().getPublisher().getRtbConfig().getAuctionType().equals(RtbAuctionType.FIRST_PRICE)) {
            result.setRtbBidMultiplierCPC(getBidMultiplierValue("rtb_1p_bid_multiplier_cpc"));
            result.setRtbBidMultiplierCPM(getBidMultiplierValue("rtb_1p_bid_multiplier_cpm"));
            result.setRtbBidMultiplierCPI(getBidMultiplierValue("rtb_1p_bid_multiplier_cpi"));
            result.setRtbBidMultiplierCPA(getBidMultiplierValue("rtb_1p_bid_multiplier_cpa"));

        } else {
            result.setRtbBidMultiplierCPC(getBidMultiplierValue("rtb_2p_bid_multiplier_cpc"));
            result.setRtbBidMultiplierCPM(getBidMultiplierValue("rtb_2p_bid_multiplier_cpm"));
            result.setRtbBidMultiplierCPI(getBidMultiplierValue("rtb_2p_bid_multiplier_cpi"));
            result.setRtbBidMultiplierCPA(getBidMultiplierValue("rtb_2p_bid_multiplier_cpa"));
        }

        result.setDefaultCtrTarget(getDefaultDoubleValue("default_ctr_target", 0.01));
        result.setDefaultCvrTarget(getDefaultDoubleValue("default_cvr_target", 0.01));
        result.setDefaultCpcCtrTarget(getDefaultDoubleValue("default_cpc_ctr_target", 0.0001));
        double ctrThreshold = creative.getCampaign().isMediaCostOptimisationEnabled() ? getDefaultDoubleValue("cpm_ctr_underperformance_threshold_media_cost_opt", 0.7)
                : getDefaultDoubleValue("cpm_ctr_underperformance_threshold", 0.5);
        result.setCpmCtrUnderperformanceThreshold(ctrThreshold);
        result.setCpcCtrUnderperformanceThreshold(ctrThreshold);
        result.setCpcCvrUnderperformanceThreshold(ctrThreshold);

        //SC-451
        result.setSoftFloorMultiplier(getDefaultDoubleValue("soft_floor_multiplier", 3.0));

        CampaignCtrInfo campaignCtrInfo = dataCache.getCampaignCtrInfo(creative.getCampaign().getId());
        if (campaignCtrInfo == null) {
            result.setCampaignCurrentCtr(null);
            result.setCampaignTargetCtr(null);
        } else {
            result.setCampaignCurrentCtr(campaignCtrInfo.getCurrentCtr());
            result.setCampaignTargetCtr(campaignCtrInfo.getTargetCtr());
        }
        CampaignCvrInfo campaignCvrInfo = dataCache.getCampaignCvrInfo(creative.getCampaign().getId());
        if (campaignCvrInfo == null) {
            result.setCampaignCurrentCvr(null);
            result.setCampaignTargetCvr(null);
        } else {
            result.setCampaignCurrentCvr(campaignCvrInfo.getCurrentCvr());
            result.setCampaignTargetCvr(campaignCvrInfo.getTargetCvr());
        }

        result.setDefaultCtr(getDefaultDoubleValue("network_default_ctr", 0.0));
        result.setDefaultCvr(getDefaultDoubleValue("network_default_cvr", 0.0));
        result.setDefaultRtbCvr(getDefaultDoubleValue("network_default_cvr_rtb", 0.0));
        result.setNetworkMaxExpectedRgr(getDefaultDoubleValue("network_max_expected_rgr", 0.0));

        result.setCountryWeighting(dataCache.getCampaignCountryWeight(creative.getCampaign().getId(), countryId));
        result.setBuyerPremium(adspace.getPublication().getPublisher().getBuyerPremium());

        result.setElapsedTime((System.nanoTime() - start) / 1000);

        // MAD-2667 (Media Cost Optimisation)
        result.setCampaignMarginRecommendation(dataCache.getCampaignMarginRecommendation(creative.getCampaign().getId()));
        this.tradingDeskMarginMediaCostOptimisation = dataCache.getCampaignTradingDeskMargin(creative.getCampaign());

        return result;
    }

    private double getDefaultDoubleValue(String variableName, double defaultValue) {
        return dataCacheUpdate.getCache().getDefaultDoubleValue(variableName, defaultValue);
    }

    public EcpmData getEcpmDataFromDataCache() {
        return ecpmDataFromDataCache;
    }

    private double getBidMultiplierValue(String variableName) {

        return getDefaultDoubleValue(variableName, 1.0);

    }

    public AdSpaceDto getAdspace() {
        return adspace;
    }

    public void setAdspace(AdSpaceDto adspace) {
        this.adspace = adspace;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public void setCreative(CreativeDto creative) {
        this.creative = creative;
    }

    public DomainCacheManager getDomainCacheManager() {
        return domainCacheManager;
    }

    public void setDomainCacheManager(DomainCacheManager domainCacheManager) {
        this.domainCacheManager = domainCacheManager;
    }

    public List<PlatformDto> getAllPlatforms() {
        if (allPlatforms == null || allPlatforms.size() <= 0) {
            allPlatforms = domainCacheManager.getCache().getPlatforms();
        }
        return allPlatforms;
    }

    public void setAllPlatforms(List<PlatformDto> allPlatforms) {
        this.allPlatforms = allPlatforms;
    }

    public PlatformDto getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformDto platform) {
        this.platform = platform;
    }

    public List<CountryDto> getAllCountries() {
        if (allCountries == null || allCountries.size() <= 0) {
            allCountries = new ArrayList<CountryDto>(domainCacheManager.getCache().getCountriesByIsoCode().values());
            Collections.sort(allCountries, new Comparator<CountryDto>() {
                @Override
                public int compare(CountryDto country1, CountryDto country2) {
                    return country1.getName().compareToIgnoreCase(country2.getName());
                }
            });
        }

        return allCountries;
    }

    public void setAllCountries(List<CountryDto> allCountries) {
        this.allCountries = allCountries;
    }

    public CountryDto getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(CountryDto selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public Double getEnteredBidFloorPrice() {
        return enteredBidFloorPrice;
    }

    public void setEnteredBidFloorPrice(Double enteredBidFloorPrice) {
        this.enteredBidFloorPrice = enteredBidFloorPrice;
    }

    public void setDataCacheUpdate(AdserverDataCacheManager dataCacheUpdate) {
        this.dataCacheUpdate = dataCacheUpdate;
    }

    public Double getAdfonicCtrBuffer() {
        return adfonicCtrBuffer;
    }

    public void setAdfonicCtrBuffer(Double adfonicCtrBuffer) {
        this.adfonicCtrBuffer = adfonicCtrBuffer;
    }

    public Double getCurrencyConversionRate() {
        return currencyConversionRate;
    }

    public void setCurrencyConversionRate(Double currencyConversionRate) {
        this.currencyConversionRate = currencyConversionRate;
    }

    public Double getTradingDeskMarginMediaCostOptimisation() {
        return tradingDeskMarginMediaCostOptimisation;
    }

    public void setTradingDeskMarginMediaCostOptimisation(Double tradingDeskMarginMediaCostOptimisation) {
        this.tradingDeskMarginMediaCostOptimisation = tradingDeskMarginMediaCostOptimisation;
    }
}
