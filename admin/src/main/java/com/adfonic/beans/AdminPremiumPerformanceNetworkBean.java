package com.adfonic.beans;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.adfonic.domain.BidType;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.TransparentNetwork_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RequestScoped
@ManagedBean(name="adminPerformanceNetwork")
public class AdminPremiumPerformanceNetworkBean extends BaseBean {

    private static final transient Logger LOG = Logger.getLogger(AdminPremiumPerformanceNetworkBean.class.getName());

    protected TransparentNetwork ppn;
    protected BigDecimal minCPC;
    protected BigDecimal minCPM;
    protected RateCard CPCCard;
    protected RateCard CPMCard;
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected boolean ppnPlaceHolder;

    private static final FetchStrategy TRANSPARENT_NETWORK_FS = new FetchStrategyBuilder()
        .addLeft(TransparentNetwork_.rateCardMap)
        .build();

    public AdminPremiumPerformanceNetworkBean(){
        doInit();
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

    public void doInit(){
        ppn = getPublicationManager().getTransparentNetworkByName(TransparentNetwork.PERFORMANCE_NETWORK_NAME, TRANSPARENT_NETWORK_FS);
        CPMCard = ppn.getRateCard(BidType.CPM);
        minCPM = CPMCard.getDefaultMinimum();
        if(minCPM == null){
            minCPM = BigDecimal.valueOf(0);
        }
        CPCCard = ppn.getRateCard(BidType.CPC);
        minCPC = CPCCard.getDefaultMinimum();
        if(minCPC == null){
            minCPC = BigDecimal.valueOf(0);
        }
    }

    public BigDecimal getMinCPC() {
        return minCPC;
    }

    public void setMinCPC(BigDecimal minCPC) {
        this.minCPC = minCPC;
    }

    public BigDecimal getMinCPM() {
        return minCPM;
    }

    public void setMinCPM(BigDecimal minCPM) {
        this.minCPM = minCPM;
    }

    public boolean isPpnPlaceHolder() {
        return ppnPlaceHolder;
    }

    public void setPpnPlaceHolder(boolean ppnPlaceHolder) {
        this.ppnPlaceHolder = ppnPlaceHolder;
    }

    public void doSaveMinimums(){

        try {
            RateCard CPCCard = ppn.getRateCard(BidType.CPC);
            RateCard CPMCard = ppn.getRateCard(BidType.CPM);
            CPMCard.setDefaultMinimum(minCPM);
            CPCCard.setDefaultMinimum(minCPC);

            CPMCard = getPublicationManager().update(CPMCard);
            CPCCard = getPublicationManager().update(CPCCard);

            ppn.getRateCardMap().put(BidType.CPM, CPMCard);
            ppn.getRateCardMap().put(BidType.CPC, CPCCard);

            ppn = getPublicationManager().update(ppn);
            setRequestFlag("didUpdateRateCard");
        }
        catch (Exception e) {
            LOG.log(Level.FINE, "Failed to update Premium Performance Network item id: + " + ppn.getId(), e);
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("infoContent",
                    new FacesMessage("There was an error saving settings"));
        }

    }

}
