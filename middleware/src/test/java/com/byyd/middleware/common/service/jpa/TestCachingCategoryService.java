package com.byyd.middleware.common.service.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.Category;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.common.service.jpa.CachingCategoryService;

public class TestCachingCategoryService extends AbstractAdfonicTest {
    
    private CommonManager commonManager;

    @Before
    public void setup() {
        commonManager = mock(CommonManager.class);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSearchAndHierarchy() throws InterruptedException {
        final Category c1 = mock(Category.class, "c1");
        final Category c1_1 = mock(Category.class, "c1_1");
        final Category c1_1_1 = mock(Category.class, "c1_1_1");
        final Category c1_2 = mock(Category.class, "c1_2");
        final Category c2 = mock(Category.class, "c2");
        final Category c2_1 = mock(Category.class, "c2_1");
        final Category c2_2 = mock(Category.class, "c2_2");
        final Category notCategorized = mock(Category.class, "notCategorized");

        final Category a = mock(Category.class, "a");
        final Category a_b = mock(Category.class, "a_b");
        final Category b = mock(Category.class, "b");
        final Category b_a = mock(Category.class, "b_a");
        
        final List<Category> allCategories = new ArrayList<Category>() {{
                add(c1);
                add(c1);
                add(c1_1);
                add(c1_1_1);
                add(c1_2);
                add(c2);
                add(c2_1);
                add(c2_2);
                add(notCategorized);
                add(a);
                add(a_b);
                add(b);
                add(b_a);
            }};
        
        final String c1name = randomAlphaNumericString(10);
        final String c1_1name = randomAlphaNumericString(10);
        final String c1_1_1name = randomAlphaNumericString(10);
        final String c1_2name = randomAlphaNumericString(10);
        final String c2name = randomAlphaNumericString(10);
        final String c2_1name = randomAlphaNumericString(10);
        final String c2_2name = randomAlphaNumericString(10);
        final String notCategorizedName = Category.NOT_CATEGORIZED_NAME;
        
        final String a_name = "a";
        final String a_b_name = "b in a";
        final String b_name = "b";
        final String b_a_name = "a in b";

        final String separator = " > ";

        // Expected hierarchical names
        final String c1hn = c1name;
        final String c1_1hn = c1name + separator + c1_1name;
        final String c1_1_1hn = c1name + separator + c1_1name + separator + c1_1_1name;
        final String c1_2hn = c1name + separator + c1_2name;
        final String c2hn = c2name;
        final String c2_1hn = c2name + separator + c2_1name;
        final String c2_2hn = c2name + separator + c2_2name;

        long nextId = randomLong();
        final long c1id = nextId++;
        final long c1_1id = nextId++;
        final long c1_1_1id = nextId++;
        final long c1_2id = nextId++;
        final long c2id = nextId++;
        final long c2_1id = nextId++;
        final long c2_2id = nextId++;
        final long notCategorizedId = nextId++;

        final long a_id = nextId++;
        final long a_b_id = nextId++;
        final long b_id = nextId++;
        final long b_a_id = nextId++;
        
        expect(new Expectations() {{
            allowing (commonManager).getAllCategories(); will(returnValue(allCategories));
            
            allowing (c1).getParent(); will(returnValue(null));
            allowing (c1_1).getParent(); will(returnValue(c1));
            allowing (c1_1_1).getParent(); will(returnValue(c1_1));
            allowing (c1_2).getParent(); will(returnValue(c1));
            allowing (c2).getParent(); will(returnValue(null));
            allowing (c2_1).getParent(); will(returnValue(c2));
            allowing (c2_2).getParent(); will(returnValue(c2));

            allowing (a).getParent(); will(returnValue(null));
            allowing (a_b).getParent(); will(returnValue(a));
            allowing (b).getParent(); will(returnValue(null));
            allowing (b_a).getParent(); will(returnValue(b));

            allowing (c1).getName(); will(returnValue(c1name));
            allowing (c1_1).getName(); will(returnValue(c1_1name));
            allowing (c1_1_1).getName(); will(returnValue(c1_1_1name));
            allowing (c1_2).getName(); will(returnValue(c1_2name));
            allowing (c2).getName(); will(returnValue(c2name));
            allowing (c2_1).getName(); will(returnValue(c2_1name));
            allowing (c2_2).getName(); will(returnValue(c2_2name));
            allowing (notCategorized).getName(); will(returnValue(notCategorizedName));

            allowing (a).getName(); will(returnValue(a_name));
            allowing (a_b).getName(); will(returnValue(a_b_name));
            allowing (b).getName(); will(returnValue(b_name));
            allowing (b_a).getName(); will(returnValue(b_a_name));

            allowing (c1).getId(); will(returnValue(c1id));
            allowing (c1_1).getId(); will(returnValue(c1_1id));
            allowing (c1_1_1).getId(); will(returnValue(c1_1_1id));
            allowing (c1_2).getId(); will(returnValue(c1_2id));
            allowing (c2).getId(); will(returnValue(c2id));
            allowing (c2_1).getId(); will(returnValue(c2_1id));
            allowing (c2_2).getId(); will(returnValue(c2_2id));
            allowing (notCategorized).getId(); will(returnValue(notCategorizedId));

            allowing (a).getId(); will(returnValue(a_id));
            allowing (a_b).getId(); will(returnValue(a_b_id));
            allowing (b).getId(); will(returnValue(b_id));
            allowing (b_a).getId(); will(returnValue(b_a_id));
        }});
        
        CachingCategoryService ccs = new CachingCategoryService(commonManager, -1);
        ccs.initialize();

        List<Category> results;

        // Exact match test, case sensitive, no limit
        results = ccs.getCategoriesStartingWith(c1name, true, 0);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));

        // Exact match test, case sensitive, limit 1 (force the break)
        results = ccs.getCategoriesStartingWith(c1name, true, 1);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));
        
        // Exact match test, case sensitive, limit high (no break)
        results = ccs.getCategoriesStartingWith(c1name, true, 100);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));
        
        // Exact match test, case insensitive, no limit
        results = ccs.getCategoriesStartingWith(c1name, false, 0);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));

        // Exact match test, case insensitive, limit 1 (force the break)
        results = ccs.getCategoriesStartingWith(c1name, false, 1);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));
        
        // Exact match test, case insensitive, limit high (no break)
        results = ccs.getCategoriesStartingWith(c1name, false, 100);
        assertEquals(1, results.size());
        assertTrue(results.contains(c1));

        // Partial match test
        String startingWith = c1name.substring(0, 1);
        results = ccs.getCategoriesStartingWith(startingWith, false, 0);
        assertTrue(results.contains(c1));

        // No match test
        String mismatch = randomAlphaNumericString(20); // longer than all names
        results = ccs.getCategoriesStartingWith(mismatch, false, 0);
        assertTrue(results.isEmpty());

        // Hierarchical name tests
        assertEquals(c1hn, ccs.getHierarchicalName(c1, separator));
        assertEquals(c1_1hn, ccs.getHierarchicalName(c1_1, separator));
        assertEquals(c1_1_1hn, ccs.getHierarchicalName(c1_1_1, separator));
        assertEquals(c1_2hn, ccs.getHierarchicalName(c1_2, separator));
        assertEquals(c2hn, ccs.getHierarchicalName(c2, separator));
        assertEquals(c2_1hn, ccs.getHierarchicalName(c2_1, separator));
        assertEquals(c2_2hn, ccs.getHierarchicalName(c2_2, separator));

        // Hierarchy tests
        assertEquals(1, ccs.getHierarchy(c1).size());
        assertEquals(c1, ccs.getHierarchy(c1).get(0));

        assertEquals(2, ccs.getHierarchy(c1_1).size());
        assertEquals(c1, ccs.getHierarchy(c1_1).get(0));
        assertEquals(c1_1, ccs.getHierarchy(c1_1).get(1));

        assertEquals(3, ccs.getHierarchy(c1_1_1).size());
        assertEquals(c1, ccs.getHierarchy(c1_1_1).get(0));
        assertEquals(c1_1, ccs.getHierarchy(c1_1_1).get(1));
        assertEquals(c1_1_1, ccs.getHierarchy(c1_1_1).get(2));

        assertTrue(ccs.getHierarchy(notCategorized).isEmpty());

        // Sort by hierarchical name
        for (boolean caseSensitive : new boolean[] { false, true }) {
            List<Category> sortMe = new ArrayList<Category>() {{
                    add(b_a);
                    add(a);
                    add(b);
                    add(a_b);
                }};
            ccs.sortCategoriesByHierarchicalName(sortMe, caseSensitive);
            assertEquals(a, sortMe.get(0));
            assertEquals(a_b, sortMe.get(1));
            assertEquals(b, sortMe.get(2));
            assertEquals(b_a, sortMe.get(3));
        }

        // Test destroy
        ccs.destroy();

        // Now create it with a reload period
        ccs = new CachingCategoryService(commonManager, 2);
        ccs.initialize();
        // Sleep long enough that it should have reloaded
        Thread.sleep(3000);
        ccs.destroy();
    }

    @Test
    @SuppressWarnings("serial")
    public void testCompareHierarchiesByName() {
        final Category a = mock(Category.class, "a");
        final Category b = mock(Category.class, "b");
        final Category c = mock(Category.class, "c");
        final Category d = mock(Category.class, "d");
        
        expect(new Expectations() {{
            allowing (a).getName(); will(returnValue("a"));
            allowing (b).getName(); will(returnValue("b"));
            allowing (c).getName(); will(returnValue("c"));
            allowing (d).getName(); will(returnValue("d"));
        }});
        
        // Test compare method standalone with differently sized lists
        List<Category> list1 = new ArrayList<Category>() {{
                    add(a);
                    add(b);
                    add(c);
            }};
        List<Category> list2 = new ArrayList<Category>() {{
                    add(a);
                    add(b);
                    add(c);
                    add(d);
            }};
        // Same size list as list2, different sort order
        List<Category> list3 = new ArrayList<Category>() {{
                    add(a);
                    add(b);
                    add(d);
                    add(c);
            }};

        for (boolean caseSensitive : new boolean[] { false, true }) {
            assertEquals(0, CachingCategoryService.compareHierarchiesByName(list1, list1, caseSensitive));
            assertEquals(0, CachingCategoryService.compareHierarchiesByName(list2, list2, caseSensitive));
            assertTrue(CachingCategoryService.compareHierarchiesByName(list1, list2, caseSensitive) < 0);
            assertTrue(CachingCategoryService.compareHierarchiesByName(list2, list1, caseSensitive) > 0);
            assertTrue(CachingCategoryService.compareHierarchiesByName(list2, list3, caseSensitive) < 0);
            assertTrue(CachingCategoryService.compareHierarchiesByName(list3, list2, caseSensitive) > 0);
        }
    }
}
