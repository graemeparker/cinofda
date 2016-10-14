package com.byyd.middleware.common.service.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.adfonic.domain.Category;
import com.byyd.middleware.common.service.CategoryHierarchyService;
import com.byyd.middleware.common.service.CategorySearchService;
import com.byyd.middleware.common.service.CommonManager;

/**
 * Caching implementation of the CategoryHierarchyService.  It loads the
 * category hierarchy at initialization, and reloads it occasionally
 * according to a configurable period.
 */
public class CachingCategoryService implements CategorySearchService, CategoryHierarchyService {
    
    private static final transient Logger LOG = Logger.getLogger(CachingCategoryService.class.getName());

    private final CommonManager commonManager;
    private final int reloadPeriodSec;
    private final AtomicReference<CategoryCache> cacheRef = new AtomicReference<CategoryCache>();
    private Timer reloadTimer;

    private final Comparator<Category> caseSensitiveHierarchicalNameComparator = new Comparator<Category>() {
        @Override
        public int compare(Category c1, Category c2) {
            return compareHierarchiesByName(getHierarchy(c1), getHierarchy(c2), true);
        }
    };

    private final Comparator<Category> caseInsensitiveHierarchicalNameComparator = new Comparator<Category>() {
        @Override
        public int compare(Category c1, Category c2) {
            return compareHierarchiesByName(getHierarchy(c1), getHierarchy(c2), false);
        }
    };

    public CachingCategoryService(CommonManager commonManager, int reloadPeriodSec) {
        this.commonManager = commonManager;
        this.reloadPeriodSec = reloadPeriodSec;
    }

    @PostConstruct
    public void initialize() {
        LOG.info("Initializing");
        reloadCategoryData();

        if (reloadPeriodSec <= 0) {
            LOG.warning("Periodic reload disabled");
        } else {
            LOG.info("Starting reload timer with reloadPeriodSec=" + reloadPeriodSec);
            reloadTimer = new Timer(getClass().getName() + " Reload Timer", true);
            reloadTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Reload timer kicked in, reloading now");
                        }
                        // Since this is being run by the timer thread, if it throws
                        // then nobody will know...so we try/catch and log.
                        try {
                            reloadCategoryData();
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Reload failed", e);
                        }
                    }
                }, reloadPeriodSec * 1000L, reloadPeriodSec * 1000L);
        }
    }

    @PreDestroy
    public void destroy() {
        if (reloadTimer != null) {
            LOG.info("Stopping reload timer");
            reloadTimer.cancel();
        }
    }

    private synchronized void reloadCategoryData() {
        LOG.info("Reloading category data");

        CategoryCache cache = new CategoryCache();
        
        // First pass queries all categories and maps them all by id
        Map<Long,Category> categoriesById = new HashMap<Long,Category>();
        for (Category category : commonManager.getAllCategories()) {
            if (Category.NOT_CATEGORIZED_NAME.equals(category.getName())) {
                continue; // filter this out of our results
            }
            categoriesById.put(category.getId(), category);
            cache.categories.add(category);
            cache.categoriesByLowerName.put(category.getName().toLowerCase(), category);
        }

        // Second pass through the query results recursively loads hierarchies
        for (Category category : cache.categories) {
            List<Category> hierarchy = new LinkedList<Category>();
            hierarchy.add(category);
            for (Category parent = category.getParent(); parent != null; parent = parent.getParent()) {
                ((LinkedList<Category>) hierarchy).addFirst(categoriesById.get(parent.getId()));
            }
            cache.hierarchiesByCategory.put(category, hierarchy);
        }
        
        cacheRef.set(cache); // atomically swap in the new cache
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loaded " + cache.categories.size() + " categories");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public List<Category> getCategoriesStartingWith(String startingWith, boolean caseSensitive, int maxResults) {
        List<Category> matches = new ArrayList<Category>();
        if (caseSensitive) {
            // We can just check category names directly
            findCategoryStartingWith(matches, startingWith, maxResults);
        } else {
            // We'll use our lowercase mappings
            findCategoryStartingWithLowercase(startingWith, maxResults, matches);
        }
        return matches;
    }

    private void findCategoryStartingWith(List<Category> matches, String startingWith, int maxResults) {
        for (Category category : cacheRef.get().categories) {
            if (category.getName().startsWith(startingWith)) {
                matches.add(category);
                if (maxResults > 0 && matches.size() == maxResults) {
                    break;
                }
            }
        }
    }
    
    private void findCategoryStartingWithLowercase(String startingWith, int maxResults, List<Category> matches) {
        String lowerStartingWith = startingWith.toLowerCase();
        for (Map.Entry<String,Category> entry : cacheRef.get().categoriesByLowerName.entrySet()) {
            if (entry.getKey().startsWith(lowerStartingWith)) {
                matches.add(entry.getValue());
                if (maxResults > 0 && matches.size() == maxResults) {
                    break;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getHierarchicalName(Category category, String separator) {
        // First get the pre-cached hierarchy
        List<Category> hierarchy = getHierarchy(category);
        if (hierarchy.size() == 1) {
            // It's just the one category, no need to build anything
            return category.getName();
        }

        // Build the hierarchical name with the supplied separator
        StringBuilder bld = new StringBuilder();
        for (Category node : hierarchy) {
            if (bld.length() > 0) {
                bld.append(separator);
            }
            bld.append(node.getName());
        }
        return bld.toString();
    }

    /** {@inheritDoc} */
    @Override
    public List<Category> getHierarchy(Category category) {
        List<Category> hierarchy = cacheRef.get().hierarchiesByCategory.get(category);
        if (hierarchy == null) {
            // The caller must have passed us junk
            LOG.fine("No hierarchy found for Category id=" + category.getId());
            return new ArrayList<Category>(0);
        }
        // Return a copy so the caller can modify it, sort it, etc.
        return new ArrayList<Category>(hierarchy);
    }

    /** {@inheritDoc} */
    @Override
    public void sortCategoriesByHierarchicalName(List<Category> categoriesToSort, boolean caseSensitive) {
        if (caseSensitive) {
            Collections.sort(categoriesToSort, caseSensitiveHierarchicalNameComparator);
        } else {
            Collections.sort(categoriesToSort, caseInsensitiveHierarchicalNameComparator);
        }
    }

    /*package*/ static int compareHierarchiesByName(List<Category> h1, List<Category> h2, boolean caseSensitive) {
        for (int idx = 0; idx < h1.size(); ++idx) {
            if (idx >= h2.size()) {
                return 1; // h1 is longer than h2...so h2 comes first, it's a parent of h1
            }
            String name1 = h1.get(idx).getName();
            String name2 = h2.get(idx).getName();
            int c;
            if (caseSensitive) {
                c = name1.compareTo(name2);
            } else {
                c = String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
            }
            if (c != 0) {
                return c;
            }
        }
        // Everything is equal up to this point...but if h2 is longer than h1
        // then h1 comes first.  Otherwise they're identical.
        return h2.size() > h1.size() ? -1 : 0;
    }

    private static final class CategoryCache {
        private final Set<Category> categories = new HashSet<Category>();
        private final Map<String,Category> categoriesByLowerName = new TreeMap<String,Category>();
        private final Map<Category,List<Category>> hierarchiesByCategory = new HashMap<Category,List<Category>>();
    }
}
