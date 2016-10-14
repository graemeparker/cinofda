package com.adfonic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class ConstraintsHelper {
    private static final Pattern OPERATION_PATTERN = Pattern.compile("\\s*(!?[^=!<>]+)\\s*(==|!=|<=|>=|<|>|=)\\s*(.+)\\s*");

    public interface PropertySource {
        String getProperty(String name);
    }

    public static class MapPropertySource implements PropertySource {
        private final Map<String, String> map;

        public MapPropertySource(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public String getProperty(String name) {
            return map.get(name);
        }
    }

    public static <T extends Constrained> T findMatch(List<T> list, PropertySource propertySource) {
        if (list != null) {
            for (T t : list) {
                if (eval(t.getConstraints(), propertySource)) {
                    return t;
                }
            }
        }
        return null;
    }

    public static <T extends Constrained> int findMatchingIndex(List<T> list, PropertySource propertySource) {
        for (int idx = 0; idx < list.size(); ++idx) {
            T t = list.get(idx);
            if (eval(t.getConstraints(), propertySource)) {
                return idx;
            }
        }
        return -1;
    }

    public static <T extends Constrained> List<T> findAllMatches(List<T> list, PropertySource propertySource) {
        List<T> matching = new ArrayList<T>();
        for (T t : list) {
            if (eval(t.getConstraints(), propertySource)) {
                matching.add(t);
            }
        }
        return matching;
    }

    public static boolean eval(String constraints, PropertySource propertySource) {
        if (StringUtils.isEmpty(constraints)) {
            return true; // No constraints is as good as all having passed
        }

        // The way this is set up right now is a silly stupid dumb simple
        // way of supporting "and" and "or". Originally we just had and'ed
        // expressions separated by semicolons. I'm adding the | separator,
        // but to do this so simply, you have to pick precedence going strongly
        // one way or the other. As of this first rev, it goes:
        //
        // a;b;c == a && b && c
        // a|b;c == (a || b) && c
        // a|b;c|d == (a || b) && (c || d)
        // a;b|c;d == a && (b || c) && d
        //
        // If you want to be able to support (x && y) || z, then we either
        // need to flip the logic around, or we need to need to take the time
        // to add support for parentheses, or maybe just do it right with a
        // templating engine. The research I did on EL and Velocity and all
        // that made me kinda skerred of this whole thing, because there's
        // no dirt simple expression language (that I'm aware of) that has
        // simple enough expressions that support "the possible absence of"
        // values. Velocity and EL and all that ended up blowing our simple
        // constraints up into crazy expanded language. I mean, we gotta do
        // what we gotta do, but for now I'm taking the easy road.
        //
        // Anyway, on the whole expanded constraints capability thing...
        for (String constraint : StringUtils.split(constraints, ';')) {
            boolean anyTrue = false;
            for (String orThis : constraint.split("\\|")) {
                if (!StringUtils.isBlank(orThis) && (evalSingle(orThis.trim(), propertySource))) {
                    anyTrue = true; // cuz I'm too scared to use goto
                    break; // we're OR'ing, so no need to look any further
                }
            }
            if (!anyTrue) {
                return false; // we're AND'ing, so if any one is false, bail
            }
        }
        return true;
    }

    public static boolean evalSingle(String constraint, PropertySource propertySource) {
        if (StringUtils.isBlank(constraint)) {
            return true; // No constraint is the same as one having passed
        }

        constraint = constraint.trim();
        Matcher matcher = OPERATION_PATTERN.matcher(constraint);
        if (!matcher.matches()) {
            // There's no operation, just a property name. We need to do
            // a presence/non-false/non-0/non-blank check.
            String value = parseOperand(constraint, propertySource, false);
            return StringUtils.isNotEmpty(value) && !"0".equals(value) && !"false".equalsIgnoreCase(value);
        }

        String lhs = parseOperand(matcher.group(1), propertySource, false); // NOT a non-quoted string literal
        String op = matcher.group(2);
        String rhs = parseOperand(matcher.group(3), propertySource, true); // may be a non-quoted string literal

        if ("==".equals(op) || "=".equals(op)) {
            return evalEqualsOp(lhs, rhs);
        } else if ("!=".equals(op)) {
            return evalDistinctOp(lhs, rhs);
        } else if (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op)) {
            return evalLessGreaterThanOp(lhs, rhs, op, constraint);
        } else {
            throw new ParseException("Unsupported operator: " + op + ", (constraint: " + constraint + ")");
        }
    }

    private static boolean evalLessGreaterThanOp(String lhs, String rhs, String op, String constraint) {
        // Right off the bat, if either side is null, we can just consider
        // the whole thing to have evaluated false. This is potentially
        // debatable, but what's the alternative? Throwing an exception
        // every time a property (i.e. a device property) isn't defined?
        // Nah, let's just consider it to have evaluated false.
        if (lhs == null || rhs == null) {
            return false;
        }

        double dlhs, drhs;
        try {
            dlhs = Double.parseDouble(lhs);
        } catch (Exception e) {
            throw new ParseException("Illegal use of " + op + ", non-numeric lhs: " + lhs + ", (constraint: " + constraint + ")", e);
        }
        try {
            drhs = Double.parseDouble(rhs);
        } catch (Exception e) {
            throw new ParseException("Illegal use of " + op + ", non-numeric rhs: " + rhs + ", (constraint: " + constraint + ")", e);
        }
        if (">".equals(op)) {
            return dlhs > drhs;
        } else if ("<".equals(op)) {
            return dlhs < drhs;
        } else if (">=".equals(op)) {
            return dlhs >= drhs;
        } else {
            return dlhs <= drhs;
        }
    }

    private static boolean evalDistinctOp(String lhs, String rhs) {
        if (lhs == null) {
            return StringUtils.isNotEmpty(rhs);
        } else if (rhs == null) {
            return !"".equals(lhs);
        } else {
            return !lhs.equals(rhs);
        }
    }

    private static boolean evalEqualsOp(String lhs, String rhs) {
        if (lhs == null) {
            return StringUtils.isEmpty(rhs);
        } else if (rhs == null) {
            return "".equals(lhs);
        } else {
            return lhs.equals(rhs);
        }
    }

    private static String parseOperand(String val, PropertySource propertySource, boolean nonQuotedUnresolvedIsStringLiteral) {
        String localVal = val.trim();

        // See if it's preceded with !
        boolean invert = false;
        if (localVal.length() > 0 && localVal.charAt(0) == '!') {
            invert = true;
            localVal = localVal.substring(1).trim();
        }

        if (localVal.charAt(0) == '"' && localVal.charAt(localVal.length() - 1) == '"') {
            localVal = localVal.substring(1, localVal.length() - 1);
            localVal = localVal.replaceAll("\\\\\"", "\"");
            return maybeInvert(localVal, invert);
        } else if (localVal.charAt(0) == '\'' && localVal.charAt(localVal.length() - 1) == '\'') {
            localVal = localVal.substring(1, localVal.length() - 1);
            localVal = localVal.replaceAll("\\\\'", "'");
            return maybeInvert(localVal, invert);
        } else if ("null".equals(localVal)) {
            return maybeInvert(null, invert);
        }

        // It's either a numeric value, a property name, or a string literal
        try {
            Double.parseDouble(localVal); // return value not needed
            // We didn't throw an exception, so it's a numeric value
            return maybeInvert(localVal, invert);
        } catch (Exception ignored) {
            // do nothing
        }

        // Non-numeric...see if it's a property name
        String propVal = propertySource.getProperty(localVal);
        if (propVal != null) {
            // Yep, it was a property name
            return maybeInvert(propVal, invert);
        }

        // It's not a property name either...
        if (nonQuotedUnresolvedIsStringLiteral) {
            // Return it as-is
            return maybeInvert(localVal, invert);
        } else {
            // It's probably the name of a non-existent property
            return maybeInvert(null, invert);
        }
    }

    /*
     * private static String maybeInvert(double value, boolean invert) { if
     * (invert) { return value == 0.0 ? "true" : "false"; } else { return
     * String.valueOf(value); } }
     */

    private static String maybeInvert(String value, boolean invert) {
        if (invert) {
            if (value == null) {
                return "true";
            } else if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
                return "true";
            } else {
                return "false";
            }
        } else {
            return value;
        }
    }

    public static class ParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private ParseException(String msg) {
            super(msg);
        }

        private ParseException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
