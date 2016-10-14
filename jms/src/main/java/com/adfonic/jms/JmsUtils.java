package com.adfonic.jms;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class JmsUtils {
    private static final transient Logger LOG = Logger.getLogger(JmsUtils.class.getName());

    /**
     * Helper method to send any byte[] to any destination using any JmsTemplate
     */
    public void sendBytes(JmsTemplate jmsTemplate, Destination destination, byte[] data) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending " + data.length + " bytes to " + destination.toString());
        }
        jmsTemplate.send(destination, new BytesMessageCreator(data));
    }

    /**
     * Helper method to send any object to any destination using any JmsTemplate
     */
    public void sendObject(JmsTemplate jmsTemplate, Destination destination, Serializable obj) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending a " + obj.getClass().getName() + " to " + destination.toString());
        }
        jmsTemplate.send(destination, new ObjectMessageCreator(obj));
    }

    /**
     * Helper method to send a string of text to any destination using any JmsTemplate
     */
    public void sendText(JmsTemplate jmsTemplate, Destination destination, String text) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Sending \"" + text + "\" to " + destination.toString());
        }
        jmsTemplate.send(destination, new TextMessageCreator(text));
    }

    static final class BytesMessageCreator implements MessageCreator {
        private final byte[] data;

        BytesMessageCreator(byte[] data) {
            this.data = data;
        }

        @Override
        public Message createMessage(Session session) throws javax.jms.JMSException {
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(data);
            return message;
        }
    }

    static final class ObjectMessageCreator implements MessageCreator {
        private final Serializable obj;

        ObjectMessageCreator(Serializable obj) {
            this.obj = obj;
        }

        @Override
        public Message createMessage(Session session) throws javax.jms.JMSException {
            return session.createObjectMessage(obj);
        }
    }

    static final class TextMessageCreator implements MessageCreator {
        private final String text;

        TextMessageCreator(String text) {
            this.text = text;
        }

        @Override
        public Message createMessage(Session session) throws javax.jms.JMSException {
            return session.createTextMessage(text);
        }
    }

    public static DefaultMessageListenerContainer buildTopicContainer(ConnectionFactory connectionFactory, Topic topic, MessageListener listener) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setPubSubDomain(true); // true = Topic
        container.setDestination(topic);
        container.setMaxConcurrentConsumers(1); //For JMS Topic, more then 1 consumer will cause multiple deliveries
        container.setMessageListener(listener);
        try {
            container.setBeanName(topic.getTopicName()); // will become thread name
        } catch (JMSException jmsx) {
            throw new IllegalStateException("Cannot get JMS destination name", jmsx);
        }
        // container.setTaskExecutor(Executors.newSingleThreadExecutor(ThreadFactory.));
        return container;
    }

    public static DefaultMessageListenerContainer buildQueueContainer(ConnectionFactory connectionFactory, Queue queue, MessageListener listener) {
        return buildQueueContainer(connectionFactory, queue, 1, listener);
    }

    public static DefaultMessageListenerContainer buildQueueContainer(ConnectionFactory connectionFactory, Queue queue, int maxConsumers, MessageListener listener) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setPubSubDomain(false); // false = Queue
        container.setDestination(queue);
        container.setMaxConcurrentConsumers(maxConsumers);
        container.setMessageListener(listener);
        try {
            container.setBeanName(queue.getQueueName()); // will become thread name
        } catch (JMSException jmsx) {
            throw new IllegalStateException("Cannot get JMS destination name", jmsx);
        }
        return container;
    }
}
