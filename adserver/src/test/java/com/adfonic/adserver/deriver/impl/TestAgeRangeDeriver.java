package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.util.AgeRangeTargetingLogic;
import com.adfonic.util.Range;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAgeRangeDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	AgeRangeDeriver ageRangeDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		ageRangeDeriver = new AgeRangeDeriver(deriverManager);
		context = mock(TargetingContext.class);

	}

	@Test
	public void testAgeRangeDeriver_unsupportedAttribute() {
		assertNull(ageRangeDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}

	@Test
	public void testAgeRangeDeriver_nothingDefined() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue(null));
		}});
		assertNull(ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context));
	}

	@Test
    public void testAgeRangeDeriver_dob_supplied() {
        int age = 20 + randomInteger(50);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.YEAR, -age);
        final Date dob = cal.getTime();
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(dob));
        }});
        
		Range<Integer> r = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)age, r.getStart());
        assertEquals("Range end", (Object)age, r.getEnd());
    }
    
    @Test
    public void testAgeRangeDeriver_explicitAge_withNoDob() {
        final int age = 40;
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE); will(returnValue(String.valueOf(age)));
        }});
		Range<Integer> r = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)age, r.getStart());
        assertEquals("Range end", (Object)age, r.getEnd());
    }
    
    @Test
    public void testAgeRangeDeriver_invalidAge_withNoDob() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE); will(returnValue("invalid"));
			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue(null));
        }});
		assertNull(ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context));
    }
    
    @Test
    public void testAgeRangeDeriver_lowHigh_invalidAge_withNoDob() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("3"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("66"));
        }});
		Range<Integer> r = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)3, r.getStart());
        assertEquals("Range end", (Object)66, r.getEnd());
    }

    @Test
    public void testAgeRangeDeriver_lowHighVariations_withNoDobOrAge() {
		expect(new Expectations() {{
            // Both dob and age are not specified
			allowing (context).getAttribute(TargetingContext.DATE_OF_BIRTH); will(returnValue(null));
			allowing (context).getAttribute(Parameters.AGE); will(returnValue(null));
            
			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue(null));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("3"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue(null));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue(null));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("66"));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("invalid"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("invalid"));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("-1"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("34"));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("8"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("340"));

			oneOf (context).getAttribute(Parameters.AGE_LOW); will(returnValue("invalid"));
			oneOf (context).getAttribute(Parameters.AGE_HIGH); will(returnValue("22"));
		}});
        
		// low and high are both null
		assertNull(ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context));
        
		// low defined, but high is null
		Range<Integer> range5 = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)3, range5.getStart());
        assertEquals("Range end", (Object)AgeRangeTargetingLogic.MAX_AGE, range5.getEnd());

		// low null, but high defined
		Range<Integer> range6 = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)AgeRangeTargetingLogic.MIN_AGE, range6.getStart());
        assertEquals("Range end", (Object)66, range6.getEnd());
        
		// invalid low and high
		assertNull(ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context));
        
		// invalid low value
		Range<Integer> range7 = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)AgeRangeTargetingLogic.MIN_AGE, range7.getStart());
        assertEquals("Range end", (Object)34, range7.getEnd());
        
		// invalid high value
		Range<Integer> range8 = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)8, range8.getStart());
        assertEquals("Range end", (Object)AgeRangeTargetingLogic.MAX_AGE, range8.getEnd());
        
		// invalid low value
		Range<Integer> range9 = (Range)ageRangeDeriver.getAttribute(TargetingContext.AGE_RANGE, context);
		assertEquals("Range start", (Object)AgeRangeTargetingLogic.MIN_AGE, range9.getStart());
        assertEquals("Range end", (Object)22, range9.getEnd());
	}

}
