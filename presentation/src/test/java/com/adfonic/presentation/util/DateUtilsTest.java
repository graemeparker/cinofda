package com.adfonic.presentation.util;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class DateUtilsTest {
    
    @Test
    public void doFormatTimestamp() {
        String str_date1 = "11-June-12";
        Date date1 = DateUtils.getDateFormatFromString(str_date1, "dd-MMM-yy");
        Assert.assertNotNull("Normalized date1 should not be null", date1);
    }
}
