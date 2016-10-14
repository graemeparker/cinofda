package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

public class TestDateOfBirthDeriver extends BaseAdserverTest {
	private DeriverManager deriverManager;
	private DateOfBirthDeriver dateOfBirthDeriver;
	private TargetingContext context;

	@Before
	public void runBeforeEachTest() {
		deriverManager = new DeriverManager();
		dateOfBirthDeriver = new DateOfBirthDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void test01_getAttribute_unsupportedAttribute() {
		assertNull(dateOfBirthDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}

	@Test
	public void test02_getAttribute_not_supplied() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.DATE_OF_BIRTH); will(returnValue(null));
		}});
		assertNull(dateOfBirthDeriver.getAttribute(TargetingContext.DATE_OF_BIRTH, context));
	}

	@Test
	public void test03_getAttribute_yearOnly() {
        int age = 20 + randomInteger(50);
        int year = Calendar.getInstance().get(Calendar.YEAR) - (age + 1);
        final String param = String.valueOf(year);
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.DATE_OF_BIRTH); will(returnValue(param));
        }});

        Date dateOfBirth = (Date)dateOfBirthDeriver.getAttribute(TargetingContext.DATE_OF_BIRTH, context);
        assertNotNull(dateOfBirth);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOfBirth);
        assertEquals(year, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void test04_getAttribute_fullDob() {
        int age = 20 + randomInteger(50);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -age);
        cal = DateUtils.truncate(cal, Calendar.YEAR); // Jan 1 at midnight
        final String param = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.DATE_OF_BIRTH); will(returnValue(param));
        }});
        
        Date dateOfBirth = (Date)dateOfBirthDeriver.getAttribute(TargetingContext.DATE_OF_BIRTH, context);
        assertNotNull(dateOfBirth);
        assertEquals(cal.getTime(), dateOfBirth);
    }
    
    @Test
    public void test05_getAttribute_invalidDob() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.DATE_OF_BIRTH); will(returnValue("198423"));
        }});
        assertNull(dateOfBirthDeriver.getAttribute(TargetingContext.DATE_OF_BIRTH, context));
    }
}
