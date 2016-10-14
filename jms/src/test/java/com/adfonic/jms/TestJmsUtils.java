package com.adfonic.jms;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.test.AbstractAdfonicTest;

public class TestJmsUtils extends AbstractAdfonicTest {
    private JmsUtils jmsUtils;

    @Before
    public void runBeforeEachTest() {
        jmsUtils = new JmsUtils();
    }
    
    @Test
    public void test01_sendBytes() {
        final JmsTemplate jmsTemplate = mock(JmsTemplate.class, "jmsTemplate");
        final Destination destination = mock(Destination.class);
        final byte[] data = randomAlphaNumericString(10).getBytes();
        expect(new Expectations() {{
            oneOf (jmsTemplate).send(with(destination), with(any(JmsUtils.BytesMessageCreator.class)));
        }});
        jmsUtils.sendBytes(jmsTemplate, destination, data);
    }
    
    @Test
    public void test02_sendObject() {
        final JmsTemplate jmsTemplate = mock(JmsTemplate.class, "jmsTemplate");
        final Destination destination = mock(Destination.class);
        final Serializable obj = mock(Serializable.class, "obj");
        expect(new Expectations() {{
            oneOf (jmsTemplate).send(with(destination), with(any(JmsUtils.ObjectMessageCreator.class)));
        }});
        jmsUtils.sendObject(jmsTemplate, destination, obj);
    }
    
    @Test
    public void test03_sendText() {
        final JmsTemplate jmsTemplate = mock(JmsTemplate.class, "jmsTemplate");
        final Destination destination = mock(Destination.class);
        final String text = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf (jmsTemplate).send(with(destination), with(any(JmsUtils.TextMessageCreator.class)));
        }});
        jmsUtils.sendText(jmsTemplate, destination, text);
    }
    
    @Test
    public void test04_BytesMessageCreator() throws javax.jms.JMSException {
        final BytesMessage message = mock(BytesMessage.class, "message");
        final Session session = mock(Session.class);
        final byte[] data = randomAlphaNumericString(10).getBytes();
        expect(new Expectations() {{
            oneOf (session).createBytesMessage(); will(returnValue(message));
            oneOf (message).writeBytes(data);
        }});
        assertEquals(message, new JmsUtils.BytesMessageCreator(data).createMessage(session));
    }

    @Test
    public void test05_ObjectMessageCreator() throws javax.jms.JMSException {
        final Serializable obj = mock(Serializable.class, "obj");
        final ObjectMessage message = mock(ObjectMessage.class, "message");
        final Session session = mock(Session.class);
        expect(new Expectations() {{
            oneOf (session).createObjectMessage(obj); will(returnValue(message));
        }});
        assertEquals(message, new JmsUtils.ObjectMessageCreator(obj).createMessage(session));
    }

    @Test
    public void test06_TextMessageCreator() throws javax.jms.JMSException {
        final String text = randomAlphaNumericString(10);
        final TextMessage message = mock(TextMessage.class, "message");
        final Session session = mock(Session.class);
        expect(new Expectations() {{
            oneOf (session).createTextMessage(text); will(returnValue(message));
        }});
        assertEquals(message, new JmsUtils.TextMessageCreator(text).createMessage(session));
    }
}
