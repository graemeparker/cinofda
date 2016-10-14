package com.adfonic.beans;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.User;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class CompanyRateCardBean extends BaseBean {
    
	@ManagedProperty(value = "#{adminAccountBean}")
    AdminAccountBean adminAccountBean;
 
    private User user;
    private Publisher publisher;
    
    private BigDecimal defaultMinimumCPC;
    private BigDecimal defaultMinimumCPM;
    private BigDecimal defaultEcpmTarget;

    private RateCard defaultECPMRateCard;
    private RateCard defaultCPCRateCard;
    private RateCard defaultCPMRateCard;
    
    private Map<BidType,BigDecimal> lowestRateCardBidMap;
    
    
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
        
    }
    
    public CompanyRateCardBean(){
    	//
    }

    public void doSave() {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (!isValidDefaultRateCardMinimum(defaultMinimumCPC, BidType.CPC)) {
            fc.addMessage("rateCardForm:defaultCPC",
                    messageForId("error.adminRateCardDefaultMinimum.exceedsCountry"));
            return;
        }

        if (!isValidDefaultRateCardMinimum(defaultMinimumCPM, BidType.CPM)) {
            fc.addMessage("rateCardForm:defaultCPM",
                    messageForId("error.adminRateCardDefaultMinimum.exceedsCountry"));
            return;
        }

        if (defaultCPCRateCard == null) {
            defaultCPCRateCard = new RateCard();
        }
        defaultCPCRateCard.setDefaultMinimum(defaultMinimumCPC);
        
        if (defaultCPMRateCard == null) {
            defaultCPMRateCard = new RateCard();
        }
        defaultCPMRateCard.setDefaultMinimum(defaultMinimumCPM);
        
        if (defaultECPMRateCard == null) {
            defaultECPMRateCard = new RateCard();
        }
        if(defaultEcpmTarget==null){
            // SC-459: If ratecard is cleared in the UI but has country ratecard must be set to 0
            if(defaultECPMRateCard.getMinimumBidMap()!=null && defaultECPMRateCard.getMinimumBidMap().size()>0){
                defaultEcpmTarget = new BigDecimal(0.00);
            }
            else{
                defaultECPMRateCard = null;
            }
        }
        if(defaultEcpmTarget!=null){
            defaultECPMRateCard.setDefaultMinimum(defaultEcpmTarget);
        }

        try {
            TransactionalRunner runner = getTransactionalRunner();
            publisher = runner.callTransactional(
                        new Callable<Publisher>() {
                            public Publisher call() throws Exception {
                                return updatePublisher(
                                        publisher, 
                                        defaultCPCRateCard,
                                        defaultCPMRateCard,
                                        defaultECPMRateCard);
                            }
                        }
                    );
            setRequestFlag("didUpdateRateCard");
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Error saving publisher rate cards for publisher item id=" +
                    publisher.getId(),
                    e);
        }            
    }
    
    public BigDecimal getDefaultMinimumCPC() {
        if (this.defaultCPCRateCard != null) {
            this.defaultMinimumCPC = this.defaultCPCRateCard.getDefaultMinimum();
        }
        return this.defaultMinimumCPC;
    }

    public void setDefaultMinimumCPC(BigDecimal defaultMinimumCPC) {
        this.defaultMinimumCPC = defaultMinimumCPC;
    }

    public BigDecimal getDefaultMinimumCPM() {
        if (this.defaultCPMRateCard != null) {
            this.defaultMinimumCPM = this.defaultCPMRateCard.getDefaultMinimum();
        }
        return this.defaultMinimumCPM;
    }

    public void setDefaultMinimumCPM(BigDecimal defaultMinimumCPM) {
        this.defaultMinimumCPM = defaultMinimumCPM;
    }

    public BigDecimal getDefaultEcpmTarget() {
        if (this.defaultECPMRateCard != null) {
            this.defaultEcpmTarget = this.defaultECPMRateCard.getDefaultMinimum();
        }
        return defaultEcpmTarget;
    }

    public void setDefaultEcpmTarget(BigDecimal defaultEcpmTarget) {
        this.defaultEcpmTarget = defaultEcpmTarget;
    }

    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }

    public Map<BidType, BigDecimal> getLowestRateCardBidMap() {
        return lowestRateCardBidMap;
    }
    
    /*
     * Check that the minimum default is not greater than country
     * specific values for the rate card.
     */
    private boolean isValidDefaultRateCardMinimum(BigDecimal min, BidType b) {
        // the minimum can always be set to null
        if (min != null && this.lowestRateCardBidMap.get(b) != null) {
            if (min.compareTo(this.lowestRateCardBidMap.get(b)) > 0) {
                return false;
            }
        }
        return true;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        publisher = user.getCompany().getPublisher();
        loadRateCards();
    }
    
    public Publisher updatePublisher(
            Publisher publisher,
            RateCard cpcCard,
            RateCard cpmCard,
            RateCard ecpmCard) {
        if (!getPublicationManager().isPersisted(cpcCard)) {
            cpcCard = getPublicationManager().create(cpcCard);
        }
        else {
            cpcCard = getPublicationManager().update(cpcCard);
        }
        publisher.getDefaultRateCardMap().put(BidType.CPC, cpcCard);
        
        if (!getPublicationManager().isPersisted(cpmCard)) {
            cpmCard = getPublicationManager().create(cpmCard);
        }
        else {
            cpmCard = getPublicationManager().update(cpmCard);
        }
        publisher.getDefaultRateCardMap().put(BidType.CPM, cpmCard);
        
        boolean deleteRateCard = false;
        
        if(ecpmCard!=null){
            if (!getPublicationManager().isPersisted(ecpmCard)) {
                ecpmCard = getPublicationManager().create(ecpmCard);
            }
            else {
                ecpmCard = getPublicationManager().update(ecpmCard);
            }
        }
        else{
         // SC-459: Delete the rate card if it's cleared
            deleteRateCard = true;
        }
        RateCard rateCardToDelete = null;
        if(deleteRateCard){
            rateCardToDelete = publisher.getEcpmTargetRateCard();
        }
        publisher.setEcpmTargetRateCard(ecpmCard);
        
        Publisher pub = getPublisherManager().update(publisher);
        if(deleteRateCard){
            getPublicationManager().delete(rateCardToDelete);
        }
        return pub;
    }

    private void loadRateCards() {
        // set up a map of bid type to lowest bid
        Map<BidType,BigDecimal> lowest = new HashMap<BidType,BigDecimal>();
        for (BidType bidType : BidType.values()) {
            RateCard defaultRateCard = publisher.getDefaultRateCardMap().get(bidType);
            if (defaultRateCard != null) {
                defaultRateCard = getPublicationManager().getRateCardById(defaultRateCard.getId());
                if (defaultRateCard != null && !defaultRateCard.getMinimumBidMap().isEmpty()) {
                    lowest.put(bidType, Collections.min(defaultRateCard.getMinimumBidMap().values()));
                }
            }
            else {
                lowest.put(bidType, null);
            }
        }
        this.lowestRateCardBidMap = lowest;
        
        // load publisher's default/ecpm target ratecards
        this.defaultECPMRateCard = getPublisherManager().getEcpmTargetRateCardForPublisher(publisher);
        if (defaultECPMRateCard!=null && defaultECPMRateCard.getMinimumBidMap() != null) {
            defaultECPMRateCard.getMinimumBidMap().size();
        }
        this.defaultCPMRateCard = publisher.getDefaultRateCard(BidType.CPM);
        this.defaultCPCRateCard = publisher.getDefaultRateCard(BidType.CPC);
    }
}