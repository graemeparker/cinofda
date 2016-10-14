package com.adfonic.tools.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.tools.beans.util.Utils;

public class UtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doGetNormalizedDate() {
        Date[] dates = new Date[] { Calendar.getInstance().getTime(), Calendar.getInstance().getTime() };
        Date[] datesNormalized = Utils.getNormalizedDate(dates);
        Assert.assertNotNull("Normalized Date should not be null", datesNormalized);
    }

    @Test
    public void doGetPercenteatgeAmount() {
        double percenValue = Utils.getPercenteatgeAmount(42, 10);
        Assert.assertNotNull("Some Value should ", percenValue);
    }
}
