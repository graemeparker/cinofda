package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestAgeRangeTargetingLogic {
    static final int[][] TEST_AGE_RANGES = new int[][] { { 0, 75, 0, 75 }, { 0, 75, 0, 40 }, { 0, 75, 40, 40 }, { 0, 40, 30, 30 }, { 0, 40, 20, 20 }, { 0, 40, 20, 30 },
            { 0, 40, 30, 40 }, { 0, 40, 30, 50 }, { 0, 40, 30, 75 }, { 18, 35, 16, 16 }, { 18, 35, 18, 18 }, { 18, 35, 20, 20 }, { 18, 35, 26, 26 }, { 18, 35, 30, 30 },
            { 18, 35, 35, 35 }, { 18, 35, 50, 50 }, { 18, 35, 20, 30 }, { 18, 35, 20, 25 }, { 18, 35, 30, 40 }, { 18, 35, 30, 50 }, { 19, 21, 20, 20 }, { 15, 25, 20, 20 },
            { 10, 30, 20, 20 }, { 18, 25, 18, 25 }, { 18, 30, 18, 30 }, { 18, 35, 18, 35 }, { 18, 40, 18, 40 }, { 18, 60, 18, 60 }, { 19, 21, 19, 19 }, { 19, 21, 20, 20 },
            { 19, 21, 21, 21 }, { 19, 21, 19, 21 }, { 1, 75, 1, 75 }, { 19, 21, 20, 22 }, { 19, 21, 21, 23 }, { 19, 24, 19, 20 }, { 19, 24, 20, 21 }, { 19, 24, 21, 22 },
            { 19, 24, 22, 23 }, { 19, 24, 23, 24 }, { 25, 25, 25, 25 }, };

    @Test
    public void testCalculateAgeRangeFactor() {
        java.text.DecimalFormat fmt = new java.text.DecimalFormat("0.00");
        for (int[] row : TEST_AGE_RANGES) {
            int segAgeMin = row[0];
            int segAgeMax = row[1];
            int pubAgeMin = row[2];
            int pubAgeMax = row[3];
            double ageRangeFactor = AgeRangeTargetingLogic.calculateAgeRangeFactor(segAgeMin, segAgeMax, pubAgeMin, pubAgeMax);
            double reverseAgeRangeFactor = AgeRangeTargetingLogic.calculateAgeRangeFactor(pubAgeMin, pubAgeMax, segAgeMin, segAgeMax);
            System.out.println("====> seg=" + segAgeMin + "-" + segAgeMax + ", pub=" + pubAgeMin + "-" + pubAgeMax + ", factor=" + fmt.format(ageRangeFactor));
            assertEquals(ageRangeFactor, reverseAgeRangeFactor, 0.000000001);
        }
    }

    @Test
    public void testAreAgeRangesEligible() {
        int segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge;

        // Test 1: segment has single age value, less than pub.minAge
        segmentMinAge = 20;
        segmentMaxAge = 20;
        pubMinAge = 30;
        pubMaxAge = 40;
        assertFalse(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 2: segment has single age value, greater than pub.maxAge
        segmentMinAge = 50;
        segmentMaxAge = 50;
        pubMinAge = 30;
        pubMaxAge = 40;
        assertFalse(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 3: segment has single age value, within pub age range
        segmentMinAge = 35;
        segmentMaxAge = 35;
        pubMinAge = 30;
        pubMaxAge = 40;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 4: pub has single age value, less than segment.minAge
        pubMinAge = 20;
        pubMaxAge = 20;
        segmentMinAge = 30;
        segmentMaxAge = 40;
        assertFalse(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 5: pub has single age value, greater than segment.maxAge
        pubMinAge = 50;
        pubMaxAge = 50;
        segmentMinAge = 30;
        segmentMaxAge = 40;
        assertFalse(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 6: pub has single age value, within segment age range
        pubMinAge = 35;
        pubMaxAge = 35;
        segmentMinAge = 30;
        segmentMaxAge = 40;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 7: both have ranges, mutually exclusive
        pubMinAge = 10;
        pubMaxAge = 20;
        segmentMinAge = 30;
        segmentMaxAge = 40;
        assertFalse(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 8: both have ranges, equal
        pubMinAge = 20;
        pubMaxAge = 40;
        segmentMinAge = 20;
        segmentMaxAge = 40;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 9: both have ranges, overlapping
        pubMinAge = 20;
        pubMaxAge = 30;
        segmentMinAge = 25;
        segmentMaxAge = 35;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 10: both have ranges, one includes the other
        pubMinAge = 20;
        pubMaxAge = 50;
        segmentMinAge = 30;
        segmentMaxAge = 40;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));

        // Test 11: both have ranges, one includes the other, shared boundary
        pubMinAge = 20;
        pubMaxAge = 40;
        segmentMinAge = 20;
        segmentMaxAge = 30;
        assertTrue(AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge));
    }

    @Test
    public void testCoerceIntoRange() {
        assertEquals(AgeRangeTargetingLogic.MIN_AGE, AgeRangeTargetingLogic.coerceIntoRange(Integer.MIN_VALUE));
        assertEquals(AgeRangeTargetingLogic.MIN_AGE, AgeRangeTargetingLogic.coerceIntoRange(0));
        assertEquals(AgeRangeTargetingLogic.MIN_AGE, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MIN_AGE - 1));
        assertEquals(AgeRangeTargetingLogic.MIN_AGE, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MIN_AGE));
        assertEquals(AgeRangeTargetingLogic.MIN_AGE + 1, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MIN_AGE + 1));
        assertEquals(AgeRangeTargetingLogic.MAX_AGE, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MAX_AGE));
        assertEquals(AgeRangeTargetingLogic.MAX_AGE, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MAX_AGE + 1));
        assertEquals(AgeRangeTargetingLogic.MAX_AGE - 1, AgeRangeTargetingLogic.coerceIntoRange(AgeRangeTargetingLogic.MAX_AGE - 1));
        assertEquals(AgeRangeTargetingLogic.MAX_AGE, AgeRangeTargetingLogic.coerceIntoRange(1000));
        assertEquals(AgeRangeTargetingLogic.MAX_AGE, AgeRangeTargetingLogic.coerceIntoRange(Integer.MAX_VALUE));
    }
}
