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
import com.adfonic.domain.Company.PublisherCategory;
import com.adfonic.domain.User;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class PublisherCategoryBean extends BaseBean {
    
	@ManagedProperty(value = "#{adminAccountBean}")
    AdminAccountBean adminAccountBean;
 
    private User user;
    private Company company;
    
    // company.publisherCategory
    private PublisherCategory publisherCategory;
    private List<SelectItem> publisherCategories;
    
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
    
    public PublisherCategoryBean(){
    	//
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
        if (publisherCategory != null && !publisherCategory.equals(company.getPublisherCategory())) {
            try {
                TransactionalRunner runner = getTransactionalRunner();
                company = runner.callTransactional(
                            new Callable<Company>() {
                                public Company call() throws Exception {
                                    return updateCompany(
                                            company,
                                            publisherCategory);
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
        
    public PublisherCategory getPublisherCategory() {
        return this.publisherCategory;
    }

    public void setPublisherCategory(PublisherCategory publisherCategory) {
        this.publisherCategory = publisherCategory;
    }

    public List<SelectItem> getPublisherCategories() {
        if (this.publisherCategories == null) {
            List<SelectItem> categories = new ArrayList<SelectItem>();
            for (PublisherCategory cat : PublisherCategory.values()) {
                categories.add(new SelectItem(cat));
            }
            this.publisherCategories = categories;
        }
        return this.publisherCategories;
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
        publisherCategory = company.getPublisherCategory();
    }
    
    public Company updateCompany(
            Company company,
            PublisherCategory publisherCategory) {
        company.setPublisherCategory(publisherCategory);
        return getCompanyManager().update(company);
    }
}