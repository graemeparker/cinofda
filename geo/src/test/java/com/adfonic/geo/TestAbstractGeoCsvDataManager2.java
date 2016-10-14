package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestAbstractGeoCsvDataManager2 extends AbstractAdfonicTest {
    @Test
    public void testGetGridKey() {
        assertEquals("0.0x0.0", AbstractGeoCsvDataManager2.getGridKey(0.0, 0.0));
        assertEquals("0.0x0.0", AbstractGeoCsvDataManager2.getGridKey(0.4999, 0.4999));
        assertEquals("37.0x-117.5", AbstractGeoCsvDataManager2.getGridKey(37.45802, -117.06192));
        assertEquals("89.5x-180.0", AbstractGeoCsvDataManager2.getGridKey(90.0, 180.0));
        assertEquals("-90.0x-180.0", AbstractGeoCsvDataManager2.getGridKey(-90.0, -179.999));
        assertEquals("-90.0x-180.0", AbstractGeoCsvDataManager2.getGridKey(-90.0, -179.5001));
        assertEquals("45.0x-100.0", AbstractGeoCsvDataManager2.getGridKey(45.499, -99.50001));
    }

    @Test
    public void testRoundDownToNearest() {
        assertEquals(9.0, AbstractGeoCsvDataManager2.roundDownToNearest(9.278, 0.5), 0.0);
        assertEquals(9.5, AbstractGeoCsvDataManager2.roundDownToNearest(9.778, 0.5), 0.0);
        assertEquals(9.5, AbstractGeoCsvDataManager2.roundDownToNearest(9.999999999, 0.5), 0.0);
        assertEquals(0.0, AbstractGeoCsvDataManager2.roundDownToNearest(0.0, 0.5), 0.0);
        assertEquals(0.0, AbstractGeoCsvDataManager2.roundDownToNearest(0.1, 0.5), 0.0);
        assertEquals(0.5, AbstractGeoCsvDataManager2.roundDownToNearest(0.5, 0.5), 0.0);
        assertEquals(-0.5, AbstractGeoCsvDataManager2.roundDownToNearest(-0.1, 0.5), 0.0);
        assertEquals(-1.0, AbstractGeoCsvDataManager2.roundDownToNearest(-0.5, 0.5), 0.0);
        assertEquals(179.5, AbstractGeoCsvDataManager2.roundDownToNearest(179.8, 0.5), 0.0);
        assertEquals(-180.0, AbstractGeoCsvDataManager2.roundDownToNearest(-179.8, 0.5), 0.0);
        assertEquals(180.0, AbstractGeoCsvDataManager2.roundDownToNearest(180.0, 0.5), 0.0);
        assertEquals(-10.5, AbstractGeoCsvDataManager2.roundDownToNearest(-10.123, 0.5), 0.0);
        assertEquals(-10.5, AbstractGeoCsvDataManager2.roundDownToNearest(-10.0, 0.5), 0.0);
        assertEquals(-11.0, AbstractGeoCsvDataManager2.roundDownToNearest(-10.5, 0.5), 0.0);
        assertEquals(-11.0, AbstractGeoCsvDataManager2.roundDownToNearest(-10.5000001, 0.5), 0.0);
    }

    @Test
    public void testGetNearestGridKeys_01_middle() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(38.625, -117.409, 0.5));
        assertEquals(9, gridKeys.size());
        assertTrue(gridKeys.contains("38.0x-117.5"));
        assertTrue(gridKeys.contains("38.0x-118.0"));
        assertTrue(gridKeys.contains("38.0x-117.0"));
        assertTrue(gridKeys.contains("38.5x-117.5"));
        assertTrue(gridKeys.contains("38.5x-118.0"));
        assertTrue(gridKeys.contains("38.5x-117.0"));
        assertTrue(gridKeys.contains("39.0x-117.5"));
        assertTrue(gridKeys.contains("39.0x-118.0"));
        assertTrue(gridKeys.contains("39.0x-117.0"));
    }
    
    @Test
    public void testGetNearestGridKeys_02_north_pole() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(89.9, -117.409, 0.5));
        assertEquals(6, gridKeys.size());
        assertTrue(gridKeys.contains("89.5x-118.0"));
        assertTrue(gridKeys.contains("89.5x-117.5"));
        assertTrue(gridKeys.contains("89.5x-117.0"));
        assertTrue(gridKeys.contains("89.0x-118.0"));
        assertTrue(gridKeys.contains("89.0x-117.5"));
        assertTrue(gridKeys.contains("89.0x-117.0"));
    }
    
    @Test
    public void testGetNearestGridKeys_03_south_pole() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(-89.9, 45.522, 0.5));
        assertEquals(6, gridKeys.size());
        assertTrue(gridKeys.contains("-90.0x45.0"));
        assertTrue(gridKeys.contains("-90.0x45.5"));
        assertTrue(gridKeys.contains("-90.0x46.0"));
        assertTrue(gridKeys.contains("-89.5x45.0"));
        assertTrue(gridKeys.contains("-89.5x45.5"));
        assertTrue(gridKeys.contains("-89.5x46.0"));
    }
    
    @Test
    public void testGetNearestGridKeys_04_near_date_line() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(30.0, 179.75, 0.5));
        assertEquals(9, gridKeys.size());
        assertTrue(gridKeys.contains("30.0x179.0"));
        assertTrue(gridKeys.contains("30.0x179.5"));
        assertTrue(gridKeys.contains("30.0x-180.0"));
        assertTrue(gridKeys.contains("30.5x179.0"));
        assertTrue(gridKeys.contains("30.5x179.5"));
        assertTrue(gridKeys.contains("30.5x-180.0"));
        assertTrue(gridKeys.contains("29.5x179.0"));
        assertTrue(gridKeys.contains("29.5x179.5"));
        assertTrue(gridKeys.contains("29.5x-180.0"));
    }
    
    @Test
    public void testGetNearestGridKeys_05_equator_prime_meridian() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(0.0, 0.0, 0.5));
        assertEquals(9, gridKeys.size());
        assertTrue(gridKeys.contains("0.0x-1.0"));
        assertTrue(gridKeys.contains("0.0x0.0"));
        assertTrue(gridKeys.contains("0.0x0.5"));
        assertTrue(gridKeys.contains("0.5x-1.0"));
        assertTrue(gridKeys.contains("0.5x0.0"));
        assertTrue(gridKeys.contains("0.5x0.5"));
        assertTrue(gridKeys.contains("-1.0x-1.0"));
        assertTrue(gridKeys.contains("-1.0x0.0"));
        assertTrue(gridKeys.contains("-1.0x0.5"));
        
    }
    
    @Test
    public void testGetNearestGridKeys_06_prime_meridian_northern_hemi() {
        List<String> gridKeys = Arrays.asList(AbstractGeoCsvDataManager2.getNearestGridKeys(52.001, 0.0, 0.5));
        assertEquals(9, gridKeys.size());
        assertTrue(gridKeys.contains("51.5x0.5"));
        assertTrue(gridKeys.contains("51.5x0.0"));
        assertTrue(gridKeys.contains("51.5x-1.0"));
        assertTrue(gridKeys.contains("52.0x0.5"));
        assertTrue(gridKeys.contains("52.0x0.0"));
        assertTrue(gridKeys.contains("52.5x-1.0"));
        assertTrue(gridKeys.contains("52.0x0.5"));
        assertTrue(gridKeys.contains("52.0x0.0"));
        assertTrue(gridKeys.contains("52.0x-1.0"));
    }

    @Test
    public void testCoerceLongitudeIntoRange() {
        assertEquals(0.0, AbstractGeoCsvDataManager2.coerceLongitudeIntoRange(0.0), 0.0);
        assertEquals(20.1234, AbstractGeoCsvDataManager2.coerceLongitudeIntoRange(20.1234), 0.0);
        assertEquals(-179.2, AbstractGeoCsvDataManager2.coerceLongitudeIntoRange(180.8), 0.0);
        assertEquals(179.6, AbstractGeoCsvDataManager2.coerceLongitudeIntoRange(-180.4), 0.0);
    }
}
