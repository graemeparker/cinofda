package com.adfonic.adserver.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.anthavio.aspect.ApiPolicyOverride;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author vanek
 * 
 * Logback TurboFilter matching thread name. 
 * As TurboFilter, it can be only use globally and not per Appender
 */
public class ThreadNameTurboFilter extends TurboFilter {

    private String regex;

    private Pattern pattern;

    protected FilterReply onMatch = FilterReply.NEUTRAL;
    protected FilterReply onMismatch = FilterReply.NEUTRAL;

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        Matcher matcher = pattern.matcher(Thread.currentThread().getName());
        if (matcher.matches()) {
            return onMatch;
        } else {
            return onMismatch;
        }
    }

    public void setRegex(String regex) {
        this.regex = regex;
        pattern = Pattern.compile(regex);
    }

    public String getRegex() {
        return regex;
    }

    final public void setOnMatch(FilterReply reply) {
        this.onMatch = reply;
    }

    final public void setOnMismatch(FilterReply reply) {
        this.onMismatch = reply;
    }

    final public FilterReply getOnMatch() {
        return onMatch;
    }

    final public FilterReply getOnMismatch() {
        return onMismatch;
    }

    @ApiPolicyOverride
    public static void main(String[] args) {
        ThreadNameTurboFilter filter = new ThreadNameTurboFilter();
        filter.setOnMatch(FilterReply.ACCEPT);
        filter.setOnMismatch(FilterReply.DENY);
        filter.setRegex("^autorun.*|^sis2timer.*");
        //filter.setRegex("^(?!autorun|sis2timer).*");

        Thread.currentThread().setName("sis2timer-thread");
        FilterReply decide = filter.decide(null, null, null, null, null, null);
        System.out.println(decide);

        Thread.currentThread().setName("xxx");
        decide = filter.decide(null, null, null, null, null, null);
        System.out.println(decide);
    }

}
