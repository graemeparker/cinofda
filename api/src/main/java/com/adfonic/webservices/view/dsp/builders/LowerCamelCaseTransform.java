package com.adfonic.webservices.view.dsp.builders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LowerCamelCaseTransform implements ValueNameTransformer {
    INSTANCE;

    private static final Pattern US_N_CHAR = Pattern.compile("_([a-z])");


    @Override
    public String convert(String underscoreSeparatedName) {
        if (underscoreSeparatedName == null || !underscoreSeparatedName.contains("_")) {// to speed up normal case
            return underscoreSeparatedName;
        }

        Matcher matcher = US_N_CHAR.matcher(underscoreSeparatedName);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buf, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

}
