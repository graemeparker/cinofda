package com.adfonic.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringEscapeUtils;

public class XmlWriter {
    
    private PrintWriter out;
    private boolean startedDoc;
    private boolean endedDoc;
    private boolean inStartTag;
    private boolean allowNonStandardAscii;
    private List<String> tagNames;
    private boolean flushOnEndDoc;

    public XmlWriter(OutputStream o) {
        this(o, true);
    }

    public XmlWriter(OutputStream o, boolean flushOnEndDoc) {
        try {
            out = new PrintWriter(new OutputStreamWriter(o, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // this should never happen, because UTF_8 is required on
            // all java implementations
            throw new UnsupportedOperationException(e);
        }
        startedDoc = false;
        endedDoc = false;
        inStartTag = false;
        allowNonStandardAscii = false;
        tagNames = new LinkedList<String>();
        this.flushOnEndDoc = flushOnEndDoc;
    }

    public boolean getFlushOnEndDoc() {
        return flushOnEndDoc;
    }

    public void setFlushOnEndDoc(boolean flush) {
        flushOnEndDoc = flush;
    }

    /**
     * Get whether ascii values 0-8,11-12,14-31, and 127-255, are escaped or
     * omitted. True means they are escaped as unicode entities, and false means
     * they are silently dropped from attributes and element text. Defaults to
     * false.
     */
    public boolean getAllowNonStandardAscii() {
        return allowNonStandardAscii;
    }

    /**
     * Set whether ascii values 0-8,11-12,14-31, and 127-255, are escaped or
     * omitted. True means they are escaped as unicode entities, and false means
     * they are silently dropped from attributes and element text. Defaults to
     * false.
     */
    public void setAllowNonStandardAscii(boolean allow) {
        allowNonStandardAscii = allow;
    }

    public XmlWriter startDoc() {
        if (endedDoc) {
            throw new XmlWriterException("XmlWriter already ended");
        } else if (startedDoc) {
            throw new XmlWriterException("XmlWriter already started");
        } else {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            startedDoc = true;
        }
        return this;
    }

    public XmlWriter endDoc() {
        checkDocStarted();
        while (!tagNames.isEmpty()) {
            endTag();
        }
        if (flushOnEndDoc) {
            flush();
        }
        endedDoc = true;
        return this;
    }

    public XmlWriter startTag(String name) {
        checkDocStarted();
        if (inStartTag) {
            finishStartTag();
        }
        out.print("<");
        out.print(name);
        ((LinkedList<String>) tagNames).addLast(name);
        inStartTag = true;
        return this;
    }

    private void finishStartTag() {
        out.print(">");
        inStartTag = false;
    }

    public XmlWriter newAttr(String name, boolean bool) {
        return newAttr(name, String.valueOf(bool));
    }

    public XmlWriter newAttr(String name, int value) {
        return newAttr(name, String.valueOf(value));
    }

    public XmlWriter newAttr(String name, long value) {
        return newAttr(name, String.valueOf(value));
    }

    public XmlWriter newAttr(String name, double value) {
        return newAttr(name, String.valueOf(value));
    }

    public XmlWriter newAttr(String name, Enum<?> value) {
        return newAttr(name, String.valueOf(value));
    }

    public XmlWriter newAttr(String name, Date dateTime) {
        return newAttr(name, formatDateTime(dateTime));
    }

    public XmlWriter newAttr(String name, String value) {
        checkDocStarted();
        if (!inStartTag) {
            throw new XmlWriterException("XmlWriter has no started tag");
        }
        out.print(" ");
        out.print(name);
        out.print("=\"");
        out.print(StringEscapeUtils.escapeXml(value));
        out.print("\"");
        return this;
    }

    public XmlWriter text(boolean bool) {
        return text(String.valueOf(bool));
    }

    public XmlWriter text(int num) {
        return text(String.valueOf(num));
    }

    public XmlWriter text(long num) {
        return text(String.valueOf(num));
    }

    public XmlWriter text(double num) {
        return text(String.valueOf(num));
    }

    public XmlWriter text(String someText) {
        return text(someText, true);
    }

    public XmlWriter text(Enum<?> value) {
        return text(value.name());
    }

    public XmlWriter text(Date dateTime) {
        return text(formatDateTime(dateTime));
    }

    public XmlWriter text(String someText, boolean omitNulls) {
        checkDocStarted();
        if (inStartTag) {
            finishStartTag();
        }
        if (someText != null || !omitNulls) {
            out.print(StringEscapeUtils.escapeXml(someText));
        }
        return this;
    }

    public XmlWriter cdata(String someText) {
        checkDocStarted();
        if (inStartTag) {
            finishStartTag();
        }
        out.print("<![CDATA[");
        if (someText != null) {
            out.print(someText);
        }
        out.print("]]>");
        return this;
    }

    public XmlWriter endTag(boolean writeNewLine) {
        endTag();
        if (writeNewLine) {
            newLine();
        }
        return this;
    }

    public XmlWriter endTag() {
        checkDocStarted();
        try {
            String tagName = ((LinkedList<String>) tagNames).removeLast();
            if (inStartTag) {
                out.print("/");
                finishStartTag();
            } else {
                out.print("</");
                out.print(tagName);
                out.print(">");
            }
            return this;
        } catch (NoSuchElementException e) {
            // if list doesn't have any names
            throw new XmlWriterException("XmlWriter has no corresponding open tag", e);
        }
    }

    public XmlWriter newLine() {
        if (inStartTag) {
            finishStartTag();
        }
        out.println("");
        return this;
    }

    private void checkDocStarted() {
        if (endedDoc) {
            throw new XmlWriterException("XmlWriter already ended");
        } else if (!startedDoc) {
            throw new XmlWriterException("XmlWriter not started yet");
        }
    }

    public void flush() {
        out.flush();
    }

    public static String formatDateTime(Date date) {
        String s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
        return s.substring(0, s.length() - 2) + ":" + s.substring(s.length() - 2);
    }

    public static class XmlWriterException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public XmlWriterException() {
            super();
        }

        public XmlWriterException(String message) {
            super(message);
        }

        public XmlWriterException(Throwable cause) {
            super(cause);
        }

        public XmlWriterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
