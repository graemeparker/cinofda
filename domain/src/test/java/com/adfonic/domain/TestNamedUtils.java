package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestNamedUtils {
    private static class NamedImpl implements Named {
        private String name;

        private NamedImpl(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

	@Test
	@SuppressWarnings("unchecked")
    public void testContains() {
        assertFalse(NamedUtils.contains(Collections.EMPTY_SET, "anything"));
        assertFalse(NamedUtils.contains(Collections.EMPTY_LIST, "anything"));
        
        Set<NamedImpl> set = new HashSet<NamedImpl>();
        set.add(new NamedImpl("whatever"));
        set.add(new NamedImpl("yadda yadda"));
        assertFalse(NamedUtils.contains(set, "blah"));
        assertTrue(NamedUtils.contains(set, "whatever"));
        assertTrue(NamedUtils.contains(set, "yadda yadda"));
        assertFalse(NamedUtils.contains(set, "Whatever"));
        assertFalse(NamedUtils.contains(set, "Yadda yadda"));
        
        List<NamedImpl> list = new ArrayList<NamedImpl>();
        list.add(new NamedImpl("whatever"));
        list.add(new NamedImpl("yadda yadda"));
        assertFalse(NamedUtils.contains(list, "blah"));
        assertTrue(NamedUtils.contains(list, "whatever"));
        assertTrue(NamedUtils.contains(list, "yadda yadda"));
        assertFalse(NamedUtils.contains(list, "Whatever"));
        assertFalse(NamedUtils.contains(list, "Yadda yadda"));
    }

    @Test
    public void testNamedCollectionToString() {
        assertNull(NamedUtils.namedCollectionToString(null));
        List<NamedImpl> list = new ArrayList<NamedImpl>();
        assertEquals("", NamedUtils.namedCollectionToString(list));
        list.add(new NamedImpl("foo"));
        assertEquals("foo", NamedUtils.namedCollectionToString(list));
        list.add(new NamedImpl("bar"));
        assertEquals("foo,bar", NamedUtils.namedCollectionToString(list));
        list.add(new NamedImpl("baz"));
        assertEquals("foo,bar,baz", NamedUtils.namedCollectionToString(list));
        list.add(new NamedImpl("baz"));
        assertEquals("foo,bar,baz,baz", NamedUtils.namedCollectionToString(list));
    }
}
