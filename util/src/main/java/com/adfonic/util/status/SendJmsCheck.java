package com.adfonic.util.status;

import java.io.Serializable;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.adfonic.util.HostUtils;

public class SendJmsCheck<ID extends Serializable> extends BaseResourceCheck<ID> {

    private final ConnectionFactory connectionFactory;
    private final Queue queue;

    public SendJmsCheck(ConnectionFactory connectionFactory, Queue queue) {
        this.connectionFactory = connectionFactory;
        this.queue = queue;
    }

    @Override
    public String doCheck(ResourceId<ID> resource) throws Exception {
        String text = "Test at: " + new Date() + " from: " + HostUtils.getHostName();
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(queue);
        TextMessage message = session.createTextMessage(text);
        producer.send(message);
        producer.close();
        session.close();
        connection.close();
        return "Sent TextMessage: " + text;
    }
}
