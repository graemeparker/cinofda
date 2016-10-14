package com.adfonic.util;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Helper functions to deal with ranges of comparable objects.
 *
 * Integral ranges have compareTo operations that return exact integers and thus
 * a range of 1 and a range of 2 can be combined into (1,2).
 *
 * An integral range is one where the values can be equated to integers, and
 * combining ranges of adjacent (but non-overlapping) values is therefore
 * possible, e.g. Sun-Mon combined with Tue-Wed is Sun-Wed. For this to work the
 * generic class T must implement a compareTo function that returns the integral
 * distance between the two points, not just negative/zero/positive.
 */
public class Range<T extends Comparable<T>> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private T start;
    private T end;
    private boolean integral;

    public Range(T start, T end) {
        this(start, end, false);
    }

    public Range(T start, T end, boolean integral) {
        this.start = start;
        this.end = end;
        this.integral = integral;
    }

    public Range(T point) {
        this(point, point, false);
    }

    public Range(T point, boolean integral) {
        this(point, point, integral);
    }

    public Range(Range<T> other) {
        this.start = other.start;
        this.end = other.end;
        this.integral = other.integral;
    }

    public boolean isPoint() {
        return start.compareTo(end) == 0;
    }

    public T getStart() {
        return start;
    }

    public T getEnd() {
        return end;
    }

    public boolean isIntegral() {
        return integral;
    }

    /*
     * // Not happy with the naming convention here but out of ideas public
     * static <V extends Enum> Range<V> forEnum(V enumValue) { // Creates a
     * range from this enum value to the next, if any int ordinal =
     * enumValue.ordinal(); for (Object o :
     * enumValue.getDeclaringClass().getEnumConstants()) { Enum e = (Enum) o; if
     * (e.ordinal() > ordinal) { return new Range(enumValue, e, true); } }
     * return new Range(enumValue, enumValue, true); }
     */

    public boolean contains(T value) {
        return start.compareTo(value) <= 0 && end.compareTo(value) >= 0;
    }

    public static <T extends Comparable<T>> void combine(List<Range<T>> existing, T integralPoint) {
        combine(existing, new Range<T>(integralPoint, true));
    }

    /**
     * This function does not yet properly deal with overlapping (versus merely
     * contiguous) ranges.
     */
    public static <T extends Comparable<T>> void combine(List<Range<T>> existing, Range<T> range) {
        // If range.integral, range is really (start,end+1)
        int compValue = 0;
        if (range.integral) {
            compValue = -1;
        }
        for (int i = 0; i < existing.size(); i++) {
            Range<T> r = existing.get(i);
            // Special handling for Integer instances
            int rangeEndCompareToRangeStart;
            int rangeStartCompareToRangeEnd;
            if (range.start instanceof Integer) {
                rangeEndCompareToRangeStart = ((Integer) range.end) - ((Integer) r.start);
                rangeStartCompareToRangeEnd = ((Integer) range.start) - ((Integer) r.end);
            } else {
                rangeEndCompareToRangeStart = range.end.compareTo(r.start);
                rangeStartCompareToRangeEnd = range.start.compareTo(r.end);
            }

            if (range.start.compareTo(r.start) < 0) {
                if (rangeEndCompareToRangeStart < compValue) {
                    // new range is noncontiguous before existing[i]
                    existing.add(i, range);
                    return;
                } else if (rangeEndCompareToRangeStart == compValue) {
                    // new range is contiguous before existing[i]
                    r.start = range.start;
                    return;
                } else {
                    // new range is overlapping
                    java.util.logging.Logger.getLogger(Range.class.getName()).warning(range + " -- overlap TODO");
                }
            } else if (rangeStartCompareToRangeEnd == compValue) {
                // new range is contiguous after existing[i]
                // combine the two.
                r.end = range.end;
                return;
            } else {
                if (r.end.compareTo(range.end) > 0) {
                    // No action
                    return;
                } else if /* ends after && */(-(rangeStartCompareToRangeEnd) >= compValue) {
                    // new range extends existing[i]
                    r.end = range.end;

                    // see if it bumps into existing[i+1] and so on
                    int j = i + 1;
                    while (j < existing.size()) {
                        Range<T> r2 = existing.get(j);
                        int rangeEndCompareToR2Start;
                        if (range.start instanceof Integer) {
                            rangeEndCompareToR2Start = ((Integer) range.end) - ((Integer) r2.start);
                        } else {
                            rangeEndCompareToR2Start = range.end.compareTo(r2.start);
                        }
                        if (rangeEndCompareToR2Start >= compValue) {
                            existing.remove(i);
                            r2.start = r.start;
                        } else {
                            return;
                        }
                    }
                    return;
                }
            }
        }
        // If we get here, range goes as a separate element at the end
        existing.add(range);
    }

    @Override
    public String toString() {
        if (start.compareTo(end) == 0) {
            return start.toString();
        } else {
            return start + "-" + end;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof Range<?>) {
            Range<T> other = (Range<T>) obj;
            return ObjectUtils.equals(this.start, other.start) && ObjectUtils.equals(this.end, other.end) && this.integral == other.integral;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(start).append(end).append(integral).toHashCode();
    }
}
