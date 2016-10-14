package com.byyd.middleware.common.service;

import java.util.List;

import com.adfonic.domain.Category;

/**
 * Service that provides category access and search methods based on the
 * category hierarchy (parent/child tree).
 */
public interface CategoryHierarchyService {
    /**
     * For a given category, produce a string that shows the complete hierarchy
     * of the category, from top-level parent all the way to the child.  For
     * example, using a separator string of " > "...
     * The "Blog" category's hierarchical name is:
     *     Technology & Computing > Internet Technology > Blog
     * The "Mobile Content" category's hierarchical name is:
     *     Technology & Computing > Internet Technology > Mobile Content
     * The "Weather" category's hierarchical name is:
     *     News > National News > Weather
     *
     * @param category the category for which a hierarchical name is generated
     * @param separator the separator string to use in between each name
     * @return the hierarchical name string
     */
    String getHierarchicalName(Category category, String separator);

    /**
     * Get the complete hierarchy, starting with top-level parent and ending with
     * the child, for a given category.  For example, if you passed in the "Blog"
     * category, whose hierarchy is:
     *     Technology & Computing > Internet Technology > Blog
     * ...then you would get back a list of those three categories in that order.
     * If you passed in "Internet Technology" then you'd get back a list containing
     * the categories "Technology & Computing" and "Internet Technology".
     *
     * @param category the category for which a hierarchy is generated
     * @return the hierarchy from the top-level down to the given category
     */
    List<Category> getHierarchy(Category category);

    /**
     * Sort a list of categories by their hierarchical names.  For example,
     * if the given list contained: Auctions, Blog, Reference, Weather, the
     * list would be sorted as: Reference, Weather, Auctions, Blog, since
     * their respective hierarchical names are:
     *
     *     Arts & Entertainment > Books & Literature > Reference
     *     News > National News > Weather
     *     Shopping > Comparison Engines > Auctions
     *     Technology & Computing > Internet Technology > Blog
     *
     * @param categoriesToSort the list of categories to sort by hierarchical name
     * @param caseSensitive whether or not the sort should be case sensitive
     */
    void sortCategoriesByHierarchicalName(List<Category> categoriesToSort, boolean caseSensitive);
}
