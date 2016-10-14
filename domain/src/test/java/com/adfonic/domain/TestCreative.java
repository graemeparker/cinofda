package com.adfonic.domain;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestCreative extends AbstractAdfonicTest {
    @Test
    public void test01_creationTime_auto_set_variation1() {
        assertNotNull(new Creative().getCreationTime());
    }
    
    @Test
    public void test01_creationTime_auto_set_variation2() {
        Segment segment = mock(Segment.class);
        Format format = mock(Format.class);
        assertNotNull(new Creative(segment, format).getCreationTime());
    }
    
    @Test
    public void test01_creationTime_auto_set_variation3() {
        Campaign campaign = mock(Campaign.class);
        Segment segment = mock(Segment.class);
        Format format = mock(Format.class);
        assertNotNull(new Creative(campaign, segment, format).getCreationTime());
    }
    @Test
    public void test02_closedMode() {
        Creative creative = new Creative();
        creative.setClosedMode(true);
        assertTrue(creative.isClosedMode());
    }    
    
    @Test
    public void test02_allowExternalAudit() {
        Creative creative = new Creative();
        creative.setAllowExternalAudit(true);
        assertTrue(creative.isAllowExternalAudit());
    } 
}
