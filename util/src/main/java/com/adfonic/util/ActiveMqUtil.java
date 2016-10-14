package com.adfonic.util;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

public class ActiveMqUtil {

    public static BrokerService ensureLocalActiveMq() {
        return ensureActiveMq("tcp://localhost:61616");
    }

    public static BrokerService ensureActiveMq(String brokerUrl) {
        if (System.getProperty("skip.activemq") != null) {
            return null;
        }
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            Connection connection = factory.createConnection();
            connection.close();
            System.out.println("ActiveMQ running " + brokerUrl);
            return null;
        } catch (JMSException jmsx) {
            // cannot connect to existing...
        }

        try {
            BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(true);
            TransportConnector connector = new TransportConnector();
            connector.setUri(new URI(brokerUrl));
            broker.addConnector(connector);
            broker.start();
            System.out.println("ActiveMQ started " + brokerUrl);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.println("ActiveMQ shutdown");
                        broker.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return broker;
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(-1);
        }

        return null;
    }
}
