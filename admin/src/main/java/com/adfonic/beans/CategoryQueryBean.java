package com.adfonic.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.commons.collections.CollectionUtils;

import com.adfonic.domain.Category;

/*
 * for auto-complete categories list
 *
 * limit 50
 */
@RequestScoped
@ManagedBean(name="categoryQueryBean")
public class CategoryQueryBean extends BaseBean {
    private static final int MAX_RESULTS = 50;
    public static final String CATEGORY_SEPARATOR = " > ";
    private String search;
    private Map<Category,String> labelMap = new HashMap<Category,String>();

    public List<Category> doQuery(String search) {
        List<Category> results = categorySearchService.getCategoriesStartingWith(search, false, MAX_RESULTS);
        if (CollectionUtils.isNotEmpty(results)) {
            for (Category c : results) {
                labelMap.put(c, categoryHierarchyService.getHierarchicalName(c, CATEGORY_SEPARATOR));
            }
            categoryHierarchyService.sortCategoriesByHierarchicalName(results, false);
        }
        return results;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Map<Category, String> getLabelMap() {
        return labelMap;
    }
}
