package com.byyd.middleware.common.service;

import java.util.List;

import com.adfonic.domain.Category;

/**
 * Service that provides simple category search methods
 */
public interface CategorySearchService {
    /**
     * Perform a search for categories starting with a fragment of a name.
     * This is intended for use by UI components that support the "type-ahead"
     * search metaphor.
     *
     * @param startingWith the starting name fragment of the categories to search for
     * @param caseSensitive whether or not the search should be case-sensitive
     * @param maxResults the maximum number of results to return
     * @return a list of matching categories sorted by name (with sorting case
     * sensitivity according to the caseSensitive param)
     */
    List<Category> getCategoriesStartingWith(String startingWith, boolean caseSensitive, int maxResults);
}
