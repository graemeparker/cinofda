package com.adfonic.domain.cache.service;

import java.util.Set;

public interface CategoryService extends BaseCache {

    void addExpendedCategoryIds(Long categoryId, Set<Long> listOfExpendedCategories);

    @Deprecated
    /**
     * use isExistsInExpandedCategoryIds instead
     * @param categoryId
     * @return
     */
    Set<Long> getExpandedCategoryIds(Long categoryId);

    boolean isExistsInExpandedCategoryIds(Long parentCategoryId, Long lookupCategoryId);

    @Deprecated
    /*
     * Now creative do not have any category so no need for this to be in cache
     */
    Set<Long> getExpandedCreativeCategoryIds(Long creativeId);

    @Deprecated
    /*
     * Now publications do not have list of category so no need for this to be in cache
     * and also we don't expand publication category, even if need arise do the following
     * cat = publication.getCategory();
     * Set<Category> cats = cache.getExpandedCategoryIds();
     */
    Set<Long> getExpandedPublicationCategoryIds(Long publicationId);

    Set<String> getCachedPluginCategories(Long publicationId, String pluginName);

    void cachePluginCategories(Long publicationId, String pluginName, Set<String> pluginCategories);

}
