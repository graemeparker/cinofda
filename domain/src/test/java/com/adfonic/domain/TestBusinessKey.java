package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestBusinessKey {
    public static class TestClass extends BusinessKey {
    	private static final long serialVersionUID = 1L;
        private final long id;

        public TestClass(long id) {
            this.id = id;
        }
        
        public long getId() {
            return id;
        }
    }
    
    public static class TestClass2 extends BusinessKey {
    	private static final long serialVersionUID = 1L;
        private final long id;

        public TestClass2(long id) {
            this.id = id;
        }
        
        public long getId() {
            return id;
        }
    }
    
    public static class NegativeTestClass extends BusinessKey {
    	private static final long serialVersionUID = 1L;
    	
        private final long id;

        public NegativeTestClass(long id) {
            this.id = id;
        }
        
        public long getId() {
            return id;
        }
    }
    
    public static class NamedTestClass extends TestClass implements Named {

		private static final long serialVersionUID = 1L;
		
		private final String name;

        public NamedTestClass(long id, String name) {
            super(id);
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    @Test
    public void testHashCode() {
        long idRangeSize = 1000000;
        Map<TestClass,TestClass> map = new HashMap<TestClass,TestClass>();

        // We deliberately start with 1 here instead of 0 because we know that when
        // id is 0, hashCode() doesn't return a value corresponding to the id.
        for (long id = 1; id <= idRangeSize; ++id) {
            TestClass obj = new TestClass(id);

            // This first call forces the calculation
            int hashCode = obj.hashCode();
            
            // This next call ensures that it's repeatable and that "caching" works
            assertEquals(hashCode, obj.hashCode());

            map.put(obj, obj);
        }
        
        // Enforce uniqueness
        assertEquals(idRangeSize, map.size());

        // Make sure it works as a lookup...
        for (long id = 1; id <= idRangeSize; ++id) {
            TestClass newWithSameId = new TestClass(id);
            assertNotNull(map.get(newWithSameId));
        }
    }

    @Test
    public void testEquals_null() {
        TestClass tc1 = new TestClass(123);
        assertFalse(tc1.equals(null));
    }

    @Test
    public void testEquals_this() {
        TestClass tc1 = new TestClass(123);
        assertTrue(tc1.equals(tc1));
    }

    @Test
    public void testEquals_withId0() {
        TestClass tc0a = new TestClass(0);
        TestClass tc0b = new TestClass(0);
        assertFalse(tc0a.equals(tc0b));
        assertFalse(tc0b.equals(tc0a));
    }

    @Test
    public void testEquals_differentClasses() {
        TestClass tc = new TestClass(123);
        TestClass2 tc2 = new TestClass2(123);
        assertFalse(tc.equals(tc2));
        assertFalse(tc2.equals(tc));
    }

    @Test
    public void testEquals_differentIds() {
        TestClass tc1 = new TestClass(123);
        TestClass tc2 = new TestClass(124);
        assertFalse(tc1.equals(tc2));
        assertFalse(tc2.equals(tc1));
    }

    @Test
    public void testEquals_sameIds() {
        TestClass tc1 = new TestClass(99999);
        TestClass tc2 = new TestClass(99999);
        assertTrue(tc1.equals(tc2));
        assertTrue(tc2.equals(tc1));
    }

    @Test
    public void testCompareTo() {
        TestClass tc1a = new TestClass(1234);
        TestClass tc1b = new TestClass(1234);
        TestClass tc1c = new TestClass(1234);
        TestClass tc2 = new TestClass(2000);

        assertEquals(1, tc1a.compareTo(null));
        assertEquals(1, tc2.compareTo(null));
        
        assertEquals(0, tc1a.compareTo(tc1a));
        assertEquals(0, tc2.compareTo(tc2));
        
        assertEquals(0, tc1a.compareTo(tc1b));
        assertEquals(0, tc1a.compareTo(tc1c));
        assertEquals(0, tc1b.compareTo(tc1a));
        assertEquals(0, tc1b.compareTo(tc1c));
        assertEquals(0, tc1c.compareTo(tc1a));
        assertEquals(0, tc1c.compareTo(tc1b));
        
        assertEquals(-1, tc1a.compareTo(tc2));
        assertEquals(1, tc2.compareTo(tc1a));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCompareTo_differentClasses() {
        TestClass tc1 = new TestClass(1);
        NegativeTestClass tc2 = new NegativeTestClass(1); // same id, just in case
        tc1.compareTo(tc2); // should throw IllegalArgumentException
    }

    @Test
    public void testToString() {
        assertEquals("TestBusinessKey.TestClass/1234", new TestClass(1234).toString());
        assertEquals("TestBusinessKey.TestClass/123456", new TestClass(123456).toString());
        assertEquals("TestBusinessKey.TestClass/0", new TestClass(0).toString());
        
        String name = "This is my name";
        assertEquals(name, new NamedTestClass(2345, name).toString());
        assertEquals(name, new NamedTestClass(0, name).toString());
    }
}