package com.adfonic.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.adfonic.test.AbstractAdfonicTest;

import org.jmock.Expectations;
import org.junit.Test;

public class TestPublicationHistory extends AbstractAdfonicTest {
    @Test
    public void test() {
        final Publication publication = mock(Publication.class);
        final Publication.Status status = Publication.Status.PENDING;
        final Publication.AdOpsStatus adOpsStatus = Publication.AdOpsStatus.MORE_INFO_REQUIRED;
        final AdfonicUser assignedTo = mock(AdfonicUser.class, "assignedTo");
        final String comment = randomMultiLineString(500);
        final AdfonicUser adfonicUser = mock(AdfonicUser.class, "adfonicUser");
        expect(new Expectations() {{
            oneOf (publication).getStatus(); will(returnValue(status));
            oneOf (publication).getAdOpsStatus(); will(returnValue(adOpsStatus));
            oneOf (publication).getAssignedTo(); will(returnValue(assignedTo));
        }});
        
        PublicationHistory history = new PublicationHistory(publication);
        assertNotNull(history.getEventTime());
        assertEquals(publication, history.getPublication());
        assertEquals(status, history.getStatus());
        assertEquals(adOpsStatus, history.getAdOpsStatus());
        assertEquals(assignedTo, history.getAssignedTo());

        assertNull(history.getComment());
        assertNull(history.getAdfonicUser());

        history.setComment(comment);
        assertEquals(comment, history.getComment());

        history.setAdfonicUser(adfonicUser);
        assertEquals(adfonicUser, history.getAdfonicUser());
    }
}
