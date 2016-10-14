package com.adfonic.presentation.category.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Category;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.category.dao.CategorySearchDao;
import com.adfonic.presentation.category.service.CategoryTypeAheadSearchService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.common.service.CategoryHierarchyService;
import com.byyd.middleware.common.service.CategorySearchService;
import com.byyd.middleware.common.service.CommonManager;

@Service("categoryTypeAheadSearchService")
public class CategoryTypeAheadSearchServiceImpl extends GenericServiceImpl implements CategoryTypeAheadSearchService {

    @Autowired
    private CategorySearchService categorySearchService;
    @Autowired
    private CategoryHierarchyService categoryHierarchyService;
    @Autowired
    private CommonManager commonManager;
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private CategorySearchDao categorySearchDao;

    @Override
    @Transactional(readOnly=true)
    public List<CategoryHierarchyDto> getCategoriesStartingWith(String startingWith, boolean caseSensitive, int maxResults) {
        List<Category> categories = categorySearchService.getCategoriesStartingWith(startingWith, caseSensitive, maxResults);
        return makeListFromCollection(getList(CategoryHierarchyDto.class, categories));
    }

    @Override
    @Transactional(readOnly=true)
    public String getHierarchicalName(CategoryHierarchyDto category, String separator) {
        Category cat = commonManager.getCategoryById(category.getId());
        return categoryHierarchyService.getHierarchicalName(cat, separator);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CategoryHierarchyDto> getHierarchy(CategoryHierarchyDto category) {
        Category cat = commonManager.getCategoryById(category.getId());
        List<Category> categories = categoryHierarchyService.getHierarchy(cat);
        return makeListFromCollection(getList(CategoryHierarchyDto.class, categories));
    }

    @Override
    @Transactional(readOnly=true)
    public List<CategoryHierarchyDto> sortCategoriesByHierarchicalName(List<CategoryHierarchyDto> categoriesToSort, boolean caseSensitive) {
        // This might seem a bit kludgy, but to take advantage of the caching, actual entities must
        // be passed down. This is much more efficient than emulating the comparators written in 
        // the categoryHierarchyService and have them wotk on DTOs
        List<Category> list = new ArrayList<Category>();
        if(categoriesToSort != null) {
            for(CategoryHierarchyDto dto : categoriesToSort) {
                list.add(commonManager.getCategoryById(dto.getId()));
            }
            categoryHierarchyService.sortCategoriesByHierarchicalName(list, caseSensitive);
        }
        return makeListFromCollection(getList(CategoryHierarchyDto.class, list));
    }

    @Override
    @Transactional(readOnly=true)
    public List<CategoryHierarchyDto> getExcludedCategoriesForCampaign(
            CampaignDto campaignDto) {
        if(campaignDto == null) {
            return null;
        }
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        Collection<Category> list = campaign.getSegments().get(0).getExcludedCategories();
        return makeListFromCollection(getList(CategoryHierarchyDto.class, list));
    }

    @Override
    @Transactional(readOnly=true)
    public CategoryHierarchyDto getCampaignIabCategory(CampaignDto campaignDto) {
        if(campaignDto == null) {
            return null;
        }
        Campaign campaign = campaignManager.getCampaignById(campaignDto.getId());
        return getObjectDto(CategoryHierarchyDto.class, campaign.getCategory());
    }

    @Override
    public List<NameIdModel> searchForCategories(String categoryNamePrefix) {
        return categorySearchDao.searchForCategories(categoryNamePrefix);
    }

}
