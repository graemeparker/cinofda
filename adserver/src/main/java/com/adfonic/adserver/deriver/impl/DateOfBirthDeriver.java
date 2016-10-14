package com.adfonic.adserver.deriver.impl;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

/**
 * Derive the end user's date of birth, but only if supplied by the publisher
 */
@Component
public class DateOfBirthDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(DateOfBirthDeriver.class.getName());

    private final ThreadLocal<SimpleDateFormat> dobFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    @Autowired
    public DateOfBirthDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.DATE_OF_BIRTH);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.DATE_OF_BIRTH.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String dobStr = context.getAttribute(Parameters.DATE_OF_BIRTH);
        if (StringUtils.isNotBlank(dobStr)) {
            if (dobStr.length() == 4) {
                // If they only specified the year, we should append "1231" so
                // that we err on the younger side when calculating their age.
                dobStr = dobStr + "1231";
            }

            try {
                return dobFormat.get().parse(dobStr);
            } catch (Exception e) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for " + Parameters.DATE_OF_BIRTH + ": " + dobStr);
                }
            }
        }

        return null;
    }
}
