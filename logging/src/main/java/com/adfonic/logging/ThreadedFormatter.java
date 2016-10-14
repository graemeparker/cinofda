package com.adfonic.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.text.DateFormat;
import java.util.Date;

/**
 * Formatter that includes thread id for tracking a request and puts the
 * info all on one line for easy grepping and splunking.
 */
public class ThreadedFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        public DateFormat initialValue() {
            return DateFormat.getDateTimeInstance();
        }
    };
    
    @Override
    public String format(LogRecord record) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(record.getLevel().toString());
        buffer.append("; ");
        buffer.append(DATE_FORMAT.get().format(new Date(record.getMillis())));
        buffer.append("; tid:");

        // Hopefully formatter runs in same thread as caller... 
        buffer.append(Thread.currentThread().getId());
        buffer.append("; ");

        String sourceClass = record.getSourceClassName();
        if (sourceClass != null) {
            int lastDot = sourceClass.lastIndexOf('.');
            if (lastDot != -1) {
                sourceClass = sourceClass.substring(lastDot+1);
            }
        } else {
            sourceClass = "unknown";
        }
        buffer.append(sourceClass);
        buffer.append(' ');
        buffer.append(record.getSourceMethodName());
        buffer.append("; ");
        buffer.append(record.getMessage());
    
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            buffer.append(LINE_SEPARATOR);
            buffer.append(sw.toString());
        }
        buffer.append(LINE_SEPARATOR);
        return buffer.toString();
    }
} 
