package com.adfonic.domain.cache.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryServiceImpl implements CategoryService {

    private static final long serialVersionUID = 1L;

    final Map<Long, Set<Long>> expandedCategoryIdsByCategoryId = new HashMap<Long, Set<Long>>();
    final Map<Long, Map<String, Set<String>>> cachedPluginCategoriesByPublicationId = Collections.synchronizedMap(new HashMap<Long, Map<String, Set<String>>>());

    public CategoryServiceImpl() {

    }

    public CategoryServiceImpl(CategoryServiceImpl copy) {
        this.expandedCategoryIdsByCategoryId.putAll(copy.expandedCategoryIdsByCategoryId);
    }

    @Override
    public void addExpendedCategoryIds(Long categoryId, Set<Long> listOfExpendedCategories) {
        expandedCategoryIdsByCategoryId.put(categoryId, listOfExpendedCategories);
    }

    @Override
    public Set<Long> getExpandedCategoryIds(Long categoryId) {
        return expandedCategoryIdsByCategoryId.get(categoryId);
    }

    @Override
    public boolean isExistsInExpandedCategoryIds(Long parentCategoryId, Long lookupCategoryId) {
        Set<Long> expendedCategories = expandedCategoryIdsByCategoryId.get(parentCategoryId);
        if (expendedCategories == null) {
            return false;
        }
        return expendedCategories.contains(lookupCategoryId);
    }

    @Override
    public Set<Long> getExpandedCreativeCategoryIds(Long creativeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Long> getExpandedPublicationCategoryIds(Long publicationId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getCachedPluginCategories(Long publicationId, String pluginName) {
        Map<String, Set<String>> byPluginName = cachedPluginCategoriesByPublicationId.get(publicationId);
        return byPluginName == null ? null : byPluginName.get(pluginName);
    }

    @Override
    public void cachePluginCategories(Long publicationId, String pluginName, Set<String> pluginCategories) {
        Map<String, Set<String>> byPluginName;
        if ((byPluginName = cachedPluginCategoriesByPublicationId.get(publicationId)) == null) {
            synchronized (cachedPluginCategoriesByPublicationId) {
                if ((byPluginName = cachedPluginCategoriesByPublicationId.get(publicationId)) == null) {
                    byPluginName = Collections.synchronizedMap(new HashMap<String, Set<String>>());
                    cachedPluginCategoriesByPublicationId.put(publicationId, byPluginName);
                }
            }
        }
        synchronized (byPluginName) {
            byPluginName.put(pluginName, pluginCategories);
        }
    }

    @Override
    public void afterDeserialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeSerialization() {
        // TODO Auto-generated method stub

    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Total expandedCategoryIdsByCategoryId = " + this.expandedCategoryIdsByCategoryId.size());
            logger.log(level, "Total cachedPluginCategoriesByPublicationId = " + this.cachedPluginCategoriesByPublicationId.size());
        }
    }

}
