package com.adfonic.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.adfonic.domain.Company;
import com.adfonic.domain.Company.AdvertiserCategory;
import com.adfonic.domain.User;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class AdvertiserCategoryBean extends BaseBean {
    
	@ManagedProperty(value = "#{adminAccountBean}")
    AdminAccountBean adminAccountBean;
 
    private User user;
    private Company company;
    
    // company.advertiserCategory
    private AdvertiserCategory advertiserCategory;
    private List<SelectItem> advertiserCategories;
    
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
    
    public AdvertiserCategoryBean(){
    	//
    }

    public void doSave() {
        if (advertiserCategory != null && !advertiserCategory.equals(company.getAdvertiserCategory())) {
            try {
                TransactionalRunner runner = getTransactionalRunner();
                company = runner.callTransactional(
                            new Callable<Company>() {
                                public Company call() throws Exception {
                                    return updateCompany(
                                            company,
                                            advertiserCategory);
                                }
                            }
                        );
                setRequestFlag("didUpdate");
            } catch (Exception e) {
                logger.log(
                        Level.SEVERE,
                        "Error saving company publisher category for company item id=" +
                        company.getId(),
                        e);
            }            
        }
    }
    
    
    public List<SelectItem> getAdvertiserCategories() {
        if (this.advertiserCategories == null) {
            List<SelectItem> categories = new ArrayList<SelectItem>();
            for (AdvertiserCategory ac : AdvertiserCategory.values()) {
                categories.add(new SelectItem(ac));
            }
            this.advertiserCategories = categories;
        }
        return this.advertiserCategories;
    }

    public AdvertiserCategory getAdvertiserCategory() {
        return this.advertiserCategory;
    }

    public void setAdvertiserCategory(AdvertiserCategory advertiserCategory) {
        this.advertiserCategory = advertiserCategory;
    }
    
    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        company = user.getCompany();
        advertiserCategory = company.getAdvertiserCategory();
    }
    
    public Company updateCompany(
            Company company,
            AdvertiserCategory advertiserCategory) {
        company.setAdvertiserCategory(advertiserCategory);
        return getCompanyManager().update(company);
    }
}