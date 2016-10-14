package com.adfonic.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.WordUtils;

public class LogUtils {
    
    private LogUtils() {}
    
    /** Create logs when applicable with titled message */
    public static void logWithTitle(Logger logger, Level level, String title, Object message, Throwable t) {
        if (logger.isLoggable(level)) {
            StringBuilder sb = new StringBuilder()
                .append("[ " + WordUtils.capitalize(title) + " ] ")
                .append(message);
            logger.log(level, sb.toString(), t);
        }
    }
    
    /** Create logs when applicable with titled message */
    public static void logWithTitle(Logger logger, Level level, String title, Object message) {
        logWithTitle(logger, level, title, message, null);
    }

}
