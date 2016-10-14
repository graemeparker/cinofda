package com.adfonic.beans;

import static com.adfonic.beans.CategoryQueryBean.CATEGORY_SEPARATOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.SelectEvent;

import com.adfonic.domain.Category;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.User;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class PublisherExcludedCategoryBean extends BaseBean {
    
	@ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;
 
    private User user;
    private Publisher publisher;
   
    // iab categories
    private Map<Category,String> excludedCategoryLabelMap;
    private Set<Category> excludedCategories = new HashSet<Category>();
    
	@PostConstruct
    private void init() {
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
    
    public PublisherExcludedCategoryBean(){
    	//
    }

    public void doSave() {
        try {
            TransactionalRunner runner = getTransactionalRunner();
            publisher = runner.callTransactional(
                        new Callable<Publisher>() {
                            public Publisher call() throws Exception {
                                return updatePublisher(
                                        publisher, 
                                        excludedCategories);
                            }
                        }
                    );
            setRequestFlag("didUpdate");
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Error saving excluded iab categories for publisher item id=" +
                    publisher.getId(),
                    e);
        }            
    }  

    // read-only list, hierarchy sorted
    public List<Category> getExcludedCategories() {
        if (CollectionUtils.isNotEmpty(excludedCategories)) {
            List<Category> excluded =
                new ArrayList<Category>(excludedCategories);
            categoryHierarchyService.sortCategoriesByHierarchicalName(excluded,
                    false);
            Map<Category,String> labelMap = new HashMap<Category,String>();
            for (Category c : excluded) {
                labelMap.put(c, categoryHierarchyService.getHierarchicalName(c, CATEGORY_SEPARATOR));
            }
            this.excludedCategoryLabelMap = labelMap;
            return excluded;
        }
        return Collections.emptyList();
    }
 
    public Map<Category, String> getExcludedCategoryLabelMap() {
        return this.excludedCategoryLabelMap;
    }

    public void doRemoveExcludedCategory(Category category) {
        if (CollectionUtils.isNotEmpty(excludedCategories) &&
                excludedCategories.contains(category)) {
            excludedCategories.remove(category);
        }
    }

    public void handleSelectedExcludedCategory(SelectEvent event) {
        Category category = (Category)event.getObject();
        if (category != null && !excludedCategories.contains(category)) {
            excludedCategories.add(category);
        }
    }

    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        publisher = user.getCompany().getPublisher();
        // hydrate
        publisher.getExcludedCategories().size();
        excludedCategories.addAll(publisher.getExcludedCategories());
    }
    
    public Publisher updatePublisher(
            Publisher publisher,
            Set<Category> excludedCategories) {
        publisher.getExcludedCategories().clear();
        publisher.getExcludedCategories().addAll(excludedCategories);
        return getPublisherManager().update(publisher);
    }
}