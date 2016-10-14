package com.adfonic.beans;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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
import com.adfonic.domain.User;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class PublisherBlockedBidTypeBean extends BaseBean {
    
    @ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;
 
    private User user;
    private Publisher publisher;
    private Set<BidType> blockedBidTypes = new HashSet<BidType>();
    
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
    }
    
    public PublisherBlockedBidTypeBean(){
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

    public void doSave() {
        try {
            TransactionalRunner runner = getTransactionalRunner();
            publisher = runner.callTransactional(
                        new Callable<Publisher>() {
                            public Publisher call() throws Exception {
                                return updatePublisher(
                                        publisher,
                                        blockedBidTypes);
                            }
                        }
                    );
            setRequestFlag("didUpdate");
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Error saving blocked bid types for publisher item id=" +
                    publisher.getId(),
                    e);
        }            
    }
        
    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }
    
    public boolean isRtbEnabled() {
        return this.publisher.isRtbEnabled();
    }
    
    public Set<BidType> getBlockedBidTypes() {
        return blockedBidTypes;
    }

    public void setBlockedBidTypes(Set<BidType> blockedBidTypes) {
        this.blockedBidTypes = blockedBidTypes;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        publisher = user.getCompany().getPublisher();
        blockedBidTypes.addAll(publisher.getBlockedBidTypes());
    }
    
    public Publisher updatePublisher(
            Publisher publisher,
            Set<BidType> blockedBidTypes) {

        publisher.getBlockedBidTypes().clear();
        publisher.getBlockedBidTypes().addAll(blockedBidTypes);
        return getPublisherManager().update(publisher);
    }
}