package com.byyd.elasticsearch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ElasticSearchUtil {

    public static final String PROP_NOT_FOUND = "not found";
    private static final String PROP_SEP = ".";
    private static final String PROP_ARR_SEP_START = "[";
    private static final String PROP_ARR_SEP_END = "]";
    private static final String PROP_DEF_SEP = "=";

    private ElasticSearchUtil() {
        // Utility class
    }

    /**
     * Look up the desired property which can be nested or within a list and satisfy an expression
     */
    @SuppressWarnings("unchecked")
    public static String lookupValueFromHitSource(Map<String, Object> source, String columnKey) {
        int propSepIdx = -1;
        if (source == null) {
            return PROP_NOT_FOUND;
        } else if ((propSepIdx = columnKey.indexOf(PROP_SEP)) != -1) {
            String parentKey = columnKey.substring(0, propSepIdx);
            String restKey = columnKey.substring(propSepIdx + 1);

            int propArrSepIdx = -1;
            if ((propArrSepIdx = parentKey.indexOf(PROP_ARR_SEP_START)) == -1) {
                return lookupValueFromHitSource((Map<String, Object>) source.get(parentKey), restKey);
            } else {
                String arrayParentKey = parentKey.substring(0, propArrSepIdx);
                String filterExpression = parentKey.substring(propArrSepIdx + 1, parentKey.indexOf(PROP_ARR_SEP_END));
                return lookupValueFromList((ArrayList<Map<String, Object>>) source.get(arrayParentKey), filterExpression, restKey);
            }
        } else {
            Object value = source.get(columnKey);
            return (value != null) ? String.valueOf(value) : PROP_NOT_FOUND;
        }
    }
    
    /**
     * Build lookup value definition based on nested value matcher
     * Pattern: "nestedProperty[matcherKey=matcherValue].property"
     * Example: "notifications[audienceId={0}].sessionId"
     * 
     * @param nestedProperty the property which contains an array where you need a specific property
     * @param matcherKey the property key used for matching
     * @param matcherValue the property value you search for during matching
     * @param property the desired property to retrieve if exists else PROP_NOT_FOUND
     */
    public static String buildNestedPropertyMatcher(String nestedProperty, String matcherKey, String matcherValue, String property) {
        StringBuffer sb = new StringBuffer(nestedProperty);
        sb.append(PROP_ARR_SEP_START).append(matcherKey).append(PROP_DEF_SEP).append(matcherValue).append(PROP_ARR_SEP_END);
        sb.append(PROP_SEP).append(property);
        
        return sb.toString();
    }
    
    // ////////////////////
    // Private methods
    // ////////////////////

    private static String lookupValueFromList(List<Map<String, Object>> sourceList, String filterExpression, String restKey) {
        String[] expression = filterExpression.split(PROP_DEF_SEP);
        for (Map<String, Object> source : sourceList) {
            for (Entry<String, Object> sourceEntry : source.entrySet()) {
                if (sourceEntry.getKey().equals(expression[0]) && sourceEntry.getValue().equals(expression[1])) {
                    return lookupValueFromHitSource(source, restKey);
                }
            }
        }
        return PROP_NOT_FOUND;
    }

}
