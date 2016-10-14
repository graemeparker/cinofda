package com.adfonic.beans;

import static com.adfonic.beans.CategoryQueryBean.CATEGORY_SEPARATOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.SelectEvent;

import com.adfonic.domain.Category;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher_;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@ViewScoped
@ManagedBean
public class PublicationSettingsBean extends BaseBean {
    public static final FetchStrategy PUBLICATION_FS = new FetchStrategyBuilder()
    .addLeft(Publication_.publisher)
    .addLeft(Publication_.transparentNetwork)
    .addLeft(Publication_.category)
    .addLeft(Publication_.excludedCategories)
    .addLeft(Publisher_.company)
    .addLeft(Company_.users)
    .build();

    private Publication selectedPublication;
    private boolean premiumPublication;
    private Map<Category,String> excludedCategoryLabelMap;
    
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

    private void loadPublication(Publication publication) {
        this.selectedPublication =
            getPublicationManager().getPublicationById(
                    publication.getId(), PUBLICATION_FS);
        this.premiumPublication = (
                (selectedPublication.getTransparentNetwork() != null) &&
                (selectedPublication.getTransparentNetwork().equals(
                getPublicationManager().getTransparentNetworkByName(
                        TransparentNetwork.PERFORMANCE_NETWORK_NAME))));
    }

    public void handleSelectedPublication(SelectEvent event) {
        Publication p = (Publication)event.getObject();
        if (p != null) {
            loadPublication(p);
        }
    }

    public void handleSelectedCategory(SelectEvent event) {
        Category c = (Category)event.getObject();
        if (c != null && selectedPublication != null) {
            selectedPublication.setCategory(c);
        }
    }

    public void handleSelectedExcludedCategory(SelectEvent event) {
        Category c = (Category)event.getObject();
        if (c != null && selectedPublication != null) {
            selectedPublication.getExcludedCategories().add(c);
        }
    }

    /***
     * Validates that the Category chosen is different from the Adfonic not Categorized one (default one)
     * @return true if the category is diferent from {@link com.adfonic.domain.Category.NOT_CATEGORIZED_NAME}
     *
     * */
    private boolean isValidIABCategory() {
        boolean result = true;
        FacesContext fc = FacesContext.getCurrentInstance();
        if (selectedPublication.getCategory() == null) {
            fc.addMessage("pubSettingsForm:publicationCategory",
                messageForId("error.pubCatSearch.categorynotnull"));
            result = false;
        }
        else if (Category.NOT_CATEGORIZED_NAME.equals(selectedPublication.getCategory().getName())){
            //has to select another one before saving.
            fc.addMessage("pubSettingsForm:publicationCategory",
                    messageForId("error.pubCatSearch.changenotcategorizedcategory"));
            result = false;
        }
        return result;
    }

    public void doSubmit() {
        if (selectedPublication != null) {
            if (!isValidIABCategory()) {
                // errors are in the context
                return;
            }
            // set the network appropriately
            if (premiumPublication) {
                TransparentNetwork ppn =
                    getPublicationManager().getTransparentNetworkByName(
                            TransparentNetwork.PERFORMANCE_NETWORK_NAME);
                selectedPublication.setTransparentNetwork(ppn);
            }
            else {
                selectedPublication.setTransparentNetwork(null);
            }

            selectedPublication = getPublicationManager().update(selectedPublication);
            selectedPublication = getPublicationManager().getPublicationById(selectedPublication.getId(), PUBLICATION_FS);
            setRequestFlag("didUpdate");
        }
    }

    public void setSelectedPublication(Publication p) {
        this.selectedPublication = p;
    }

    public Publication getSelectedPublication() {
        return this.selectedPublication;
    }

    public void setPremiumPublication(boolean premiumPublication) {
        this.premiumPublication = premiumPublication;
    }

    public boolean isPremiumPublication() {
        return this.premiumPublication;
    }

    public String getPublicationCategoryHierarchyName() {
        if (this.selectedPublication != null &&
                this.selectedPublication.getCategory() != null) {
            return categoryHierarchyService.getHierarchicalName(
                    this.selectedPublication.getCategory(),
                    CATEGORY_SEPARATOR);
        }
        return StringUtils.EMPTY;
    }

    // read-only list, hierarchy sorted
    public List<Category> getExcludedCategories() {
        if (selectedPublication != null &&
                CollectionUtils.isNotEmpty(selectedPublication.getExcludedCategories())) {
            List<Category> excluded =
                new ArrayList<Category>(selectedPublication.getExcludedCategories());
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
        if (category != null && selectedPublication != null &&
                CollectionUtils.isNotEmpty(selectedPublication.getExcludedCategories())) {
            selectedPublication.getExcludedCategories().remove(category);
        }
    }
}
