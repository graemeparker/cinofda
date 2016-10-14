package com.adfonic.email.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Queue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.jms.JmsUtils;
import com.adfonic.test.AbstractAdfonicTest;

public class TestJmsBasedEmailService extends AbstractAdfonicTest {
    private JmsTemplate jmsTemplate;
    private JmsUtils jmsUtils;
    private Queue outboundEmailQueue;
    private JmsBasedEmailService emailService;

    @Before
    public void setup() {
        jmsTemplate = mock(JmsTemplate.class);
        jmsUtils = mock(JmsUtils.class);
        outboundEmailQueue = mock(Queue.class, "outboundEmailQueue");
        emailService = new JmsBasedEmailService(jmsTemplate, jmsUtils);
        emailService.setOutboundEmailQueue(outboundEmailQueue);
    }
    
    @Test
    public void test() throws Exception {
        final String from = randomAlphaNumericString(10);
        final String replyTo = randomAlphaNumericString(10);
        final String to = randomAlphaNumericString(10);
        final String cc = randomAlphaNumericString(10);
        final String bcc = randomAlphaNumericString(10);
        final String subject = randomAlphaNumericString(10);
        final String body = randomAlphaNumericString(10);
        final String contentType = randomAlphaNumericString(10);
        final String headerName = randomAlphaNumericString(10);
        final String headerValue = randomAlphaNumericString(10);
        
        Map<String,String> headers = new HashMap<String,String>();
        headers.put(headerName, headerValue);

        expect(new Expectations() {{
            oneOf (jmsUtils).sendObject(with(jmsTemplate), with(outboundEmailQueue), with(any(HashMap.class)));
        }});
        
        emailService.sendEmail(from, replyTo, Collections.singletonList(to), Collections.singletonList(cc), Collections.singletonList(bcc), headers, subject, body, contentType);

        // TODO: we have code coverage, just need to assert results
        /*
        Map message = (Map)producerTemplate.message;
        assertEquals(from, message.get(JmsBasedEmailService.FROM));
        assertEquals(replyTo, message.get(JmsBasedEmailService.REPLY_TO));
        assertEquals(to, message.get(JmsBasedEmailService.TO));
        assertEquals(cc, message.get(JmsBasedEmailService.CC));
        assertEquals(bcc, message.get(JmsBasedEmailService.BCC));
        assertEquals(subject, message.get(JmsBasedEmailService.SUBJECT));
        assertEquals(contentType, message.get(JmsBasedEmailService.CONTENT_TYPE));
        for (Map.Entry<String,String> entry : headers.entrySet()) {
            assertEquals(entry.getValue(), message.get(entry.getKey()));
        }
        assertEquals(body, message.get(JmsBasedEmailService.BODY));
        */
   }
}