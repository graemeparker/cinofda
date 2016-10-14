package com.adfonic.adserver.spring.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

/**
 * 
 * @author mvanek
 *
 */
@EnableJms
@Configuration
@Import(AdserverJmsListenerMapping.class)
public class AdserverJmsSpringConfig { // implements JmsListenerConfigurer {

    @Bean
    public JmsUtils jmsUtils() {
        return new JmsUtils();
    }

    /**
     * Physical ActiveMQ server connection factory
     */
    @Bean(name = JmsResource.CENTRAL_JMS_FACTORY, initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory centralActiveMqConnectionFactory(@Value("${central.jms.broker.url}") String brokerUrl,
            @Value("${central.jms.pool.maxConnections}") Integer maxConnections) {
        ActiveMQConnectionFactory activemqFactory = new ActiveMQConnectionFactory(brokerUrl);
        PooledConnectionFactory pooledFactory = new PooledConnectionFactory(activemqFactory);
        pooledFactory.setMaxConnections(maxConnections);
        return pooledFactory;
    }

    @Bean(name = JmsResource.CENTRAL_JMS_TEMPLATE)
    public JmsTemplate centralJmsTemplate(@Qualifier(JmsResource.CENTRAL_JMS_FACTORY) PooledConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
     * Physical ActiveMQ server connection factory for AdEvents cluster
     */
    @Bean(name = JmsResource.ADEVENT_JMS_FACTORY, initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory admqJmsConnectionFactory(@Value("${adevent.jms.broker.url}") String brokerUrl,
            @Value("${adevent.jms.pool.maxConnections}") Integer maxConnections, @Value("${adevent.jms.alwaysSessionAsync}") boolean alwaysSessionAsync,
            @Value("${adevent.jms.copyMessageOnSend}") boolean copyMessageOnSend, @Value("${adevent.jms.disableTimeStampsByDefault}") boolean disableTimeStampsByDefault,
            @Value("${adevent.jms.dispatchAsync}") boolean dispatchAsync, @Value("${adevent.jms.useAsyncSend}") boolean useAsyncSend,
            @Value("${adevent.jms.useCompression}") boolean useCompression) {
        ActiveMQConnectionFactory activemqFactory = new ActiveMQConnectionFactory(brokerUrl);
        activemqFactory.setAlwaysSessionAsync(alwaysSessionAsync);
        activemqFactory.setCopyMessageOnSend(copyMessageOnSend);
        activemqFactory.setDisableTimeStampsByDefault(disableTimeStampsByDefault);
        activemqFactory.setDispatchAsync(dispatchAsync);
        activemqFactory.setUseAsyncSend(useAsyncSend);
        activemqFactory.setUseCompression(useCompression);
        PooledConnectionFactory pooledFactory = new PooledConnectionFactory(activemqFactory);
        pooledFactory.setMaxConnections(maxConnections);
        return pooledFactory;
    }

    @Bean(name = JmsResource.ADEVENT_JMS_TEMPLATE)
    public JmsTemplate admqJmsTemplate(@Qualifier(JmsResource.ADEVENT_JMS_FACTORY) PooledConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

}
