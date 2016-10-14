package com.adfonic.adserver.web.jsf.managed.beans;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.cache.CacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public class FrequencyCapBean {
	
    private AdserverDomainCacheManager adserverDomainCacheManager;
    private FrequencyCounter frequencyCounter;
    private CacheManager cacheManager;
	
	private int defaultFrequencyCapPeriodSec = 3600;
	private Long creativeIdForFrequencyCap = 0L;
	private String uniqueIdentifierForFrequencyCap ="";
	private Integer windowSecondsForFrequencyCap = 0;
	private Integer frequencyCap = 0;
	private FrequencyCounter.FrequencyEntity capApplied = null;
	
	public void getFrequencyCap(ActionEvent event) {
		CreativeDto creative = adserverDomainCacheManager.getCache().getCreativeById(creativeIdForFrequencyCap); 
		
		if (creative == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Creative not found", "Creative not found"));
        }else if (creative.getCampaign() == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Campaign not found", "Campaign not found"));
        }else{
    		if (creative.getCampaign().getCapPeriodSeconds() != null){
    			windowSecondsForFrequencyCap = creative.getCampaign().getCapPeriodSeconds();
		    }else{
    		    windowSecondsForFrequencyCap = defaultFrequencyCapPeriodSec;
    		}
    		
    		long entityIdForFrequencyCap = creativeIdForFrequencyCap;
    		capApplied = FrequencyCounter.FrequencyEntity.CREATIVE;
    		if (creative.getCampaign().isCapPerCampaign()){
    		    entityIdForFrequencyCap = creative.getCampaign().getId();
    		    capApplied = FrequencyCounter.FrequencyEntity.CAMPAIGN;
    		}
    		
    		this.frequencyCap = frequencyCounter.getFrequencyCount(uniqueIdentifierForFrequencyCap, entityIdForFrequencyCap, windowSecondsForFrequencyCap, capApplied);
		}
	}
	
	public void incFrequencyCap(ActionEvent event) {
	    CreativeDto creative = adserverDomainCacheManager.getCache().getCreativeById(creativeIdForFrequencyCap);
		
	    if (creative == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Creative not found", "Creative not found"));
        }else if (creative.getCampaign() == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Campaign not found", "Campaign not found"));
        }else{
    		if (creative.getCampaign().getCapPeriodSeconds() != null){
    			windowSecondsForFrequencyCap = creative.getCampaign().getCapPeriodSeconds();
    		}else{
    		    windowSecondsForFrequencyCap = defaultFrequencyCapPeriodSec;
    		}
    		
    		long entityIdForFrequencyCap = creativeIdForFrequencyCap;
            capApplied = FrequencyCounter.FrequencyEntity.CREATIVE;
            if (creative.getCampaign().isCapPerCampaign()){
                entityIdForFrequencyCap = creative.getCampaign().getId();
                capApplied = FrequencyCounter.FrequencyEntity.CAMPAIGN;
            }
    		
    		this.frequencyCap = frequencyCounter.incrementFrequencyCount(uniqueIdentifierForFrequencyCap, entityIdForFrequencyCap, windowSecondsForFrequencyCap, capApplied);
	    }
	}

	public void decFrequencyCap(ActionEvent event) {
	    CreativeDto creative = adserverDomainCacheManager.getCache().getCreativeById(creativeIdForFrequencyCap);
		
	    if (creative == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Creative not found", "Creative not found"));
        }else if (creative.getCampaign() == null) {
            FacesContext.getCurrentInstance().addMessage("creativeId", new FacesMessage(FacesMessage.SEVERITY_INFO, "Campaign not found", "Campaign not found"));
        }else{
            if (creative.getCampaign().getCapPeriodSeconds() != null){
                windowSecondsForFrequencyCap = creative.getCampaign().getCapPeriodSeconds();
            }else{
                windowSecondsForFrequencyCap = defaultFrequencyCapPeriodSec;
            }
            
            long entityIdForFrequencyCap = creativeIdForFrequencyCap;
            capApplied = FrequencyCounter.FrequencyEntity.CREATIVE;
            if (creative.getCampaign().isCapPerCampaign()){
                entityIdForFrequencyCap = creative.getCampaign().getId();
                capApplied = FrequencyCounter.FrequencyEntity.CAMPAIGN;
            }
		
            this.frequencyCap = frequencyCounter.decrementFrequencyCount(uniqueIdentifierForFrequencyCap, entityIdForFrequencyCap, windowSecondsForFrequencyCap, capApplied);
	    }
	}
	
	public List<String> getCacheInfo(){
	    return cacheManager.cacheInfo();
	}
	
	public Long getCreativeIdForFrequencyCap() {
		return creativeIdForFrequencyCap;
	}
	public void setCreativeIdForFrequencyCap(Long creativeIdForFrequencyCap) {
		this.creativeIdForFrequencyCap = creativeIdForFrequencyCap;
	}
	public String getUniqueIdentifierForFrequencyCap() {
		return uniqueIdentifierForFrequencyCap;
	}
	public void setUniqueIdentifierForFrequencyCap(
			String uniqueIdentifierForFrequencyCap) {
		this.uniqueIdentifierForFrequencyCap = uniqueIdentifierForFrequencyCap;
	}
	public Integer getWindowSecondsForFrequencyCap() {
		return windowSecondsForFrequencyCap;
	}
	public void setWindowSecondsForFrequencyCap(Integer windowSecondsForFrequencyCap) {
		this.windowSecondsForFrequencyCap = windowSecondsForFrequencyCap;
	}
	public FrequencyCounter getFrequencyCounter() {
		return frequencyCounter;
	}
	public void setFrequencyCounter(FrequencyCounter frequencyCounter) {
		this.frequencyCounter = frequencyCounter;
	}
	public Integer getFrequencyCap() {
		return frequencyCap;
	}
	public void setFrequencyCap(Integer frequencyCap) {
		this.frequencyCap = frequencyCap;
	}
	public FrequencyCounter.FrequencyEntity getCapApplied() {
        return capApplied;
	}
    public void setCapApplied(FrequencyCounter.FrequencyEntity capApplied) {
        this.capApplied = capApplied;
    }
    public FrequencyCounter.FrequencyEntity[] getFrequencyEntities(){
        return FrequencyCounter.FrequencyEntity.values();
    }

    public AdserverDomainCacheManager getAdserverDomainCacheManager() {
        return adserverDomainCacheManager;
    }

    public void setAdserverDomainCacheManager(AdserverDomainCacheManager adserverDomainCacheManager) {
        this.adserverDomainCacheManager = adserverDomainCacheManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
