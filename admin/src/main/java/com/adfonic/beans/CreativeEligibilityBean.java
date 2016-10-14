package com.adfonic.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Publication;
import com.byyd.middleware.creative.service.CreativeEligibilityManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RequestScoped
@ManagedBean
public class CreativeEligibilityBean extends BaseBean {
    private String creativeId;
    private String adSpaceId;
    private String publicationId;
    private Creative creative;
    private AdSpace adSpace;
    private Publication publication;
    private List<String> reasonsWhyNot;
    private boolean eligible = false;
    Collection<CampaignStoppage> campaignStoppages;
    Collection<AdvertiserStoppage> advertiserStoppages;

    @ManagedProperty(value = "#{creativeEligibilityManager}", name = "creativeEligibilityManager")
    private CreativeEligibilityManager creativeEligibilityManager;
    public void setCreativeEligibilityManager(CreativeEligibilityManager creativeEligibilityManager) {
        this.creativeEligibilityManager = creativeEligibilityManager;
    }
    
    @PostConstruct
    public void init() {
        if(isRestrictedUser()){
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }
    }

    private static final FetchStrategy CREATIVE_FS = new FetchStrategyBuilder()
        .addLeft(Creative_.campaign)
        .addInner(Campaign_.advertiser)
        .build();

    public void load() {
        FacesContext fc = FacesContext.getCurrentInstance();
        boolean valid = true;
        
        if (StringUtils.isNotEmpty(creativeId)) {
            creative = getCreativeManager().getCreativeByIdOrExternalId(creativeId, CREATIVE_FS);

            if (creative == null) {
                fc.addMessage("creativeEligibilityForm:creativeId",
                        new FacesMessage("Invalid Creative ID: " + creativeId));
                valid = false;
            }
        }

        // AdSpace OR Publication
        if (StringUtils.isNotEmpty(adSpaceId)) {
            adSpace = getPublicationManager().getAdSpaceByIdOrExternalId(
                    adSpaceId, 
                    new FetchStrategyBuilder()
                    .addLeft(AdSpace_.publication)
                    .build());
            if (adSpace == null) {
                fc.addMessage("creativeEligibilityForm:adSpaceId",
                        new FacesMessage("Invalid AdSpace ID: " + adSpaceId));
                valid = false;
            } 
            else if (adSpace.getPublication() == null) {
                fc.addMessage("creativeEligibilityForm:adSpaceId",
                        new FacesMessage("AdSpace ID: " + adSpaceId + " has null publication."));
                valid = false;
            }
        } else if (StringUtils.isNotEmpty(publicationId)) {
            publication = getPublicationManager().getPublicationByIdOrExternalId(publicationId);
            if (publication == null) {
                fc.addMessage("creativeEligibilityForm:publicationId",
                        new FacesMessage("Invalid Publication ID: " + publicationId));
                valid = false;
            }
        }

        if (valid && creative != null) {
            if (adSpace != null) {
                reasonsWhyNot = new ArrayList<String>();
                eligible = creativeEligibilityManager.isCreativeEligible(creative, adSpace, reasonsWhyNot);
            }
            else if (publication != null) {
                reasonsWhyNot = new ArrayList<String>();
                eligible = creativeEligibilityManager.isCreativeEligible(creative, publication, reasonsWhyNot);
            }

            campaignStoppages = getCampaignManager().getCampaignStoppagesForCampaignAndNullOrFutureReactivateDate(creative.getCampaign());
            advertiserStoppages = getAdvertiserManager().getAdvertiserStoppagesForAdvertiserAndNullOrFutureReactivateDate(creative.getCampaign().getAdvertiser());
        }
    }

    public String getCreativeId() {
        return creativeId;
    }
    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public String getAdSpaceId() {
        return adSpaceId;
    }
    public void setAdSpaceId(String adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public String getPublicationId() {
        return publicationId;
    }
    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public Creative getCreative() {
        return creative;
    }

    public void setCreative(Creative creative) {
        this.creative = creative;
    }

    public AdSpace getAdSpace() {
        return adSpace;
    }

    public void setAdSpace(AdSpace adSpace) {
        this.adSpace = adSpace;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }
    public List<String> getReasonsWhyNot() {
        return reasonsWhyNot;
    }

    public void setReasonsWhyNot(List<String> reasonsWhyNot) {
        this.reasonsWhyNot = reasonsWhyNot;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public Collection<CampaignStoppage> getCampaignStoppages() {
        return campaignStoppages;
    }

    public void setCampaignStoppages(Collection<CampaignStoppage> campaignStoppages) {
        this.campaignStoppages = campaignStoppages;
    }

    public Collection<AdvertiserStoppage> getAdvertiserStoppages() {
        return advertiserStoppages;
    }

    public void setAdvertiserStoppages(Collection<AdvertiserStoppage> advertiserStoppages) {
        this.advertiserStoppages = advertiserStoppages;
    }
 }
