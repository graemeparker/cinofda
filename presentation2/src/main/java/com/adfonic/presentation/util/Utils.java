package com.adfonic.presentation.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;

public class Utils {

    private static final long _1000L = 1000L;

    private Utils() {}
    
    public static String getDelimitedIds(List<?> list) {
        if (!CollectionUtils.isEmpty(list)) {
            StringBuilder sb = new StringBuilder();
            Iterator<?> i = list.iterator();
            for (;;) {
                Object e = i.next();
                sb.append(e.toString());
                if (!i.hasNext()) {
                    return sb.toString();
                } else {
                    sb.append("~");
                }
            }
        } else {
            return null;
        }
    }

    public static Date dateFromTimestamp(String szTimestamp) {
        return new Date((long) (Double.parseDouble(szTimestamp) * _1000L));
    }

    /** Create logs when applicable with titled message */
    public static void logWithTitle(Logger logger, Level level, String title, Object message) {
        if (logger.isLoggable(level)) {
            StringBuilder sb = new StringBuilder()
                .append("[ " + WordUtils.capitalize(title) + " ] ")
                .append(message);
            logger.log(level, sb.toString());
        }
    }
}
