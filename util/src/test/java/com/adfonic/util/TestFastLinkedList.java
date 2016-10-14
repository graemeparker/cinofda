package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestFastLinkedList {
    @Test
    public void test() throws Exception {
        List<String> list = new FastLinkedList<String>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        Iterator<String> iter = list.iterator();
        assertFalse(iter.hasNext());
        try {
            iter.next();
            throw new Exception("iter.next should have failed on an empty list");
        } catch (Exception e) {
            // good
        }

        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());

        final int numElements = 10000;
        for (int k = 0; k < numElements; ++k) {
            assertEquals(k, list.size());
            list.add(String.valueOf(k));
        }
        assertEquals(numElements, list.size());

        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());

        for (int k = 0; k < numElements; ++k) {
            assertEquals(k, list.size());
            list.add(String.valueOf(k));
        }
        assertEquals(numElements, list.size());

        int counter = 0;
        for (iter = list.iterator(); iter.hasNext();) {
            String value = iter.next();
            assertEquals(String.valueOf(counter), value);
            ++counter;
        }
        assertEquals(numElements, counter);

        final int removeEveryNth = 10;
        counter = 0;
        int removedCount = 0;
        for (iter = list.iterator(); iter.hasNext();) {
            iter.next();
            if (counter++ % removeEveryNth == 0) {
                iter.remove();
                ++removedCount;
            }
        }
        assertEquals(counter - removedCount, list.size());

        // Test removing the first node
        iter = list.iterator();
        int value = Integer.parseInt(iter.next());
        iter.remove();
        assertEquals(String.valueOf(value + 1), list.iterator().next());
        assertEquals(counter - removedCount - 1, list.size());

        // Test removing the last node
        while (iter.hasNext()) {
            iter.next();
        }
        iter.remove();
        assertEquals(counter - removedCount - 2, list.size());

        counter = 0;
        for (@SuppressWarnings("unused") String element : list) {
            ++counter;
        }
        assertEquals(list.size(), counter);

        Object[] objArray = list.toArray();
        assertEquals(list.size(), objArray.length);
        assertEquals(list.iterator().next(), objArray[0]);

        try {
            list.toArray(null);
            fail("toArray(null) isn't expected to work");
        } catch (Exception expected) {
        }

        try {
            list.toArray(new String[0]);
            fail("toArray(tooSmall) isn't expected to work");
        } catch (Exception expected) {
        }

        try {
            String[] array = new String[list.size()];
            array = list.toArray(array);
            assertEquals(list.iterator().next(), array[0]);
            assertNotNull(array[list.size() - 1]);
        } catch (Exception e) {
            e.printStackTrace();
            fail("toArray(properSize) failed");
        }

        ArrayList<String> arrayList = new ArrayList<String>();
        try {
            arrayList.addAll(list);
            assertEquals(list.size(), arrayList.size());
            assertEquals(list.iterator().next(), arrayList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("ArrayList.addAll failed");
        }

        LinkedList<String> linkedList = new LinkedList<String>();
        try {
            linkedList.addAll(list);
            assertEquals(list.size(), linkedList.size());
            assertEquals(list.iterator().next(), linkedList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("LinkedList.addAll failed");
        }

        list = new FastLinkedList<String>();
        list.add("0");
        list.add("1");
        list.add("2");
        String str = list.remove(0);
        assertEquals("0", str);
        str = list.remove(0);
        assertEquals("1", str);
        str = list.remove(0);
        assertEquals("2", str);
        assertTrue(list.isEmpty());
        try {
            list.remove(0);
            fail("remove(0) on empty list should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Good
        }

        list = new FastLinkedList<String>();
        for (int k = 0; k < 3000; ++k) {
            list.add(String.valueOf(k));
        }
        assertEquals(3000, list.size());
        iter = list.iterator();
        for (int k = 0; iter.hasNext(); ++k) {
            iter.next();
            if (k % 3 == 0) {
                iter.remove();
            }
        }
        assertEquals(2000, list.size());

        for (int k = 0; k < 500; ++k) {
            list.remove(0);
        }
        assertEquals(1500, list.size());

        try {
            list.remove(1);
            fail("remove(1) should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Good
        }
    }

    @Test
    public void testWithRemovalReceiver() throws Exception {
        FastLinkedList<String> removalReceiver = new FastLinkedList<String>();

        // Pass it the removal receiver
        List<String> list = new FastLinkedList<String>(removalReceiver);

        assertEquals(0, removalReceiver.size());
        assertEquals(0, list.size());

        for (int k = 0; k < 3000; ++k) {
            list.add(String.valueOf(k));
        }

        assertEquals(3000, list.size());
        assertEquals(0, removalReceiver.size());

        // Test clear, make sure they all end up in the removalReceiver
        list.clear();
        assertEquals(0, list.size());
        assertEquals(3000, removalReceiver.size());

        // Clear out the removal receiver again, starting over
        removalReceiver.clear();
        assertEquals(0, removalReceiver.size());

        for (int k = 0; k < 3000; ++k) {
            list.add(String.valueOf(k));
        }
        assertEquals(3000, list.size());

        Iterator<String> iter = list.iterator();
        for (int k = 0; iter.hasNext(); ++k) {
            iter.next();
            if (k % 3 == 0) {
                iter.remove();
            }
        }
        assertEquals(2000, list.size());
        assertEquals(1000, removalReceiver.size());

        for (int k = 0; k < 500; ++k) {
            list.remove(0);
        }
        assertEquals(1500, list.size());
        assertEquals(1500, removalReceiver.size());
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<Integer>();
        for (int k = 0; k < 100000; ++k) {
            FastLinkedList<String> list = new FastLinkedList<String>();
            int hashCode = list.hashCode();
            // Assert consistency
            assertEquals(hashCode, list.hashCode());
            // Assert uniqueness(ish)
            assertTrue(!hashCodes.contains(hashCode));
            hashCodes.add(hashCode);
        }
    }
}
