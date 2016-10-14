package com.adfonic.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AgeRangeTargetingLogic {
    private static final transient Logger LOG = Logger.getLogger(AgeRangeTargetingLogic.class.getName());

    // Originally in com.adfonic.domain.Segment
    public static final int MIN_AGE = 0; // displays as <16
    public static final int MAX_AGE = 75; // displays as 65+

    private static final double MAX_RANGE_SIZE = MAX_AGE - MIN_AGE;
    
    private AgeRangeTargetingLogic(){
        
    }

    /**
     * Determine whether or not a segment's age range is eligible against a
     * given publication's age range.
     * 
     * @param segmentMinAge
     *            the value of Segment.minAge
     * @param segmentMaxAge
     *            the value of Segment.maxAge
     * @param pubMinAge
     *            the value of Publication.minAge
     * @param pubMaxAge
     *            the value of Publication.maxAge
     */
    public static boolean areAgeRangesEligible(int segmentMinAge, int segmentMaxAge, int pubMinAge, int pubMaxAge) {
        return segmentMaxAge >= pubMinAge && segmentMinAge <= pubMaxAge;
    }

    public static double calculateAgeRangeFactor(int aMin, int aMax, int bMin, int bMax) {
        // First make sure they're not mutually exclusive. This should never
        // apply, but this method should stand alone without any assumptions
        // of how it will be used...
        if (aMin > bMax || bMin > aMax) {
            return 0.0;
        }

        double aRangeSize = aMax - aMin;
        double bRangeSize = bMax - bMin;

        // If either range is the "full" range, then obviously the other range
        // will fall within...that's not very highly "targeted" per se, so
        // we'll return a factor of 0.0 in that case, since we shouldn't be
        // boosting weight if the age match is "obviously inclusive."
        if (aRangeSize >= MAX_RANGE_SIZE) {
            return 0.0;
        } else if (bRangeSize >= MAX_RANGE_SIZE) {
            return 0.0;
        }

        // Both age ranges are smaller than MAX_RANGE_SIZE -- let's see how
        // tightly matched they are...

        // We'll be determining a "range match factor" (0.0 to 1.0), which is
        // used to indicate how well the ranges match each other. That will
        // then be scaled (very last step) based on the size of the ranges.
        // Tigher ranges will be given a higher weight than looser ranges, for
        // the same range match factor.
        double rangeMatchFactor;

        // Before doing anything "fancy" we should just do a quick check to
        // see if the ranges are identical.
        if (aMin == bMin && aMax == bMax) {
            // Yep, the ranges are identical. Let's see if they're just a
            // single age value, or if they're a range.
            if (aRangeSize == 0) {
                // Wow, both ranges are just a single age value. This is
                // literally as perfect as it gets.
                return 1.0;
            } else {
                // The range match factor is determined based on the size of the
                // range. i.e. if the ranges match perfectly but they're huge,
                // that doesn't mean the "age targeting" was highly effective.
                // The smaller/tighter the range, the higher the value will be.
                // But for now, we just give it a range match factor of 1.0.
                // It will be scaled appropriately in the very last step.
                rangeMatchFactor = 1.0;
            }
        } else {
            // The ranges aren't identical. Next step is to see if one range
            // falls completely within the other, or if only parts overlap
            // each other.
            if ((aMin <= bMin && aMax >= bMax) || (bMin <= aMin && bMax >= aMax)) {
                // In this case, either b falls complete within a, or a falls
                // completely within b. The range match factor will be
                // calculated based on the "distance" between their average
                // centers. We'll calculate a value between 0.0 and 1.0, where
                // 0.0 is the case where the centers are as far apart as they
                // could be, and 1.0 is the case where the centers are aligned.
                double aAvg = average(aMin, aMax);
                double bAvg = average(bMin, bMax);
                double distBetweenCenters = Math.abs(aAvg - bAvg);
                rangeMatchFactor = 1.0 - (distBetweenCenters / MAX_RANGE_SIZE);
            } else {
                // They overlap. Let's look at what percentage of each range
                // overlaps the other...being careful about the "fencepost"
                // scenario, where the ranges share an end. We build in an
                // overlap of 1 + the actual numerical overlap to remedy this.
                double overlap;
                if (aMin < bMin) {
                    overlap = 1 + aMax - bMin; // +1 "built-in" overlap
                } else {
                    overlap = 1 + bMax - aMin; // +1 "built-in" overlap
                }
                double aOverlapPercent = overlap / aRangeSize;
                double bOverlapPercent = overlap / bRangeSize;
                // The average of the two overlap percentages will be
                // between, but not including, 0.0 and 1.0. We can use
                // this overlap percent average as our range match factor.
                rangeMatchFactor = average(aOverlapPercent, bOverlapPercent);
            }
        }

        // At this point we need to take rangeMatchFactor (0.0 to 1.0) and
        // scale it based on the sizes of the two ranges. Larger ranges will
        // be penalized. The scale factor will always be between 0.5 and 1.0.
        double sumOfRanges = aRangeSize + bRangeSize;
        double scaleFactor = 1.0 - ((sumOfRanges / (2 * MAX_RANGE_SIZE)) / 2);
        return rangeMatchFactor * scaleFactor;
    }

    private static double average(Number... values) {
        double total = 0;
        int count = 0;
        for (Number value : values) {
            total += value.doubleValue();
            ++count;
        }
        return total / count;
    }

    /**
     * Coerce a given age value into range for targeting.
     * 
     * @return a value between MIN_AGE and MAX_AGE, inclusive
     */
    public static int coerceIntoRange(int age) {
        if (age < MIN_AGE) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Coercing out-of-range age value " + age + " to " + MIN_AGE);
            }
            return MIN_AGE;
        } else if (age > MAX_AGE) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Coercing out-of-range age value " + age + " to " + MAX_AGE);
            }
            return MAX_AGE;
        } else {
            return age;
        }
    }
}
