package com.adfonic.adserver.deriver.impl;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.util.AgeRangeTargetingLogic;
import com.adfonic.util.AgeUtils;
import com.adfonic.util.Range;

/** Derive the viewer's age range from the request */
@Component
public class AgeRangeDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(AgeRangeDeriver.class.getName());

    @Autowired
    public AgeRangeDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.AGE_RANGE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.AGE_RANGE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        // 1. DOB
        Date dateOfBirth = context.getAttribute(TargetingContext.DATE_OF_BIRTH);
        if (dateOfBirth != null) {
            int age = AgeUtils.getAgeInYears(dateOfBirth);
            age = AgeRangeTargetingLogic.coerceIntoRange(age);
            return new Range<Integer>(age, true);
        }

        // 2. Age
        String ageStr = context.getAttribute(Parameters.AGE);
        if (ageStr != null) {
            try {
                int age = Integer.parseInt(ageStr);
                age = AgeRangeTargetingLogic.coerceIntoRange(age);
                return new Range<Integer>(age, true);
            } catch (Exception e) {
                // TODO: tie this into the error checker framework
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for " + Parameters.AGE + ": " + ageStr);
                }
            }
        }

        // 3. Age Range
        Integer ageLow = null;
        String ageLowStr = context.getAttribute(Parameters.AGE_LOW);
        if (ageLowStr != null) {
            try {
                ageLow = Integer.valueOf(ageLowStr);
                ageLow = AgeRangeTargetingLogic.coerceIntoRange(ageLow);
            } catch (Exception e) {
                // TODO: tie this into the error checker framework
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for " + Parameters.AGE_LOW + ": " + ageLowStr);
                }
            }
        }

        Integer ageHigh = null;
        String ageHighStr = context.getAttribute(Parameters.AGE_HIGH);
        if (ageHighStr != null) {
            try {
                ageHigh = Integer.valueOf(ageHighStr);
                ageHigh = AgeRangeTargetingLogic.coerceIntoRange(ageHigh);
            } catch (Exception e) {
                // TODO: tie this into the error checker framework
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for " + Parameters.AGE_HIGH + ": " + ageHighStr);
                }
            }
        }

        if (ageLow != null || ageHigh != null) {
            if (ageLow == null) {
                // Only high was specified...default low
                ageLow = AgeRangeTargetingLogic.MIN_AGE;
            }
            if (ageHigh == null) {
                // Only low was specified...default high
                ageHigh = AgeRangeTargetingLogic.MAX_AGE;
            }
            return new Range<Integer>(ageLow, ageHigh, true);
        }

        // No way to derive age range
        return null;
    }
}
