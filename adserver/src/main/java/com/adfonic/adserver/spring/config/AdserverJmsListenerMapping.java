package com.adfonic.adserver.spring.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.adfonic.adserver.impl.StatusChangeManagerImpl;
import com.adfonic.adserver.impl.StoppageManagerImpl;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StatusChangeMessage;
import com.adfonic.jms.StopAdvertiserMessage;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.jms.UnStopAdvertiserMessage;
import com.adfonic.jms.UnStopCampaignMessage;

/**
 * @author mvanek
 */
@Configuration
public class AdserverJmsListenerMapping {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Autowired
    private StatusChangeManagerImpl statusChangeManager;

    @Autowired
    private StoppageManagerImpl stoppageManager;

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_FACTORY)
    private PooledConnectionFactory connectionFactory;

    /**
     * Note: DefaultMessageListenerContainer Bean name becomes Thread name (example: onStatusChangeJmsListener-1)
     */

    @Bean
    public DefaultMessageListenerContainer onStatusChangeJmsListener() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    statusChangeManager.onStatusChange((StatusChangeMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.log(Level.SEVERE, "Topic message recieve failed", x);
                }
            }
        };

        return JmsUtils.buildTopicContainer(connectionFactory, JmsResource.STATUS_CHANGE_TOPIC, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onStopAdvertiserJmsListener() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    stoppageManager.onStopAdvertiser((StopAdvertiserMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.log(Level.SEVERE, "Topic message recieve failed", x);
                }
            }
        };

        return JmsUtils.buildTopicContainer(connectionFactory, JmsResource.STOP_ADVERTISER_TOPIC, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onUnStopAdvertiserJmsListener() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    stoppageManager.onUnStopAdvertiser((UnStopAdvertiserMessage) ((ObjectMessage) jms).getObject());
                } catch (JMSException jmsx) {
                    LOG.log(Level.SEVERE, "Topic message recieve failed", jmsx);
                }
            }
        };

        return JmsUtils.buildTopicContainer(connectionFactory, JmsResource.UNSTOP_ADVERTISER_TOPIC, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onStopCampaignJmsListener() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    stoppageManager.onStopCampaign((StopCampaignMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.log(Level.SEVERE, "Topic message recieve failed", x);
                }
            }
        };

        return JmsUtils.buildTopicContainer(connectionFactory, JmsResource.STOP_CAMPAIGN_TOPIC, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onUnStopCampaignJmsListener() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    stoppageManager.onUnStopCampaign((UnStopCampaignMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.log(Level.SEVERE, "Topic message recieve failed", x);
                }
            }
        };

        return JmsUtils.buildTopicContainer(connectionFactory, JmsResource.UNSTOP_CAMPAIGN_TOPIC, listener);
    }

    /**
     * Possibly easier way...but still requires to define DefaultMessageListenerContainerFactory somewhere else...
     */
    /*
    @JmsListener(containerFactory = AdserverJmsSpringConfig.CENTRAL_JMS_TOPIC_FACTORY, destination = "adfonic.status.change", concurrency = "1")
    public void onStatusChange(StatusChangeMessage message) {
        statusChangeManager.onStatusChange(message);
    }
    
    @JmsListener(containerFactory = AdserverJmsSpringConfig.CENTRAL_JMS_TOPIC_FACTORY, destination = "adfonic.stopAdvertiser", concurrency = "1")
    public void onStopAdvertiser(StopAdvertiserMessage message) {
        stoppageManager.onStopAdvertiser(message);
    }

    @JmsListener(containerFactory = AdserverJmsSpringConfig.CENTRAL_JMS_TOPIC_FACTORY, destination = "adfonic.unStopAdvertiser", concurrency = "1")
    public void onUnStopAdvertiser(UnStopAdvertiserMessage message) {
        stoppageManager.onUnStopAdvertiser(message);
    }

    @JmsListener(containerFactory = AdserverJmsSpringConfig.CENTRAL_JMS_TOPIC_FACTORY, destination = "adfonic.stopCampaign", concurrency = "1")
    public void onStopCampaign(StopCampaignMessage message) {
        stoppageManager.onStopCampaign(message);
    }

    @JmsListener(containerFactory = AdserverJmsSpringConfig.CENTRAL_JMS_TOPIC_FACTORY, destination = "adfonic.unStopCampaign", concurrency = "1")
    public void onUnStopCampaign(UnStopCampaignMessage message) {
        stoppageManager.onUnStopCampaign(message);
    }
    */
}
