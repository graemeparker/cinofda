package com.adfonic.domain;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class TestSegment {
    @Test
    public void testGetGenderMixHumanReadable() {
    	int genderMix = 45;
    	String valueExpected = genderMix + "% / " + (100-genderMix) + "%";
    	Segment segment = new Segment();
    	segment.setGenderMix(new BigDecimal(45).divide(new BigDecimal(100)));
    	assertEquals(valueExpected + "<- NOT EQUAL TO ->" + segment.getGenderMixHumanReadable(), valueExpected, segment.getGenderMixHumanReadable());
    }
}
