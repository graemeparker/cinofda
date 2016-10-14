package com.adfonic.tools.beans.campaign.category;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.presentation.category.service.CategoryTypeAheadSearchService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CategorySearchMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {
    }

    private static final int MAX_RESULTS = 50;
    public static final String CATEGORY_SEPARATOR = " > ";
    // private Map<CategoryHierarchyDto,String> labelMap = new
    // HashMap<CategoryHierarchyDto,String>();

    @Autowired
    private CategoryTypeAheadSearchService categoryTypeAheadSearchService;

    public List<CategoryHierarchyDto> doQuery(String search) {
        List<CategoryHierarchyDto> results = categoryTypeAheadSearchService.getCategoriesStartingWith(search, false, MAX_RESULTS);
        /*
         * if (CollectionUtils.isNotEmpty(results)) { for (CategoryHierarchyDto
         * c : results) { labelMap.put(c,
         * categoryTypeAheadSearchService.getHierarchicalName(c,
         * CATEGORY_SEPARATOR)); }
         * categoryTypeAheadSearchService.sortCategoriesByHierarchicalName
         * (results, false); }
         */
        return results;
    }

    // public Map<CategoryHierarchyDto, String> getLabelMap() {
    // return labelMap;
    // }

    public List<CategoryHierarchyDto> getExcludedCategoriesForCampaign(CampaignDto campaign) {
        return categoryTypeAheadSearchService.sortCategoriesByHierarchicalName(
                categoryTypeAheadSearchService.getExcludedCategoriesForCampaign(campaign), false);
    }

    public CategoryHierarchyDto getCampaignIabCategory(CampaignDto campaign) {
        return categoryTypeAheadSearchService.getCampaignIabCategory(campaign);
    }

    public String getHierarchicalName(CategoryHierarchyDto category) {
        return getHierarchicalName(category, CATEGORY_SEPARATOR);
    }

    public String getHierarchicalName(CategoryHierarchyDto category, String separator) {
        return categoryTypeAheadSearchService.getHierarchicalName(category, separator);
    }

    public CategoryTypeAheadSearchService getCategorySearchService() {
        return categoryTypeAheadSearchService;
    }

    public void setCategoryTypeAheadSearchService(CategoryTypeAheadSearchService categoryTypeAheadSearchService) {
        this.categoryTypeAheadSearchService = categoryTypeAheadSearchService;
    }

}
