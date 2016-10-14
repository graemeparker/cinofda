package com.adfonic.util;

import java.util.ArrayList;
import java.util.List;

/** CSV (comma-separated values) formatting and parsing utilities */
public class CsvUtils {
    
    private CsvUtils(){
    }
    
    /** Escape a value as needed for output in CSV format */
    public static String escape(Object value) {
        if (value == null) {
            return "";
        } else {
            String str = value.toString();
            if (str.indexOf(',') == -1 && str.indexOf('"') == -1) {
                return str.replaceAll("[\\r\\n]+", ";");
            } else {
                return '"' + str.replaceAll("\"", "\"\"").replaceAll("[\\r\\n]+", ";") + '"';
            }
        }
    }

    /** Unescape an escaped CSV field for input */
    public static String unescape(String value) {
        if (value == null) {
            return null;
        } else if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1).replaceAll("\"\"", "\"");
        } else {
            return value;
        }
    }

    /** Parse a CSV line into a list of string values */
    public static List<String> parseLine(String csvLine) {
        List<String> values = new ArrayList<String>();
        boolean inQuotes = false;
        StringBuilder buf = new StringBuilder();
        int readCount = 0;
        for (int k = 0; k < csvLine.length(); ++k) {
            ++readCount;
            char c = csvLine.charAt(k);
            switch (c) {
            case ',':
                if (inQuotes) {
                    buf.append(c);
                } else {
                    values.add(buf.toString());
                    buf.setLength(0);
                    readCount = 0;
                }
                break;
            case '"':
                if (readCount == 1) {
                    inQuotes = true;
                } else if (inQuotes) {
                    if ((k + 1) < csvLine.length() && csvLine.charAt(k + 1) == '"') {
                        buf.append('"');
                        ++k;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    buf.append(c);
                }
                break;
            default:
                buf.append(c);
                break;
            }
        }
        values.add(buf.toString());
        return values;
    }
}
