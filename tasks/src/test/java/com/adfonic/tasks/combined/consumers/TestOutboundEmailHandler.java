package com.adfonic.tasks.combined.consumers;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.adfonic.email.impl.JmsBasedEmailService;
import com.adfonic.test.AbstractAdfonicTest;

public class TestOutboundEmailHandler extends AbstractAdfonicTest {
    @Test
    public void testOnOutboundEmail() {
        final JavaMailSender mailSender = mock(JavaMailSender.class, "mailSender");
        
        final String to = randomEmailAddress();
        final String from = randomEmailAddress();
        final String subject = randomSingleLineString(40);
        final String body = randomMultiLineString(500);
        final String headerName = randomAlphaNumericString(10);
        final String headerValue = randomAlphaNumericString(10);
        
        final Map<String,Object> msg = new HashMap<String,Object>() {{
                put(JmsBasedEmailService.BODY, body);
                put(JmsBasedEmailService.TO, to);
                put(JmsBasedEmailService.FROM, from);
                put(JmsBasedEmailService.SUBJECT, subject);
                put(headerName, headerValue);
            }};

        final Map<String,Object> headers = msg; // msg gets reused after BODY is removed
        
        expect(new Expectations() {{
            oneOf (mailSender).send(with(any(MimeMessagePreparator.class)));
        }});

        OutboundEmailHandler outboundEmailHandler = new OutboundEmailHandler(mailSender);
        outboundEmailHandler.onOutboundEmail(msg);

        // Make sure the non-headers were removed from the map
        assertFalse(msg.containsKey(JmsBasedEmailService.BODY));
        assertFalse(msg.containsKey(JmsBasedEmailService.TO));
        assertFalse(msg.containsKey(JmsBasedEmailService.FROM));
        assertFalse(msg.containsKey(JmsBasedEmailService.SUBJECT));
    }
}
