package com.adfonic.presentation.targetpublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.test.AbstractAdfonicTest;

/**
 * @author Attila
 */
public class TargetPublisherServiceTest extends AbstractAdfonicTest {
	private TargetPublisherService service;

	@Before
	public void setUp() throws Exception {
		service = mock(TargetPublisherService.class);
	}

	@After
	public void tearDown() throws Exception {
		service = null;
	}
	
	@Test
	public void doGetTargetPublisherById() {
	    final long targetPublisherId = 5;
	    final String name = "Test Publisher name";
	    final boolean rtb = true;
	    final boolean pmpAvailable = false;
	    final int displayPriority = 2;
	    
	    final TargetPublisherDto expectedTargetPublisherDto = mock(TargetPublisherDto.class);
	    
	    expect(new Expectations() {
	        {
	            oneOf(service).getTargetPublisherById(targetPublisherId);
	            will(returnValue(expectedTargetPublisherDto));
	            allowing(expectedTargetPublisherDto).getId(); will(returnValue(targetPublisherId));
	            allowing(expectedTargetPublisherDto).getName(); will(returnValue(name));
	            allowing(expectedTargetPublisherDto).isRtb(); will(returnValue(rtb));
	            allowing(expectedTargetPublisherDto).isPmpAvailable(); will(returnValue(pmpAvailable));
	            allowing(expectedTargetPublisherDto).getDisplayPriority(); will(returnValue(displayPriority));
	        }
	    });
        TargetPublisherDto actualTargetPublisherDto = service.getTargetPublisherById(targetPublisherId);
        
	    assertNotNull(actualTargetPublisherDto);
	    assertNotNull(actualTargetPublisherDto.getId());
	    assertEquals(Long.valueOf(targetPublisherId), actualTargetPublisherDto.getId());
	    assertEquals(name, actualTargetPublisherDto.getName());
	    assertEquals(rtb, actualTargetPublisherDto.isRtb());
	    assertEquals(pmpAvailable, actualTargetPublisherDto.isPmpAvailable());
	    assertEquals(displayPriority, actualTargetPublisherDto.getDisplayPriority());
	}

	@Test
	public void doGetAllTargetPublishersForPmp() {	
        final Collection<TargetPublisherDto> expectedTargetPublishersForPmp = Collections.singletonList(new TargetPublisherDto());

		expect(new Expectations() {
			{
				oneOf(service).getAllTargetPublishersForPmp(true, true, false);
				will(returnValue(expectedTargetPublishersForPmp));
			}
		});
		Collection<TargetPublisherDto> actualTargetPublishersForPmp = service.getAllTargetPublishersForPmp(true, true, false);
		
		assertEquals("Result should be equal to the expected", expectedTargetPublishersForPmp, actualTargetPublishersForPmp);
	}
	
    @Test
    public void doGetAllNonRtbTargetPublishers() {  
        final Collection<TargetPublisherDto> expectedNonRtbTargetPublishers = Collections.singletonList(new TargetPublisherDto());

        expect(new Expectations() {
            {
                oneOf(service).getAllTargetPublishers(false, false);
                will(returnValue(expectedNonRtbTargetPublishers));
            }
        });
        Collection<TargetPublisherDto> actualNonRtbTargetPublishers = service.getAllTargetPublishers(false, false);
        
        assertEquals("Result should be equal to the expected", expectedNonRtbTargetPublishers, actualNonRtbTargetPublishers);
    }
	
}
