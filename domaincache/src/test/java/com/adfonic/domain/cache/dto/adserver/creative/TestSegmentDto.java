package com.adfonic.domain.cache.dto.adserver.creative;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.domain.Segment.DayOfWeek;

public class TestSegmentDto {

	private void target24X7Hours(SegmentDto segmentDto){
		segmentDto.addDayHour(DayOfWeek.Sunday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Monday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Tuesday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Wednesday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Thursday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Friday, 16777215);
		segmentDto.addDayHour(DayOfWeek.Saturday, 16777215);
		
	}
	/**
	 * Test when segment is targeting 7 X 24 and country ISO code not provided(null)
	 */
	@Test
    public void testIsTimeEnabled01() {
        SegmentDto segmentDto = new SegmentDto();
        target24X7Hours(segmentDto);
        
        String countryIsoCode = null;
        boolean timeEnabled = segmentDto.isTimeEnabled(countryIsoCode, new Date());
        assertTrue(timeEnabled);
    }
	
	/**
	 * Test when segment is targeting 7 X 24 and correct country ISO code is provided
	 */
	@Test
    public void testIsTimeEnabled02() {
        SegmentDto segmentDto = new SegmentDto();
        target24X7Hours(segmentDto);
        
        String countryIsoCode = "GB";
        boolean timeEnabled = segmentDto.isTimeEnabled(countryIsoCode, new Date());
        assertTrue(timeEnabled);
    }
	
	/**
	 * Test when segment is targeting 7 X 24 and incorrect country ISO code is provided
	 */
	@Test
    public void testIsTimeEnabled03() {
        SegmentDto segmentDto = new SegmentDto();
        target24X7Hours(segmentDto);
        
        String countryIsoCode = "IncorrectIsoCode";
        boolean timeEnabled = segmentDto.isTimeEnabled(countryIsoCode, new Date());
        assertFalse(timeEnabled);
    }
	
	/**
	 * Test when segment is Not Targeting Weekend and country code iso code is null
	 */
	@Test
    public void testIsTimeEnabled04() {
        SegmentDto segmentDto = new SegmentDto();
        target24X7Hours(segmentDto);
        //sunday,saturday not targeted   so update the value for saturday and sunday
		segmentDto.addDayHour(DayOfWeek.Sunday, 0);
		segmentDto.addDayHour(DayOfWeek.Saturday, 0);
        
        String countryIsoCode = null;
        //pass any date as countryCode is null
        boolean sundayTimeEnabled = segmentDto.isTimeEnabled(countryIsoCode, new Date());
        assertFalse(sundayTimeEnabled);
        
    }
	
	/**
	 * Test when segment is Not Targeting Weekend and country code is provided
	 */
	@Test
    public void testIsTimeEnabled05() {
        SegmentDto segmentDto = new SegmentDto();
        target24X7Hours(segmentDto);
        //sunday,saturday not targeted   so update the value for saturday and sunday
		segmentDto.addDayHour(DayOfWeek.Sunday, 0);
		segmentDto.addDayHour(DayOfWeek.Saturday, 0);
        
        String countryIsoCode = "IN";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Date sundayDate = cal.getTime();
        boolean sundayTimeEnabled = segmentDto.isTimeEnabled(countryIsoCode, sundayDate);
        assertFalse(sundayTimeEnabled);
        
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Date saturdayDate = cal.getTime();
        boolean saturdayTimeEnabled = segmentDto.isTimeEnabled(countryIsoCode, saturdayDate);
        assertFalse(saturdayTimeEnabled);
        System.out.println("DayOfWeek.Sunday.ordinal()="+DayOfWeek.Sunday.ordinal());
        System.out.println("DayOfWeek.Saturday.ordinal()="+DayOfWeek.Saturday.ordinal());
        System.out.println("DayOfWeek.Monday.ordinal()="+DayOfWeek.Monday.ordinal());
    }
}
