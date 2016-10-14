package com.adfonic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class WeightedUtils {
    private WeightedUtils() {
    }

    public static <T extends Weighted> T getRandomWeighted(Collection<T> list, double exponent) {
        // Avoid divide-by-zero issues, etc.
        if (list == null || list.isEmpty()) {
            return null;
        }

        // Weighted objects can be passed to us in any order. Sure, ideally
        // the caller would sort 'em prior to calling us, but we don't make
        // that assumption. So we start out by sorting the weighted objects
        // by weight, high to low.
        final List<T> sorted = new ArrayList<T>(list.size()); // initialCapacity
                                                              // for
                                                              // ++performance
        double totalWeight = 0;
        for (T obj : list) {
            sorted.add(obj);
            // And while we're building the list, we also track the total...
            // But only add the weight if it's greater than zero. If, somehow,
            // the object had a negative weight, we don't want that to affect
            // our indexing. Treat negative weights as zero.
            if (obj.getWeight() > 0) {

                totalWeight += Math.pow(obj.getWeight(), exponent);
            }
        }

        // There's an oddball case, where all of the objects in the list
        // have zero weight. In that case, we'd see total == 0.
        if (totalWeight <= 0) {
            // Ok, so in this case, we have a list full of objects whose
            // weights are zero, or maybe even negative. The best thing
            // we can do here is just pick one truly at random.
            int idx = ThreadLocalRandom.getRandom().nextInt(sorted.size());
            return sorted.get(idx);
        }

        // Ok, cool...the total is non-zero positive, which means there is
        // at least one element in the list with a positive weight.

        // Now we sort the objects, high to low
        Collections.sort(sorted, Collections.reverseOrder(Weighted.COMPARATOR));

        // Pick a random number between 0 and 1
        double r = totalWeight * ThreadLocalRandom.getRandom().nextDouble();
        // Now iterate through the high-to-low sorted weights, keeping
        // a running tally, until the tally exceeds "r". Here's the deal...
        // Consider a sorted weight list containing: 10-9-7-7-3-3-1
        // If r==12, then the 9 would be picked (10+9 > 12).
        // If r==19, then the first 7 would be picked (10+9+7 > 19).
        // If r==25, then the first 7 would be picked (10+9+7 > 25).
        // If r==0, then the 10 would be picked (10 > 0).
        // This methodology "biases" higher weights in fair proportion.
        double tally = 0;
        for (T obj : sorted) {
            if (obj.getWeight() > 0) { // Negative is treated as zero
                tally += Math.pow(obj.getWeight(), exponent);
            }
            if (tally > r) {
                return obj;
            }
        }

        // Should never reach this point...but just in case, and to make
        // the compiler happy...
        // Instead of returning null, let's just return the first element
        // in the list.
        return sorted.get(0);
    }
}
