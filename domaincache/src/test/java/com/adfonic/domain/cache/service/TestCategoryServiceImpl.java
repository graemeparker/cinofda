package com.adfonic.domain.cache.service;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestCategoryServiceImpl {

    private CategoryService categoryService;

    @Before
    public void init() {
        categoryService = new CategoryServiceImpl();
    }

    @Test
    public void test01_addExpendedCategoryIds() {
        Long parentCategoryId = 1L;
        Set<Long> expandedCategoryIds = new HashSet<Long>();
        Long childCategoryId1 = 2L;
        Long childCategoryId2 = 3L;
        Long childCategoryId3 = 4L;
        Long childCategoryIdNotInExpandedList = 5L;
        expandedCategoryIds.add(childCategoryId1);
        expandedCategoryIds.add(childCategoryId2);
        expandedCategoryIds.add(childCategoryId3);
        categoryService.addExpendedCategoryIds(parentCategoryId, expandedCategoryIds);

        Set<Long> expandedCatgoryidsFromCache = categoryService.getExpandedCategoryIds(parentCategoryId);

        assertEquals(expandedCategoryIds, expandedCatgoryidsFromCache);

        assertTrue(categoryService.isExistsInExpandedCategoryIds(parentCategoryId, childCategoryId1));
        assertTrue(categoryService.isExistsInExpandedCategoryIds(parentCategoryId, childCategoryId2));
        assertTrue(categoryService.isExistsInExpandedCategoryIds(parentCategoryId, childCategoryId3));
        assertFalse(categoryService.isExistsInExpandedCategoryIds(parentCategoryId, childCategoryIdNotInExpandedList));

    }

    @Test
    public void test02_cachePluginCategories() {
        Long publicationId = 1L;
        String pluginName = "My Plugin";
        Long publicationIdNotCached = 2L;
        String pluginNameNotCached = "My Plugin Not Cached";

        String pluginCategory1 = "Cat1P";
        String pluginCategory2 = "Cat2P";
        String pluginCategory3 = "Cat3P";

        Set<String> pluginCategories = new HashSet<String>();
        pluginCategories.add(pluginCategory1);
        pluginCategories.add(pluginCategory2);
        pluginCategories.add(pluginCategory3);


        categoryService.cachePluginCategories(publicationId, pluginName, pluginCategories);

        Set<String> pluginCategoriesFromCache = categoryService.getCachedPluginCategories(publicationId, pluginName);
        assertEquals(pluginCategories, pluginCategoriesFromCache);

        pluginCategoriesFromCache = categoryService.getCachedPluginCategories(publicationIdNotCached, pluginName);
        assertNull(pluginCategoriesFromCache);
        pluginCategoriesFromCache = categoryService.getCachedPluginCategories(publicationId, pluginNameNotCached);
        assertNull(pluginCategoriesFromCache);
        pluginCategoriesFromCache = categoryService.getCachedPluginCategories(publicationIdNotCached, pluginNameNotCached);
        assertNull(pluginCategoriesFromCache);
    }

}
