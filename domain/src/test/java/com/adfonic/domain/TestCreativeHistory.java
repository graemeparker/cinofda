package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.adfonic.test.AbstractAdfonicTest;

import org.jmock.Expectations;
import org.junit.Test;

public class TestCreativeHistory extends AbstractAdfonicTest {
    @Test
    public void test() {
        final Creative creative = mock(Creative.class);
        final Creative.Status status = Creative.Status.PENDING;
        final AdfonicUser assignedTo = mock(AdfonicUser.class, "assignedTo");
        final String comment = randomMultiLineString(500);
        final AdfonicUser adfonicUser = mock(AdfonicUser.class, "adfonicUser");
        expect(new Expectations() {{
            oneOf (creative).getStatus(); will(returnValue(status));
            oneOf (creative).getAssignedTo(); will(returnValue(assignedTo));
        }});
        
        CreativeHistory history = new CreativeHistory(creative);
        assertNotNull(history.getEventTime());
        assertEquals(creative, history.getCreative());
        assertEquals(status, history.getStatus());
        assertEquals(assignedTo, history.getAssignedTo());

        assertNull(history.getComment());
        assertNull(history.getAdfonicUser());

        history.setComment(comment);
        assertEquals(comment, history.getComment());

        history.setAdfonicUser(adfonicUser);
        assertEquals(adfonicUser, history.getAdfonicUser());
    }
}
