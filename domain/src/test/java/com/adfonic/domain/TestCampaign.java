package com.adfonic.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adfonic.domain.Campaign.CapPeriodSecondsEnum;

public class TestCampaign {
    @Test
    public void testCapPeriodSecondsHumanReadable() {
    	Campaign campaign = new Campaign();
    	campaign.setCapPeriodSeconds(CapPeriodSecondsEnum.HOUR.getSeconds());
    	assertEquals(CapPeriodSecondsEnum.HOUR + "<- NOT EQUAL TO ->" + campaign.getCapPeriodSecondsHumanReadable(), CapPeriodSecondsEnum.HOUR, campaign.getCapPeriodSecondsHumanReadable());
    	
    	campaign.setCapPeriodSeconds(CapPeriodSecondsEnum.DAY.getSeconds());
    	assertEquals(CapPeriodSecondsEnum.DAY + "<- NOT EQUAL TO ->" + campaign.getCapPeriodSecondsHumanReadable(), CapPeriodSecondsEnum.DAY, campaign.getCapPeriodSecondsHumanReadable());
    	
    	campaign.setCapPeriodSeconds(CapPeriodSecondsEnum.WEEK.getSeconds());
    	assertEquals(CapPeriodSecondsEnum.WEEK + "<- NOT EQUAL TO ->" + campaign.getCapPeriodSecondsHumanReadable(), CapPeriodSecondsEnum.WEEK, campaign.getCapPeriodSecondsHumanReadable());
    	
    	campaign.setCapPeriodSeconds(CapPeriodSecondsEnum.MONTH.getSeconds());
        assertEquals(CapPeriodSecondsEnum.MONTH + "<- NOT EQUAL TO ->" + campaign.getCapPeriodSecondsHumanReadable(), CapPeriodSecondsEnum.MONTH, campaign.getCapPeriodSecondsHumanReadable());
    }
}
