package com.adfonic.presentation.category.service;

import java.util.List;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.presentation.NameIdModel;

public interface CategoryTypeAheadSearchService {

	public List<CategoryHierarchyDto> getCategoriesStartingWith(String startingWith, boolean caseSensitive, int maxResults);
	public String getHierarchicalName(CategoryHierarchyDto category, String separator);
	public List<CategoryHierarchyDto> getHierarchy(CategoryHierarchyDto category);
	public List<CategoryHierarchyDto> sortCategoriesByHierarchicalName(List<CategoryHierarchyDto> categoriesToSort, boolean caseSensitive);

	public List<CategoryHierarchyDto> getExcludedCategoriesForCampaign(CampaignDto campaignDto);
	public CategoryHierarchyDto getCampaignIabCategory(CampaignDto campaignDto);
	
	// Search categories based on proc (use specific read only data source)
	public List<NameIdModel> searchForCategories(String categoryNamePrefix);
}
